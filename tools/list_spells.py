import json

with open(r'C:\Users\El Hadji\Downloads\STAT_MOD\SIHRIYA\src\main\resources\data\sihriya\spells.json') as f:
    spells = json.load(f)

for s in spells:
    effects_desc = ' + '.join([e['type'] for e in s['effects']])
    print(f"{s['id']:45s} T{s['tier']} {s['type']:10s} {effects_desc}")
