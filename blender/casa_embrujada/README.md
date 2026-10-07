# Casa embrujada sobre el peñasco (Blender)

Recreación 3D, totalmente procedural, de la ilustración de la casa embrujada sobre la roca.

| Archivo | Qué es |
|---|---|
| `casa_embrujada.py` | Script que genera toda la escena (geometría, materiales, luces, cámara, render) |
| `casa_embrujada.blend` | Escena ya generada (guardada con Blender 5.2) |
| `render_casa_embrujada.png` | Render final en Cycles, 1920×1056, 128 muestras + denoise |

## Cómo usarlo

**En Blender (4.2 o posterior):** abre `casa_embrujada.blend`, o abre un archivo vacío, carga
`casa_embrujada.py` en el *Text Editor* y pulsa **Run Script**.

**Desde la terminal:**
```bash
blender -b -P casa_embrujada.py -- --save escena.blend --render render.png --res 1920x1056 --samples 128
```

## Qué contiene la escena (organizada en colecciones)

- **Penasco**: malla de terreno (meseta + acantilados con terrazas y columnas verticales tipo Voronoi),
  desplazamiento fino y unas 130 rocas angulosas (envolventes convexas) incrustadas en las paredes.
- **Casa** (agrupada bajo `Casa_Pivote`, ligeramente vencida):
  - base de mampostería con puerta baja y ventana tapiada;
  - torre revocada con parches de ladrillo, puerta entreabierta, repisa con baranda y cerca rota;
  - escalera exterior con baranda;
  - nivel de madera izquierdo y nivel del porche con galería envolvente, postes, puntales y alero de tejas;
  - dos alas superiores con hastiales cruzados, balconcito, ventanas con marcos/cruces (algunas rotas)
    y tejados de tejas árabes modeladas una a una, con leve hundimiento.
- **Arboles**: 4 árboles secos retorcidos generados por crecimiento recursivo (curvas con radio por punto) y raíces.
- **Luces**: luna fría principal, contraluz verdoso y relleno morado. El cielo tormentoso morado/verde es un
  shader procedural en el *World*.

Los parámetros clave (semilla `SEED`, posiciones de árboles, cámara, materiales) están al principio de cada
función para retocarlos fácilmente.
