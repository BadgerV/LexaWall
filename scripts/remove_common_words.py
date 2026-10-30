#!/usr/bin/env python3
import csv, shutil, time
from collections import Counter

SRC='app/src/main/assets/lexawall_words_v2.csv'
TS=int(time.time())
BACK=f'app/src/main/assets/lexawall_words_v2.csv.bak.{TS}'

# Backup
shutil.copy2(SRC,BACK)
print('Backup created:',BACK)

# Read
with open(SRC,encoding='utf-8') as f:
    rdr=list(csv.DictReader(f))
    fieldnames=rdr[0].keys()

# Build candidate list: prefer A1/A2, then short B1 words
candidates=[]
for r in rdr:
    word=(r['word'] or '').strip()
    cefr=(r.get('cefr_level') or '').strip()
    pos=(r.get('part_of_speech') or '').strip()
    score=0
    if cefr=='A1': score+=100
    if cefr=='A2': score+=80
    if cefr=='B1': score+=20
    # penalize rarer/higher-level
    if cefr in ('B2','C1'): score-=100
    # short common words more likely spoken
    if len(word)<=4: score+=10
    # function words likely very common
    if pos in ('preposition','conjunction','determiner','adverb'): score+=15
    candidates.append((score, r))

# sort descending by score so most common/simple come first
candidates.sort(key=lambda x: (-x[0], x[1]['word']))

# select top 100 unique ids to remove but avoid removing any B2/C1 (score negative)
remove_ids=set()
removed_rows=[]
for sc,r in candidates:
    if len(remove_ids)>=100:
        break
    # avoid removing rarer levels (score negative)
    if sc<0:
        continue
    remove_ids.add(r['id'])
    removed_rows.append((r['id'], r['word'], r.get('cefr_level')))

print('Planned removals:', len(removed_rows))

# Apply removal: keep rows whose id not in remove_ids
new_rows=[r for r in rdr if r['id'] not in remove_ids]

# Reassign nothing — keep ids as-is (we're deleting rows only)
with open(SRC,'w',encoding='utf-8',newline='') as f:
    w=csv.DictWriter(f, fieldnames=fieldnames)
    w.writeheader()
    w.writerows(new_rows)

print('Removed rows written. New total rows:', len(new_rows))
print('Sample removed (first 30):')
for t in removed_rows[:30]:
    print(t)

# Print CEFR/POS summary after removal
from collections import Counter, defaultdict
cefr=Counter(r['cefr_level'] for r in new_rows)
pos=Counter(r['part_of_speech'] for r in new_rows)
print('CEFR after removal:', dict(cefr))
print('POS after removal:', dict(pos))
print('Backup file:',BACK)
