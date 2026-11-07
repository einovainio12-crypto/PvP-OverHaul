"""Resource-only integrations with convention-tag and tooltip consumers."""
import json

def extend(g):
 root,write,lang=g['ROOT'],g['write'],g['LANG']
 groups={'bow':'bow','crossbow':'crossbow','shield':'shield','flail':'fishing_rod','hammer':'mace','knife':'knife','battleaxe':'axe','scythe':'scythe','halberd':'spear','spear':'spear'}
 tags={}
 for p in (root/'assets/pvpoverhaul/items').glob('*.json'):
  kind=p.stem.rsplit('_',1)[-1]
  if kind not in groups:continue
  item='pvpoverhaul:'+p.stem
  for tag in ['tools','tools/'+groups[kind]]+(['tools/ranged_weapon'] if kind in ['bow','crossbow'] else ['tools/melee_weapon'] if kind in ['knife','battleaxe','scythe','halberd','spear','hammer'] else []):tags.setdefault(tag,[]).append(item)
 for tag,values in tags.items():
  path='data/c/tags/item/'+tag+'.json';p=root/path;d=json.loads(p.read_text()) if p.exists() else {'replace':False,'values':[]}
  d['replace']=False;d['values']=list(dict.fromkeys(d['values']+sorted(values)));write(path,d)
 knives=tags.get('tools/knife',[])
 for tag in ['tools/knives','enchantable/knife']:
  write('data/farmersdelight/tags/item/'+tag+'.json',{'replace':False,'values':sorted(knives)})
 descriptions={'throwable':'Allows this weapon to be charged and thrown.','pole_jump':'Launches you upward when using a pole weapon while looking down.','double_jump':'Allows an additional jump in the air. Level II also raises the first jump.','speed':'Increases movement speed while worn.','boost':'Provides an Elytra speed boost using the assigned ability key.','overload':'Provides a powerful Elytra boost at the cost of half its durability.','aerodynamics':'Improves Elytra flight endurance.','cleaving':'Increases battleaxe damage.','impact':'Increases battleaxe knockback.','ice_aspect':'Slows targets with freezing attacks.','leeching':'Restores health when landing a melee hit.','stunning':'Gives melee hits a chance to briefly slow the target.','chaining':'Causes melee hits to affect nearby targets.'}
 for key,value in descriptions.items():lang['enchantment.pvpoverhaul.'+key+'.desc']=value
