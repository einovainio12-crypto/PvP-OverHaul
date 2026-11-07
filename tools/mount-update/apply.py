"""Merge mount entries before Gradle reads the project's existing metadata."""
from pathlib import Path
import json,re
root=Path(__file__).resolve().parents[2]
meta=root/'src/main/resources/fabric.mod.json'
d=json.loads(meta.read_text())
if d.get('id')!='pvpoverhaul':
    raise SystemExit('Expected PvP+ project metadata. Restore the PvP+ fabric.mod.json from before the standalone upload.')
props=root/'gradle.properties'
old=props.read_text()
pat=r'(?m)^(mod_version[ \t]*=[ \t]*)0\.10\.(15|16|17|18|19|20)([ \t]*)$'
if not re.search(pat,old):raise SystemExit('Expected PvP+ 0.10.15–0.10.20 compatibility sources.')
edits={props:re.sub(pat,r'\g<1>0.10.20\3',old)}
if d.get('version') in ('0.10.15','0.10.16','0.10.17','0.10.18','0.10.19'): d['version']='0.10.20'
entry='dev.pvpplusmounts.DragonMounts'
ep=d['entrypoints']['main']
if entry not in [x.get('value') if isinstance(x,dict) else x for x in ep]:ep.append(entry)
if 'dev.pvpplusdiscs.DiscUpdate' not in ep:ep.append('dev.pvpplusdiscs.DiscUpdate')
d.setdefault('breaks',{})['pvpplusmounts']='*'
dump=lambda obj:json.dumps(obj,indent=2,ensure_ascii=False)+'\n'
edits[meta]=dump(d)
for rel,entries in json.loads(Path(__file__).with_name('tags.json').read_text()).items():
    p=root/rel
    data=json.loads(p.read_text()) if p.exists() else {'replace':False,'values':[]}
    vals=data['values']
    for x in entries:
        key=x['id']
        ids=[v if isinstance(v,str) else v['id'] for v in vals]
        if key not in ids: vals.append(x)
        elif key.startswith('pvpplusmounts:'):
            vals[:]=[x if (v if isinstance(v,str) else v['id'])==key else v for v in vals]
    edits[p]=dump(data)
for p,s in edits.items():
    if not p.exists() or p.read_text()!=s:
        p.parent.mkdir(parents=True,exist_ok=True);p.write_text(s)

# Drop superseded disc assets from checkouts upgraded from 0.10.18.
for rel in ['src/main/resources/assets/pvpplusdiscs/items/music_disc_count_the_flash.json', 'src/main/resources/assets/pvpplusdiscs/sounds/records/count_the_flash.ogg', 'src/main/resources/data/pvpplusdiscs/recipe/music_disc_count_the_flash.json', 'src/main/resources/data/pvpplusdiscs/jukebox_song/count_the_flash.json']:
    (root/rel).unlink(missing_ok=True)
