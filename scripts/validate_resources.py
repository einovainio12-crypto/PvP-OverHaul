#!/usr/bin/env python3
"""Check internal resource links, recipes, item coverage and enchantment rules before packaging."""
import json,pathlib,re,sys
root=pathlib.Path(__file__).resolve().parents[1]
res=root/'src/main/resources';ns='pvpoverhaul';errors=[]
def error(message):errors.append(message)
obsolete=set(json.loads((root/'scripts/obsolete_resources.json').read_text()))
parsed={}
for p in res.rglob('*.json'):
 if p.relative_to(res).as_posix() in obsolete:continue
 try:parsed[p.relative_to(res).as_posix()]=json.loads(p.read_text())
 except (OSError,json.JSONDecodeError) as e:error(f'{p}: {e}')
items={p.stem for p in (res/f'assets/{ns}/items').glob('*.json') if p.relative_to(res).as_posix() not in obsolete}
expected={f'{t}_{k}' for t in ['wood','copper','iron','gold','diamond','netherite'] for k in ['bow','crossbow','knife','battleaxe','shield']}
expected.difference_update(['wood_bow','wood_crossbow'])
expected.update(['stone_bow','stone_crossbow'])
expected.update(['dragon_scale','wood_arrow','copper_arrow','iron_arrow','gold_arrow','dragon_scale_arrow','netherite_arrow','blunt_arrow','blaze_arrow','breeze_arrow','firework_arrow','fire_charge_arrow'])
expected.add('stone_battleaxe')
expected.add('stone_knife')
expected.add('grappling_arrow')
expected.update(['dragon_scale_wolf_armor','quiver'])
expected.update(f'{t}_{k}' for t in ['wood','stone','copper','iron','gold','diamond','netherite'] for k in ['shuriken','flail','hammer','longsword','scythe','halberd','quarterstaff'])
expected.update(['weapon_sheath','storm_core','healing_totem','combat_cache','ice_arrow'])
if items!=expected:error(f'Item coverage mismatch: missing={expected-items}, unexpected={items-expected}')
lang=parsed.get(f'assets/{ns}/lang/en_us.json',{})
for item in expected:
 if f'item.{ns}.{item}' not in lang:error(f'Missing item translation: {item}')
def walk(value):
 if isinstance(value,dict):
  for k,v in value.items():
   if k in ['model','base','parent'] and isinstance(v,str) and v.startswith(ns+':'):
    path=f'assets/{ns}/models/{v.split(":",1)[1]}.json'
    if path not in parsed:error(f'Missing model {path}')
   if k=='textures':
    for v2 in v.values():
     if isinstance(v2,str) and v2.startswith(ns+':') and not (res/f'assets/{ns}/textures/{v2.split(":",1)[1]}.png').exists():error(f'Missing texture {v2}')
   walk(v)
 elif isinstance(value,list):
  for v in value:walk(v)
for path,d in parsed.items():
 if path.startswith('assets/'):walk(d)
 if path.startswith(f'assets/{ns}/equipment/'):
  for kind,layers in d.get('layers',{}).items():
   for layer in layers:
    texture=layer.get('texture','')
    if texture.startswith(ns+':') and not (res/f'assets/{ns}/textures/entity/equipment/{kind}/{texture.split(":",1)[1]}.png').exists():error(f'Missing equipment texture {texture}')
 if '/recipe/' in path:
  result=d.get('result',{})
  if result.get('id','').startswith(ns+':') and result['id'].split(':')[1] not in items:error(f'Unknown result {path}')
  if d.get('type')=='minecraft:crafting_shaped':
   symbols=set(''.join(d['pattern']))-{' '}
   if symbols!=set(d['key']):error(f'Invalid recipe symbols in {path}')
 if '/tags/item/' in path:
  for v in d.get('values',[]):
   if isinstance(v,str) and v.startswith(ns+':') and v.split(':')[1] not in items:error(f'Unknown item {v} in {path}')
# Every added item must have a normal crafting or smithing route, never creative-only.
craftable={d.get('result',{}).get('id','').removeprefix(ns+':') for path,d in parsed.items()
 if '/recipe/' in path and d.get('type') in ['minecraft:crafting_shaped','minecraft:crafting_shapeless','minecraft:smithing_transform']
 and d.get('result',{}).get('id','').startswith(ns+':')}
if expected-craftable:error(f'Missing survival recipes: {sorted(expected-craftable)}')
throwable=parsed.get(f'data/{ns}/enchantment/throwable.json',{})
pole=parsed.get(f'data/{ns}/enchantment/pole_jump.json',{})
if set(throwable.get('exclusive_set',[]))!={'minecraft:lunge','pvpoverhaul:pole_jump'}:error('Throwable must exclude Lunge and Pole Jump')
if pole.get('exclusive_set')!=['pvpoverhaul:throwable']:error('Pole Jump must allow Lunge and exclude Throwable')
if 'pvpoverhaul:throwable' in parsed.get('data/minecraft/enchantment/loyalty.json',{}).get('exclusive_set',[]):error('Loyalty must allow Throwable')
speed=parsed.get(f'data/{ns}/enchantment/speed.json',{})
double_jump=parsed.get(f'data/{ns}/enchantment/double_jump.json',{})
if speed.get('exclusive_set')!=['pvpoverhaul:double_jump'] or double_jump.get('exclusive_set')!=['pvpoverhaul:speed']:error('Boot mobility enchantments must be mutually exclusive')
for path in parsed:
 if path in obsolete:error(f'Obsolete spear resource will be packaged: {path}')
for path,d in parsed.items():
 if '/recipe/' in path and d.get('result',{}).get('id','').startswith('pvpoverhaul:') and d['result']['id'].endswith('_spear'):error('Duplicate spear recipe')
# Check declared Java/mixin entrypoints actually exist; catches mispackaged class names.
meta=parsed.get('fabric.mod.json',{})
def class_exists(name):return any((root/f'src/{s}/java'/pathlib.Path(name.replace('.','/')+'.java')).exists() for s in ['main','client'])
for entries in meta.get('entrypoints',{}).values():
 for name in entries:
  if not class_exists(name):error(f'Missing entrypoint {name}')
for path in ['pvpoverhaul.mixins.json','pvpoverhaul.client.mixins.json']:
 config=parsed.get(path,{})
 for name in config.get('mixins',[])+config.get('client',[]):
  if not class_exists(config.get('package','')+'.'+name):error(f'Missing mixin {name}')
if errors:
 print('\n'.join(errors),file=sys.stderr);sys.exit(1)
print(f'PASS: {len(parsed)} JSON files; {len(items)} items; all internal links, recipes and enchantment rules checked.')
