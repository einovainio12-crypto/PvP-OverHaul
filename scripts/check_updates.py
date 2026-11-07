#!/usr/bin/env python3
"""Read-only Modrinth compatibility report. Never rewrites dependencies."""
import argparse,json,pathlib,urllib.request,urllib.parse,concurrent.futures
ROOT=pathlib.Path(__file__).resolve().parents[1]
p=argparse.ArgumentParser();p.add_argument('--minecraft',default='26.3');p.add_argument('--output',default='compatibility-report.json');a=p.parse_args()
mats=json.loads((ROOT/'scripts/compat_materials.json').read_text());projects={m['slug']:m['version'] for m in mats}
projects.update({'punchy-fpa':'3.1b','punchymoves':'1.0.7','iron-wolf-armor':None,'tiered-backpacks':None,'enchantment-descriptions':None,'rei':None,'emi':None,'jei':None,'farmers-delight-refabricated':None,'better-combat':None})
def check(pair):
 slug,tested=pair
 try:
  query=urllib.parse.urlencode({'loaders':json.dumps(['fabric']),'game_versions':json.dumps([a.minecraft])})
  req=urllib.request.Request('https://api.modrinth.com/v2/project/'+slug+'/version?'+query,headers={'User-Agent':'PvPPlus-compat-report/1.0'})
  with urllib.request.urlopen(req,timeout=20) as response:versions=json.load(response)
  releases=[v for v in versions if v['version_type']=='release'];v=(releases or versions or [None])[0]
  return {'project':slug,'reference_version':tested,'minecraft':a.minecraft,'available_version':v['version_number'] if v else None,'version_id':v['id'] if v else None,'status':'no matching release' if not v else 'review update' if tested and tested!=v['version_number'] else 'reference release available' if tested else 'available, integration unverified','required_dependencies':v['dependencies'] if v else []}
 except Exception as e:return {'project':slug,'status':'lookup failed','error':str(e)}
with concurrent.futures.ThreadPoolExecutor(max_workers=4) as executor:rows=list(executor.map(check,sorted(projects.items())))
pathlib.Path(a.output).write_text(json.dumps({'note':'Availability is not gameplay compatibility. Candidate versions require testing.','results':rows},indent=2)+'\n')
print('\n'.join(r['project']+': '+r['status'] for r in rows))
if any(r['status']=='lookup failed' for r in rows):raise SystemExit(1)
