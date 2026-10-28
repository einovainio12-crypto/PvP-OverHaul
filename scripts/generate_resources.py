#!/usr/bin/env python3
"""Reproducible resources, including supplied shield assets; no external Python packages required."""
import json, pathlib, shutil, base64
from pixel_art import render
from shield_assets import export_shield
from wolf_assets import export_wolf
ROOT=pathlib.Path(__file__).resolve().parents[1]/'src/main/resources'
# Remove retired resources even when uploaded over an older repository.
for obsolete in json.loads((pathlib.Path(__file__).parent/'obsolete_resources.json').read_text()):
 (ROOT/obsolete).unlink(missing_ok=True)
MOD='pvpoverhaul' 
TIERS=['wood','copper','iron','gold','diamond','netherite']
MATERIAL=['#minecraft:planks','minecraft:copper_ingot','minecraft:iron_ingot','minecraft:gold_ingot','minecraft:diamond','minecraft:netherite_ingot']
COLORS=[(139,92,47),(213,119,73),(209,218,228),(255,207,67),(73,221,209),(87,73,86)]
AMMO=['wood_arrow','copper_arrow','iron_arrow','gold_arrow','dragon_scale_arrow','netherite_arrow','blunt_arrow','blaze_arrow','breeze_arrow','firework_arrow','fire_charge_arrow']
LANG={}
PACKED_PATH=pathlib.Path(__file__).parent/'art'/'sprites_packed.json'
PACKED_ART=json.loads(PACKED_PATH.read_text()) if PACKED_PATH.exists() else {}
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
def png(item,kind,color,state=0):
 if item+'.png' in PACKED_ART:
  target=ROOT/f'assets/{MOD}/textures/item/{item}.png';target.parent.mkdir(parents=True,exist_ok=True)
  target.write_bytes(base64.b64decode(PACKED_ART[item+'.png'],validate=True));return
 source=pathlib.Path(__file__).parent/'art'/'textures'/f'{item}.png'
 if source.exists():
  target=ROOT/f'assets/{MOD}/textures/item/{item}.png';target.parent.mkdir(parents=True,exist_ok=True)
  shutil.copyfile(source,target);return
 render(ROOT/f'assets/{MOD}/textures/item/{item}.png',kind,color,state,item)
def textured_model(item,parent='minecraft:item/handheld'):
 write(f'assets/{MOD}/models/item/{item}.json',{'parent':parent,'textures':{'layer0':f'{MOD}:item/{item}'}})
def bow_model(item,gold=False,color=None):
 draw=10 if gold else 26 if item=="diamond_bow" else 30 if item=="netherite_bow" else 20
 for stage,suffix in enumerate(['','_pulling_0','_pulling_1','_pulling_2']):
  textured_model(item+suffix,'minecraft:item/bow'+suffix);png(item+suffix,'bow',color,stage)
 m=lambda s:{'type':'minecraft:model','model':f'{MOD}:item/{item}{s}'}
 write(f'assets/{MOD}/items/{item}.json',{'model':{'type':'minecraft:condition','property':'minecraft:using_item','on_false':m(''),'on_true':{'type':'minecraft:range_dispatch','property':'minecraft:use_duration','scale':1/draw,'fallback':m('_pulling_0'),'entries':[{'threshold':.65,'model':m('_pulling_1')},{'threshold':.9,'model':m('_pulling_2')}]}}})
def crossbow_model(item,color):
 for stage,suffix in enumerate(['','_pulling_0','_pulling_1','_pulling_2','_arrow','_firework']):
  textured_model(item+suffix,'minecraft:item/crossbow'+suffix);png(item+suffix,'crossbow',color,stage)
 m=lambda s:{'type':'minecraft:model','model':f'{MOD}:item/{item}{s}'}
 write(f'assets/{MOD}/items/{item}.json',{'model':{'type':'minecraft:select','property':'minecraft:charge_type','cases':[{'when':'arrow','model':m('_arrow')},{'when':'rocket','model':m('_firework')}],'fallback':{'type':'minecraft:condition','property':'minecraft:using_item','on_false':m(''),'on_true':{'type':'minecraft:range_dispatch','property':'minecraft:crossbow/pull','fallback':m('_pulling_0'),'entries':[{'threshold':.58,'model':m('_pulling_1')},{'threshold':1,'model':m('_pulling_2')}]}}}})
def shield_model(item,color):
 export_shield(ROOT,item)
obsolete=json.loads((pathlib.Path(__file__).parent/'obsolete_resources.json').read_text())
for relative in obsolete:(ROOT/relative).unlink(missing_ok=True)
items=[]
for i,t in enumerate(TIERS):
 for kind in ['bow','crossbow','knife','battleaxe','shield']:
  if t=='wood' and kind in ['bow','crossbow']:continue
  item=f'{t}_{kind}';items.append(f'{MOD}:{item}');name(item)
  if kind=='bow':bow_model(item,t=='gold',COLORS[i])
  elif kind=='crossbow':crossbow_model(item,COLORS[i])
  elif kind=='shield':shield_model(item,COLORS[i])
  else:model(item);png(item,kind,COLORS[i])
  if kind in ['bow','crossbow']:
   if i==5:
    write(f'data/{MOD}/recipe/{item}.json',{'type':'minecraft:smithing_transform','template':'minecraft:netherite_upgrade_smithing_template','base':f'{MOD}:diamond_{kind}','addition':'minecraft:netherite_ingot','result':{'id':f'{MOD}:{item}'}})
   else:recipe(item,[f'minecraft:{kind}',MATERIAL[i],MATERIAL[i],MATERIAL[i]])
  elif i==5:
   write(f'data/{MOD}/recipe/{item}.json',{'type':'minecraft:smithing_transform','template':'minecraft:netherite_upgrade_smithing_template','base':f'{MOD}:diamond_{kind}','addition':'minecraft:netherite_ingot','result':{'id':f'{MOD}:{item}'}})
  else:
   patterns={'knife':[' M','S '],'battleaxe':['MMM',' SM',' S '],'shield':['MSM','MMM',' M ']}
   recipe(item,pattern=patterns[kind],key={'M':MATERIAL[i],'S':'minecraft:stick'})
for kind in ['bow','crossbow']:
 item=f'stone_{kind}';name(item);items.append(f'{MOD}:{item}')
 if kind=='bow':bow_model(item,color=(115,115,115))
 else:crossbow_model(item,(115,115,115))
 recipe(item,[f'minecraft:{kind}','minecraft:cobblestone','minecraft:cobblestone','minecraft:cobblestone'])
name('stone_battleaxe');items.append(f'{MOD}:stone_battleaxe');model('stone_battleaxe');png('stone_battleaxe','battleaxe',(115,115,115))
recipe('stone_battleaxe',pattern=['MMM',' SM',' S '],key={'M':'minecraft:cobblestone','S':'minecraft:stick'})
name('stone_knife');items.append(f'{MOD}:stone_knife');model('stone_knife');png('stone_knife','knife',(115,115,115))
recipe('stone_knife',pattern=[' M','S '],key={'M':'minecraft:cobblestone','S':'minecraft:stick'})
arrow_ingredients=['#minecraft:planks','minecraft:copper_ingot','minecraft:iron_ingot','minecraft:gold_ingot',f'{MOD}:dragon_scale','minecraft:netherite_scrap','minecraft:iron_nugget','minecraft:blaze_powder','minecraft:wind_charge','minecraft:gunpowder','minecraft:fire_charge']
for i,a in enumerate(AMMO):
 name(a);model(a);png(a,'arrow',[COLORS[0],COLORS[1],COLORS[2],COLORS[3],(176,119,219),COLORS[5],(159,165,175),(255,134,30),(140,229,245),(255,91,172),(235,78,20)][i])
 recipe(a,pattern=['M','S','F'],key={'M':arrow_ingredients[i],'S':'minecraft:stick','F':'minecraft:feather'},count=4)
AMMO.append('grappling_arrow')
name('grappling_arrow');model('grappling_arrow');png('grappling_arrow','arrow',(156,175,184))
recipe('grappling_arrow',['minecraft:arrow','minecraft:string','minecraft:iron_ingot'])
name('dragon_scale');model('dragon_scale');png('dragon_scale','scale',(177,118,222))
recipe('dragon_scale',['minecraft:dragon_breath','minecraft:echo_shard','minecraft:popped_chorus_fruit'])
name('dragon_scale_wolf_armor');model('dragon_scale_wolf_armor');png('dragon_scale_wolf_armor','wolf_armor',(158,105,211))
items.append(f'{MOD}:dragon_scale_wolf_armor')
recipe('dragon_scale_wolf_armor',pattern=['S S','SAS','S S'],key={'S':f'{MOD}:dragon_scale','A':'minecraft:wolf_armor'})
name('quiver');model('quiver');png('quiver','quiver',(148,93,50))
recipe('quiver',pattern=['LSL','L L','LLL'],key={'L':'minecraft:leather','S':'minecraft:string'})
export_wolf(ROOT)
write(f'assets/{MOD}/equipment/dragon_scale.json',{'layers':{'wolf_body':[{'texture':f'{MOD}:dragon_scale'}]}})
recipe('craftable_trident',pattern=[' PP',' SP','S  '],key={'P':'minecraft:prismarine_shard','S':'minecraft:stick'},output='minecraft:trident')
# The recipe is deliberately affordable for a combat mod; adjust it freely in this source.
bykind=lambda k:[f'{MOD}:{t}_{k}' for t in TIERS if not (t=='wood' and k in ['bow','crossbow'])]+([f'{MOD}:stone_{k}'] if k in ['bow','crossbow','battleaxe','knife'] else [])
for kind in ['knives','spears','battleaxes','shields']:
 singular={'knives':'knife','battleaxes':'battleaxe','shields':'shield'}.get(kind);tag(MOD,kind,bykind(singular) if singular else ['#minecraft:spears'])
tag(MOD,'throwable_weapons',['#pvpoverhaul:spears','#pvpoverhaul:battleaxes','#minecraft:axes','#minecraft:spears'])
tag(MOD,'elytra',['minecraft:elytra'])
tag(MOD,'loyalty_weapons',['minecraft:trident','#pvpoverhaul:throwable_weapons','#pvpoverhaul:knives'])
tag('minecraft','arrows',[f'{MOD}:{a}' for a in AMMO])
# Native spear items, recipes and models are reused without registering duplicates.
for enchant,values in {
 'bow':bykind('bow'),'crossbow':bykind('crossbow'),'durability':items,
 'weapon':bykind('knife')+['#minecraft:spears']+bykind('battleaxe'),
 'melee_weapon':bykind('knife')+['#minecraft:spears']+bykind('battleaxe'),
 'sharp_weapon':bykind('knife')+['#minecraft:spears']+bykind('battleaxe'),
 'lunge':['#minecraft:spears']}.items():tag('minecraft','enchantable/'+enchant,values)
def ench(id,supported,slots,max_level=1,effects=None,exclusive=None,weight=5):
 LANG[f'enchantment.{MOD}.{id}']=id.replace('_',' ').title()
 d={'description':{'translate':f'enchantment.{MOD}.{id}'},'supported_items':supported,'weight':weight,'max_level':max_level,'min_cost':{'base':10,'per_level_above_first':8},'max_cost':{'base':40,'per_level_above_first':8},'anvil_cost':3,'slots':slots,'effects':effects or {}}
 if exclusive:d['exclusive_set']=exclusive
 write(f'data/{MOD}/enchantment/{id}.json',d)
linear=lambda base,step:{'type':'minecraft:linear','base':base,'per_level_above_first':step}
ench('throwable','#pvpoverhaul:throwable_weapons',['mainhand'],exclusive=['minecraft:lunge','pvpoverhaul:pole_jump'])
ench('pole_jump','#pvpoverhaul:pole_weapons',['mainhand'],3,exclusive=['pvpoverhaul:throwable'])
ench('double_jump','#minecraft:enchantable/foot_armor',['feet'],2,exclusive=['pvpoverhaul:speed'])
ench('speed','#minecraft:enchantable/foot_armor',['feet'],3,{'minecraft:attributes':[{'id':'pvpoverhaul:speed','attribute':'minecraft:movement_speed','operation':'add_multiplied_base','amount':linear(.03,.03)}]},exclusive=['pvpoverhaul:double_jump'],weight=2)
ench('boost','#pvpoverhaul:elytra',['chest'],3,weight=2)
ench('overload','#pvpoverhaul:elytra',['chest'],1,weight=1)
ench('aerodynamics','#pvpoverhaul:elytra',['chest'],2)
ench('cleaving','#pvpoverhaul:battleaxes',['mainhand'],3,{'minecraft:damage':[{'effect':{'type':'minecraft:add','value':linear(1,1)}}]},'#minecraft:exclusive_set/damage')
ench('impact','#pvpoverhaul:battleaxes',['mainhand'],2,{'minecraft:knockback':[{'effect':{'type':'minecraft:add','value':linear(.5,.5)}}]})
# Patch only the supported-items and one mutual exclusion field of the 26.3 vanilla definitions.
loyalty={'anvil_cost':2,'description':{'translate':'enchantment.minecraft.loyalty'},'effects':{'minecraft:trident_return_acceleration':[{'effect':{'type':'minecraft:add','value':linear(1,1)}}]},'max_cost':{'base':50,'per_level_above_first':0},'max_level':3,'min_cost':{'base':12,'per_level_above_first':7},'slots':['mainhand'],'supported_items':'#pvpoverhaul:loyalty_weapons','weight':5}
write('data/minecraft/enchantment/loyalty.json',loyalty)
# Compatibility checks both enchantments' exclusive sets; Lunge's original effects stay intact.
for path in ['in_enchanting_table','tradeable','on_random_loot','on_traded_equipment']:
 write(f'data/minecraft/tags/enchantment/{path}.json',{'replace':False,'values':[f'{MOD}:{n}' for n in ['throwable','pole_jump','double_jump','speed','boost','overload','aerodynamics','cleaving','impact']]})
write('data/minecraft/tags/enchantment/exclusive_set/damage.json',{'replace':False,'values':['pvpoverhaul:cleaving']})
for key,title in {'zoom':'Zoom (hold)','target_lock':'Target Lock','dead_eye':'Dead Eye','boost':'Elytra Boost','pole_jump':'Pole Jump','overload':'Elytra Overload','quiver':'Quiver Arrow Wheel','sheath_swap':'Swap Sheathed Weapon'}.items():LANG[f'key.{MOD}.{key}']=title
LANG['container.pvpoverhaul.fletching']='Fletching Table'
LANG['container.pvpoverhaul.quiver']='Quiver'
LANG['key.category.pvpoverhaul.combat']='PvP+'
from arsenal_resources import extend
extend(globals())
# Uploaded arsenal textures override generated placeholders after the arsenal pass.
name('slingshot');model('slingshot')
recipe('slingshot',pattern=['S S',' L ',' S '],key={'S':'minecraft:stick','L':'minecraft:string'})
for asset,encoded in PACKED_ART.items():
 if any(k in asset for k in ['halberd','hammer','scythe','flail']) or asset in ['grappling_hook.png','grappling_rope.png','slingshot.png','ice_arrow.png','dragon_scale.png','dragon_scale_wolf_armor.png','weapon_sheath.png','combat_cache.png','healing_totem.png','storm_core.png']:
  target=ROOT/f'assets/{MOD}/textures/item/{asset}';target.parent.mkdir(parents=True,exist_ok=True);target.write_bytes(base64.b64decode(encoded,validate=True))
for material in ['wood','stone','copper','iron','gold','diamond','netherite']:
 item=material+'_halberd';held=item+'_held'
 write(f'assets/{MOD}/models/item/{held}.json',{'parent':'minecraft:item/handheld','textures':{'layer0':f'{MOD}:item/{held}'},'display':{'firstperson_righthand':{'rotation':[-20,90,-45],'translation':[1.13,3.2,1.13],'scale':[1.25,1.25,1.25]},'firstperson_lefthand':{'rotation':[-20,-90,45],'translation':[1.13,3.2,1.13],'scale':[1.25,1.25,1.25]},'thirdperson_righthand':{'rotation':[5,270,-45],'translation':[0,4,0.5],'scale':[1.3,1.3,1.3]},'thirdperson_lefthand':{'rotation':[5,-270,45],'translation':[0,4,0.5],'scale':[1.3,1.3,1.3]}}})
 write(f'assets/{MOD}/items/{item}.json',{'model':{'type':'minecraft:select','property':'minecraft:display_context','cases':[{'when':['gui','ground','fixed'],'model':{'type':'minecraft:model','model':f'{MOD}:item/{item}'}}],'fallback':{'type':'minecraft:model','model':f'{MOD}:item/{held}'}}})
 write(f'assets/{MOD}/items/thrown_visual/{item}.json',{'model':{'type':'minecraft:model','model':f'{MOD}:item/{held}'}})
# Use the exact vanilla fishing-rod transforms in both hands and camera views.
for material in ['wood','stone','copper','iron','gold','diamond','netherite']:
 item=material+'_flail'
 write(f'assets/{MOD}/models/item/{item}.json',{'parent':'minecraft:item/handheld_rod','textures':{'layer0':f'{MOD}:item/{item}'}})
# Uploaded handle-only sprites appear only while the fishing hook is cast.
for material in ['wood','stone','netherite','iron','gold','diamond']:
 item=material+'_flail';cast=item+'_cast'
 write(f'assets/{MOD}/models/item/{cast}.json',{'parent':'minecraft:item/handheld_rod','textures':{'layer0':f'{MOD}:item/{cast}'}})
 write(f'assets/{MOD}/items/{item}.json',{'model':{'type':'minecraft:condition','property':'minecraft:fishing_rod/cast',
 'on_true':{'type':'minecraft:model','model':f'{MOD}:item/{cast}'},
 'on_false':{'type':'minecraft:model','model':f'{MOD}:item/{item}'}}})
write(f'assets/{MOD}/lang/en_us.json',LANG)
print('Generated',len(items)+len(AMMO)+2,'items and all recipes/models/enchantments.')
