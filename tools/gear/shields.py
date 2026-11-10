from pathlib import Path
import json,sys
r=Path(sys.argv[1])
def put(p,v):p.parent.mkdir(parents=True,exist_ok=True);p.write_text(json.dumps(v,indent=2)+'\n')
for f in (r/'assets/pvpoverhaul/items').glob('*_shield.json'):
 id='pvpoverhaul:'+f.stem;doc=json.loads(f.read_text());plain=doc['model']
 if plain.get('component')=='minecraft:base_color':continue
 def special(block):
  return {'type':'minecraft:special','base':'minecraft:item/shield'+('_blocking' if block else ''),'model':{'type':'minecraft:shield'}}
 banner={'type':'minecraft:condition','property':'minecraft:using_item','on_true':special(True),'on_false':special(False),'transformation':{'translation':[0,-0.12,0],'scale':[1,-1,-1]}}
 doc['model']={'type':'minecraft:condition','property':'minecraft:has_component','component':'minecraft:base_color','on_true':banner,'on_false':plain};put(f,doc)
 recipe={'type':'minecraft:crafting_special_shielddecoration','target':id,'banner':'#minecraft:banners','result':{'id':id}}
 # Conditional compat tiers can ship an item model even if the dependency is absent.
 for orig in [r/'data/pvpoverhaul/recipe'/(f.stem+'.json')]:
  if orig.exists():
   c=json.loads(orig.read_text()).get('fabric:load_conditions')
   if c:recipe['fabric:load_conditions']=c
 recipe.setdefault('fabric:load_conditions',[]).append({'condition':'fabric:registry_contains','registry':'minecraft:item','values':[id]})
 put(r/'data/pvpplusgear/recipe'/('decorate_'+f.stem+'.json'),recipe)
