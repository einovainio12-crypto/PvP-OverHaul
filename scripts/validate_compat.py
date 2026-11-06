"""Validate palette masks and optional-material recipe guards without image dependencies."""
import json,pathlib,struct,zlib

def check(root):
 res=root/'src/main/resources';mats=json.loads((root/'scripts/compat_materials.json').read_text());sources=json.loads((root/'scripts/compat_palette_sources.json').read_text())
 def colors(p):
  b=p.read_bytes();assert b[:8]==b'\x89PNG\r\n\x1a\n';offset=8;raw=b'';width=0
  while offset<len(b):
   size=int.from_bytes(b[offset:offset+4],'big');kind=b[offset+4:offset+8];body=b[offset+8:offset+8+size];offset+=size+12
   if kind==b'IHDR':width,height,depth,mode,_,_,_=struct.unpack('>IIBBBBB',body);assert height==1 and depth==8 and mode==6
   if kind==b'IDAT':raw+=body
  raw=zlib.decompress(raw);assert raw[0]==0 and len(raw)==1+width*4
  return [tuple(raw[i:i+3]) for i in range(1,len(raw),4)]
 count=0
 for m in mats:
  allowed={tuple(bytes.fromhex(c)) for c in m['palette']}
  for source,data in sources.items():
   p=res/('assets/pvpoverhaul/textures/palettes/compat/'+m['id']+'/'+source.replace(':','/')+'_palette.png')
   if not p.exists():continue
   target=colors(p);assert len(target)==len(data['colors']);metal={tuple(c) for c in data['metal']}
   for before,after in zip(data['colors'],target):
    if tuple(before) in metal:assert after in allowed,(m['id'],source,after)
    else:assert tuple(before)==after,('Handle/string changed',source)
   count+=1
  for p in (res/'data/pvpoverhaul/recipe').glob(m['id']+'_*.json'):
   d=json.loads(p.read_text());assert d.get('fabric:load_conditions')==[{'condition':'fabric:all_mods_loaded','values':[m['mod']]}],p
 # Custom Siftite equipment must follow The Sift's netherite upgrade progression.
 for kind in ['bow','crossbow','knife','battleaxe','shield','flail','hammer','scythe','halberd']:
  d=json.loads((res/f'data/pvpoverhaul/recipe/siftite_{kind}.json').read_text())
  assert d['type']=='minecraft:smithing_transform',kind
  assert d['template']=='the_sift:siftite_upgrade_smithing_template',kind
  assert d['base']=='pvpoverhaul:netherite_'+kind,kind
  assert d['addition']=='the_sift:siftite_ingot',kind
  assert d['result']=={'id':'pvpoverhaul:siftite_'+kind},kind
 for p in (res/'assets/pvpoverhaul/armor_bar').glob('*.json'):
  d=json.loads(p.read_text());assert d['type']=='armor';assert len(d['render']['color'])==6
 print(f'PASS: {count} material palette maps; handles/strings preserved; optional recipes guarded.')

if __name__=='__main__':check(pathlib.Path(__file__).resolve().parents[1])
