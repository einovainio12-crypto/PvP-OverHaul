"""Remove obsolete standalone rose gold assets, including after an overlay upload."""
import json

def cleanup(root):
 retired={'raw_rose_gold','raw_rose_gold_block'}|{'rose_gold_'+k for k in ['ingot','nugget','block','sword','axe','pickaxe','shovel','hoe','helmet','chestplate','leggings','boots']}
 for p in root.rglob('*'):
  if not p.is_file():continue
  rel=p.relative_to(root).as_posix()
  if ('rose_gold' in p.name and (any(part in rel for part in ['/items/','/models/','/recipe/','/blockstates/','/loot_table/blocks/','/trim_material/','/armor_bar/']) or p.suffix=='.png')) or rel=='assets/pvpoverhaul/equipment/rose_gold.json':p.unlink()
 # Remove retired IDs from shared tags and stale native armor-model overrides.
 for p in root.glob('data/*/tags/**/*.json'):
  d=json.loads(p.read_text());d['values']=[v for v in d.get('values',[]) if (v.get('id') if isinstance(v,dict) else v) not in {'pvpoverhaul:'+x for x in retired}];p.write_text(json.dumps(d,indent=2)+'\n')
