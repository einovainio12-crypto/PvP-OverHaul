#!/usr/bin/env python3
"""Reproducible resources; no Minecraft textures or external Python packages required."""
import json, pathlib, struct, zlib
ROOT=pathlib.Path(__file__).resolve().parents[1]/'src/main/resources'
MOD='pvpoverhaul'
TIERS=['wood','copper','iron','gold','diamond','netherite']
MATERIAL=['#minecraft:planks','minecraft:copper_ingot','minecraft:iron_ingot','minecraft:gold_ingot','minecraft:diamond','minecraft:netherite_ingot']
COLORS=[(139,92,47),(213,119,73),(209,218,228),(255,207,67),(73,221,209),(87,73,86)]
AMMO=['wood_arrow','stone_arrow','flint_arrow','copper_arrow','iron_arrow','gold_arrow','dragon_scale_arrow','netherite_arrow','blunt_arrow','blaze_arrow','breeze_arrow','firework_arrow','fire_charge_arrow']
LANG={}
def write(path,obj):
 p=ROOT/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(obj,indent=2)+'\n')
def tag(ns,path,values):write(f'data/{ns}/tags/item/{path}.json',{'replace':False,'values':values})
def recipe(name,ingredients=None,pattern=None,key=None,count=1,output=None):
 d={'type':'minecraft:crafting_shapeless' if ingredients else 'minecraft:crafting_shaped','category':'equipment'}
 if ingredients:d['ingredients']=ingredients
 else:d.update(pattern=pattern,key=key)
 d['result']={'id':output or f'{MOD}:{name}','count':count};write(f'data/{MOD}/recipe/{name}.json',d)
def name(item):LANG[f'item.{MOD}.{item}']=item.replace('_',' ').title()
def model(item,vanilla=None):
 write(f'assets/{MOD}/items/{item}.json',{'model':{'type':'minecraft:model','model':vanilla or f'{MOD}:item/{item}'}})
 if not vanilla:write(f'assets/{MOD}/models/item/{item}.json',{'parent':'minecraft:item/handheld','textures':{'layer0':f'{MOD}:item/{item}'}})
def png(item,kind,color):
 pixels=[[(0,0,0,0) for x in range(16)] for y in range(16)]
 def put(x,y,c):
  if 0<=x<16 and 0<=y<16:pixels[y][x]=(*c,255)
 def line(x0,y0,x1,y1,c,w=1):
  steps=max(abs(x1-x0),abs(y1-y0))
  for i in range(steps+1):
   x=round(x0+(x1-x0)*i/max(1,steps));y=round(y0+(y1-y0)*i/max(1,steps))
   for a in range(w):put(x+a,y,c)
 dark=tuple(int(v*.45) for v in color);light=tuple(min(255,int(v*1.15)+10) for v in color)
 if kind=='knife':
  line(4,12,7,9,(121,76,44),2);line(7,9,12,3,dark,3);line(8,8,12,3,color,2);line(9,7,12,3,light);line(5,8,9,12,(218,175,83))
 elif kind=='spear':
  line(1,14,12,3,(121,76,44),2);line(10,5,14,1,dark,2);line(11,4,14,1,color);put(13,2,light)
 elif kind=='battleaxe':
  line(3,14,11,4,(121,76,44),2)
  for y in range(1,9):
   for x in range(6,16):
    if abs(x-11)+abs(y-4)<6:put(x,y,dark if x<8 or y in [1,8] else color)
  line(7,2,11,1,light);line(13,6,15,3,light)
 elif kind=='arrow':
  line(2,13,12,3,(174,130,71));line(11,4,14,1,color,2);line(0,12,3,15,(228,221,205));line(2,10,5,13,(228,221,205))
 else:
  for y in range(3,13):
   for x in range(3,13):
    if abs(x-7)+abs(y-7)<7:put(x,y,dark if (x+y)%3==0 else color)
 raw=b''.join(b'\x00'+bytes(c for pixel in row for c in pixel) for row in pixels)
 def chunk(t,b):return struct.pack('!I',len(b))+t+b+struct.pack('!I',zlib.crc32(t+b)&0xffffffff)
 data=b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('!2I5B',16,16,8,6,0,0,0))+chunk(b'IDAT',zlib.compress(raw))+chunk(b'IEND',b'')
 p=ROOT/f'assets/{MOD}/textures/item/{item}.png';p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(data)
def bow_model(item,gold=False):
 m=lambda s:{'type':'minecraft:model','model':f'minecraft:item/bow{s}'}
 write(f'assets/{MOD}/items/{item}.json',{'model':{'type':'minecraft:condition','property':'minecraft:using_item','on_false':m(''),'on_true':{'type':'minecraft:range_dispatch','property':'minecraft:use_duration','scale':1/14 if gold else .05,'fallback':m('_pulling_0'),'entries':[{'threshold':.65,'model':m('_pulling_1')},{'threshold':.9,'model':m('_pulling_2')}]}}})
def crossbow_model(item):
 m=lambda s:{'type':'minecraft:model','model':f'minecraft:item/crossbow{s}'}
 write(f'assets/{MOD}/items/{item}.json',{'model':{'type':'minecraft:select','property':'minecraft:charge_type','cases':[{'when':'arrow','model':m('_arrow')},{'when':'rocket','model':m('_firework')}],'fallback':{'type':'minecraft:condition','property':'minecraft:using_item','on_false':m(''),'on_true':{'type':'minecraft:range_dispatch','property':'minecraft:crossbow/pull','fallback':m('_pulling_0'),'entries':[{'threshold':.58,'model':m('_pulling_1')},{'threshold':1,'model':m('_pulling_2')}]}}}})
def shield_model(item):
 def special(s):return {'type':'minecraft:special','base':f'minecraft:item/shield{s}','model':{'type':'minecraft:shield'}}
 write(f'assets/{MOD}/items/{item}.json',{'model':{'type':'minecraft:condition','property':'minecraft:using_item','on_false':special(''),'on_true':special('_blocking'),'transformation':{'scale':[1,-1,-1]}}})
items=[]
for i,t in enumerate(TIERS):
 for kind in ['bow','crossbow','knife','spear','battleaxe','shield']:
  item=f'{t}_{kind}';items.append(f'{MOD}:{item}');name(item)
  if kind=='bow':bow_model(item,t=='gold')
  elif kind=='crossbow':crossbow_model(item)
  elif kind=='shield':shield_model(item)
  else:model(item);png(item,kind,COLORS[i])
  if kind in ['bow','crossbow']:
   if i==0:recipe(item,[f'minecraft:{kind}'])
   elif i==5:
    write(f'data/{MOD}/recipe/{item}.json',{'type':'minecraft:smithing_transform','template':'minecraft:netherite_upgrade_smithing_template','base':f'{MOD}:diamond_{kind}','addition':'minecraft:netherite_ingot','result':{'id':f'{MOD}:{item}'}})
   else:recipe(item,[f'minecraft:{kind}',MATERIAL[i],MATERIAL[i],MATERIAL[i]])
  elif i==5:
   write(f'data/{MOD}/recipe/{item}.json',{'type':'minecraft:smithing_transform','template':'minecraft:netherite_upgrade_smithing_template','base':f'{MOD}:diamond_{kind}','addition':'minecraft:netherite_ingot','result':{'id':f'{MOD}:{item}'}})
  else:
   patterns={'knife':['M','S'],'spear':['  M',' S ','S  '],'battleaxe':['MMM','MSM',' S '],'shield':['MSM','MMM',' M ']}
   recipe(item,pattern=patterns[kind],key={'M':MATERIAL[i],'S':'minecraft:stick'})
arrow_ingredients=['#minecraft:planks','minecraft:cobblestone','minecraft:flint','minecraft:copper_ingot','minecraft:iron_ingot','minecraft:gold_ingot',f'{MOD}:dragon_scale','minecraft:netherite_scrap','minecraft:iron_nugget','minecraft:blaze_powder','minecraft:wind_charge','minecraft:gunpowder','minecraft:fire_charge']
for i,a in enumerate(AMMO):
 name(a);model(a);png(a,'arrow',COLORS[min(i,5)] if i<6 else [(176,119,219),(70,58,70),(159,165,175),(255,134,30),(140,229,245),(255,91,172),(235,78,20)][i-6])
 recipe(a,pattern=['M','S','F'],key={'M':arrow_ingredients[i],'S':'minecraft:stick','F':'minecraft:feather'},count=4)
name('dragon_scale');model('dragon_scale');png('dragon_scale','scale',(177,118,222))
recipe('dragon_scale',['minecraft:dragon_breath','minecraft:echo_shard','minecraft:popped_chorus_fruit'])
recipe('craftable_trident',pattern=[' PP',' SP','S  '],key={'P':'minecraft:prismarine_shard','S':'minecraft:stick'},output='minecraft:trident')
# The recipe is deliberately affordable for a combat mod; adjust it freely in this source.
bykind=lambda k:[f'{MOD}:{t}_{k}' for t in TIERS]
for kind in ['knives','spears','battleaxes','shields']:
 singular={'knives':'knife','spears':'spear','battleaxes':'battleaxe','shields':'shield'}[kind];tag(MOD,kind,bykind(singular))
tag(MOD,'throwable_weapons',['#pvpoverhaul:spears','#pvpoverhaul:battleaxes','#minecraft:axes','#minecraft:spears'])
tag(MOD,'elytra',['minecraft:elytra'])
tag(MOD,'loyalty_weapons',['minecraft:trident','#pvpoverhaul:throwable_weapons','#pvpoverhaul:knives'])
tag('minecraft','arrows',[f'{MOD}:{a}' for a in AMMO])
tag('minecraft','spears',bykind('spear'))
for enchant,values in {
 'bow':bykind('bow'),'crossbow':bykind('crossbow'),'durability':items,
 'weapon':bykind('knife')+bykind('spear')+bykind('battleaxe'),
 'melee_weapon':bykind('knife')+bykind('spear')+bykind('battleaxe'),
 'sharp_weapon':bykind('knife')+bykind('spear')+bykind('battleaxe'),
 'lunge':bykind('spear')}.items():tag('minecraft','enchantable/'+enchant,values)
def ench(id,supported,slots,max_level=1,effects=None,exclusive=None):
 LANG[f'enchantment.{MOD}.{id}']=id.replace('_',' ').title()
 d={'description':{'translate':f'enchantment.{MOD}.{id}'},'supported_items':supported,'weight':5,'max_level':max_level,'min_cost':{'base':10,'per_level_above_first':8},'max_cost':{'base':40,'per_level_above_first':8},'anvil_cost':3,'slots':slots,'effects':effects or {}}
 if exclusive:d['exclusive_set']=exclusive
 write(f'data/{MOD}/enchantment/{id}.json',d)
linear=lambda base,step:{'type':'minecraft:linear','base':base,'per_level_above_first':step}
ench('throwable','#pvpoverhaul:throwable_weapons',['mainhand'],exclusive=['minecraft:lunge','pvpoverhaul:pole_jump'])
ench('pole_jump','#pvpoverhaul:spears',['mainhand'],3,exclusive=['pvpoverhaul:throwable'])
ench('double_jump','#minecraft:enchantable/foot_armor',['feet'],2)
ench('speed','#minecraft:enchantable/foot_armor',['feet'],3,{'minecraft:attributes':[{'id':'pvpoverhaul:speed','attribute':'minecraft:movement_speed','operation':'add_multiplied_base','amount':linear(.04,.04)}]})
ench('boost','#pvpoverhaul:elytra',['chest'],3)
ench('aerodynamics','#pvpoverhaul:elytra',['chest'],2)
ench('cleaving','#pvpoverhaul:battleaxes',['mainhand'],3,{'minecraft:damage':[{'effect':{'type':'minecraft:add','value':linear(1,1)}}]},'#minecraft:exclusive_set/damage')
ench('impact','#pvpoverhaul:battleaxes',['mainhand'],2,{'minecraft:knockback':[{'effect':{'type':'minecraft:add','value':linear(.5,.5)}}]})
# Patch only the supported-items and one mutual exclusion field of the 26.3 vanilla definitions.
loyalty={'anvil_cost':2,'description':{'translate':'enchantment.minecraft.loyalty'},'effects':{'minecraft:trident_return_acceleration':[{'effect':{'type':'minecraft:add','value':linear(1,1)}}]},'max_cost':{'base':50,'per_level_above_first':0},'max_level':3,'min_cost':{'base':12,'per_level_above_first':7},'slots':['mainhand'],'supported_items':'#pvpoverhaul:loyalty_weapons','weight':5}
write('data/minecraft/enchantment/loyalty.json',loyalty)
# Compatibility checks both enchantments' exclusive sets; Lunge's original effects stay intact.
for path in ['in_enchanting_table','tradeable','on_random_loot','on_traded_equipment']:
 write(f'data/minecraft/tags/enchantment/{path}.json',{'replace':False,'values':[f'{MOD}:{n}' for n in ['throwable','pole_jump','double_jump','speed','boost','aerodynamics','cleaving','impact']]})
write('data/minecraft/tags/enchantment/exclusive_set/damage.json',{'replace':False,'values':['pvpoverhaul:cleaving']})
for key,title in {'zoom':'Zoom (hold)','target_lock':'Target Lock','dead_eye':'Dead Eye','boost':'Elytra Boost','pole_jump':'Pole Jump'}.items():LANG[f'key.{MOD}.{key}']=title
LANG['key.category.pvpoverhaul.combat']='PvP Overhaul'
write(f'assets/{MOD}/lang/en_us.json',LANG)
print('Generated',len(items)+len(AMMO)+1,'items and all recipes/models/enchantments.')
