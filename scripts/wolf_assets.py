"""Convert the supplied box-UV wolf CEM mesh into native animated wolf parts."""
import json, math, pathlib, shutil

ROLES = {'head':'head', 'mane':'upper_body', 'body':'body',
         'leg1':'right_hind_leg', 'leg2':'left_hind_leg',
         'leg3':'right_front_leg', 'leg4':'left_front_leg', 'tail':'tail'}

def export_wolf(resources):
    source = pathlib.Path(__file__).parent / 'wolf'
    model = json.loads((source / 'wolf_armor.bbmodel').read_text())
    assert model['resolution'] == {'width':64, 'height':32}
    groups = {g['uuid']:g for g in model['groups']}
    cubes = {c['uuid']:c for c in model['elements']}
    parts = []
    for node in model['outliner']:
        group = groups[node['uuid']]
        role = ROLES[group['name']]
        assert all(isinstance(child, str) for child in node['children']), 'Unexpected nested group'
        origin = group['origin']
        pivot = [-origin[0], 24-origin[1], origin[2]]
        rotation = group.get('rotation', [0,0,0])
        part = {'name':role, 'pivot':pivot,
                'rotation':[math.radians(-rotation[0]), math.radians(-rotation[1]), math.radians(rotation[2])],
                'cubes':[]}
        for uuid in node['children']:
            cube = cubes[uuid]
            assert cube['type']=='cube' and cube['box_uv'] and cube.get('export',True)
            assert not any(cube.get('rotation',[0,0,0])), 'Cube rotation needs a separate part'
            low, high = cube['from'], cube['to']
            part['cubes'].append({'origin':[-high[0]-pivot[0],24-high[1]-pivot[1],low[2]-pivot[2]],
                'size':[high[i]-low[i] for i in range(3)], 'uv':cube.get('uv_offset',[0,0]),
                'inflate':cube.get('inflate',0), 'mirror':cube.get('mirror_uv',group.get('mirror_uv',False))})
        parts.append(part)
    assert {p['name'] for p in parts} == set(ROLES.values())
    assert sum(len(p['cubes']) for p in parts)==11
    dest = resources / 'assets/pvpoverhaul/models/entity/dragon_scale_wolf_armor.json'
    dest.parent.mkdir(parents=True,exist_ok=True)
    dest.write_text(json.dumps({'texture_width':64,'texture_height':32,'parts':parts},indent=2)+'\n')
    texture = resources / 'assets/pvpoverhaul/textures/entity/equipment/wolf_body/dragon_scale.png'
    texture.parent.mkdir(parents=True,exist_ok=True)
    shutil.copyfile(source / 'wolf_armor.png',texture)
