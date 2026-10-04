"""
Base de escritorio para Focusrite Scarlett 2i2 (3ª Gen) - versión para IMPRESIÓN 3D.

Basado en los planos (anchura interior 178 mm, laterales 12 mm, profundidad 110 mm,
altura frontal 12 mm, altura trasera 65 mm, orificios de cables ø15 mm).

Pieza única, sólida y estanca (manifold), pensada para FDM:
  * se imprime tal cual (cara inferior plana sobre la cama), SIN soportes:
    la cara superior es una rampa y los orificios son verticales;
  * el slicer rellena el interior (infill), así no hace falta vaciarla;
  * rebaje de 2 mm en la rampa para pegar la goma EVA;
  * tope frontal que retiene el 2i2 en la pendiente.

Uso:
  blender --background --python build_stand.py            -> .blend + .stl
  blender --background --python build_stand.py -- --render -> además preview.png
  (o pegarlo en el Text Editor de Blender y pulsar "Run Script")

Ejes: X = anchura, Y = profundidad (Y=0 frente), Z = altura. Cotas en mm.
"""
import math
import os
import sys

import bpy  # importar bpy antes que bmesh/mathutils
import bmesh
from mathutils import Matrix, Vector

# ---------------------------------------------------------------- parámetros (mm)
INNER_W = 178.0      # anchura interior (el 2i2 mide 175 mm)
SIDE_T = 12.0        # espesor de los laterales
DEPTH = 110.0        # profundidad de la base
FRONT_H = 12.0       # altura frontal
REAR_H = 65.0        # altura trasera
CORNER_R = 4.0       # redondeo de las esquinas del perfil lateral
LIP_T = 12.0         # espesor del tope frontal (en Y)
LIP_ABOVE = 10.0     # cuánto sobresale el tope por encima de la rampa
EVA_DEPTH = 2.0      # rebaje para la goma EVA
EVA_MARGIN = 3.0     # margen entre rebaje y bordes
HOLE_D = 15.0        # orificios de gestión de cables (verticales, pasantes)
HOLES = [(0.42, 0.45), (0.70, 0.75)]  # posición (fracción ancho, fracción fondo)

WIDTH = INNER_W + 2 * SIDE_T
SLOPE = (REAR_H - FRONT_H) / DEPTH
ANGLE = math.atan(SLOPE)  # ~25,7° con estas cotas
MM = 0.001


def z_top(y):
    return FRONT_H + y * SLOPE


# ---------------------------------------------------------------- utilidades
def reset_scene():
    bpy.ops.wm.read_factory_settings(use_empty=True)
    u = bpy.context.scene.unit_settings
    u.system, u.length_unit, u.scale_length = "METRIC", "MILLIMETERS", 1.0


def material(name, color, rough=0.5):
    m = bpy.data.materials.new(name)
    m.use_nodes = True
    bsdf = m.node_tree.nodes["Principled BSDF"]
    bsdf.inputs["Base Color"].default_value = (*color, 1.0)
    bsdf.inputs["Roughness"].default_value = rough
    m.diffuse_color = (*color, 1.0)
    return m


def link(ob, col):
    for c in ob.users_collection:
        c.objects.unlink(ob)
    col.objects.link(ob)


def box(name, x0, x1, y0, y1, z0, z1, col):
    bm = bmesh.new()
    bmesh.ops.create_cube(bm, size=1.0)
    bmesh.ops.scale(bm, vec=((x1 - x0) * MM, (y1 - y0) * MM, (z1 - z0) * MM), verts=bm.verts)
    bmesh.ops.translate(bm, vec=((x0 + x1) / 2 * MM, (y0 + y1) / 2 * MM, (z0 + z1) / 2 * MM), verts=bm.verts)
    me = bpy.data.meshes.new(name)
    bm.to_mesh(me)
    bm.free()
    ob = bpy.data.objects.new(name, me)
    col.objects.link(ob)
    return ob


def body(name, col):
    """Perfil lateral redondeado (plano YZ) extruido a todo el ancho."""
    bm = bmesh.new()
    pts = [(0, 0), (DEPTH, 0), (DEPTH, REAR_H), (0, FRONT_H)]
    face = bm.faces.new([bm.verts.new((0, y * MM, z * MM)) for y, z in pts])
    bmesh.ops.bevel(bm, geom=list(face.verts), offset=CORNER_R * MM, segments=8,
                    affect="VERTICES", clamp_overlap=True)
    ext = bmesh.ops.extrude_face_region(bm, geom=list(bm.faces))
    bmesh.ops.translate(bm, vec=(WIDTH * MM, 0, 0),
                        verts=[v for v in ext["geom"] if isinstance(v, bmesh.types.BMVert)])
    bmesh.ops.recalc_face_normals(bm, faces=bm.faces)
    me = bpy.data.meshes.new(name)
    bm.to_mesh(me)
    bm.free()
    ob = bpy.data.objects.new(name, me)
    col.objects.link(ob)
    return ob


def boolean(target, tool, op):
    mod = target.modifiers.new(tool.name, "BOOLEAN")
    mod.operation, mod.object, mod.solver = op, tool, "EXACT"
    bpy.context.view_layer.objects.active = target
    bpy.ops.object.modifier_apply(modifier=mod.name)
    bpy.data.objects.remove(tool, do_unlink=True)


def apply_transform(ob):
    bpy.ops.object.select_all(action="DESELECT")
    ob.select_set(True)
    bpy.context.view_layer.objects.active = ob
    bpy.ops.object.transform_apply(location=True, rotation=True, scale=True)


# ---------------------------------------------------------------- construcción
def build():
    reset_scene()
    col = bpy.data.collections.new("Base_Scarlett_2i2")
    bpy.context.scene.collection.children.link(col)

    stand = body("Base_Scarlett_2i2", col)
    stand.data.materials.append(material("PLA_Negro", (0.04, 0.04, 0.045), 0.45))

    # tope frontal (unión) - se solapa 1 mm con el cuerpo para fusión limpia
    lip = box("Tope", SIDE_T, SIDE_T + INNER_W, 0, LIP_T, FRONT_H - 1, z_top(LIP_T) + LIP_ABOVE, col)
    boolean(stand, lip, "UNION")

    # rebaje para goma EVA sobre la rampa (entre los laterales y detrás del tope)
    y0 = LIP_T + EVA_MARGIN
    length = (DEPTH - CORNER_R - EVA_MARGIN - y0) / math.cos(ANGLE)
    pocket = box("Rebaje_EVA", 0, INNER_W - 2 * EVA_MARGIN, 0, length, -EVA_DEPTH, 10, col)
    pocket.matrix_world = Matrix.Translation(Vector((SIDE_T + EVA_MARGIN, y0, z_top(y0))) * MM) \
        @ Matrix.Rotation(ANGLE, 4, "X")
    apply_transform(pocket)
    boolean(stand, pocket, "DIFFERENCE")

    # orificios de cables: verticales y pasantes (imprimibles sin soporte)
    for i, (fx, fy) in enumerate(HOLES):
        x = SIDE_T + INNER_W * fx
        y = LIP_T + (DEPTH - LIP_T) * fy
        bpy.ops.mesh.primitive_cylinder_add(vertices=64, radius=HOLE_D / 2 * MM, depth=200 * MM,
                                            location=(x * MM, y * MM, 0))
        cyl = bpy.context.active_object
        cyl.name = f"Orificio_{i + 1}"
        boolean(stand, cyl, "DIFFERENCE")

    # origen en la cara inferior para que asiente en Z=0 en el slicer
    bpy.context.scene.cursor.location = (0, 0, 0)
    stand.select_set(True)
    bpy.ops.object.origin_set(type="ORIGIN_CURSOR")

    # referencia del Scarlett 2i2 3ª Gen (175 x 99 x 43 mm) - oculta, solo para comprobar encaje
    ref_col = bpy.data.collections.new("Referencia_2i2")
    bpy.context.scene.collection.children.link(ref_col)
    ref = box("Scarlett_2i2_ref", 0, 175, 0, 99, 0, 43, ref_col)
    ref.data.materials.append(material("Scarlett_Rojo", (0.55, 0.02, 0.02), 0.35))
    ref.matrix_world = Matrix.Translation(Vector((SIDE_T + 1.5, LIP_T + 0.5, z_top(LIP_T + 0.5))) * MM) \
        @ Matrix.Rotation(ANGLE, 4, "X")
    ref_col.hide_render = True
    bpy.context.view_layer.layer_collection.children["Referencia_2i2"].hide_viewport = True
    return stand


def check_manifold(ob):
    bm = bmesh.new()
    bm.from_mesh(ob.data)
    bad = [e for e in bm.edges if not e.is_manifold]
    vol = bm.calc_volume() / MM ** 3
    bm.free()
    print(f"[check] aristas no-manifold: {len(bad)} | volumen: {vol / 1000:.1f} cm3")
    return not bad


def setup_render(path_png):
    scn = bpy.context.scene
    target = Vector((WIDTH / 2, DEPTH / 2, 25)) * MM
    cam = bpy.data.objects.new("Camara", bpy.data.cameras.new("Camara"))
    scn.collection.objects.link(cam)
    cam.location = target + Vector((-0.30, -0.36, 0.25))
    cam.rotation_euler = (target - cam.location).to_track_quat("-Z", "Y").to_euler()
    scn.camera = cam
    for name, loc, energy in [("Key", (-0.3, -0.3, 0.5), 15), ("Fill", (0.5, -0.2, 0.3), 5),
                              ("Rim", (0.1, 0.5, 0.4), 10)]:
        ld = bpy.data.lights.new(name, "AREA")
        ld.energy, ld.size = energy, 0.3
        lo = bpy.data.objects.new(name, ld)
        lo.location = loc
        lo.rotation_euler = (target - Vector(loc)).to_track_quat("-Z", "Y").to_euler()
        scn.collection.objects.link(lo)
    world = bpy.data.worlds.new("Mundo")
    world.use_nodes = True
    world.node_tree.nodes["Background"].inputs["Color"].default_value = (0.9, 0.9, 0.92, 1)
    world.node_tree.nodes["Background"].inputs["Strength"].default_value = 0.4
    scn.world = world
    floor = box("Suelo", -300, 500, -300, 500, -1, 0, scn.collection)
    floor.data.materials.append(material("Suelo", (0.8, 0.8, 0.8), 0.8))
    scn.render.engine = "CYCLES"
    scn.cycles.device, scn.cycles.samples, scn.cycles.use_denoising = "CPU", 48, True
    scn.render.resolution_x, scn.render.resolution_y = 1280, 900
    scn.render.filepath = path_png


def main():
    out_dir = os.path.dirname(os.path.abspath(__file__)) if "__file__" in globals() else os.getcwd()
    stand = build()
    check_manifold(stand)

    bpy.ops.object.select_all(action="DESELECT")
    stand.select_set(True)
    bpy.ops.wm.stl_export(filepath=os.path.join(out_dir, "base_scarlett_2i2.stl"),
                          export_selected_objects=True, global_scale=1000.0)  # STL en mm

    setup_render(os.path.join(out_dir, "preview.png"))
    bpy.ops.wm.save_as_mainfile(filepath=os.path.join(out_dir, "base_scarlett_2i2.blend"))
    if "--render" in sys.argv:
        bpy.ops.render.render(write_still=True)


if __name__ == "__main__":
    main()
