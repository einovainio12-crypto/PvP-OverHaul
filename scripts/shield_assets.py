"""Export the supplied unrotated Blockbench CEM cubes as ordinary Java item models.

Embedded PNGs are copied unchanged. UVs are scaled from the source pixel grid
to Minecraft's 0..16 item-model grid. No OptiFine/entity-model plugin is needed.
"""
import base64, json, pathlib

TIERS = ['wood', 'copper', 'iron', 'gold', 'diamond', 'netherite']
NORMAL_DISPLAY = {
    'thirdperson_righthand': {'rotation': [0, -90, 0], 'translation': [0, 0, 0], 'scale': [1, 1, 1]},
    'thirdperson_lefthand': {'rotation': [0, -90, 0], 'translation': [0, 0, 0], 'scale': [1, 1, 1]},
    'firstperson_righthand': {'rotation': [0, 0, 5], 'translation': [0, 0, 0], 'scale': [1, 1, 1]},
    'firstperson_lefthand': {'rotation': [0, 0, 5], 'translation': [0, 0, 0], 'scale': [1, 1, 1]},
    'gui': {'rotation': [15, 155, -5], 'translation': [0, 0, 0], 'scale': [0.65, 0.65, 0.65]},
    'fixed': {'rotation': [0, 0, 0], 'translation': [0, 0, 0], 'scale': [0.55, 0.55, 0.55]},
    'on_shelf': {'rotation': [0, 180, 0], 'translation': [0, 0, 0], 'scale': [1, 1, 1]},
    'ground': {'rotation': [0, 180, 0], 'translation': [0, 3, 0], 'scale': [0.25, 0.25, 0.25]},
}
BLOCKING_DISPLAY = {
    **NORMAL_DISPLAY,
    'thirdperson_righthand': {'rotation': [25, -35, 0], 'translation': [0, 1, -1], 'scale': [1, 1, 1]},
    'thirdperson_lefthand': {'rotation': [25, -35, 0], 'translation': [0, 1, -1], 'scale': [1, 1, 1]},
    'firstperson_righthand': {'rotation': [0, -10, -5], 'translation': [-2, 1, -2], 'scale': [1, 1, 1]},
    'firstperson_lefthand': {'rotation': [0, -10, -5], 'translation': [-2, 1, -2], 'scale': [1, 1, 1]},
}

def export_shield(resources, item):
    tier = item.removesuffix('_shield')
    filename = 'shield.bbmodel' if tier == 'wood' else f'shield_{tier}.bbmodel'
    source = pathlib.Path(__file__).parent / 'shields' / filename
    model = json.loads(source.read_text())
    assert model['resolution'] == {'width': 64, 'height': 64}, source
    assert all(not any(g.get('rotation', [0, 0, 0])) for g in model['groups']), 'Unsupported group rotation'
    textures = {}
    for i, texture in enumerate(model['textures']):
        texture_name = item if i == 0 else f'{item}_{i}'
        png = base64.b64decode(texture['source'].split(',', 1)[1], validate=True)
        assert png[:8] == b'\x89PNG\r\n\x1a\n'
        dest = resources / f'assets/pvpoverhaul/textures/item/{texture_name}.png'
        dest.parent.mkdir(parents=True, exist_ok=True)
        dest.write_bytes(png)
        textures[str(i)] = f'pvpoverhaul:item/{texture_name}'
    textures['particle'] = textures['0']
    elements = []
    # Center the grip, not the plate pivot, on the hand's item origin. For the
    # supplied meshes the handle center is [0,10,1]. Item cubes are centered at 8.
    handle=next(cube for cube in model['elements'] if cube['name']=='handle')
    grip=[(a+b)/2 for a,b in zip(handle['from'],handle['to'])]
    shift=[8-value for value in grip]
    for cube in model['elements']:
        if not cube.get('export', True):
            continue
        assert cube['type'] == 'cube' and not any(cube.get('rotation', [0, 0, 0])), 'Unsupported element rotation'
        assert not cube.get('inflate', 0), 'Inflated cubes need explicit export support'
        faces = {}
        for direction, face in cube['faces'].items():
            if face.get('texture') is None:
                continue
            assert str(face['texture']) in textures
            faces[direction] = {'uv': [v * 16 / (model['resolution']['width'] if i % 2 == 0 else model['resolution']['height']) for i, v in enumerate(face['uv'])], 'texture': '#' + str(face['texture'])}
            if face.get('rotation'):
                faces[direction]['rotation'] = face['rotation']
        element = {'from': [v + shift[i] for i, v in enumerate(cube['from'])],
                   'to': [v + shift[i] for i, v in enumerate(cube['to'])], 'faces': faces}
        assert all(-16 <= value <= 32 for value in element['from'] + element['to'])
        elements.append(element)
    assert len(elements) == 5, source
    for suffix, display in [('', NORMAL_DISPLAY), ('_blocking', BLOCKING_DISPLAY)]:
        data = {'gui_light': 'side', 'textures': textures, 'display': display, 'elements': elements}
        dest = resources / f'assets/pvpoverhaul/models/item/{item}{suffix}.json'
        dest.parent.mkdir(parents=True, exist_ok=True)
        dest.write_text(json.dumps(data, indent=2) + '\n')
    def ref(suffix):
        return {'type': 'minecraft:model', 'model': f'pvpoverhaul:item/{item}{suffix}'}
    definition = {'model': {'type': 'minecraft:condition', 'property': 'minecraft:using_item',
                            'on_false': ref(''), 'on_true': ref('_blocking')}}
    dest = resources / f'assets/pvpoverhaul/items/{item}.json'
    dest.write_text(json.dumps(definition, indent=2) + '\n')
