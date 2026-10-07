"""Extrae un 'sky plate' de la ilustración: quita casa, roca y árboles y rellena con cielo."""
import cv2, numpy as np, sys
src = cv2.imread(sys.argv[1])
h, w = src.shape[:2]
L = cv2.cvtColor(src, cv2.COLOR_BGR2LAB)[:, :, 0].astype(np.float32)
blur = cv2.GaussianBlur(L, (0, 0), 25)
trees = ((L < 72) | (L < blur - 10)).astype(np.uint8) * 255
trees[:330, :] = 0
trees = cv2.morphologyEx(trees, cv2.MORPH_OPEN, np.ones((2, 2), np.uint8))
trees = cv2.dilate(trees, np.ones((7, 7), np.uint8), iterations=3)
# 1) árboles y ramas finas: inpaint
img = cv2.inpaint(src, trees, 12, cv2.INPAINT_TELEA).astype(np.float32)
# 2) bloque casa + roca: relleno espejado desde ambos lados, mezclado en degradé
poly = np.array([[670, 60], [760, 30], [1150, 20], [1230, 290], [1300, 440], [1350, 640], [1680, 960],
                 [1700, 1116], [220, 1116], [330, 860], [500, 700], [640, 600], [680, 380]], np.int32)
big = np.zeros((h, w), np.uint8)
cv2.fillPoly(big, [poly], 255)
big = cv2.dilate(big, np.ones((9, 9), np.uint8), iterations=2)
m = big > 0
out = img.copy()
for y in range(h):
    xs = np.where(m[y])[0]
    if len(xs) == 0:
        continue
    a, b = xs[0], xs[-1] + 1
    n = b - a
    for i, xx in enumerate(range(a, b)):
        t = (i + 0.5) / n
        ls = max(0, a - 1 - i)
        rs = min(w - 1, b + (b - 1 - xx))
        L_ = img[y, ls] if a > 0 else img[y, rs]
        R_ = img[y, rs] if b < w else img[y, ls]
        out[y, xx] = L_ * (1 - t) + R_ * t
fill = cv2.GaussianBlur(out, (0, 0), sigmaX=3, sigmaY=8)
m3 = cv2.GaussianBlur(big, (0, 0), 6)[:, :, None] / 255.0
res = img * (1 - m3) + fill * m3
cv2.imwrite(sys.argv[2], res.astype(np.uint8))
