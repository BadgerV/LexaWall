import csv

csv_path = '/Users/foazan-segunmaru/oce/latest-main-gitlab-files/LexaWall/LexaWall/app/src/main/assets/lexawall_words_v2.csv'

with open(csv_path, 'r', encoding='utf-8') as f:
    reader = csv.DictReader(f)
    rows = list(reader)

print(f'✅ Total words: {len(rows)}')
print(f"✅ Word 1: {rows[0]['word']}")
print(f"✅ Word 150: {rows[-1]['word']}")

# Check completion
all_filled = all(r['context_scenario'] and r['story_text'] for r in rows)
print(f'✅ All context/story filled: {all_filled}')

# Sample output
print(f"\n📝 Sample Word 1 (aberration):")
print(f"   Context: {rows[0]['context_scenario'][:100]}...")
print(f"   Story: {rows[0]['story_text'][:80]}...")
