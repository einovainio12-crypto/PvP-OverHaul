"""Original 16px game icons drawn from editable geometry and small material palettes.

Real weapon silhouettes (tapered knives, hafted double-bit axes, bent bow limbs,
wooden crossbow stocks and fletched arrows) guide the shapes. No game pixels are copied.
"""
import math,struct,zlib

class Canvas:
 def __init__(self):self.p=[[(0,0,0,0) for _ in range(16)] for _ in range(16)]
 def dot(self,x,y,c):
  if 0<=x<16 and 0<=y<16:self.p[y][x]=(*c[:3],255)
 def line(self,a,b,c):
  x0,y0=a;x1,y1=b;steps=max(abs(x1-x0),abs(y1-y0))
  for i in range(steps+1):self.dot(round(x0+(x1-x0)*i/max(1,steps)),round(y0+(y1-y0)*i/max(1,steps)),c)
 def polygon(self,points,c):
  for y in range(16):
   for x in range(16):
    inside=False;j=len(points)-1
    for i,(xi,yi) in enumerate(points):
     xj,yj=points[j]
     if (yi>y+.01)!=(yj>y+.01) and x+.01<(xj-xi)*(y+.01-yi)/(yj-yi)+xi:inside=not inside
     j=i
    if inside:self.dot(x,y,c)
  for a,b in zip(points,points[1:]+points[:1]):self.line(a,b,c)
 def edge(self,c):
  old=[row[:] for row in self.p]
  for y in range(16):
   for x in range(16):
    if old[y][x][3]:continue
    if any(0<=x+dx<16 and 0<=y+dy<16 and old[y+dy][x+dx][3] for dx,dy in [(1,0),(-1,0),(0,1),(0,-1)]):self.dot(x,y,c)
 def save(self,path):
  path.parent.mkdir(parents=True,exist_ok=True)
  raw=b''.join(b'\x00'+bytes(v for px in row for v in px) for row in self.p)
  def chunk(t,b):return struct.pack('!I',len(b))+t+b+struct.pack('!I',zlib.crc32(t+b)&0xffffffff)
  path.write_bytes(b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('!2I5B',16,16,8,6,0,0,0))+chunk(b'IDAT',zlib.compress(raw))+chunk(b'IEND',b''))

def palette(color):
 return (tuple(max(0,round(v*.38)) for v in color),tuple(round(v*.68) for v in color),color,tuple(min(255,round(v*1.10)+25) for v in color))

def render(path,kind,color,state=0,item=''):
 c=Canvas();edge,shade,mid,shine=palette(color)
 wood_dark=(64,38,22);wood=(124,78,39);wood_light=(185,131,62)
 leather=(77,44,31);ivory=(232,224,192);steel=(198,210,213)
 if kind=='quiver':
  for x in [5,8,11]:
   c.line((x,1),(x-2,7),wood_light);c.line((x-1,1),(x+1,3),ivory);c.dot(x,0,steel)
  c.polygon([(3,6),(11,5),(13,8),(10,14),(5,14),(2,10)],wood_dark)
  c.polygon([(4,7),(10,6),(11,8),(9,13),(5,13),(3,10)],mid)
  c.line((5,8),(9,7),shine);c.line((3,8),(5,12),shade)
  c.line((4,10),(10,9),leather);c.dot(8,10,ivory)
 elif kind=='wolf_armor':
  c.polygon([(2,5),(5,2),(10,2),(13,5),(12,11),(10,13),(8,10),(6,13),(3,11)],shade)
  c.polygon([(4,4),(6,3),(10,3),(11,5),(10,9),(8,8),(6,10),(4,9)],mid)
  c.line((4,4),(6,3),shine);c.line((7,4),(10,4),shine)
  for a,b in [((5,5),(7,6)),((8,5),(10,6)),((4,7),(6,8)),((7,7),(9,8))]:c.line(a,b,shade)
  c.line((3,10),(5,12),shine);c.line((10,10),(11,12),mid)
 elif kind=='knife':
  c.polygon([(2,12),(4,10),(6,12),(4,14)],leather)
  c.line((3,12),(4,13),(166,110,66));c.dot(2,13,shade)
  c.polygon([(6,9),(8,7),(12,3),(14,1),(13,5),(10,9),(8,11)],shade)
  c.polygon([(7,8),(12,3),(14,1),(11,6),(8,9)],mid)
  c.line((9,6),(13,2),shine);c.line((8,10),(12,6),edge)
  c.line((5,8),(8,11),wood_dark);c.line((5,9),(7,11),shine)
 elif kind=='battleaxe':
  c.line((2,14),(11,5),wood_dark);c.line((3,14),(11,6),wood);c.line((3,12),(9,6),wood_light)
  for p in [(3,13),(5,11),(7,9)]:c.dot(*p,leather)
  c.polygon([(3,1),(7,1),(10,4),(9,7),(6,6),(3,4)],shade)
  c.polygon([(4,2),(7,2),(9,4),(8,5),(5,4)],mid)
  c.line((3,2),(3,4),shine);c.line((4,1),(6,1),shine)
  c.polygon([(11,3),(14,3),(15,5),(15,8),(12,10),(10,7)],shade)
  c.polygon([(12,4),(14,4),(14,7),(12,9),(11,7)],mid)
  c.line((15,5),(15,8),shine);c.line((13,9),(14,8),shine)
  c.line((8,4),(11,7),edge);c.line((9,4),(11,6),mid);c.dot(10,5,shine)
 elif kind=='shield':
  c.polygon([(3,2),(12,2),(12,9),(10,12),(7,14),(4,12),(2,9),(2,3)],shade)
  c.polygon([(4,3),(11,3),(11,9),(9,11),(7,12),(4,10),(3,8),(3,4)],wood)
  for x in [4,6,8,10]:c.line((x,4),(x,9),wood_dark)
  c.line((4,3),(10,3),shine);c.line((3,4),(3,8),mid);c.line((11,4),(11,9),mid)
  c.polygon([(6,6),(8,5),(10,7),(8,9),(6,8)],shade)
  c.dot(8,6,shine);c.dot(7,7,mid);c.dot(8,7,mid);c.dot(9,7,mid)
  for p in [(4,4),(10,4),(5,10),(9,10)]:c.dot(*p,shine)
 elif kind=='bow':
  # A bent recurve bow with a wrapping grip and progressively displaced bowstring.
  limb=[(11,1),(8,2),(6,4),(5,6),(4,8),(3,11),(3,14)]
  for a,b in zip(limb,limb[1:]):c.line(a,b,shade);c.line((a[0]+1,a[1]),(b[0]+1,b[1]),mid)
  c.line((9,2),(11,1),shine);c.line((4,11),(4,13),shine)
  c.line((4,7),(6,8),leather);c.dot(5,7,wood_light)
  tension=[(7,8),(8,8),(10,8),(12,8)][state]
  for a,b in [((12,1),tension),(tension,(3,14))]:c.line(a,b,ivory)
  c.dot(12,1,shine);c.dot(3,14,shine)
  if state:
   c.line((tension[0]-5,8),tension,wood_light);c.dot(tension[0]-5,8,steel)
 elif kind=='crossbow':
  # A separate wooden stock, reinforced limbs, trigger and cocked string.
  c.polygon([(2,14),(3,11),(8,5),(10,6),(6,11),(4,14)],wood_dark)
  c.line((3,13),(9,6),wood);c.line((4,12),(8,8),wood_light)
  c.line((6,9),(7,10),shade);c.dot(7,9,shine)
  c.polygon([(2,3),(4,2),(7,3),(10,6),(13,7),(14,10),(13,12),(10,9),(8,7),(5,5)],shade)
  for a,b in [((3,3),(6,3)),((6,3),(9,6)),((9,6),(12,8)),((12,8),(13,11))]:c.line(a,b,mid)
  c.dot(3,2,shine);c.dot(13,11,shine)
  pull=[(8,6),(7,7),(6,8),(5,9),(5,9),(5,9)][state]
  c.line((3,3),pull,ivory);c.line(pull,(13,11),ivory)
  c.line((8,5),(10,7),edge);c.dot(9,6,shine)
  if state==4:
   c.line((5,10),(11,4),wood_light);c.line((10,4),(12,2),steel);c.dot(5,10,ivory)
  elif state==5:
   c.line((6,9),(10,5),(203,58,48));c.line((7,10),(11,6),(255,231,196));c.dot(11,5,(237,96,66))
 elif kind=='arrow':
  c.line((3,12),(11,4),wood_dark);c.line((4,12),(11,5),wood_light)
  c.line((1,10),(5,14),(139,153,156));c.line((1,11),(4,14),ivory);c.line((2,9),(6,13),ivory)
  c.polygon([(10,3),(14,1),(13,5),(11,6)],shade)
  c.line((11,3),(14,1),shine);c.dot(12,3,mid);c.dot(12,4,mid)
  c.dot(10,5,leather)
  if item=='blunt_arrow':
   c.polygon([(10,1),(14,1),(15,4),(12,6),(10,4)],shade);c.line((11,2),(14,2),shine);c.dot(13,3,mid)
  elif item=='blaze_arrow':
   c.line((13,0),(11,3),(255,210,66));c.line((14,2),(13,4),(239,82,24));c.dot(14,1,(255,238,153))
  elif item=='fire_charge_arrow':
   c.polygon([(11,1),(14,1),(15,3),(14,5),(11,5),(10,3)],(71,42,28))
   c.polygon([(12,1),(14,2),(14,4),(12,4),(11,3)],(220,81,21));c.dot(13,2,(255,203,62))
  elif item=='breeze_arrow':
   c.line((11,1),(14,1),(215,248,255));c.line((14,1),(15,3),(137,217,235));c.line((14,5),(12,5),(215,248,255))
  elif item=='firework_arrow':
   c.polygon([(10,3),(12,1),(14,3),(12,5)],(212,60,87));c.dot(12,2,ivory);c.dot(11,4,ivory)
  elif item=='dragon_scale_arrow':
   c.dot(13,2,(220,180,250));c.dot(11,4,(114,61,168));c.dot(14,1,(247,222,255))
 else:
  # A curved, layered dragon scale rather than an undifferentiated diamond.
  c.polygon([(4,3),(7,1),(11,3),(13,6),(12,10),(9,13),(5,12),(3,8)],shade)
  c.polygon([(5,3),(8,2),(11,4),(11,8),(9,11),(6,10),(4,7)],mid)
  c.line((5,4),(8,2),shine);c.line((4,7),(6,9),shine)
  c.line((7,5),(10,4),shade);c.line((6,8),(10,7),shade);c.line((8,11),(11,9),edge)
 # A one-pixel contour makes small icons legible against inventory backgrounds.
 c.edge(edge if kind not in ('bow','crossbow','arrow') else wood_dark)
 c.save(path)
