"""Arsenal recipes, sprites, tags and enchantments. Extends the existing code-drawn art."""
from pixel_art import Canvas,palette,render
import json,pathlib
KINDS=['flail','hammer','scythe','halberd']
PATTERNS={'flail':[' MM',' CM','S  '],'hammer':['MMM',' S ',' S '],'scythe':['MMM','  S',' S '],'halberd':[' MM',' SM','S  ']}
def sprite(path,kind,color):
 c=Canvas();edge,shade,mid,shine=palette(color)
 if kind in ['core','totem','cache']:
  c.polygon([(7,1),(13,5),(12,12),(8,14),(2,11),(2,5)],shade);c.polygon([(7,3),(11,6),(10,11),(7,12),(4,9),(4,6)],mid);c.line((8,3),(6,8),shine);c.line((6,8),(9,7),shine);c.line((9,7),(7,12),shine)
 elif kind=='sheath':
  c.polygon([(3,13),(3,10),(11,2),(14,4),(6,13)],shade);c.line((4,12),(12,4),mid);c.line((10,2),(14,6),shine)
 else:
  c.line((2,14),(12,4),(64,38,22));c.line((3,14),(12,5),(124,78,39));c.line((3,12),(11,4),(185,131,62))
  if kind=='hammer':
   c.polygon([(5,1),(9,1),(14,6),(14,9),(11,11),(3,4)],shade);c.polygon([(6,2),(9,2),(13,6),(11,8),(4,3)],mid);c.line((6,1),(9,1),shine)
  elif kind=='flail':
   c.line((7,9),(8,4),(198,210,213));c.line((8,4),(12,2),(198,210,213));c.polygon([(10,1),(14,1),(15,4),(12,6),(10,4)],mid);c.dot(12,2,shine);c.dot(14,4,edge)
  elif kind=='scythe':
   c.polygon([(4,1),(10,1),(14,4),(14,9),(12,5),(9,3)],mid);c.line((5,1),(10,1),shine);c.line((10,1),(14,4),shine)
  elif kind=='halberd':
   c.polygon([(7,2),(11,4),(11,7),(7,7),(5,5)],mid);c.line((6,4),(7,6),shine);c.polygon([(10,4),(14,0),(14,4),(12,6)],shade);c.line((11,3),(14,0),shine)
  else:c.line((2,13),(3,14),mid);c.line((12,3),(13,4),shine);c.line((7,8),(8,9),shade)
 c.edge(edge);c.save(path)
def extend(g):
 root=g['ROOT'];write=g['write'];tag=g['tag'];recipe=g['recipe'];name=g['name'];model=g['model'];ench=g['ench'];ns=g['MOD']
 extra=[]
 for t,mat,color in list(zip(g['TIERS'],g['MATERIAL'],g['COLORS']))+[('stone','minecraft:cobblestone',(115,115,115))]:
  for kind in KINDS:
   item=t+'_'+kind;extra.append(ns+':'+item);name(item);model(item);sprite(root/f'assets/{ns}/textures/item/{item}.png',kind,color)
   if t=='netherite':write(f'data/{ns}/recipe/{item}.json',{'type':'minecraft:smithing_transform','template':'minecraft:netherite_upgrade_smithing_template','base':ns+':diamond_'+kind,'addition':'minecraft:netherite_ingot','result':{'id':ns+':'+item}})
   else:
    keys={'M':mat,'S':'minecraft:stick','C':'minecraft:iron_ingot','L':'minecraft:leather'};symbols=set(''.join(PATTERNS[kind]))-{' '};recipe(item,pattern=PATTERNS[kind],key={k:v for k,v in keys.items() if k in symbols})
 for item,kind,color,ingredients in [('weapon_sheath','sheath',(130,85,46),['minecraft:leather','minecraft:leather','minecraft:string']),('storm_core','core',(115,210,250),['minecraft:netherite_scrap','minecraft:echo_shard','minecraft:heart_of_the_sea']),('healing_totem','totem',(85,220,110),['minecraft:golden_apple','minecraft:emerald','minecraft:stick']),('combat_cache','cache',(193,140,57),['minecraft:chest','minecraft:diamond','minecraft:echo_shard']),('ice_arrow','arrow',(145,230,250),['minecraft:arrow','minecraft:packed_ice'])]:
  name(item);model(item);recipe(item,ingredients);path=root/f'assets/{ns}/textures/item/{item}.png'
  if kind=='arrow':render(path,kind,color,item=item)
  else:sprite(path,kind,color)
 tag(ns,'arsenal',extra)
 tag(ns,'pole_weapons',['#minecraft:spears']+[item for item in extra if item.endswith('_halberd')])
 for path in ['weapon','melee_weapon','sharp_weapon','durability']:
  p=root/f'data/minecraft/tags/item/enchantable/{path}.json';d=json.loads(p.read_text());d['values']+=extra;write(p.relative_to(root),d)
 for path in ['throwable_weapons','loyalty_weapons']:
  p=root/f'data/{ns}/tags/item/{path}.json';d=json.loads(p.read_text());d['values']+=['#pvpoverhaul:arsenal','#pvpoverhaul:knives','#minecraft:swords','minecraft:mace'];write(p.relative_to(root),d)
 tag(ns,'storm_hammer',['pvpoverhaul:netherite_hammer'])
 for n,l in [('ice_aspect',2),('leeching',3),('chaining',3),('stunning',3)]:ench(n,'#minecraft:enchantable/melee_weapon',['mainhand'],l,exclusive=['minecraft:fire_aspect','minecraft:flame'] if n=='ice_aspect' else None,weight=2)

 vanilla=json.loads((pathlib.Path(__file__).parent/'arsenal_vanilla_enchants.json').read_text())
 for n in ['flame','channeling','riptide']:
  d=vanilla[n]
  if n=='channeling':d=json.loads(json.dumps(d).replace('\"minecraft:trident\"','\"#pvpoverhaul:trident_projectiles\"'))
  tag(ns,n+'_supported',[d['supported_items'],'#pvpoverhaul:knives'] if n=='flame' else [d['supported_items'],'#pvpoverhaul:storm_hammer']);d['supported_items']='#pvpoverhaul:'+n+'_supported';write('data/minecraft/enchantment/'+n+'.json',d)
 for path in ['in_enchanting_table','tradeable','on_random_loot','on_traded_equipment']:
  p=root/f'data/minecraft/tags/enchantment/{path}.json';d=json.loads(p.read_text());d['values'] += [ns+':'+n for n in ['ice_aspect','leeching','chaining','stunning']];write(p.relative_to(root),d)

 # Projectile predicates must include the new entity ID, not only minecraft:trident.
 write('data/pvpoverhaul/tags/entity_type/trident_projectiles.json',{'replace':False,'values':['minecraft:trident','pvpoverhaul:thrown_pvpoverhaul_netherite_hammer']})

 p=root/'data/minecraft/tags/item/arrows.json';d=json.loads(p.read_text());d['values'].append('pvpoverhaul:ice_arrow');write(p.relative_to(root),d)

 for material in ['wooden','stone','copper','iron','golden','diamond','netherite']:
  write(f'assets/{ns}/items/thrown_visual/{material}_spear.json',{'model':{'type':'minecraft:model','model':f'minecraft:item/{material}_spear_in_hand'}})
 for material in ['wood','stone','copper','iron','gold','diamond','netherite']:
  write(f'assets/{ns}/items/thrown_visual/{material}_halberd.json',{'model':{'type':'minecraft:model','model':f'{ns}:item/{material}_halberd'}})
