from pathlib import Path
import re
root=Path(__file__).resolve().parents[2]
helper='dev.pvpplusmounts.GearSlots'
for f in (root/'src').rglob('*.java'):
 if f.stem not in ('Quivers','QuiverMenu','QuiverWheelScreen','PvpOverhaulClient'):continue
 s=old=f.read_text()
 if f.stem=='Quivers' and 'return 101;' not in s:
  s=re.sub(r'(static int find\(Player (\w+)\)\s*\{)',lambda m:m[1]+'\n        if(!'+helper+'.worn('+m[2]+',"chest/quiver").isEmpty())return 101;',s)
  s=s.replace('payload.inventorySlot()>=36 &&','payload.inventorySlot()!=101 && payload.inventorySlot()>=36 &&')
  s=s.replace('inventorySlot() >= 36 &&','inventorySlot() >= 36 && payload.inventorySlot()!=101 &&')
  s=s.replace('activate(carried,quiver);','carried.add('+helper+'.worn(player,"chest/quiver"));activate(carried,quiver);')
  s=s.replace('origin.owner.getInventory().setChanged();',helper+'.dirty(origin.owner);origin.owner.getInventory().setChanged();')
  s=s.replace('select(player,payload.inventorySlot(),payload.arrowSlot());','select(player,payload.inventorySlot(),payload.arrowSlot());'+helper+'.dirty(player);')
 for who in ['minecraft.player','mc.player','origin.owner','player']:
  for idx in ['inventorySlot','origin.inventorySlot','payload.inventorySlot()','slot']:
   expr=who+'.getInventory().getItem('+idx+')'
   s=re.sub(r'(?<![.\w])'+re.escape(expr),lambda _:helper+'.at('+who+'.getInventory(),'+idx+')',s)
 if f.stem=='QuiverMenu':
  s=s.replace('inventory.getItem(source)',helper+'.at(inventory,source)').replace('setContents(quiver,saved);inventory.setChanged();','setContents(quiver,saved);'+helper+'.dirty(inventory.player);inventory.setChanged();')
 if f.stem=='PvpOverhaulClient' and 'dev.pvpplusgear.SheathKey.init()' not in s:
  s=re.sub(r'(void onInitializeClient\(\)\s*\{)',r'\1 dev.pvpplusgear.SheathKey.init();',s,count=1)
 if old!=s:f.write_text(s)
build=root/'build.gradle'
line="apply from: 'tools/gear/shields.gradle'"
if line not in build.read_text():build.write_text(build.read_text()+'\n'+line+'\n')
