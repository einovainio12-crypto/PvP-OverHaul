"""Optional Punchy mappings and original Punchy Moves clips. No third-party art."""
import json

def extend(g):
 root,write=g['ROOT'],g['write']
 kinds={'bow':'BOW','crossbow':'CROSSBOW','shield':'SHIELD','knife':'SWORD','battleaxe':'AXE','hammer':'MACE','scythe':'HOE','halberd':'SPEAR','spear':'SPEAR','flail':'FISHING_ROD'}
 items={}
 for p in (root/'assets/pvpoverhaul/items').glob('*.json'):
  kind=p.stem.rsplit('_',1)[-1]
  if kind in kinds:items['pvpoverhaul:'+p.stem]=kind
 # Exact IDs avoid assigning other mods' weapons or overriding vanilla groups.
 write('assets/punchy/compat/pvpoverhaul.json',{'itemSpecific':{i:{'kind':kinds[k]} for i,k in sorted(items.items())}})
 clips={}
 def clip(name,frames,duration=.45):
  # Explicit mirrored clips support either handedness without changing item transforms.
  for side in ['right','left']:
   bones={}
   for bone,values in frames.items():
    b=bone if side=='right' else bone.replace('right','TEMP').replace('left','right').replace('TEMP','left')
    bones[b]={'rotation':{str(t):v if side=='right' else [v[0],-v[1],-v[2]] for t,v in values}}
   clips['pvpplus_'+name+'_'+side]={'animation_length':duration,'bones':bones}
  return 'pvpplus_'+name
 slash=clip('slash',{'right_arm':[(0,[-25,0,8]),(.13,[-95,-35,35]),(.25,[-35,45,-30]),(.45,[-25,0,8])]})
 heavy=clip('heavy',{'right_arm':[(0,[-20,0,5]),(.2,[-150,0,15]),(.32,[-45,0,-8]),(.6,[-20,0,5])]},.6)
 sweep=clip('sweep',{'right_arm':[(0,[-25,0,10]),(.15,[-80,-65,20]),(.35,[-80,65,-20]),(.6,[-25,0,10])]},.6)
 jab=clip('jab',{'right_arm':[(0,[-40,0,5]),(.12,[-65,-15,10]),(.23,[-100,0,0]),(.4,[-40,0,5])]},.4)
 hold=clip('spear_hold',{'right_arm':[(0,[-65,-10,5]),(.2,[-85,-10,5])]},.2)
 throw=clip('throw',{'right_arm':[(0,[-150,0,15]),(.15,[-80,0,0]),(.4,[-25,0,5])]},.4)
 draw=clip('throw_draw',{'right_arm':[(0,[-25,0,5]),(.5,[-150,0,15])]},.5)
 cast=clip('cast',{'right_arm':[(0,[-30,0,5]),(.15,[-135,-10,10]),(.3,[-65,0,0]),(.5,[-30,0,5])]},.5)
 reel=clip('reel',{'right_arm':[(0,[-65,0,0]),(.18,[-105,0,10]),(.4,[-30,0,5])]},.4)
 idle=clip('idle',{'right_arm':[(0,[-15,0,5]),(.2,[-15,0,5])]},.2)
 block=clip('block',{'right_arm':[(0,[-60,-35,10]),(.2,[-60,-35,10])]},.2)
 bowdraw=clip('bow_draw',{'right_arm':[(0,[-35,-15,0]),(1,[-90,-20,0])],'left_arm':[(0,[-35,15,0]),(1,[-90,55,-5])]},1)
 bowhold=clip('bow_hold',{'right_arm':[(0,[-90,-20,0]),(.2,[-90,-20,0])],'left_arm':[(0,[-90,55,-5]),(.2,[-90,55,-5])]},.2)
 bowrelease=clip('bow_release',{'right_arm':[(0,[-90,-20,0]),(.1,[-95,-20,0]),(.35,[-35,-15,0])],'left_arm':[(0,[-90,55,-5]),(.1,[-95,70,-5]),(.35,[-35,15,0])]},.35)
 xdraw=clip('crossbow_draw',{'right_arm':[(0,[-55,-10,0]),(1,[-75,-10,0])],'left_arm':[(0,[-55,20,0]),(1,[-65,40,0])]},1)
 xhold=clip('crossbow_hold',{'right_arm':[(0,[-85,-10,0]),(.2,[-85,-10,0])],'left_arm':[(0,[-75,30,0]),(.2,[-75,30,0])]},.2)
 xfire=clip('crossbow_fire',{'right_arm':[(0,[-85,-10,0]),(.08,[-95,-10,0]),(.3,[-85,-10,0])],'left_arm':[(0,[-75,30,0]),(.08,[-85,30,0]),(.3,[-75,30,0])]},.3)
 moves={}
 for item,kind in sorted(items.items()):
  attack={'hammer':heavy,'battleaxe':heavy,'scythe':sweep,'spear':jab,'halberd':heavy}.get(kind,slash)
  entry={'animations':{'attack':{'clip':attack,'retrigger_progress':50}}}
  if kind in ['spear','halberd']:
   entry['animations']['spear']={'charge':hold,'attack':jab}
  if kind in ['knife','battleaxe','hammer','scythe','spear','halberd','flail']:
   entry['animations']['trident']={'charge':draw,'release':throw,'minimum_charge_ticks':10}
  if kind=='shield':entry['animations']['shield_pose']=block
  if kind=='flail':
   entry['sequence']={'behavior':'fishing'}
   entry['animations'].update({'equip':idle,'idle':{'clip':idle,'hold_last_frame':True},'cast':cast,'fishing':{'clip':idle,'hold_last_frame':True},'reel':reel})
  if kind=='bow':
   material=item.split(':')[1][:-4]
   if material=='gold':ticks=10
   elif material=='rose_gold':ticks=16
   elif material=='netherite':ticks=30
   elif material=='diamond':ticks=26
   else:
    mats=json.loads((root.parents[2]/'scripts/compat_materials.json').read_text())
    rank=next((m['rank'] for m in mats if m['id']==material),2)
    ticks=30 if rank>=5 else 26 if rank>=4 else 20
   entry['sequence']={'behavior':'bow','charge_ticks':ticks,'minimum_charge_ticks':3}
   entry['animations'].update({'equip':idle,'idle':{'clip':idle,'hold_last_frame':True},'charge':bowdraw,'hold':{'clip':bowhold,'hold_last_frame':True},'release':bowrelease})
  if kind=='crossbow':entry['animations']['crossbow']={'charge':xdraw,'charge_idle':xhold,'idle':idle,'charged':xhold,'shoot':xfire}
  moves[item]=entry
 write('assets/punchy_moves/compat/pvpoverhaul.json',{'format_version':2,'items':moves})
 write('assets/punchy_moves/animations/pvpoverhaul.json',{'format_version':'1.8.0','animations':clips})
 print('Punchy:',len(items),'item mappings;',len(clips),'original Moves clips')
