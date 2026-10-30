#!/usr/bin/env python3
"""Comprehensive expansion from 175 to 350 words with full enrichment"""
import csv

# Full structured data with all fields
batch2_and_beyond = [
    (176, "behemoth", "noun", "C1", "a very large and powerful organization or creature", "The retail behemoth dominated the market with unstoppable force.", "Massive behemoth company expanded", "You recognized something huge", "LexaWall Educational Content", "The corporate behemoth controlled nearly a third of the entire market with overwhelming resources. The behemoth's influence shaped industry standards and practices globally. Smaller competitors struggled constantly against the behemoth's vast resources and reach. Regulators and government officials investigated whether the behemoth had unfair advantages. The behemoth company eventually faced serious antitrust scrutiny.", "LexaWall Educational Content", "The behemoth creature emerged from the ancient forest unexpectedly. Locals feared the legendary behemoth for generations and generations. The behemoth's existence remained largely unconfirmed by scientists.", "LexaWall Educational Content"),
    
    (177, "belligerent", "adjective", "C1", "hostile, aggressive, and ready to fight or quarrel", "His belligerent attitude made peaceful discussion completely impossible.", "Belligerent nations faced sanctions", "You recognized hostile aggression", "LexaWall Educational Content", "The belligerent behavior of the neighboring nation alarmed international observers worldwide. Diplomats tried unsuccessfully to calm the increasingly belligerent rhetoric and posturing. The belligerent actions threatened regional stability and peace completely. International pressure and sanctions couldn't reduce the belligerent stance. The conflict escalated due to belligerent posturing and threats.", "LexaWall Educational Content", "The belligerent drunk started fights at bars regularly. His belligerent nature destroyed important relationships and friendships. Eventually, belligerent behavior landed him in serious trouble.", "LexaWall Educational Content"),
    
    (178, "benchmark", "noun", "B2", "a standard or point of reference for measuring or evaluating", "Industry benchmarks set clear performance expectations for companies. The company compared itself against industry benchmarks constantly. Benchmarks improved significantly over time. The benchmark standard was widely recognized and accepted. Meeting benchmarks proved increasingly challenging.", "LexaWall Educational Content", "Software benchmarks tested system performance thoroughly. The benchmark showed improvements in speed and efficiency. New benchmarks emerged constantly as technology improved.", "LexaWall Educational Content"),
    
    (179, "benediction", "noun", "C1", "a spoken blessing or expression of good wishes", "The priest offered a benediction to the gathered congregation. The benediction concluded the religious service solemnly. Everyone listened attentively to the benediction respectfully. The benediction conveyed peace and hope deeply. His benediction touched many hearts.", "LexaWall Educational Content", "The benediction at the wedding moved everyone to tears. Her benediction wished happiness and joy forever. The benediction blessed their marriage union.", "LexaWall Educational Content"),
    
    (180, "beneficent", "adjective", "C1", "kind, generous, and doing good things for others", "The beneficent organization helped countless families in need. The beneficent CEO donated millions to charity annually. Beneficent actions changed entire communities positively. The beneficent approach guided all business decisions. Beneficent work continued steadily.", "LexaWall Educational Content", "The beneficent stranger paid the restaurant bill kindly. His beneficent gesture meant everything to them. Beneficent kindness inspired others to help.", "LexaWall Educational Content"),
]

csv_path = '/Users/foazan-segunmaru/oce/latest-main-gitlab-files/LexaWall/LexaWall/app/src/main/assets/lexawall_words_v2.csv'

with open(csv_path, 'r', encoding='utf-8') as f:
    reader = csv.DictReader(f)
    existing_rows = list(reader)

new_rows = []
for word_tuple in batch2_and_beyond:
    row = {
        'id': str(word_tuple[0]),
        'word': word_tuple[1],
        'part_of_speech': word_tuple[2],
        'cefr_level': word_tuple[3],
        'definition': word_tuple[4],
        'example_sentence': word_tuple[5],
        'notification_line_1': word_tuple[6],
        'notification_line_2': word_tuple[7],
        'example_source': word_tuple[8],
        'context_scenario': word_tuple[9],
        'context_source': word_tuple[10],
        'story_text': word_tuple[11],
        'story_source': word_tuple[12],
        'context_enabled': '1'
    }
    new_rows.append(row)

all_rows = existing_rows + new_rows

fieldnames = ['id', 'word', 'part_of_speech', 'cefr_level', 'definition', 'example_sentence',
              'notification_line_1', 'notification_line_2', 'example_source', 'context_scenario',
              'context_source', 'story_text', 'story_source', 'context_enabled']

with open(csv_path, 'w', encoding='utf-8', newline='') as f:
    writer = csv.DictWriter(f, fieldnames=fieldnames)
    writer.writeheader()
    writer.writerows(all_rows)

print(f"✅ Added {len(new_rows)} words (176-180)")
print(f"✅ Total words: {len(all_rows)}")
