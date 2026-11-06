"""Material integrations. Sprite artwork is unchanged; native palettes supply metal colors."""
import json,pathlib,struct,zlib,copy
HERE=pathlib.Path(__file__).parent
MATERIALS=json.loads((HERE/'compat_materials.json').read_text())
SOURCES=json.loads((HERE/'compat_palette_sources.json').read_text())
ROSE={'id':'rose_gold','mod':None,'palette':['452929','8c5454','b68585','c7a1a1','e0cbcb','f2e9e9','fdfbfb','ffffff'],'ingot':'pvpoverhaul:rose_gold_ingot','standard':True,'external_spear':False,'external_horse':False}
CUSTOM=['bow','crossbow','knife','battleaxe','shield','flail','hammer','scythe','halberd','arrow']
TOOLS=['sword','axe','pickaxe','shovel','hoe'];ARMOR=['helmet','chestplate','leggings','boots']
def own_items(m):
 k=CUSTOM+([] if m['external_spear'] else ['spear'])+([] if m['standard'] else TOOLS+ARMOR)+['wolf_armor']+([] if m['external_horse'] else ['horse_armor','nautilus_armor'])
 return [m['id']+'_'+s for s in k]
def png(colors):
 def chunk(t,d):return struct.pack('>I',len(d))+t+d+struct.pack('>I',zlib.crc32(t+d)&0xffffffff)
 return b'\x89PNG\r\n\x1a\n'+chunk(b'IHDR',struct.pack('>IIBBBBB',len(colors),1,8,6,0,0,0))+chunk(b'IDAT',zlib.compress(b'\0'+bytes(v for c in colors for v in (*c,255))))+chunk(b'IEND',b'')
def extend(g):
 root=g['ROOT'];write=g['write'];lang=g['LANG'];atlas=json.loads((root/'assets/minecraft/atlases/items.json').read_text());generated={};runtime={};newitems=[]
 def tag(path,values,ns='minecraft'):
  p=f'data/{ns}/tags/item/{path}.json';d=json.loads((root/p).read_text()) if (root/p).exists() else {'replace':False,'values':[]}
  for v in values:
   if v not in d['values']:d['values'].append(v)
  write(p,d)
 def condition(m):return [{'condition':'fabric:all_mods_loaded','values':[m['mod']]}] if m.get('mod') else []
 def recipe(id,pattern,key,result=None,count=1,m=None):
  d={'type':'minecraft:crafting_shaped','category':'equipment','pattern':pattern,'key':key,'result':{'id':result or 'pvpoverhaul:'+id,'count':count}}
  if m and condition(m):d['fabric:load_conditions']=condition(m)
  write('data/pvpoverhaul/recipe/'+id+'.json',d)
 def sprite(source,m):
  pair=(source,m['id'])
  if pair in generated:return generated[pair]
  data=SOURCES[source];colors=data['colors'];metal=data['metal'];target=[tuple(bytes.fromhex(c)) for c in m['palette']]
  mapping={tuple(c):target[round(i*(len(target)-1)/max(1,len(metal)-1))] for i,c in enumerate(metal)}
  path='compat/'+m['id']+'/'+source.replace(':','/');
  for suffix,c in [('key',colors),('palette',[mapping.get(tuple(c),c) for c in colors])]:
   p=root/f'assets/pvpoverhaul/textures/palettes/{path}_{suffix}.png';p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(png(c))
  atlas['sources'].append({'type':'minecraft:paletted_permutations','textures':[source],'palette_key':'pvpoverhaul:'+path+'_key','permutations':{'pvp_'+m['id']:'pvpoverhaul:'+path+'_palette'}})
  generated[pair]=source+'_pvp_'+m['id'];return generated[pair]
 def clone(v,m,textures=False):
  if isinstance(v,dict):return {k:clone(x,m,textures or k=='textures') for k,x in v.items()}
  if isinstance(v,list):return [clone(x,m,textures) for x in v]
  if isinstance(v,str):
   if textures and v in SOURCES:return sprite(v,m)
   return v.replace('pvpoverhaul:item/iron_','pvpoverhaul:item/'+m['id']+'_').replace('pvpoverhaul:iron_','pvpoverhaul:'+m['id']+'_').replace('minecraft:iron_ingot',m['ingot'])
  return v
 def simple(item,src,m,parent='generated'):
  write('assets/pvpoverhaul/models/item/'+item+'.json',{'parent':'minecraft:item/'+parent,'textures':{'layer0':sprite(src,m)}})
  write('assets/pvpoverhaul/items/'+item+'.json',{'model':{'type':'minecraft:model','model':'pvpoverhaul:item/'+item}})
 def armor_item(m,k):return (m['mod'] if m['standard'] else 'pvpoverhaul')+':'+m['id']+'_'+k
 for m in MATERIALS+[ROSE]:
  mid=m['id'];newitems+=own_items(m) if mid!='rose_gold' else ['rose_gold_horse_armor','rose_gold_wolf_armor','rose_gold_nautilus_armor']
  # Preserve each supplied PvP+ weapon's model, transforms, handle and animation sequence.
  for folder in ['models/item','items','items/thrown_visual']:
   for p in (root/f'assets/pvpoverhaul/{folder}').glob('iron_*.json'):
    if mid=='rose_gold' and not p.stem.startswith('iron_shield'):continue
    obj=clone(json.loads(p.read_text()),m)
    if folder=='items' and p.name=='iron_bow.json':obj['model']['on_true']['scale']=1/(30 if m.get('rank',2)>=5 else 26 if m.get('rank',2)>=4 else 20)
    write(f'assets/pvpoverhaul/{folder}/{p.name.replace("iron_",mid+"_",1)}',obj)
  if mid!='rose_gold':
   for kind in CUSTOM:
    p=root/f'data/pvpoverhaul/recipe/iron_{kind}.json';d=clone(json.loads(p.read_text()),m);d['fabric:load_conditions']=condition(m)
    if m.get('fireproof') and kind!='arrow':
     prev={'netherite_iron':'netherite','netherite_gold':'netherite_iron','netherite_emerald':'netherite_gold','netherite_diamond':'netherite_emerald'}[mid]
     d={'type':'minecraft:smithing_transform','template':'minecraft:netherite_upgrade_smithing_template','base':'pvpoverhaul:'+prev+'_'+kind,'addition':m['ingot'],'result':{'id':'pvpoverhaul:'+mid+'_'+kind},'fabric:load_conditions':condition(m)}
    if mid=='siftite' and kind!='arrow':
     d={'type':'minecraft:smithing_transform','template':'the_sift:siftite_upgrade_smithing_template','base':'pvpoverhaul:netherite_'+kind,'addition':m['ingot'],'result':{'id':'pvpoverhaul:siftite_'+kind},'fabric:load_conditions':condition(m)}
    write(f'data/pvpoverhaul/recipe/{mid}_{kind}.json',d)
   if not m['external_spear']:
    simple(mid+'_spear','minecraft:item/iron_spear',m);simple(mid+'_spear_in_hand','minecraft:item/iron_spear_in_hand',m,'spear_in_hand');(root/f'assets/pvpoverhaul/items/{mid}_spear_in_hand.json').unlink()
    write(f'assets/pvpoverhaul/items/{mid}_spear.json',{'model':{'type':'minecraft:select','property':'minecraft:display_context','cases':[{'when':['gui','ground','fixed','on_shelf'],'model':{'type':'minecraft:model','model':f'pvpoverhaul:item/{mid}_spear'}}],'fallback':{'type':'minecraft:model','model':f'pvpoverhaul:item/{mid}_spear_in_hand'}}})
    write(f'assets/pvpoverhaul/items/thrown_visual/{mid}_spear.json',{'model':{'type':'minecraft:model','model':f'pvpoverhaul:item/{mid}_spear_in_hand'}})
    recipe(mid+'_spear',['  M',' S ','S  '],{'M':m['ingot'],'S':'minecraft:stick'},m=m)
   if not m['standard']:
    patterns={'sword':['M','M','S'],'pickaxe':['MMM',' S ',' S '],'axe':['MM','MS',' S'],'shovel':['M','S','S'],'hoe':['MM',' S',' S'],'helmet':['MMM','M M'],'chestplate':['M M','MMM','MMM'],'leggings':['MMM','M M','M M'],'boots':['M M','M M']}
    for k,pat in patterns.items():
     simple(mid+'_'+k,'minecraft:item/iron_'+k,m,'handheld' if k in TOOLS else 'generated');keys={'M':m['ingot']}
     if 'S' in ''.join(pat):keys['S']='minecraft:stick'
     recipe(mid+'_'+k,pat,keys,m=m)
   # Match tag behavior of existing PvP+ iron weapons.
   for p in list((root/'data').glob('*/tags/item/**/*.json')):
    d=json.loads(p.read_text());add=[clone(v,m) for v in d.get('values',[]) if isinstance(v,str) and v.startswith('pvpoverhaul:iron_')]
    if add:
     for v in add:
      if v not in d['values']:d['values'].append(v)
     write(p.relative_to(root).as_posix(),d)
   tag(mid+'_repair',[{'id':m['ingot'],'required':False}],'pvpoverhaul')
  # Animal equipment uses ordinary equipment components: works without Iron Wolf Armor too.
  for k in ['wolf_armor','horse_armor','nautilus_armor']:
   if k in ['horse_armor','nautilus_armor'] and m['external_horse']:continue
   simple(mid+'_'+k,'minecraft:item/'+('wolf_armor' if k=='wolf_armor' else 'iron_'+k),m)
   recipe(mid+'_'+k,['M M','MMM','M M'] if k=='wolf_armor' else ['MMM','M M',' M '] if k=='nautilus_armor' else ['  M','MLM','MMM'],{'M':m['ingot'],**({'L':'minecraft:leather'} if k=='horse_armor' else {})},m=m)
   for t in ['wolf_armor','enchantable/wolf_armor'] if k=='wolf_armor' else [k]:tag(t,['pvpoverhaul:'+mid+'_'+k])
  for t in ['absorbs_wolf_damage','new_wolf_armors']:tag(t,['pvpoverhaul:'+mid+'_wolf_armor'],'ironwolfarmor')
  tag('armors/wolf',['pvpoverhaul:'+mid+'_wolf_armor'],'c')
  layers={};equipment=root/f'assets/pvpoverhaul/equipment/{mid}.json'
  if equipment.exists():layers=json.loads(equipment.read_text()).get('layers',{})
  for k in (['humanoid','humanoid_baby','humanoid_leggings'] if not m['standard'] else [])+['wolf_body']+([] if m['external_horse'] else ['horse_body','nautilus_body']):
   tex=f'pvpoverhaul:textures/entity/equipment/{k}/compat_{mid}.png';base='minecraft:textures/entity/equipment/'+k+'/'+('armadillo_scute' if k=='wolf_body' else 'iron')+'.png'
   runtime[tex]={'source':base,'palette':m['palette']};layers[k]=[{'texture':'pvpoverhaul:compat_'+mid}]
  write(f'assets/pvpoverhaul/equipment/{mid}.json',{'layers':layers})
  if mid!='rose_gold':
   for k in TOOLS:
    if not m['standard']:tag(k+'s',['pvpoverhaul:'+mid+'_'+k])
   for slot,k in zip(['head','chest','leg','foot'],ARMOR):
    a=armor_item(m,k);value={'id':a,'required':False} if m['standard'] else a
    for t in [slot+'_armor','enchantable/'+slot+'_armor','enchantable/armor','enchantable/durability','trimmable_armor']:tag(t,[value])
   if not m['standard']:
    for t in ['mining','mining_loot','durability']:tag('enchantable/'+t,['pvpoverhaul:'+mid+'_'+k for k in TOOLS])
    tag('enchantable/sword',['pvpoverhaul:'+mid+'_sword'])
   if not m['external_spear']:
    for t in ['spears','enchantable/lunge','enchantable/weapon','enchantable/sharp_weapon','enchantable/melee_weapon','enchantable/durability']:tag(t,['pvpoverhaul:'+mid+'_spear'])
    for t in ['pole_weapons','loyalty_weapons','throwable_weapons']:tag(t,['pvpoverhaul:'+mid+'_spear'],'pvpoverhaul')
   else:
    for t in ['pole_weapons','loyalty_weapons','throwable_weapons']:tag(t,[{'id':m['mod']+':'+mid+'_spear','required':False}],'pvpoverhaul')
  # Tiered Backpacks uses this standard chest-slot tag, not a hardcoded item list.
  if m.get('fireproof'):tag('freeze_immune_wearables',['pvpoverhaul:'+mid+'_wolf_armor'])
  for item in own_items(m):lang['item.pvpoverhaul.'+item]=item.replace('_',' ').title()
  # Original horse armor IDs remain unchanged; provide a crafting route where absent.
  if m['external_horse'] and m['id']!='steel':recipe(mid+'_horse_armor',['  M','MLM','MMM'],{'M':m['ingot'],'L':'minecraft:leather'},result=m['mod']+':'+mid+'_horse_armor',m=m)
 for mat,item in [('copper','copper'),('iron','iron'),('gold','golden'),('diamond','diamond')]:
  ingredient='minecraft:'+('diamond' if mat=='diamond' else mat+'_ingot');recipe('craft_'+item+'_horse_armor',['  M','MLM','MMM'],{'M':ingredient,'L':'minecraft:leather'},'minecraft:'+item+'_horse_armor')
  recipe('craft_'+item+'_nautilus_armor',['MMM','M M',' M '],{'M':ingredient},'minecraft:'+item+'_nautilus_armor')
 # Keep vanilla leather crafting and netherite smithing recipes.
 # Ice Caves Backport's exact item registry ID; vanilla fallback remains when absent.
 ice=root/'data/pvpoverhaul/recipe/ice_arrow.json';old=json.loads(ice.read_text());old['fabric:load_conditions']=[{'condition':'fabric:not','value':{'condition':'fabric:all_mods_loaded','values':['ice_caves_backport']}}];write(ice.relative_to(root).as_posix(),old)
 recipe('ice_arrow_icb',['I','A'],{'I':'ice_caves_backport:ice_ball','A':'minecraft:arrow'},'pvpoverhaul:ice_arrow',m={'mod':'ice_caves_backport'})
 for m in MATERIALS:
  if m['external_horse'] and m['id']!='steel':recipe(m['id']+'_nautilus_armor',['MMM','M M',' M '],{'M':m['ingot']},m['mod']+':'+m['id']+'_nautilus_armor',m=m)
 # Detail Armor Bar Reconstructed resource API, using its neutral icon and material color.
 for m in [m for m in MATERIALS if not m['standard']]+[ROSE]:
  def bar(x):return {'texture':'detailab:textures/armor_bar.png','texture_width':128,'texture_height':128,'offset':[x,0]}
  write('assets/pvpoverhaul/armor_bar/'+m['id']+'.json',{'type':'armor','items':['pvpoverhaul:'+m['id']+'_'+k for k in ARMOR],'render':{'full':bar(0),'half':bar(18),'color':m['palette'][len(m['palette'])//2]}})
 # Native 26.3 trim materials are palette IDs, automatically consumed by armor/elytra trim renderers.
 write('data/pvpoverhaul/trim_material/rose_gold.json',{'palette_id':'pvpoverhaul:trim/rose_gold','description':{'translate':'trim_material.pvpoverhaul.rose_gold','color':'#c7a1a1'}})
 lang['trim_material.pvpoverhaul.rose_gold']='Rose Gold'
 palette=[tuple(bytes.fromhex(c)) for c in reversed(ROSE['palette'])]
 p=root/'assets/pvpoverhaul/textures/palettes/trim/rose_gold.png';p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(png(palette))
 tag('trim_materials',['pvpoverhaul:rose_gold_ingot'])
 atlas['sources'].append({'type':'minecraft:paletted_permutations','textures':['minecraft:trims/items/'+k+'_trim' for k in ARMOR],'palette_key':'minecraft:trim_base','permutations':{'pvp_rose_gold':'pvpoverhaul:trim/rose_gold'}})
 for m in [m for m in MATERIALS if not m['standard']]+[ROSE]:
  for k in ARMOR:
   item=m['id']+'_'+k;modelpath=f'assets/pvpoverhaul/models/item/{item}.json';base=json.loads((root/modelpath).read_text());cases=[]
   for trim in ['quartz','iron','netherite','redstone','copper','gold','emerald','diamond','lapis','amethyst','resin','rose_gold']:
    model=copy.deepcopy(base);model['textures']['layer1']='minecraft:trims/items/'+k+'_trim_'+('pvp_rose_gold' if trim=='rose_gold' else trim)
    modelname=item+'_'+trim+'_trim';write(f'assets/pvpoverhaul/models/item/{modelname}.json',model)
    cases.append({'when':('pvpoverhaul:' if trim=='rose_gold' else 'minecraft:')+trim,'model':{'type':'minecraft:model','model':'pvpoverhaul:item/'+modelname}})
   write('assets/pvpoverhaul/items/'+item+'.json',{'model':{'type':'minecraft:select','property':'minecraft:trim_material','cases':cases,'fallback':{'type':'minecraft:model','model':'pvpoverhaul:item/'+item}}})
 write('assets/pvpoverhaul/compat_equipment.json',runtime)
 write('assets/minecraft/atlases/items.json',atlas)
 write('assets/pvpoverhaul/compat_materials.json',MATERIALS)
 write('assets/pvpoverhaul/compat_expected_items.json',sorted(newitems))
 for old in ['steel_horse_armor','steel_nautilus_armor']:(root/f'data/pvpoverhaul/recipe/{old}.json').unlink(missing_ok=True)
 print('Compatibility:',len(MATERIALS),'materials,',len(newitems),'new items')
