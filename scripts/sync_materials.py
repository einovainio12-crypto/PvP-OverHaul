"""Generate Java material lists from compat_materials.json before compilation."""
import json,pathlib,re
ROOT=pathlib.Path(__file__).resolve().parents[1]

def sync():
 mats=json.loads((ROOT/'scripts/compat_materials.json').read_text());ids=set()
 for m in mats:
  assert re.fullmatch('[a-z][a-z0-9_]*',m['id']) and m['id'] not in ids,'Duplicate/invalid material ID'
  assert m['id'] not in ['wood','stone','copper','iron','gold','diamond','netherite'],'Base material ID reserved'
  ids.add(m['id'])
  assert 0<=m['rank']<=5 and 1<=m['durability']<=100000
  assert 0<float(m['speed'])<=100 and 0<=m['enchant']<=100
  assert re.fullmatch('[a-z0-9_.-]+:[a-z0-9_/.-]+',m['ingot'])
  assert re.fullmatch('[a-z0-9_-]+',m['mod'])
  assert len(m['palette'])>=2 and all(re.fullmatch('[0-9a-fA-F]{6}',c) for c in m['palette'])
 src=ROOT/'src/main/java/dev/pvpoverhaul'
 def rewrite(name,start,end,content):
  p=src/name;s=p.read_text();begin='// BEGIN GENERATED MATERIALS';finish='// END GENERATED MATERIALS'
  if begin in s:
   a=s.index(begin);b=s.index(finish,a)+len(finish)
  else:a=s.index(start);b=s.index(end,a)
  new=s[:a]+begin+'\n'+content+'\n'+finish+s[b:]
  if new!=s:p.write_text(new)
 tiers=[]
 for m in mats:
  if m['id']=='rose_gold':continue
  i=m['id'];d=m['durability'];rank=m['rank']
  tiers.append(f'    {i.upper()}("{i}", {rank}, {d}, CompatMaterials.tool("{i}",{d},{float(m["speed"])}f,{float(rank)}f,{m["enchant"]}))')
 rewrite('Tier.java','    BRASS(', '\n\n    public final String id;',',\n'.join(tiers)+';')
 arrows=[f'    {m["id"].upper()}("{m["id"]}_arrow",{m["rank"]},0.4f)' for m in mats if m['id']!='rose_gold']
 rewrite('Ammo.java','    BRASS(', '\n\n    public final String id;',',\n'.join(arrows)+';')
 records=[]
 for m in mats:
  b=lambda k:str(bool(m[k])).lower()
  records.append(f'        new Material("{m["id"]}","{m["mod"]}","{m["ingot"]}",{b("standard")},{b("external_spear")},{b("external_horse")},{b("fireproof")})')
 rewrite('CompatMaterials.java',' public static final List<Material> MATERIALS=List.of(', '\n public static Material find', ' public static final List<Material> MATERIALS=List.of(\n'+',\n'.join(records)+');')
 p=src/'ModItems.java';s=p.read_text();names=['wood','stone','copper','iron','gold','diamond','netherite']+[m['id'] for m in mats]
 s=re.sub(r'for\(String material:new String\[\]\{[^}]+\}\)', 'for(String material:new String[]{'+','.join(json.dumps(x) for x in names)+'})',s);p.write_text(s)
 print('Synchronized',len(mats),'material definitions')
if __name__=='__main__':sync()
