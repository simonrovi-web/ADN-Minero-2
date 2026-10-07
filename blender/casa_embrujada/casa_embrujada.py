"""
Casa embrujada sobre el peñasco — escena procedural para Blender (4.2+ / 5.x)

Uso:
  - Dentro de Blender: abrir en el Text Editor y pulsar "Run Script".
  - Por línea de comandos:
      blender -b -P casa_embrujada.py -- --render salida.png --save escena.blend
      (o con el módulo bpy:  python casa_embrujada.py --render salida.png)

Todo se genera por código: peñasco con acantilados, casa de varias plantas
(piedra, revoque, tablones, tejas), balcones y barandas rotas, escalera,
árboles secos retorcidos, cielo tormentoso morado/verde, luces y cámara.
Cada elemento está en su propia colección para poder editarlo a mano.
"""

import bpy
import bmesh
import math
import random
import sys
from mathutils import Vector, Matrix, Euler, noise

SEED = 7
HOUSE_YAW = -52.0  # la casa se ve en esquina, como en la ilustración
random.seed(SEED)
noise.seed_set(SEED)

# ---------------------------------------------------------------------------
# Utilidades generales
# ---------------------------------------------------------------------------

def clear_scene():
    for ob in list(bpy.data.objects):
        bpy.data.objects.remove(ob, do_unlink=True)
    for coll in (bpy.data.meshes, bpy.data.curves, bpy.data.materials,
                 bpy.data.lights, bpy.data.cameras, bpy.data.worlds):
        for block in list(coll):
            coll.remove(block)
    for c in list(bpy.data.collections):
        bpy.data.collections.remove(c)


def collection(name):
    c = bpy.data.collections.get(name)
    if c is None:
        c = bpy.data.collections.new(name)
        bpy.context.scene.collection.children.link(c)
    return c


def smoothstep(a, b, x):
    t = max(0.0, min(1.0, (x - a) / (b - a)))
    return t * t * (3 - 2 * t)


def lerp(a, b, t):
    return a + (b - a) * t


class MeshBuilder:
    """Acumula geometría (por material) y crea un único objeto."""

    def __init__(self):
        self.verts = []
        self.faces = []
        self.mat_idx = []
        self.mats = []

    def _mi(self, mat):
        if mat not in self.mats:
            self.mats.append(mat)
        return self.mats.index(mat)

    def add(self, verts, faces, mat):
        o = len(self.verts)
        mi = self._mi(mat)
        self.verts.extend(verts)
        for f in faces:
            self.faces.append([i + o for i in f])
            self.mat_idx.append(mi)

    def box(self, center, size, mat, rot=None, basis=None):
        """Caja centrada. basis=(u,v,n) permite orientarla en cualquier eje."""
        sx, sy, sz = size[0] / 2, size[1] / 2, size[2] / 2
        local = [Vector((x, y, z)) for x in (-sx, sx) for y in (-sy, sy) for z in (-sz, sz)]
        m = Matrix.Identity(3)
        if basis is not None:
            u, v, n = basis
            m = Matrix((u, n, v)).transposed()  # x->u, y->n, z->v
        if rot is not None:
            m = m @ Euler(rot).to_matrix()
        c = Vector(center)
        vs = [c + m @ p for p in local]
        faces = [(0, 1, 3, 2), (4, 6, 7, 5), (0, 4, 5, 1),
                 (2, 3, 7, 6), (0, 2, 6, 4), (1, 5, 7, 3)]
        self.add(vs, faces, mat)

    def build(self, name, coll, bevel=0.0, smooth=False, parent=None):
        me = bpy.data.meshes.new(name)
        me.from_pydata([tuple(v) for v in self.verts], [], self.faces)
        me.validate()
        for m in self.mats:
            me.materials.append(m)
        for p, mi in zip(me.polygons, self.mat_idx):
            p.material_index = mi
            p.use_smooth = smooth
        ob = bpy.data.objects.new(name, me)
        coll.objects.link(ob)
        if bevel > 0:
            mod = ob.modifiers.new("Bisel", 'BEVEL')
            mod.width = bevel
            mod.segments = 1
            mod.limit_method = 'ANGLE'
        if parent:
            ob.parent = parent
        return ob


# ---------------------------------------------------------------------------
# Materiales (Cycles, nodos procedurales)
# ---------------------------------------------------------------------------

def _mix_rgb(nt, fac, a, b, blend='MIX'):
    m = nt.nodes.new('ShaderNodeMix')
    m.data_type = 'RGBA'
    m.blend_type = blend
    if isinstance(fac, (int, float)):
        m.inputs[0].default_value = fac
    else:
        nt.links.new(fac, m.inputs[0])
    for idx, val in ((6, a), (7, b)):
        if isinstance(val, (tuple, list)):
            m.inputs[idx].default_value = (*val[:3], 1.0)
        else:
            nt.links.new(val, m.inputs[idx])
    return m.outputs[2]


def _ramp(nt, inp, stops):
    r = nt.nodes.new('ShaderNodeValToRGB')
    nt.links.new(inp, r.inputs[0])
    els = r.color_ramp.elements
    els[0].position, els[0].color = stops[0][0], (*stops[0][1], 1)
    els[1].position, els[1].color = stops[-1][0], (*stops[-1][1], 1)
    for pos, col in stops[1:-1]:
        e = els.new(pos)
        e.color = (*col, 1)
    return r.outputs[0]


def _noise(nt, vec, scale, detail=6.0, rough=0.6, distortion=0.0):
    n = nt.nodes.new('ShaderNodeTexNoise')
    n.inputs['Scale'].default_value = scale
    n.inputs['Detail'].default_value = detail
    n.inputs['Roughness'].default_value = rough
    n.inputs['Distortion'].default_value = distortion
    if vec is not None:
        nt.links.new(vec, n.inputs['Vector'])
    return n


def _mapping(nt, coord='Object', scale=(1, 1, 1)):
    tc = nt.nodes.new('ShaderNodeTexCoord')
    mp = nt.nodes.new('ShaderNodeMapping')
    mp.inputs['Scale'].default_value = scale
    nt.links.new(tc.outputs[coord], mp.inputs['Vector'])
    return mp.outputs[0]


def new_material(name):
    m = bpy.data.materials.new(name)
    try:
        m.use_nodes = True
    except Exception:
        pass
    nt = m.node_tree
    bsdf = nt.nodes.get('Principled BSDF')
    return m, nt, bsdf


def _bump(nt, bsdf, height, strength, distance=0.05):
    b = nt.nodes.new('ShaderNodeBump')
    b.inputs['Strength'].default_value = strength
    b.inputs['Distance'].default_value = distance
    nt.links.new(height, b.inputs['Height'])
    nt.links.new(b.outputs[0], bsdf.inputs['Normal'])


def mat_wood(name, base, dark, grain_scale=(1, 1, 14), bands='Z'):
    """Madera envejecida: veta (wave) + suciedad (noise)."""
    m, nt, bsdf = new_material(name)
    vec = _mapping(nt, 'Object', grain_scale)
    wave = nt.nodes.new('ShaderNodeTexWave')
    wave.wave_type = 'BANDS'
    wave.bands_direction = bands
    wave.inputs['Scale'].default_value = 1.6
    wave.inputs['Distortion'].default_value = 9.0
    wave.inputs['Detail'].default_value = 6.0
    wave.inputs['Detail Roughness'].default_value = 0.7
    nt.links.new(vec, wave.inputs['Vector'])
    dirt = _noise(nt, _mapping(nt, 'Object', (1, 1, 1)), 1.8, 8, 0.65)
    grain = _ramp(nt, wave.outputs['Fac'], [(0.0, dark), (0.55, base), (1.0, tuple(c * 1.15 for c in base))])
    col = _mix_rgb(nt, dirt.outputs['Fac'], grain, tuple(c * 0.45 for c in dark), 'MULTIPLY')
    nt.links.new(col, bsdf.inputs['Base Color'])
    bsdf.inputs['Roughness'].default_value = 0.88
    _bump(nt, bsdf, wave.outputs['Fac'], 0.35, 0.02)
    return m


def mat_plaster(name):
    m, nt, bsdf = new_material(name)
    vec = _mapping(nt, 'Object', (1, 1, 1))
    n1 = _noise(nt, vec, 1.3, 10, 0.7)
    n2 = _noise(nt, vec, 9.0, 12, 0.75)
    col = _ramp(nt, n1.outputs['Fac'], [(0.35, (0.09, 0.07, 0.085)), (0.55, (0.17, 0.14, 0.16)), (0.75, (0.25, 0.22, 0.23))])
    streak = _noise(nt, _mapping(nt, 'Object', (3.0, 3.0, 0.25)), 2.0, 6, 0.6)
    col = _mix_rgb(nt, 0.55, col, _ramp(nt, streak.outputs['Fac'], [(0.4, (0.35, 0.35, 0.35)), (0.7, (1, 1, 1))]), 'MULTIPLY')
    nt.links.new(col, bsdf.inputs['Base Color'])
    bsdf.inputs['Roughness'].default_value = 0.95
    _bump(nt, bsdf, n2.outputs['Fac'], 0.4, 0.03)
    return m


def mat_stone(name, base=(0.22, 0.21, 0.21)):
    m, nt, bsdf = new_material(name)
    vec = _mapping(nt, 'Object', (1, 1, 1))
    n1 = _noise(nt, vec, 4.0, 10, 0.7)
    vor = nt.nodes.new('ShaderNodeTexVoronoi')
    vor.inputs['Scale'].default_value = 3.0
    nt.links.new(vec, vor.inputs['Vector'])
    dark = tuple(c * 0.45 for c in base)
    light = tuple(min(1, c * 1.5) for c in base)
    col = _ramp(nt, n1.outputs['Fac'], [(0.3, dark), (0.5, base), (0.7, light)])
    vg = _ramp(nt, vor.outputs['Distance'], [(0.0, (0.25, 0.25, 0.25)), (0.6, (0.6, 0.6, 0.6))])
    col = _mix_rgb(nt, 0.35, col, vg, 'OVERLAY')
    nt.links.new(col, bsdf.inputs['Base Color'])
    bsdf.inputs['Roughness'].default_value = 0.92
    _bump(nt, bsdf, n1.outputs['Fac'], 0.6, 0.05)
    return m


def mat_tiles(name):
    m, nt, bsdf = new_material(name)
    vec = _mapping(nt, 'Object', (1, 1, 1))
    n1 = _noise(nt, vec, 2.5, 8, 0.6)
    n2 = _noise(nt, vec, 25.0, 6, 0.6)
    col = _ramp(nt, n1.outputs['Fac'], [(0.3, (0.025, 0.015, 0.02)), (0.5, (0.065, 0.038, 0.048)), (0.72, (0.12, 0.075, 0.085))])
    moss = _ramp(nt, n2.outputs['Fac'], [(0.55, (1, 1, 1)), (0.75, (0.55, 0.6, 0.5))])
    col = _mix_rgb(nt, 1.0, col, moss, 'MULTIPLY')
    nt.links.new(col, bsdf.inputs['Base Color'])
    bsdf.inputs['Roughness'].default_value = 0.8
    _bump(nt, bsdf, n2.outputs['Fac'], 0.25, 0.02)
    return m


def mat_rock(name):
    """Roca del peñasco: gris violáceo, con tierra en las zonas planas."""
    m, nt, bsdf = new_material(name)
    vec = _mapping(nt, 'Object', (1, 1, 1))
    big = _noise(nt, vec, 0.35, 8, 0.65, 0.4)
    fine = _noise(nt, vec, 6.0, 12, 0.75)
    strata = _noise(nt, _mapping(nt, 'Object', (0.6, 0.6, 5.0)), 1.0, 6, 0.6)
    rock = _ramp(nt, big.outputs['Fac'], [(0.3, (0.008, 0.007, 0.011)), (0.5, (0.035, 0.032, 0.044)), (0.72, (0.11, 0.105, 0.12))])
    rock = _mix_rgb(nt, 0.4, rock, strata.outputs['Color'], 'OVERLAY')
    # máscara por orientación: superficies planas = tierra / musgo
    geo = nt.nodes.new('ShaderNodeNewGeometry')
    sep = nt.nodes.new('ShaderNodeSeparateXYZ')
    nt.links.new(geo.outputs['Normal'], sep.inputs[0])
    mr = nt.nodes.new('ShaderNodeMapRange')
    mr.inputs['From Min'].default_value = 0.6
    mr.inputs['From Max'].default_value = 0.85
    nt.links.new(sep.outputs['Z'], mr.inputs['Value'])
    mask = nt.nodes.new('ShaderNodeMath')
    mask.operation = 'MULTIPLY'
    nt.links.new(mr.outputs[0], mask.inputs[0])
    nm = _noise(nt, vec, 1.5, 6, 0.6)
    mm = nt.nodes.new('ShaderNodeMapRange')
    mm.inputs['From Min'].default_value = 0.25
    mm.inputs['From Max'].default_value = 0.45
    nt.links.new(nm.outputs['Fac'], mm.inputs['Value'])
    nt.links.new(mm.outputs[0], mask.inputs[1])
    dirt = _ramp(nt, fine.outputs['Fac'], [(0.35, (0.10, 0.095, 0.075)), (0.6, (0.24, 0.23, 0.17)), (0.8, (0.32, 0.31, 0.24))])
    col = _mix_rgb(nt, mask.outputs[0], rock, dirt)
    col = _mix_rgb(nt, 1.0, col, (0.5, 0.45, 0.55), 'MULTIPLY')
    # oclusión/curvatura: grietas oscuras, aristas más claras
    pr = nt.nodes.new('ShaderNodeMapRange')
    pr.inputs['From Min'].default_value = 0.45
    pr.inputs['From Max'].default_value = 0.56
    nt.links.new(geo.outputs['Pointiness'], pr.inputs['Value'])
    cav = _ramp(nt, pr.outputs[0], [(0.0, (0.25, 0.25, 0.28)), (0.5, (1, 1, 1)), (1.0, (1.5, 1.55, 1.45))])
    col = _mix_rgb(nt, 1.0, col, cav, 'MULTIPLY')
    nt.links.new(col, bsdf.inputs['Base Color'])
    bsdf.inputs['Roughness'].default_value = 0.93
    h = nt.nodes.new('ShaderNodeMath')
    h.operation = 'ADD'
    nt.links.new(fine.outputs['Fac'], h.inputs[0])
    nt.links.new(strata.outputs['Fac'], h.inputs[1])
    _bump(nt, bsdf, h.outputs[0], 0.7, 0.15)
    # grietas verticales (voronoi aplastado en Z): relieve real por desplazamiento
    cr = nt.nodes.new('ShaderNodeTexVoronoi')
    cr.feature = 'DISTANCE_TO_EDGE'
    cr.inputs['Scale'].default_value = 0.5
    nt.links.new(_mapping(nt, 'Object', (1.0, 1.0, 0.22)), cr.inputs['Vector'])
    crr = nt.nodes.new('ShaderNodeMapRange')
    crr.inputs['From Min'].default_value = 0.0
    crr.inputs['From Max'].default_value = 0.12
    nt.links.new(cr.outputs['Distance'], crr.inputs['Value'])
    # oscurecer el fondo de las grietas
    crack_col = _ramp(nt, crr.outputs[0], [(0.0, (0.15, 0.15, 0.17)), (0.6, (1, 1, 1))])
    final = _mix_rgb(nt, 1.0, col, crack_col, 'MULTIPLY')
    nt.links.new(final, bsdf.inputs['Base Color'])
    disp = nt.nodes.new('ShaderNodeDisplacement')
    disp.inputs['Scale'].default_value = 0.6
    disp.inputs['Midlevel'].default_value = 0.0
    hh = nt.nodes.new('ShaderNodeMath')
    hh.operation = 'MULTIPLY_ADD'
    nt.links.new(crr.outputs[0], hh.inputs[0])
    hh.inputs[1].default_value = 0.8
    nt.links.new(big.outputs['Fac'], hh.inputs[2])
    # solo en las paredes: la meseta (plana) queda lisa
    steep = nt.nodes.new('ShaderNodeMath')
    steep.operation = 'SUBTRACT'
    steep.inputs[0].default_value = 1.0
    nt.links.new(mr.outputs[0], steep.inputs[1])
    hm = nt.nodes.new('ShaderNodeMath')
    hm.operation = 'MULTIPLY'
    nt.links.new(hh.outputs[0], hm.inputs[0])
    nt.links.new(steep.outputs[0], hm.inputs[1])
    nt.links.new(hm.outputs[0], disp.inputs['Height'])
    out = nt.nodes.get('Material Output')
    nt.links.new(disp.outputs[0], out.inputs['Displacement'])
    try:
        m.displacement_method = 'BOTH'
    except Exception:
        m.cycles.displacement_method = 'BOTH'
    return m


def mat_simple(name, color, rough=0.9, emission=None):
    m, nt, bsdf = new_material(name)
    bsdf.inputs['Base Color'].default_value = (*color, 1)
    bsdf.inputs['Roughness'].default_value = rough
    if emission:
        bsdf.inputs['Emission Color'].default_value = (*emission[0], 1)
        bsdf.inputs['Emission Strength'].default_value = emission[1]
    return m


def mat_glass_dark(name):
    m, nt, bsdf = new_material(name)
    bsdf.inputs['Base Color'].default_value = (0.01, 0.01, 0.012, 1)
    bsdf.inputs['Roughness'].default_value = 0.25
    bsdf.inputs['Specular IOR Level'].default_value = 0.8
    return m


def build_materials():
    M = {}
    M['wood'] = mat_wood('Madera_Tablones', (0.17, 0.12, 0.14), (0.055, 0.04, 0.05))
    M['wood_dark'] = mat_wood('Madera_Oscura', (0.09, 0.065, 0.07), (0.03, 0.02, 0.025))
    M['wood_grey'] = mat_wood('Madera_Gris', (0.2, 0.185, 0.17), (0.06, 0.055, 0.055))
    M['plaster'] = mat_plaster('Revoque')
    M['stone'] = mat_stone('Piedra_Muro', (0.15, 0.15, 0.14))
    M['brick'] = mat_stone('Ladrillo_Expuesto', (0.19, 0.16, 0.17))
    M['tiles'] = mat_tiles('Tejas')
    M['rock'] = mat_rock('Roca_Penasco')
    M['bark'] = mat_wood('Corteza', (0.075, 0.06, 0.07), (0.02, 0.016, 0.02), (5, 5, 0.5), bands='X')
    M['glass'] = mat_glass_dark('Ventana_Oscura')
    M['interior'] = mat_simple('Interior_Negro', (0.006, 0.005, 0.006), 1.0)
    return M


# ---------------------------------------------------------------------------
# Elementos arquitectónicos
# ---------------------------------------------------------------------------

X = Vector((1, 0, 0))
Y = Vector((0, 1, 0))
Z = Vector((0, 0, 1))


def plank_wall(mb, mat, origin, u, n, width, height, openings=(), plank_h=0.24,
               profile=None, thickness=0.06, jitter=0.012, vertical=False):
    """Pared de tablones horizontales sobre la cara (origin, u, Z, normal n).
    openings: [(u0, v0, u1, v1)] huecos para puertas/ventanas.
    profile(v) -> (umin, umax) para hastiales (triángulos)."""
    origin = Vector(origin)
    v = Z
    if vertical:
        # tablones verticales (puertas)
        u0 = 0.0
        while u0 < width - 1e-4:
            w = min(random.uniform(0.13, 0.18), width - u0)
            c = origin + u * (u0 + w / 2) + v * (height / 2) + n * (thickness / 2)
            mb.box(c, (w - 0.012, thickness, height - random.uniform(0, 0.04)), mat,
                   rot=(0, random.uniform(-jitter, jitter), 0), basis=(u, v, n))
            u0 += w
        return
    rows = max(1, int(round(height / plank_h)))
    ph = height / rows
    for r in range(rows):
        v0, v1 = r * ph, (r + 1) * ph
        vm = (v0 + v1) / 2
        umin, umax = (0.0, width) if profile is None else profile(vm)
        if umax - umin < 0.05:
            continue
        # segmentos libres según aberturas
        segs = [(umin, umax)]
        for (a0, b0, a1, b1) in openings:
            if b0 < vm < b1:
                new = []
                for s0, s1 in segs:
                    if a1 <= s0 or a0 >= s1:
                        new.append((s0, s1))
                    else:
                        if a0 > s0:
                            new.append((s0, a0))
                        if a1 < s1:
                            new.append((a1, s1))
                segs = new
        for s0, s1 in segs:
            # los tablones largos se cortan en piezas de largo variable
            p = s0
            while p < s1 - 0.02:
                L = min(random.uniform(1.4, 3.2), s1 - p)
                if s1 - (p + L) < 0.3:
                    L = s1 - p
                droop = random.uniform(-jitter, jitter) * 2
                c = origin + u * (p + L / 2) + v * (vm + random.uniform(-0.01, 0.01)) \
                    + n * (thickness / 2 + random.uniform(0, 0.015))
                mb.box(c, (L - 0.01, thickness, ph * 1.08),
                       mat, rot=(random.uniform(-0.05, 0.05), droop, 0), basis=(u, v, n))
                p += L


def window(mb, M, origin, u, n, u0, v0, w, h, broken=False, shutters=False, boarded=False):
    """Ventana con marco, travesaños en cruz y vidrio oscuro."""
    o = Vector(origin)
    c = o + u * (u0 + w / 2) + Z * (v0 + h / 2)
    ft = 0.08
    d = 0.09
    # vidrio / interior hundido
    mb.box(c + n * 0.01, (w, 0.02, h), M['glass'] if not broken else M['interior'], basis=(u, Z, n))
    # marco
    mb.box(c + Z * (h / 2 + ft / 2) + n * d / 2, (w + 2 * ft + 0.06, d, ft), M['wood_dark'], basis=(u, Z, n))
    mb.box(c - Z * (h / 2 + ft / 2) + n * d / 2, (w + 2 * ft + 0.12, d + 0.04, ft), M['wood_dark'], basis=(u, Z, n))
    for s in (-1, 1):
        mb.box(c + u * s * (w / 2 + ft / 2) + n * d / 2, (ft, d, h + 2 * ft), M['wood_dark'], basis=(u, Z, n))
    if boarded:
        for i in range(3):
            mb.box(c + Z * (h * (i - 1) * 0.3) + n * (d + 0.02),
                   (w * 1.15, 0.035, 0.13), M['wood_grey'],
                   rot=(0, random.uniform(-0.35, 0.35), 0), basis=(u, Z, n))
        return
    # parteluz y travesaño (algunos rotos)
    tilt = random.uniform(-0.25, 0.25) if broken else 0.0
    mb.box(c + n * 0.03, (0.04, 0.04, h), M['wood_dark'], rot=(0, tilt, 0), basis=(u, Z, n))
    mb.box(c + n * 0.03 + Z * h * 0.08, (w, 0.04, 0.04), M['wood_dark'], rot=(0, tilt * 0.5, 0), basis=(u, Z, n))
    if shutters:
        for s in (-1, 1):
            mb.box(c + u * s * (w / 2 + ft + w * 0.25) + n * (d + 0.02),
                   (w * 0.5, 0.035, h), M['wood'], rot=(0, random.uniform(-0.05, 0.05), 0), basis=(u, Z, n))


def door(mb, M, origin, u, n, u0, w, h, v0=0.0, ajar=False):
    o = Vector(origin)
    c = o + u * (u0 + w / 2) + Z * (v0 + h / 2)
    mb.box(c + n * 0.005, (w, 0.02, h), M['interior'], basis=(u, Z, n))
    sub = MeshBuilder()
    plank_wall(sub, M['wood'], (0, 0, 0), X, -Y, w - 0.04, h - 0.03, vertical=True, thickness=0.05)
    rotz = random.uniform(-0.5, -0.2) if ajar else 0.0
    rmat = Matrix((u, n, Z)).transposed() @ Matrix.Rotation(rotz, 3, 'Z')
    # bisagra en el borde izquierdo
    hinge = o + u * u0 + Z * v0 + n * 0.03
    verts = [hinge + rmat @ Vector((p.x, -p.y, p.z)) for p in sub.verts]
    mb.add(verts, sub.faces, M['wood'])
    ft = 0.09
    mb.box(c + Z * (h / 2 + ft / 2) + n * 0.05, (w + 2 * ft, 0.1, ft), M['wood_dark'], basis=(u, Z, n))
    for s in (-1, 1):
        mb.box(c + u * s * (w / 2 + ft / 2) + n * 0.05, (ft, 0.1, h + ft), M['wood_dark'], basis=(u, Z, n))


def railing(mb, M, p0, p1, height=1.0, spacing=0.17, broken=0.12, post_every=1.6, top=True):
    """Baranda de madera con balaustres finos, algunos faltantes o torcidos."""
    p0, p1 = Vector(p0), Vector(p1)
    d = p1 - p0
    L = d.length
    u = d.normalized()
    n = u.cross(Z).normalized()
    k = int(L / spacing)
    for i in range(k + 1):
        if random.random() < broken:
            continue
        p = p0 + u * (i * spacing + random.uniform(-0.02, 0.02))
        hh = height * (random.uniform(0.55, 0.9) if random.random() < 0.1 else 1.0)
        mb.box(p + Z * hh / 2, (0.035, 0.035, hh), M['wood_grey'],
               rot=(random.uniform(-0.08, 0.08), random.uniform(-0.08, 0.08), 0), basis=(u, Z, n))
    posts = max(1, int(L / post_every))
    for i in range(posts + 1):
        p = p0 + d * (i / posts)
        mb.box(p + Z * (height + 0.05) / 2, (0.09, 0.09, height + 0.05), M['wood_dark'], basis=(u, Z, n))
    if top:
        segs = max(1, int(L / 1.8))
        for i in range(segs):
            if random.random() < broken * 0.8:
                continue
            a = p0 + d * (i / segs)
            b = p0 + d * ((i + 1) / segs)
            mb.box((a + b) / 2 + Z * height, ((b - a).length + 0.04, 0.08, 0.06), M['wood_dark'],
                   rot=(0, random.uniform(-0.03, 0.03), 0), basis=(u, Z, n))
        mb.box((p0 + p1) / 2 + Z * height * 0.15, (L, 0.05, 0.05), M['wood_dark'], basis=(u, Z, n))


def tile_roof(mb, M, eave0, eave_u, length, up_dir, slope_len, row=0.30, col=0.20, missing=0.02):
    """Faldón de tejas tipo árabe: filas solapadas de tejas curvas."""
    eave0 = Vector(eave0)
    S = Vector(up_dir).normalized()
    U = Vector(eave_u).normalized()
    N = U.cross(S).normalized()
    if N.z < 0:
        N = -N
    # base (tablero) debajo de las tejas
    c = eave0 + U * length / 2 + S * slope_len / 2 - N * 0.06
    mb.box(c, (length, 0.1, slope_len), M['wood_dark'], basis=(U, S, N))
    rows = int(slope_len / row) + 1
    cols = int(length / col)
    prof = 6
    for r in range(rows):
        sag = 0.0
        for k in range(cols):
            if random.random() < missing:
                continue
            uc = (k + 0.5) * length / cols + random.uniform(-0.01, 0.01)
            sc = r * row + row * 0.5
            if sc > slope_len:
                continue
            tl = row * 1.35
            tw = length / cols * 1.05
            # leve hundimiento del tejado en el centro (aspecto viejo)
            sag = -0.08 * math.sin(math.pi * uc / length) * math.sin(math.pi * min(1, sc / slope_len))
            base = eave0 + U * uc + S * sc + N * (0.02 + sag)
            lift = random.uniform(0.06, 0.12)
            yaw = random.uniform(-0.04, 0.04)
            ru = (U * math.cos(yaw) + S * math.sin(yaw)).normalized()
            rs = (S * math.cos(yaw) - U * math.sin(yaw)).normalized()
            rn = (N + rs * -lift).normalized()
            rs = rn.cross(ru).normalized() * (1 if rn.cross(ru).dot(S) > 0 else -1)
            verts = []
            for end in (-0.5, 0.5):
                for j in range(prof + 1):
                    a = math.pi * j / prof
                    for t in (0.0, 0.025):
                        x = math.cos(a) * (tw / 2 - t)
                        y = math.sin(a) * (tw * 0.33 - t)
                        verts.append(base + ru * x + rn * y + rs * (end * tl))
            faces = []
            per = (prof + 1) * 2
            for j in range(prof):
                a0, a1 = j * 2, (j + 1) * 2
                faces.append((a0, a1, per + a1, per + a0))            # exterior
                faces.append((a0 + 1, per + a0 + 1, per + a1 + 1, a1 + 1))  # interior
            for e in (0, per):
                for j in range(prof):
                    faces.append((e + j * 2, e + j * 2 + 1, e + (j + 1) * 2 + 1, e + (j + 1) * 2))
            mb.add(verts, faces, M['tiles'])


def gable_roof(mb, M, x0, x1, y0, y1, z_eave, z_peak, axis='Y', overhang=0.4, fascia=True):
    """Tejado a dos aguas. axis = dirección de la cumbrera."""
    if axis == 'Y':
        xm = (x0 + x1) / 2
        half = (x1 - x0) / 2 + overhang
        rise = z_peak - z_eave
        run = (x1 - x0) / 2
        slope = rise / run
        ze = z_eave - overhang * slope
        sl = math.hypot(half, half * slope)
        L = (y1 - y0) + 2 * overhang
        for s in (-1, 1):
            e0 = Vector((xm + s * half, y0 - overhang, ze))
            up = Vector((-s * half, 0, half * slope))
            tile_roof(mb, M, e0, Y, L, up, sl)
            if fascia:
                for yy in (y0 - overhang, y1 + overhang):
                    a = Vector((xm + s * half, yy, ze))
                    b = Vector((xm, yy, ze + half * slope))
                    d = b - a
                    mb.box((a + b) / 2 + Z * 0.05, (d.length, 0.06, 0.22), M['wood_dark'],
                           basis=(d.normalized(), d.normalized().cross(Y).normalized(), Y))
        # caballete
        mb.box((xm, (y0 + y1) / 2, z_peak + 0.12), (0.25, L, 0.16), M['tiles'])
    else:
        ym = (y0 + y1) / 2
        half = (y1 - y0) / 2 + overhang
        rise = z_peak - z_eave
        run = (y1 - y0) / 2
        slope = rise / run
        ze = z_eave - overhang * slope
        sl = math.hypot(half, half * slope)
        L = (x1 - x0) + 2 * overhang
        for s in (-1, 1):
            e0 = Vector((x0 - overhang, ym + s * half, ze))
            up = Vector((0, -s * half, half * slope))
            tile_roof(mb, M, e0, X, L, up, sl)
            if fascia:
                for xx in (x0 - overhang, x1 + overhang):
                    a = Vector((xx, ym + s * half, ze))
                    b = Vector((xx, ym, ze + half * slope))
                    d = b - a
                    mb.box((a + b) / 2 + Z * 0.05, (d.length, 0.06, 0.22), M['wood_dark'],
                           basis=(d.normalized(), d.normalized().cross(X).normalized(), X))
        mb.box(((x0 + x1) / 2, ym, z_peak + 0.12), (L, 0.25, 0.16), M['tiles'])


def stone_blocks(mb, M, origin, u, n, width, height, row_h=0.32, depth=0.12, openings=()):
    """Mampostería de bloques irregulares sobre una cara."""
    o = Vector(origin)
    v = 0.0
    while v < height - 0.05:
        rh = row_h * random.uniform(0.7, 1.25)
        rh = min(rh, height - v)
        p = random.uniform(-0.3, 0.0)
        while p < width - 0.02:
            bw = random.uniform(0.35, 0.75)
            a0, a1 = max(p, 0.0), min(p + bw, width)
            vm = v + rh / 2
            blocked = any(oa0 - 0.02 < (a0 + a1) / 2 < oa1 + 0.02 and ob0 < vm < ob1
                          for (oa0, ob0, oa1, ob1) in openings)
            if a1 - a0 > 0.08 and not blocked:
                c = o + u * ((a0 + a1) / 2) + Z * vm + n * (depth / 2 + random.uniform(-0.02, 0.03))
                mb.box(c, (a1 - a0 - 0.035, depth, rh - 0.035), M['stone'],
                       rot=(random.uniform(-0.04, 0.04), random.uniform(-0.04, 0.04), random.uniform(-0.03, 0.03)),
                       basis=(u, Z, n))
            p += bw
        v += rh


def brick_patch(mb, M, origin, u, n, uc, vc, w, h):
    """Parche de ladrillo/piedra donde se cayó el revoque."""
    o = Vector(origin)
    rows = int(h / 0.16)
    for r in range(rows):
        v = vc - h / 2 + r * 0.16
        # forma irregular: cada fila con ancho distinto
        ww = w * (0.55 + 0.45 * math.sin(math.pi * (r + 0.5) / rows)) * random.uniform(0.8, 1.1)
        p = uc - ww / 2 + (0.13 if r % 2 else 0.0)
        while p < uc + ww / 2 - 0.1:
            bw = random.uniform(0.22, 0.3)
            c = o + u * (p + bw / 2) + Z * (v + 0.08) + n * 0.02
            mb.box(c, (bw - 0.03, 0.05, 0.13), M['brick'],
                   rot=(0, random.uniform(-0.05, 0.05), 0), basis=(u, Z, n))
            p += bw


def wood_volume(mb, M, x0, x1, y0, y1, z0, z1, faces, mat='wood'):
    """Volumen con núcleo + tablones en las caras indicadas.
    faces: dict cara->lista de aberturas. Caras: '-Y','+X','+Y','-X'."""
    mb.box(((x0 + x1) / 2, (y0 + y1) / 2, (z0 + z1) / 2), (x1 - x0 - 0.02, y1 - y0 - 0.02, z1 - z0), M['wood_dark'])
    H = z1 - z0
    defs = {
        '-Y': ((x0, y0, z0), X, -Y, x1 - x0),
        '+X': ((x1, y0, z0), Y, X, y1 - y0),
        '+Y': ((x1, y1, z0), -X, Y, x1 - x0),
        '-X': ((x0, y1, z0), -Y, -X, y1 - y0),
    }
    for f, opens in faces.items():
        o, u, n, w = defs[f]
        plank_wall(mb, M[mat], o, u, n, w, H, openings=opens)
        # esquineros
    for (cx, cy) in ((x0, y0), (x1, y0), (x1, y1), (x0, y1)):
        mb.box((cx, cy, (z0 + z1) / 2), (0.16, 0.16, H), M['wood_dark'])
    return defs


def gable_wall(mb, M, origin, u, n, width, z0, h_wall, h_peak, openings=()):
    """Hastial (pared + triángulo) de tablones."""
    def prof(v):
        if v <= h_wall:
            return (0.0, width)
        t = (v - h_wall) / (h_peak - h_wall)
        half = width / 2 * (1 - t)
        return (width / 2 - half, width / 2 + half)
    o = Vector(origin)
    o.z = z0
    plank_wall(mb, M['wood'], o, u, n, width, h_peak, openings=openings, profile=prof)
    # núcleo triangular
    a = o + u * 0 + Z * h_wall
    b = o + u * width + Z * h_wall
    c = o + u * (width / 2) + Z * h_peak
    off = -n * 0.05
    verts = [a + off, b + off, c + off, a - n * 0.3, b - n * 0.3, c - n * 0.3]
    mb.add(verts, [(0, 1, 2), (3, 5, 4), (0, 3, 4, 1), (1, 4, 5, 2), (2, 5, 3, 0)], M['wood_dark'])


# ---------------------------------------------------------------------------
# La casa
# ---------------------------------------------------------------------------

def build_house(M):
    coll = collection("Casa")
    random.seed(SEED + 1)

    # ---------- A: base de piedra (torre derecha, parte baja) ----------
    mb = MeshBuilder()
    ax0, ax1, ay0, ay1 = 0.6, 4.4, -1.8, 1.8
    mb.box(((ax0 + ax1) / 2, (ay0 + ay1) / 2, -2.8), (ax1 - ax0, ay1 - ay0, 6.4), M['stone'])
    # la puerta baja está en la cara derecha (+X), que mira hacia la cámara-derecha
    low_door = (1.55, -3.75, 2.35, -1.75)
    stone_blocks(mb, M, (ax0, ay0, -5.5), X, -Y, ax1 - ax0, 5.9)
    stone_blocks(mb, M, (ax1, ay0, -5.5), Y, X, ay1 - ay0, 5.9,
                 openings=[(low_door[0], low_door[1] + 5.5, low_door[2], low_door[3] + 5.5)])
    door(mb, M, (ax1 + 0.06, ay0, 0), Y, X, low_door[0], low_door[2] - low_door[0], 2.0, v0=-3.75)
    # dintel de piedra sobre la puerta baja
    mb.box((ax1 + 0.12, ay0 + (low_door[0] + low_door[2]) / 2, -1.62), (0.2, 1.1, 0.22), M['stone'])
    mb.build("Casa_BasePiedra", coll, bevel=0.015)

    # ---------- B: torre revocada + C: base izquierda revocada ----------
    mb = MeshBuilder()
    mb.box(((ax0 + ax1) / 2, (ay0 + ay1) / 2, 1.7), (ax1 - ax0, ay1 - ay0, 2.6), M['plaster'])
    cx0, cx1, cy0, cy1 = -3.4, 0.6, -2.0, 1.8
    mb.box(((cx0 + cx1) / 2, (cy0 + cy1) / 2, 0.6), (cx1 - cx0, cy1 - cy0, 3.6), M['plaster'])
    for (o, u, n, w, patches) in [
        ((ax0, ay0, 0), X, -Y, ax1 - ax0, [(0.5, 2.4, 0.7, 0.8), (3.4, 0.9, 0.8, 0.9), (3.6, 2.6, 0.5, 0.5)]),
        ((ax1, ay0, 0), Y, X, ay1 - ay0, [(0.8, 1.2, 0.9, 1.0), (2.6, 2.4, 0.6, 0.6)]),
        ((cx0, cy0, 0), X, -Y, cx1 - cx0, [(0.6, 0.6, 0.9, 0.8), (2.2, 1.6, 0.6, 0.5), (3.1, 0.3, 0.6, 0.5)]),
        ((cx0, cy1, 0), -Y, -X, cy1 - cy0, [(1.0, 1.2, 1.0, 0.8)]),
    ]:
        for (uc, vc, w_, h_) in patches:
            brick_patch(mb, M, o, u, n, uc, vc, w_, h_)
    # puerta media y ventana tapiada
    # cara derecha (+X): puerta media entreabierta, ventana tapiada y baranda
    door(mb, M, (ax1 + 0.02, ay0, 0), Y, X, 1.0, 0.75, 1.95, v0=0.45, ajar=True)
    window(mb, M, (ax1 + 0.02, ay0, 0), Y, X, 2.35, 1.15, 0.75, 0.95, boarded=True)
    mb.box((ax1 + 0.45, ay0 + 0.85, 0.38), (0.9, 1.9, 0.12), M['wood_dark'])
    railing(mb, M, (ax1 + 0.85, ay0 - 0.05, 0.44), (ax1 + 0.85, ay0 + 1.0, 0.44), height=0.95, spacing=0.15, broken=0.1)
    railing(mb, M, (ax1 + 0.05, ay0 - 0.05, 0.44), (ax1 + 0.85, ay0 - 0.05, 0.44), height=0.95, spacing=0.15, broken=0.1)
    # cara frontal (-Y) de la torre: ventanita
    window(mb, M, (ax0, ay0 - 0.02, 0), X, -Y, 2.6, 1.3, 0.5, 0.75, broken=True)
    # repisa trasera-derecha con cerca rota (sobresale de la torre)
    mb.box((ax1 + 0.5, ay1 + 0.4, 0.32), (1.4, 1.6, 0.14), M['wood_dark'], rot=(0.03, -0.04, 0))
    for i in range(7):
        if random.random() < 0.2:
            continue
        yy = ay1 - 0.3 + i * 0.2
        h = random.uniform(0.6, 1.2)
        mb.box((ax1 + 1.15, yy, 0.38 + h / 2), (0.06, 0.06, h), M['wood_grey'],
               rot=(random.uniform(-0.25, 0.25), random.uniform(-0.35, 0.35), 0))
    mb.box((ax1 + 1.17, ay1 + 0.3, 1.0), (0.06, 1.4, 0.07), M['wood_grey'], rot=(0.25, 0, 0))
    # escalera izquierda (sube hasta el nivel de madera)
    steps = 12
    sx0, sx1, sz0, sz1 = -4.7, -1.35, -0.25, 2.4
    for i in range(steps):
        t = (i + 0.5) / steps
        x = lerp(sx0, sx1, t)
        z = lerp(sz0, sz1, t)
        mb.box((x, cy0 - 0.45, z), ((sx1 - sx0) / steps + 0.06, 0.9, 0.08), M['wood_grey'],
               rot=(0, 0, random.uniform(-0.04, 0.04)))
        mb.box((x, cy0 - 0.45, z - 0.25), ((sx1 - sx0) / steps, 0.85, 0.42), M['stone'])
    railing(mb, M, (sx0, cy0 - 0.9, sz0), (sx1, cy0 - 0.9, sz1), height=0.95, spacing=0.22, broken=0.15, post_every=1.2)
    # baranda de la escalera inclinada: balaustres siguen la pendiente
    mb.box((-1.0, cy0 - 0.5, 2.4), (0.9, 1.0, 0.12), M['wood_dark'])
    mb.build("Casa_BaseRevocada", coll, bevel=0.01)

    # ---------- D: nivel de madera izquierdo ----------
    mb = MeshBuilder()
    dx0, dx1, dy0, dy1 = -3.2, 0.6, -1.9, 1.8
    dz0, dz1 = 2.4, 5.4
    wood_volume(mb, M, dx0, dx1, dy0, dy1, dz0, dz1, {
        '-Y': [(1.75, 0.05, 2.6, 2.1), (0.45, 0.9, 1.05, 2.0)],
        '-X': [(1.0, 1.0, 1.6, 2.0)],
    })
    door(mb, M, (dx0, dy0 - 0.06, 0), X, -Y, 1.8, 0.75, 2.0, v0=dz0 + 0.05)
    window(mb, M, (dx0, dy0 - 0.06, 0), X, -Y, 0.5, dz0 + 0.95, 0.5, 1.0, shutters=False)
    window(mb, M, (dx0 - 0.06, dy1, 0), -Y, -X, 1.05, dz0 + 1.05, 0.5, 0.9, broken=True)
    # cornisa / alero entre plantas
    mb.box(((dx0 + dx1) / 2, dy0 - 0.15, dz1 + 0.05), (dx1 - dx0 + 0.4, 0.35, 0.12), M['wood_dark'])
    mb.box((dx0 - 0.15, (dy0 + dy1) / 2, dz1 + 0.05), (0.35, dy1 - dy0 + 0.4, 0.12), M['wood_dark'])
    mb.build("Casa_NivelMadera_Izq", coll, bevel=0.006)

    # ---------- E: nivel del porche (derecha) ----------
    mb = MeshBuilder()
    ex0, ex1, ey0, ey1 = 0.6, 4.2, -1.4, 1.8
    ez0, ez1 = 3.0, 5.8
    wood_volume(mb, M, ex0, ex1, ey0, ey1, ez0, ez1, {
        '-Y': [(0.35, 0.8, 0.95, 2.0), (1.45, 0.05, 2.25, 2.1), (2.7, 0.8, 3.3, 2.0)],
        '+X': [(0.6, 0.8, 1.2, 2.0), (1.9, 0.8, 2.5, 2.0)],
    })
    window(mb, M, (ex0, ey0 - 0.06, 0), X, -Y, 0.35, ez0 + 0.8, 0.6, 1.2)
    door(mb, M, (ex0, ey0 - 0.06, 0), X, -Y, 1.45, 0.8, 2.05, v0=ez0 + 0.05)
    window(mb, M, (ex0, ey0 - 0.06, 0), X, -Y, 2.7, ez0 + 0.8, 0.6, 1.2, broken=True)
    window(mb, M, (ex1 + 0.06, ey0, 0), Y, X, 0.6, ez0 + 0.8, 0.6, 1.2)
    window(mb, M, (ex1 + 0.06, ey0, 0), Y, X, 1.9, ez0 + 0.8, 0.6, 1.2)
    # piso del porche + vigas
    px0, px1, py0, py1 = 0.1, 5.1, -2.65, 2.3
    mb.box(((px0 + px1) / 2, (py0 + py1) / 2, ez0 - 0.07), (px1 - px0, py1 - py0, 0.14), M['wood_dark'])
    for i in range(10):
        x = lerp(px0 + 0.15, px1 - 0.15, i / 9)
        mb.box((x, py0 + 0.6, ez0 - 0.25), (0.14, 1.4, 0.22), M['wood_dark'])
    for i in range(8):
        y = lerp(py0 + 0.15, py1 - 0.15, i / 7)
        mb.box((px1 - 0.5, y, ez0 - 0.25), (1.2, 0.14, 0.22), M['wood_dark'])
    # tablas del piso (tablones sueltos)
    for i in range(int((px1 - px0) / 0.2)):
        x = px0 + 0.1 + i * 0.2
        mb.box((x, py0 + 0.6, ez0 + 0.01), (0.18, 1.25, 0.04), M['wood_grey'], rot=(0, random.uniform(-0.03, 0.03), 0))
    # postes (algunos torcidos) hasta el alero
    post_h = 2.75
    for (x, y) in [(px0 + 0.1, py0 + 0.1), (1.8, py0 + 0.1), (3.4, py0 + 0.1), (px1 - 0.1, py0 + 0.1),
                   (px1 - 0.1, 0.0), (px1 - 0.1, py1 - 0.1)]:
        lean = (random.uniform(-0.04, 0.04), random.uniform(-0.05, 0.05), 0)
        mb.box((x, y, ez0 + post_h / 2), (0.12, 0.12, post_h), M['wood_dark'], rot=lean)
    # poste de apoyo largo bajo el porche (desde la roca)
    mb.box((px1 - 0.15, py0 + 0.15, ez0 - 1.6), (0.12, 0.12, 3.2), M['wood_dark'], rot=(0.0, 0.08, 0))
    mb.box((px1 - 0.6, py0 + 0.15, ez0 - 1.2), (0.1, 0.1, 1.8), M['wood_dark'], rot=(0.0, -0.6, 0))
    # barandas del porche
    railing(mb, M, (px0 + 0.1, py0 + 0.1, ez0), (px1 - 0.1, py0 + 0.1, ez0), height=1.0, broken=0.1)
    railing(mb, M, (px1 - 0.1, py0 + 0.1, ez0), (px1 - 0.1, py1 - 0.1, ez0), height=1.0, broken=0.12)
    # alero de tejas del porche (frente y lado derecho)
    tile_roof(mb, M, Vector((px0 - 0.4, py0 - 0.35, ez1 - 0.25)), X, (px1 - px0) + 0.8,
              Vector((0, (ey0 - (py0 - 0.35)), 0.55)), math.hypot(ey0 - (py0 - 0.35), 0.55))
    tile_roof(mb, M, Vector((px1 + 0.35, py1 + 0.35, ez1 - 0.25)), -Y, (py1 - py0) + 0.6,
              Vector((-(px1 + 0.35 - ex1), 0, 0.55)), math.hypot(px1 + 0.35 - ex1, 0.55))
    # vigas del alero
    mb.box(((px0 + px1) / 2, py0 + 0.1, ez0 + post_h), (px1 - px0 + 0.3, 0.14, 0.18), M['wood_dark'])
    mb.box((px1 - 0.1, (py0 + py1) / 2, ez0 + post_h), (0.14, py1 - py0 + 0.3, 0.18), M['wood_dark'])
    # alero corto sobre la planta izquierda (continuación)
    tile_roof(mb, M, Vector((dx0 - 0.4, dy0 - 0.75, dz1 - 0.05)), X, dx1 - dx0 + 0.3,
              Vector((0, 0.75, 0.45)), math.hypot(0.75, 0.45))
    mb.build("Casa_NivelPorche", coll, bevel=0.006)

    # ---------- F: ala superior izquierda (hastial al frente) ----------
    mb = MeshBuilder()
    fx0, fx1, fy0, fy1 = -3.6, 0.4, -2.0, 1.8
    fz0, fz_eave, fz_peak = 5.55, 8.4, 11.3
    mb.box(((fx0 + fx1) / 2, (fy0 + fy1) / 2, (fz0 + fz_eave) / 2), (fx1 - fx0 - 0.02, fy1 - fy0 - 0.02, fz_eave - fz0), M['wood_dark'])
    w = fx1 - fx0
    gable_wall(mb, M, (fx0, fy0, 0), X, -Y, w, fz0, fz_eave - fz0, fz_peak - fz0,
               openings=[(0.85, 0.0, 1.7, 2.1), (1.75, 3.3, 2.25, 4.2)])
    gable_wall(mb, M, (fx1, fy1, 0), -X, Y, w, fz0, fz_eave - fz0, fz_peak - fz0)
    plank_wall(mb, M['wood'], (fx0, fy1, fz0), -Y, -X, fy1 - fy0, fz_eave - fz0, openings=[(1.0, 0.8, 1.6, 1.9)])
    plank_wall(mb, M['wood'], (fx1, fy0, fz0), Y, X, fy1 - fy0, fz_eave - fz0)
    for (cx, cy) in ((fx0, fy0), (fx1, fy0), (fx1, fy1), (fx0, fy1)):
        mb.box((cx, cy, (fz0 + fz_eave) / 2), (0.16, 0.16, fz_eave - fz0), M['wood_dark'])
    door(mb, M, (fx0, fy0 - 0.06, 0), X, -Y, 0.9, 0.75, 2.0, v0=fz0 + 0.05)
    window(mb, M, (fx0, fy0 - 0.06, 0), X, -Y, 1.75, fz0 + 3.3, 0.5, 0.9)
    window(mb, M, (fx0 - 0.06, fy1, 0), -Y, -X, 1.0, fz0 + 0.85, 0.55, 1.0)
    # balconcito
    mb.box((fx0 + 1.3, fy0 - 0.5, fz0 - 0.02), (2.3, 1.0, 0.12), M['wood_dark'])
    for x in (fx0 + 0.25, fx0 + 2.35):
        mb.box((x, fy0 - 0.55, fz0 - 0.4), (0.1, 0.1, 0.7), M['wood_dark'], rot=(0.6, 0, 0))
    railing(mb, M, (fx0 + 0.2, fy0 - 0.95, fz0 + 0.04), (fx0 + 2.4, fy0 - 0.95, fz0 + 0.04), height=0.9, broken=0.1)
    railing(mb, M, (fx0 + 0.2, fy0 - 0.05, fz0 + 0.04), (fx0 + 0.2, fy0 - 0.95, fz0 + 0.04), height=0.9, broken=0.1)
    gable_roof(mb, M, fx0, fx1, fy0, fy1, fz_eave, fz_peak, axis='Y', overhang=0.45)
    mb.build("Casa_AlaSuperior_Izq", coll, bevel=0.006)

    # ---------- G: ala superior derecha (hastial al lado derecho) ----------
    mb = MeshBuilder()
    gx0, gx1, gy0, gy1 = 0.4, 4.0, -1.6, 2.0
    gz0, gz_eave, gz_peak = 5.8, 9.2, 11.8
    mb.box(((gx0 + gx1) / 2, (gy0 + gy1) / 2, (gz0 + gz_eave) / 2), (gx1 - gx0 - 0.02, gy1 - gy0 - 0.02, gz_eave - gz0), M['wood_dark'])
    wd = gy1 - gy0
    gable_wall(mb, M, (gx1, gy0, 0), Y, X, wd, gz0, gz_eave - gz0, gz_peak - gz0,
               openings=[(0.55, 0.45, 1.25, 1.95), (2.3, 0.45, 3.0, 1.95),
                         (0.55, 2.3, 1.25, 3.25), (2.3, 2.3, 3.0, 3.25), (1.5, 3.8, 2.1, 4.7)])
    gable_wall(mb, M, (gx0, gy1, 0), -Y, -X, wd, gz0, gz_eave - gz0, gz_peak - gz0)
    plank_wall(mb, M['wood'], (gx0, gy0, gz0), X, -Y, gx1 - gx0, gz_eave - gz0,
               openings=[(0.7, 0.4, 1.35, 1.95), (2.2, 0.4, 2.85, 1.95), (1.3, 2.3, 1.9, 3.2)])
    plank_wall(mb, M['wood'], (gx1, gy1, gz0), -X, Y, gx1 - gx0, gz_eave - gz0)
    for (cx, cy) in ((gx0, gy0), (gx1, gy0), (gx1, gy1), (gx0, gy1)):
        mb.box((cx, cy, (gz0 + gz_eave) / 2), (0.16, 0.16, gz_eave - gz0), M['wood_dark'])
    o = (gx1 + 0.06, gy0, 0)
    window(mb, M, o, Y, X, 0.55, gz0 + 0.45, 0.7, 1.5)
    window(mb, M, o, Y, X, 2.3, gz0 + 0.45, 0.7, 1.5, broken=True)
    window(mb, M, o, Y, X, 0.55, gz0 + 2.3, 0.7, 0.95)
    window(mb, M, o, Y, X, 2.3, gz0 + 2.3, 0.7, 0.95)
    window(mb, M, o, Y, X, 1.5, gz0 + 3.8, 0.6, 0.9)
    o = (gx0, gy0 - 0.06, 0)
    window(mb, M, o, X, -Y, 0.7, gz0 + 0.4, 0.65, 1.55)
    window(mb, M, o, X, -Y, 2.2, gz0 + 0.4, 0.65, 1.55, broken=True)
    window(mb, M, o, X, -Y, 1.3, gz0 + 2.3, 0.6, 0.9)
    gable_roof(mb, M, gx0, gx1, gy0, gy1, gz_eave, gz_peak, axis='X', overhang=0.45)
    # cornisa inferior que separa del porche
    mb.box(((gx0 + gx1) / 2, gy0 - 0.12, gz0), (gx1 - gx0 + 0.3, 0.3, 0.12), M['wood_dark'])
    mb.box((gx1 + 0.12, (gy0 + gy1) / 2, gz0), (0.3, gy1 - gy0 + 0.3, 0.12), M['wood_dark'])
    mb.build("Casa_AlaSuperior_Der", coll, bevel=0.006)

    # leve inclinación general: la casa "se vence" (aspecto torcido de la ilustración)
    pivot = bpy.data.objects.new("Casa_Pivote", None)
    coll.objects.link(pivot)
    for ob in coll.objects:
        if ob is not pivot:
            ob.parent = pivot
    pivot.rotation_euler = (0.0, -0.015, math.radians(HOUSE_YAW))
    return pivot


# ---------------------------------------------------------------------------
# Peñasco / terreno
# ---------------------------------------------------------------------------

def terrain_height(x, y):
    """Mesa rocosa con acantilados escalonados."""
    # Meseta superior (casi plana, desciende hacia la derecha por el sendero)
    top = 0.15 * noise.noise(Vector((x * 0.3, y * 0.3, 0.0)))
    # escalón delante de la torre de piedra (la base baja de la casa)
    # (en coordenadas locales de la casa, que está girada HOUSE_YAW)
    th = math.radians(-HOUSE_YAW)
    lx = x * math.cos(th) - y * math.sin(th)
    ly = x * math.sin(th) + y * math.cos(th)
    local = smoothstep(4.3, 5.0, lx) * smoothstep(3.4, 2.6, ly) * smoothstep(-4.5, -3.0, ly)
    # repisa baja que sigue hacia la derecha (donde están los árboles de la derecha)
    right = smoothstep(2.5, 4.0, x) * smoothstep(5.0, 3.5, y)
    step_mask = max(local, right)
    top -= 3.75 * step_mask
    # la meseta baja suavemente hacia el frente (se ve su superficie desde la cámara)
    top -= 0.45 * max(0.0, -y - 2.5) * (1 - step_mask)
    # sendero que baja a la derecha
    top -= max(0.0, x - 6.0) * 0.35 * step_mask
    # forma de la mesa: radio variable con el ángulo
    cx, cy = 0.8, 1.5
    dx, dy = (x - cx) / 1.0, (y - cy) / 0.85
    r = math.hypot(dx, dy)
    ang = math.atan2(dy, dx)
    R = 7.2 + 1.6 * noise.noise(Vector((math.cos(ang) * 1.5, math.sin(ang) * 1.5, 3.3))) \
        + 2.5 * max(0.0, math.cos(ang - 0.0)) * 0.9 - 0.8 * max(0.0, -math.sin(ang)) \
        + 1.8 * max(0.0, -math.cos(ang)) \
        + 4.0 * max(0.0, -math.sin(ang)) * max(0.0, -math.cos(ang) + 0.3)
    drop = smoothstep(R, R + 17.0, r)
    h = top - 36.0 * drop ** 0.8
    # terrazas / estratos irregulares en el acantilado
    if drop > 0.01:
        step = 1.9
        hq = math.floor(h / step) * step
        w = 0.55 * smoothstep(0.02, 0.2, drop)
        h = lerp(h, hq + step * 0.5 * (1 + noise.noise(Vector((x * 0.5, y * 0.5, h * 0.3)))), w)
        # facetas tipo voronoi (bloques rocosos)
        d, _ = noise.voronoi(Vector((x * 0.35, y * 0.35, h * 0.12)), distance_metric='DISTANCE', exponent=2.5)
        h += (d[1] - d[0]) * 3.2 * smoothstep(0.02, 0.3, drop)
        h += noise.fractal(Vector((x * 0.6, y * 0.6, 1.7)), 0.6, 2.0, 4) * 0.6 * drop
        # columnas verticales: celdas voronoi 2D con desfase de altura por celda
        dc, pc = noise.voronoi(Vector((x * 0.38, y * 0.38, 0.0)), distance_metric='DISTANCE', exponent=2.5)
        cell = noise.noise(pc[0] * 7.31)
        h -= abs(cell) * 3.5 * smoothstep(0.05, 0.35, drop)
        # borde de cada columna ligeramente biselado
        h -= max(0.0, 0.25 - (dc[1] - dc[0])) * 4.0 * smoothstep(0.05, 0.35, drop)
    return h


def build_terrain(M):
    coll = collection("Penasco")
    random.seed(SEED + 2)
    size, res = 60.0, 420
    bm = bmesh.new()
    verts = []
    off = Vector((0.0, 6.0))
    for j in range(res + 1):
        row = []
        for i in range(res + 1):
            x = -size / 2 + size * i / res + off.x
            y = -size / 2 + size * j / res + off.y
            row.append(bm.verts.new((x, y, terrain_height(x, y))))
        verts.append(row)
    for j in range(res):
        for i in range(res):
            bm.faces.new((verts[j][i], verts[j][i + 1], verts[j + 1][i + 1], verts[j + 1][i]))
    me = bpy.data.meshes.new("Penasco")
    bm.to_mesh(me)
    bm.free()
    me.materials.append(M['rock'])
    for p in me.polygons:
        p.use_smooth = True
    ob = bpy.data.objects.new("Penasco", me)
    coll.objects.link(ob)
    # detalle fino por desplazamiento
    tex = bpy.data.textures.new("Roca_Voronoi", 'VORONOI')
    tex.noise_scale = 1.6
    tex.distance_metric = 'DISTANCE'
    d = ob.modifiers.new("Desplazar_Roca", 'DISPLACE')
    d.texture = tex
    d.strength = 0.55
    d.texture_coords = 'GLOBAL'

    # Rocas grandes angulosas incrustadas en el acantilado
    rocks = MeshBuilder()
    placed = 0
    tries = 0
    while placed < 130 and tries < 8000:
        tries += 1
        x = random.uniform(-14, 18)
        y = random.uniform(-14, 10)
        h = terrain_height(x, y)
        # pendiente (diferencia finita)
        e = 0.6
        gx = terrain_height(x + e, y) - terrain_height(x - e, y)
        gy = terrain_height(x, y + e) - terrain_height(x, y - e)
        slope = math.hypot(gx, gy) / (2 * e)
        if slope < 0.8 or h > -1.0 or h < -30:
            continue
        # evitar tapar la base de la casa
        s = random.uniform(0.8, 2.6)
        # la roca no debe asomar por encima del borde de la meseta (taparía la casa)
        if h - s * 0.5 + s * 2.2 > -1.2 - 0.0 * x and y < 0:
            continue
        add_boulder(rocks, M, Vector((x, y, h - s * 0.5)), s, tall=2.2)
        placed += 1
    # algunas rocas sueltas en la meseta y bordes
    for (x, y, s) in [(-5.6, -2.8, 0.9), (-4.2, -3.6, 0.6), (5.8, -3.6, 0.7), (6.5, -2.4, 0.5),
                      (-1.0, -3.2, 0.5), (2.9, -4.6, 0.8), (-6.5, 0.5, 0.7)]:
        add_boulder(rocks, M, Vector((x, y, terrain_height(x, y) - s * 0.35)), s)
    rob = rocks.build("Rocas_Acantilado", coll, smooth=True)
    # redondear las aristas (aspecto esculpido) y luego romper la superficie
    sub = rob.modifiers.new("Suavizar", 'SUBSURF')
    sub.levels = sub.render_levels = 1
    clouds = bpy.data.textures.new("Roca_Nubes", 'CLOUDS')
    clouds.noise_scale = 0.9
    clouds.noise_depth = 4
    d = rob.modifiers.new("Desplazar_Grande", 'DISPLACE')
    d.texture = clouds
    d.strength = 0.6
    d.mid_level = 0.5
    d.texture_coords = 'GLOBAL'
    d = rob.modifiers.new("Desplazar_Grietas", 'DISPLACE')
    d.texture = tex
    d.strength = 0.3
    d.texture_coords = 'GLOBAL'
    return ob


def add_boulder(mb, M, center, s, tall=1.4):
    """Roca angulosa: envolvente convexa de puntos aleatorios (facetas planas)."""
    bm = bmesh.new()
    sc = Vector((random.uniform(0.7, 1.2), random.uniform(0.7, 1.2), random.uniform(0.9, tall))) * s
    for _ in range(18):
        p = Vector((random.gauss(0, 1), random.gauss(0, 1), random.gauss(0, 1))).normalized()
        p *= random.uniform(0.75, 1.0)
        bm.verts.new((p.x * sc.x, p.y * sc.y, p.z * sc.z))
    bmesh.ops.convex_hull(bm, input=bm.verts[:])
    for f in [f for f in bm.faces if not f.is_valid]:
        pass
    rot = Euler((random.uniform(-0.25, 0.25), random.uniform(-0.25, 0.25), random.uniform(0, 6.28))).to_matrix()
    bm.verts.index_update()
    vs = [center + rot @ v.co for v in bm.verts]
    faces = [[v.index for v in f.verts] for f in bm.faces]
    bm.free()
    mb.add(vs, faces, M['rock'])


# ---------------------------------------------------------------------------
# Árboles secos retorcidos
# ---------------------------------------------------------------------------

def build_tree(name, coll, M, base, height, radius, seed, lean=(0.0, 0.0), spread=1.0, depth=4):
    rnd = random.Random(seed)
    cu = bpy.data.curves.new(name, 'CURVE')
    cu.dimensions = '3D'
    cu.bevel_depth = 1.0
    cu.bevel_resolution = 3
    cu.use_fill_caps = True
    cu.materials.append(M['bark'])
    ob = bpy.data.objects.new(name, cu)
    coll.objects.link(ob)

    def grow(p, d, length, r, level):
        """Rama retorcida: curvatura continua (no ruido), pocas ramas hijas
        gruesas que se abren y se curvan hacia arriba como garras."""
        steps = max(5, int(length / 0.2))
        seg = length / steps
        pts = [(p.copy(), r)]
        cur = p.copy()
        dd = d.normalized()
        # posiciones fijas de las ramas hijas (pocas, como en la ilustración)
        nkids = 0 if level >= depth else rnd.choice((4, 5) if level == 0 else (2, 3) if level == 1 else (2, 2))
        kid_t = sorted(rnd.uniform(0.32 if level == 0 else 0.25, 0.8 if level == 0 else 0.95) for _ in range(nkids))
        children = []
        bend = Vector((rnd.uniform(-1, 1), rnd.uniform(-1, 1), 0)).normalized() * (rnd.uniform(0.12, 0.18) if level == 0 else rnd.uniform(0.07, 0.13))
        az0 = rnd.uniform(0, math.tau)
        kid_i = 0
        twist = rnd.uniform(-1, 1)
        for k in range(1, steps + 1):
            t = k / steps
            jitter = Vector((rnd.uniform(-1, 1), rnd.uniform(-1, 1), rnd.uniform(-0.5, 0.5))) * 0.07
            curl = Vector((-dd.y, dd.x, 0)) * twist * 0.12
            # las puntas tienden a curvarse hacia arriba
            up = Z * (0.01 + 0.06 * t if level > 0 else 0.05)
            dd = (dd + jitter + curl + bend * math.sin(t * math.pi * 2.0) + up).normalized()
            cur = cur + dd * seg
            rr = r * (1 - 0.93 * t ** 1.1)
            if level == 0 and t < 0.15:
                rr = r * (1.7 - 4.7 * t)
            pts.append((cur.copy(), max(rr, 0.008)))
            while kid_t and t >= kid_t[0]:
                kid_t.pop(0)
                # ramas repartidas alrededor del tronco, abiertas casi en horizontal
                az = az0 + kid_i * (math.tau / max(1, nkids)) + rnd.uniform(-0.5, 0.5)
                kid_i += 1
                side = Vector((math.cos(az), math.sin(az) * 0.6, rnd.uniform(-0.05, 0.4))).normalized()
                nd = (dd * 0.3 + side * spread * 1.4).normalized()
                children.append((cur.copy(), nd, length * rnd.uniform(0.7, 0.95) * (1 - t * 0.3),
                                 rr * rnd.uniform(0.72, 0.88), level + 1))
        sp = cu.splines.new('NURBS')
        sp.points.add(len(pts) - 1)
        for i, (co, rr) in enumerate(pts):
            sp.points[i].co = (co.x, co.y, co.z, 1.0)
            sp.points[i].radius = rr
        sp.order_u = 3
        sp.use_endpoint_u = True
        sp.resolution_u = 3
        # puntas: 2-3 ramitas cortas y curvas
        if level == depth:
            for _ in range(rnd.randint(1, 2)):
                tw = Vector((rnd.uniform(-1, 1), rnd.uniform(-1, 1), rnd.uniform(0.2, 1))).normalized()
                children.append((cur.copy(), (dd + tw * 0.8).normalized(), length * 0.45, pts[-1][1] * 0.9, depth + 1))
        for c in children:
            if c[4] <= depth + 1:
                grow(*c)

    d0 = Vector((lean[0], lean[1], 1.0))
    grow(Vector(base) - Z * 0.4, d0, height, radius, 0)
    # raíces que asoman
    for i in range(5):
        a = i / 5 * math.tau + rnd.uniform(-0.3, 0.3)
        dr = Vector((math.cos(a), math.sin(a), -0.35))
        sp = cu.splines.new('NURBS')
        pts = []
        cur = Vector(base) + Z * 0.3
        rr = radius * 0.8
        for k in range(6):
            pts.append((cur.copy(), rr))
            cur += dr * 0.35 + Vector((rnd.uniform(-0.1, 0.1), rnd.uniform(-0.1, 0.1), -0.06))
            rr *= 0.68
        sp.points.add(len(pts) - 1)
        for k, (co, r) in enumerate(pts):
            sp.points[k].co = (co.x, co.y, co.z, 1)
            sp.points[k].radius = r
        sp.order_u = 3
        sp.use_endpoint_u = True
    # convertir a malla para poder deformar la corteza (nudos, torceduras)
    dg = bpy.context.evaluated_depsgraph_get()
    me = bpy.data.meshes.new_from_object(ob.evaluated_get(dg))
    mob = bpy.data.objects.new(name, me)
    coll.objects.link(mob)
    bpy.data.objects.remove(ob, do_unlink=True)
    bpy.data.curves.remove(cu)
    mob.name = name
    for p in me.polygons:
        p.use_smooth = True
    bark = bpy.data.textures.get("Corteza_Nudos") or bpy.data.textures.new("Corteza_Nudos", 'CLOUDS')
    bark.noise_scale = 0.35
    bark.noise_depth = 3
    dm = mob.modifiers.new("Nudos", 'DISPLACE')
    dm.texture = bark
    dm.strength = 0.1
    dm.texture_coords = 'GLOBAL'
    return mob


def build_trees(M):
    coll = collection("Arboles")
    trees = [
        # (nombre, base xy, altura tronco, radio, inclinación, apertura, profundidad)
        ("Arbol_Izq_Fondo", (-7.0, 0.4), 7.0, 0.6, (-0.3, 0.05), 0.85, 2),
        ("Arbol_Primer_Plano", (-5.5, -8.0), 6.5, 0.5, (0.15, -0.1), 1.0, 2),
        ("Arbol_Derecha", (8.4, -1.4), 7.5, 0.65, (-0.25, -0.05), 1.3, 2),
        ("Arbol_Derecha_Lejos", (11.4, -0.6), 5.0, 0.38, (0.15, 0.0), 1.2, 2),
    ]
    for i, (n, (x, y), h, r, lean, spread, depth) in enumerate(trees):
        z = terrain_height(x, y)
        build_tree(n, coll, M, Vector((x, y, z)), h, r, SEED * 31 + i * 7, lean, spread, depth)

    # cerca rota en el sendero de la derecha
    mb = MeshBuilder()
    random.seed(SEED + 9)
    for k in range(6):
        x = 10.4 + k * 0.22
        y = -0.9 + k * 0.05
        h = random.uniform(0.5, 1.1)
        z = terrain_height(x, y)
        mb.box((x, y, z + h / 2 - 0.1), (0.07, 0.07, h), M['wood_grey'],
               rot=(random.uniform(-0.3, 0.3), random.uniform(-0.3, 0.3), 0))
    z = terrain_height(10.9, -0.8)
    mb.box((10.9, -0.8, z + 0.6), (1.3, 0.06, 0.08), M['wood_grey'], rot=(0, 0.25, 0.2))
    mb.build("Cerca_Rota", coll)


# ---------------------------------------------------------------------------
# Cielo, luces, cámara, render
# ---------------------------------------------------------------------------

def build_world():
    w = bpy.data.worlds.new("Cielo_Tormentoso")
    bpy.context.scene.world = w
    try:
        w.use_nodes = True
    except Exception:
        pass
    nt = w.node_tree
    nt.nodes.clear()
    out = nt.nodes.new('ShaderNodeOutputWorld')
    tc = nt.nodes.new('ShaderNodeTexCoord')
    mp = nt.nodes.new('ShaderNodeMapping')
    mp.inputs['Scale'].default_value = (1.0, 1.0, 2.6)
    nt.links.new(tc.outputs['Generated'], mp.inputs['Vector'])
    # degradado lateral: morado a la izquierda, verde a la derecha (según la cámara)
    dot = nt.nodes.new('ShaderNodeVectorMath')
    dot.operation = 'DOT_PRODUCT'
    nt.links.new(tc.outputs['Generated'], dot.inputs[0])
    dot.inputs[1].default_value = (0.94, -0.33, -0.15)
    warp = _noise(nt, mp.outputs[0], 1.2, 3, 0.5, 0.3)
    side = nt.nodes.new('ShaderNodeMath')
    side.operation = 'MULTIPLY_ADD'
    nt.links.new(warp.outputs['Fac'], side.inputs[0])
    side.inputs[1].default_value = 0.6
    nt.links.new(dot.outputs['Value'], side.inputs[2])
    sr = nt.nodes.new('ShaderNodeMapRange')
    sr.inputs['From Min'].default_value = -0.1
    sr.inputs['From Max'].default_value = 1.0
    nt.links.new(side.outputs[0], sr.inputs['Value'])
    hue = _ramp(nt, sr.outputs[0], [(0.0, (0.12, 0.10, 0.135)), (0.25, (0.13, 0.125, 0.14)), (0.45, (0.12, 0.17, 0.15)), (0.7, (0.13, 0.25, 0.18)), (1.0, (0.16, 0.31, 0.22))])
    # nubes suaves y anchas
    n1 = _noise(nt, mp.outputs[0], 4.0, 8, 0.6, 0.12)
    n2 = _noise(nt, mp.outputs[0], 8.0, 6, 0.6, 0.0)
    dark = _ramp(nt, n1.outputs['Fac'], [(0.34, (0.22, 0.18, 0.28)), (0.48, (0.6, 0.58, 0.66)), (0.6, (1.3, 1.4, 1.35)), (0.74, (2.1, 2.3, 2.15))])
    col = _mix_rgb(nt, 1.0, hue, dark, 'MULTIPLY')
    wisps = _ramp(nt, n2.outputs['Fac'], [(0.5, (0, 0, 0)), (0.8, (0.05, 0.08, 0.065))])
    col = _mix_rgb(nt, 1.0, col, wisps, 'ADD')
    bg = nt.nodes.new('ShaderNodeBackground')
    bg.inputs['Strength'].default_value = 1.0
    nt.links.new(col, bg.inputs['Color'])
    # para la iluminación, un tono uniforme suave (luz ambiental)
    amb = nt.nodes.new('ShaderNodeBackground')
    amb.inputs['Color'].default_value = (0.11, 0.12, 0.13, 1)
    amb.inputs['Strength'].default_value = 0.5
    lp = nt.nodes.new('ShaderNodeLightPath')
    mix = nt.nodes.new('ShaderNodeMixShader')
    nt.links.new(lp.outputs['Is Camera Ray'], mix.inputs[0])
    nt.links.new(amb.outputs[0], mix.inputs[1])
    nt.links.new(bg.outputs[0], mix.inputs[2])
    nt.links.new(mix.outputs[0], out.inputs['Surface'])


def build_lights():
    coll = collection("Luces")
    def sun(name, rot, energy, color, angle):
        ld = bpy.data.lights.new(name, 'SUN')
        ld.energy = energy
        ld.color = color
        ld.angle = math.radians(angle)
        ob = bpy.data.objects.new(name, ld)
        ob.rotation_euler = [math.radians(a) for a in rot]
        coll.objects.link(ob)
        return ob
    # luz de luna fría desde arriba-izquierda (ilumina la fachada frontal)
    sun("Luna_Principal", (50, 0, -25), 3.6, (0.82, 0.86, 0.85), 15)
    # contraluz verdoso desde atrás-derecha (bordes de los árboles y tejado)
    sun("Contraluz_Verde", (70, 0, 150), 1.2, (0.55, 0.85, 0.65), 10)
    # relleno morado desde abajo (rebote del cielo)
    sun("Relleno_Morado", (115, 0, 20), 0.35, (0.75, 0.6, 0.9), 30)


def build_camera():
    cam = bpy.data.cameras.new("Camara")
    cam.lens = 38
    cam.sensor_width = 36
    ob = bpy.data.objects.new("Camara", cam)
    bpy.context.scene.collection.objects.link(ob)
    ob.location = (-5.0, -40.0, 7.0)
    target = Vector((1.0, 0.0, 2.9))
    d = target - ob.location
    ob.rotation_euler = d.to_track_quat('-Z', 'Y').to_euler()
    bpy.context.scene.camera = ob
    cam.dof.use_dof = False
    return ob


def setup_render(res=(1920, 1048), samples=128):
    sc = bpy.context.scene
    sc.render.engine = 'CYCLES'
    sc.cycles.device = 'CPU'
    sc.cycles.samples = samples
    sc.cycles.use_denoising = True
    sc.cycles.max_bounces = 6
    sc.render.resolution_x, sc.render.resolution_y = res
    sc.render.resolution_percentage = 100
    sc.render.film_transparent = False
    sc.view_settings.view_transform = 'AgX'
    try:
        sc.view_settings.look = 'AgX - Medium High Contrast'
    except Exception:
        pass
    sc.view_settings.exposure = 0.45


def main():
    argv = sys.argv[sys.argv.index('--') + 1:] if '--' in sys.argv else sys.argv[1:]
    out_png = None
    out_blend = None
    samples = 128
    res = (1920, 1048)
    for i, a in enumerate(argv):
        if a == '--render':
            out_png = argv[i + 1]
        elif a == '--save':
            out_blend = argv[i + 1]
        elif a == '--samples':
            samples = int(argv[i + 1])
        elif a == '--res':
            res = tuple(int(v) for v in argv[i + 1].split('x'))
    clear_scene()
    M = build_materials()
    build_terrain(M)
    build_house(M)
    build_trees(M)
    build_world()
    build_lights()
    build_camera()
    setup_render(res, samples)
    if out_blend:
        bpy.ops.wm.save_as_mainfile(filepath=out_blend)
    if out_png:
        bpy.context.scene.render.filepath = out_png
        bpy.ops.render.render(write_still=True)


if __name__ == "__main__":
    main()
