#!/usr/bin/env python3
import csv

p='app/src/main/assets/lexawall_words_v2.csv'
with open(p,encoding='utf-8') as f:
    rows=list(csv.DictReader(f))

# map id -> list of indices
from collections import defaultdict
id_map=defaultdict(list)
for i,r in enumerate(rows):
    id_map[r['id']].append(i)

max_id=max(int(r['id']) for r in rows)
changes=[]
for idv, idxs in id_map.items():
    if len(idxs)>1:
        # keep first occurrence, renumber others
        for dup_idx in idxs[1:]:
            max_id+=1
            old=rows[dup_idx]['id']
            rows[dup_idx]['id']=str(max_id)
            changes.append((old,str(max_id), rows[dup_idx]['word']))

# write back
fieldnames=['id','word','part_of_speech','cefr_level','definition','example_sentence','notification_line_1','notification_line_2','example_source','context_scenario','context_source','story_text','story_source','context_enabled']
with open(p,'w',encoding='utf-8',newline='') as f:
    w=csv.DictWriter(f,fieldnames=fieldnames)
    w.writeheader()
    w.writerows(rows)

print('Renumbered',len(changes),'duplicate rows. New max id=',max_id)
for c in changes[:10]:
    print('  ',c)
if len(changes)>10:
    print('  ...')
