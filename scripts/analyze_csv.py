#!/usr/bin/env python3
import csv, json
from collections import Counter, defaultdict
p='/Users/foazan-segunmaru/oce/latest-main-gitlab-files/LexaWall/LexaWall/app/src/main/assets/lexawall_words_v2.csv'
with open(p,encoding='utf-8') as f:
    rdr=csv.DictReader(f)
    rows=list(rdr)
fieldnames=rdr.fieldnames
ids=[int(r['id']) for r in rows if r.get('id') and r['id'].isdigit()]
id_counts=Counter(ids)
dup_ids=[k for k,v in id_counts.items() if v>1]
dup_count=sum(v-1 for v in id_counts.values() if v>1)
cefr=Counter((r.get('cefr_level') or '').strip() for r in rows)
pos=Counter((r.get('part_of_speech') or '').strip() for r in rows)
# map id to words
id_map=defaultdict(list)
for r in rows:
    id_map[r.get('id')].append(r.get('word'))
malformed=[]
for i,r in enumerate(rows, start=2):
    if not r.get('id') or not r.get('word'):
        malformed.append({'line':i,'id':r.get('id'),'word':r.get('word')})
    if any(v is None for v in r.values()):
        malformed.append({'line':i,'id':r.get('id'),'word':r.get('word')})
summary={
    'total_rows': len(rows),
    'header_field_count': len(fieldnames),
    'id_min': min(ids) if ids else None,
    'id_max': max(ids) if ids else None,
    'duplicate_id_count': dup_count,
    'duplicate_ids_sample': dup_ids[:20],
    'cefr_counts': dict(cefr),
    'pos_counts': dict(pos),
    'a2_count': sum(1 for r in rows if (r.get('cefr_level') or '').strip()=='A2'),
    'a2_sample': [r.get('word') for r in rows if (r.get('cefr_level') or '').strip()=='A2'][:40],
    'malformed_rows_count': len(malformed),
    'malformed_sample': malformed[:10],
}
print(json.dumps(summary, ensure_ascii=False, indent=2))
