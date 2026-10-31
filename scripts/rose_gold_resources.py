"""Native rose gold equipment and non-destructive palette-derived custom item sprites."""
import json,base64,pathlib

def extend(g):
 root=g['ROOT'];write=g['write'];recipe=g['recipe'];name=g['name'];model=g['model'];ns='pvpoverhaul';ingot=ns+':rose_gold_ingot'
 for path,data in json.loads((pathlib.Path(__file__).parent/'art/rose_gold_assets.json').read_text()).items():
  p=root/path;p.parent.mkdir(parents=True,exist_ok=True);p.write_bytes(base64.b64decode(data))
 # Clone the user's gold model geometry, display transforms and animation trees.
 def swap(v,textures=False):
  if isinstance(v,dict):return {k:swap(x,textures or k=='textures') for k,x in v.items()}
  if isinstance(v,list):return [swap(x,textures) for x in v]
  if isinstance(v,str):
   if textures and v.startswith(ns+':item/gold_'):return v+'_rose_gold'
   return v.replace(ns+':item/gold_',ns+':item/rose_gold_').replace(ns+':gold_',ns+':rose_gold_').replace('minecraft:gold_ingot',ingot)
  return v
 for folder in ['models/item','items','items/thrown_visual']:
  for p in (root/f'assets/{ns}/{folder}').glob('gold_*.json'):
   obj=swap(json.loads(p.read_text()))
   if p.name=='gold_bow.json' and folder=='items':obj['model']['on_true']['scale']=1/16
   write(f'assets/{ns}/{folder}/{p.name.replace("gold_","rose_gold_",1)}',obj)
 custom=['bow','crossbow','knife','battleaxe','shield','flail','hammer','scythe','halberd','arrow']
 for kind in custom:
  item='rose_gold_'+kind;name(item)
  source=root/f'data/{ns}/recipe/gold_{kind}.json'
  write(f'data/{ns}/recipe/{item}.json',swap(json.loads(source.read_text())))
 # Standard equipment uses the original Rose Gold Equipment icons, byte for byte.
 patterns={'sword':['M','M','S'],'pickaxe':['MMM',' S ',' S '],'axe':['MM','MS',' S'],'shovel':['M','S','S'],'hoe':['MM',' S',' S'],'helmet':['MMM','M M'],'chestplate':['M M','MMM','MMM'],'leggings':['MMM','M M','M M'],'boots':['M M','M M']}
 for kind,pattern in patterns.items():
  item='rose_gold_'+kind;name(item);model(item)
  write(f'assets/{ns}/models/item/{item}.json',{'parent':'minecraft:item/'+('handheld' if kind in ['sword','pickaxe','axe','shovel','hoe'] else 'generated'),'textures':{'layer0':ns+':item/'+item}})
  key={'M':ingot}
  if 'S' in ''.join(pattern):key['S']='minecraft:stick'
  recipe(item,pattern=pattern,key=key)
 for item in ['raw_rose_gold','rose_gold_ingot','rose_gold_nugget']:name(item);model(item)
 recipe('raw_rose_gold',ingredients=['minecraft:copper_ingot','minecraft:gold_ingot'])
 recipe('rose_gold_ingot',pattern=['NNN','NNN','NNN'],key={'N':ns+':rose_gold_nugget'})
 recipe('rose_gold_nugget',ingredients=[ingot],count=9)
 for method,time in [('smelting',200),('blasting',100)]:
  write(f'data/{ns}/recipe/rose_gold_ingot_from_{method}.json',{'type':'minecraft:'+method,'category':'misc','ingredient':ns+':raw_rose_gold','result':{'id':ingot},'experience':.7,'cookingtime':time})
 for item,material in [('rose_gold_block',ingot),('raw_rose_gold_block',ns+':raw_rose_gold')]:
  name(item);g['LANG']['block.'+ns+'.'+item]=item.replace('_',' ').title()
  write(f'assets/{ns}/blockstates/{item}.json',{'variants':{'':{'model':ns+':block/'+item}}})
  write(f'assets/{ns}/models/block/{item}.json',{'parent':'minecraft:block/cube_all','textures':{'all':ns+':block/'+item}})
  model(item,ns+':block/'+item)
  recipe(item,pattern=['MMM','MMM','MMM'],key={'M':material})
  recipe(item+'_unpack',ingredients=[ns+':'+item],output=material,count=9)
  write(f'data/{ns}/loot_table/blocks/{item}.json',{'type':'minecraft:block','pools':[{'rolls':1,'entries':[{'type':'minecraft:item','name':ns+':'+item}],'conditions':[{'condition':'minecraft:survives_explosion'}]}]})
 write(f'assets/{ns}/equipment/rose_gold.json',{'layers':{k:[{'texture':ns+':rose_gold'}] for k in ['humanoid','humanoid_leggings']}})
 name('rose_gold_spear');recipe('rose_gold_spear',pattern=['  M',' S ','S  '],key={'M':ingot,'S':'minecraft:stick'})
 for suffix,parent in [('', 'generated'),('_in_hand','spear_in_hand')]:
  write(f'assets/{ns}/models/item/rose_gold_spear{suffix}.json',{'parent':'minecraft:item/'+parent,'textures':{'layer0':'minecraft:item/golden_spear'+suffix+'_rose_gold'}})
 write(f'assets/{ns}/items/rose_gold_spear.json',{'model':{'type':'minecraft:select','property':'minecraft:display_context','cases':[{'when':['gui','ground','fixed','on_shelf'],'model':{'type':'minecraft:model','model':ns+':item/rose_gold_spear'}}],'fallback':{'type':'minecraft:model','model':ns+':item/rose_gold_spear_in_hand'}},'swap_animation_scale':1.95})
 write(f'assets/{ns}/items/thrown_visual/rose_gold_spear.json',{'model':{'type':'minecraft:model','model':ns+':item/rose_gold_spear_in_hand'}})
 # Inherit the equivalent gold custom-item tags without replacing other mods' entries.
 for p in list((root/'data').glob('*/tags/item/**/*.json')):
  d=json.loads(p.read_text());values=d.get('values',[]);add=[swap(v) for v in values if isinstance(v,str) and v.startswith(ns+':gold_')]
  if add:d['values']=list(dict.fromkeys(values+add));write(p.relative_to(root).as_posix(),d)
 def tag(path,values,namespace='minecraft',kind='item'):
  path=f'data/{namespace}/tags/{kind}/{path}.json';p=root/path;d=json.loads(p.read_text()) if p.exists() else {'replace':False,'values':[]};d['values']=list(dict.fromkeys(d['values']+values));write(path,d)
 def ids(*k):return [ns+':rose_gold_'+x for x in k]
 tag('rose_gold_repair',[ingot],ns)
 tag('spears',ids('spear'));tag('pole_weapons',ids('spear'),ns);tag('throwable_weapons',ids('spear','sword','axe'),ns);tag('loyalty_weapons',ids('spear','sword','axe'),ns)
 for k in ['sword','axe','pickaxe','shovel','hoe']:tag(k+'s',ids(k))
 for k in ['durability','weapon','sharp_weapon','melee_weapon']:tag('enchantable/'+k,ids('sword','axe','spear'))
 for k in ['mining','mining_loot']:tag('enchantable/'+k,ids('pickaxe','axe','shovel','hoe'))
 tag('enchantable/sword',ids('sword'));tag('enchantable/durability',ids('pickaxe','shovel','hoe','helmet','chestplate','leggings','boots'))
 for kind,slot in [('helmet','head'),('chestplate','chest'),('leggings','leg'),('boots','foot')]:
  tag('enchantable/'+slot+'_armor',ids(kind));tag(slot+'_armor',ids(kind))
 tag('enchantable/armor',ids('helmet','chestplate','leggings','boots'));tag('trimmable_armor',ids('helmet','chestplate','leggings','boots'))
 for k in ['mineable/pickaxe','needs_iron_tool']:tag(k,[ns+':rose_gold_block',ns+':raw_rose_gold_block'],kind='block')
 for path,values in [('ingots/rose_gold',[ingot]),('nuggets/rose_gold',ids('nugget')),('storage_blocks/rose_gold',ids('block'))]:tag(path,values,'c')
