"""Check item coverage, clip references and left/right animation pairs."""
import json,pathlib

def check(root):
 res=root/'src/main/resources/assets'
 first=json.loads((res/'punchy/compat/pvpoverhaul.json').read_text())['itemSpecific']
 moves=json.loads((res/'punchy_moves/compat/pvpoverhaul.json').read_text())
 clips=json.loads((res/'punchy_moves/animations/pvpoverhaul.json').read_text())['animations']
 assert moves['format_version']==2 and set(first)==set(moves['items'])
 def refs(v):
  if isinstance(v,dict):
   for x in v.values():yield from refs(x)
  elif isinstance(v,list):
   for x in v:yield from refs(x)
  elif isinstance(v,str) and v.startswith('pvpplus_'):yield v
 for item,entry in moves['items'].items():
  assert item.startswith('pvpoverhaul:')
  assert (res/'pvpoverhaul/items'/(item.split(':')[1]+'.json')).exists(),item
  for name in refs(entry):
   for hand in ['left','right']:assert name+'_'+hand in clips,(item,name,hand)
 for name,clip in clips.items():
  assert clip['animation_length']>0
  for bone,channels in clip['bones'].items():
   assert bone in ['right_arm','left_arm','body','head','right_item','left_item']
   for t,v in channels['rotation'].items():assert 0<=float(t)<=clip['animation_length'] and len(v)==3 and all(isinstance(n,(int,float)) for n in v)
 assert moves['items']['pvpoverhaul:gold_bow']['sequence']['charge_ticks']==10
 assert moves['items']['pvpoverhaul:rose_gold_bow']['sequence']['charge_ticks']==16
 print(f'PASS: {len(first)} Punchy mappings; all Moves clips resolve for both hands.')
if __name__=='__main__':check(pathlib.Path(__file__).resolve().parents[1])
