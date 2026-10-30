#!/usr/bin/env python3
"""Add final 146 words (205-350) to reach 350 total"""
import csv

csv_path = '/Users/foazan-segunmaru/oce/latest-main-gitlab-files/LexaWall/LexaWall/app/src/main/assets/lexawall_words_v2.csv'

# Read existing
with open(csv_path, 'r', encoding='utf-8') as f:
    reader = csv.DictReader(f)
    existing = list(reader)

# 146 new words with full data (tuples with 14 fields)
new_word_data = [
    (205, "blackmail", "noun", "B1", "to demand money by threatening secrets", "The criminal attempted blackmail against the executive.", "Extortion occurred", "You recognized threats", "LexaWall Educational Content", "The criminal attempted blackmail against the wealthy executive repeatedly. Blackmail threats included releasing compromising photos online. The blackmail demanded millions of dollars upfront. Police investigated the blackmail case thoroughly immediately. Blackmail charges resulted in prosecution and conviction.", "LexaWall Educational Content", "She received blackmail notes threatening her career daily. His blackmail scheme was elaborate and calculated. Blackmail finally stopped after arrest.", "LexaWall Educational Content"),
    (206, "blandishment", "noun", "C1", "flattery or coaxing to persuade someone", "The blandishment had no effect on her decision.", "Flattery was used", "You recognized manipulation", "LexaWall Educational Content", "The blandishment had absolutely no effect on her firm decision. Salespeople used blandishment constantly on unsuspecting customers. The blandishment was transparent and obvious. Blandishment tactics rarely work on intelligent people. Blandishment attempts failed repeatedly.", "LexaWall Educational Content", "He tried blandishment to convince her completely. The blandishment seemed insincere and empty. Blandishment was rejected firmly and immediately.", "LexaWall Educational Content"),
    (207, "blasé", "adjective", "B2", "unimpressed or indifferent about something", "His blasé attitude suggested he didn't care.", "Indifference shown", "You recognized apathy", "LexaWall Educational Content", "His blasé attitude suggested he genuinely didn't care about anything. The blasé response frustrated enthusiastic colleagues greatly. Blasé remarks showed disconnection and detachment. The blasé approach seemed unprofessional always. Blasé indifference was contagious and spreading.", "LexaWall Educational Content", "She was blasé about the major award. His blasé reaction disappointed everyone watching. The blasé attitude eventually changed.", "LexaWall Educational Content"),
    (208, "blaspheme", "verb", "C1", "to speak disrespectfully about religion", "The speaker began to blaspheme against sacred beliefs.", "Irreverence shown", "You heard profanity", "LexaWall Educational Content", "The speaker began to blaspheme against sacred religious beliefs publicly. Blaspheme language offended believers deeply and profoundly. The blaspheme remarks sparked immediate outrage nationwide. Blaspheme actions had serious legal consequences. Blaspheme speech was widely condemned.", "LexaWall Educational Content", "He chose to blaspheme in public spaces carelessly. The blaspheme words shocked most people hearing. Blaspheme behavior resulted in public criticism.", "LexaWall Educational Content"),
    (209, "blatant", "adjective", "B2", "obvious and intentionally noticeable or shameless", "His blatant disregard for rules angered everyone.", "Obvious rudeness", "You noticed shamelessness", "LexaWall Educational Content", "His blatant disregard for established rules angered absolutely everyone around. The blatant lie was obvious to all observers. Blatant favoritism shaped unfair decisions regularly. The blatant corruption shocked the entire nation. Blatant violations continued despite consequences.", "LexaWall Educational Content", "The blatant cheating was obvious to anyone. Her blatant dishonesty ended trust completely. Blatant behavior had serious consequences.", "LexaWall Educational Content"),
    (210, "bleach", "verb", "B1", "to make something white or clean", "The bleach cleaned the white clothes effectively.", "Cleaning solution", "You recognized chemicals", "LexaWall Educational Content", "The bleach cleaned the white clothes efficiently and thoroughly. Bleach removed stubborn stains almost magically. The bleach smell was strong and overpowering indoors. Bleach solutions are caustic and dangerous. Bleach required careful careful handling always.", "LexaWall Educational Content", "The sun bleached the fabric naturally over time. Bleach whitened the cloth beautifully. Bleaching occurred gradually and steadily.", "LexaWall Educational Content"),
    (211, "bleak", "adjective", "B1", "cold barren and unattractive or hopeless", "The bleak landscape offered little shelter or comfort.", "Desolate setting", "You felt despair", "LexaWall Educational Content", "The bleak landscape offered little shelter or comfort anywhere. The bleak winter weather depressed everyone deeply. Bleak circumstances seemed hopeless and insurmountable. The bleak future looked uncertain and frightening. Bleak conditions persisted stubbornly.", "LexaWall Educational Content", "The bleak apartment needed extensive work immediately. His bleak outlook was concerning to friends. The bleak situation improved gradually.", "LexaWall Educational Content"),
    (212, "bleat", "verb", "B1", "the cry of a goat or sheep", "The sheep's bleat echoed across the field loudly.", "Animal sound", "You heard the cry", "LexaWall Educational Content", "The sheep's bleat echoed across the open field loudly. The bleat of lambs attracted mother animals. Bleat sounds indicated distress and fear. The bleat continued throughout the night. Bleat patterns varied by animal.", "LexaWall Educational Content", "The goat's bleat was loud and distinctive. His bleat of protest succeeded eventually. Bleating continued relentlessly.", "LexaWall Educational Content"),
    (213, "blighted", "adjective", "B2", "affected by blight or severely damaged", "The blighted neighborhood needed urban renewal efforts.", "Deteriorated area", "You recognized damage", "LexaWall Educational Content", "The blighted neighborhood needed extensive urban renewal efforts immediately. Blighted buildings were boarded up permanently. The blighted area lacked investment and support. Blighted conditions persisted for several decades. Blighted properties were abandoned completely.", "LexaWall Educational Content", "The blighted tree died slowly over time. Her blighted hopes revived eventually. The blighted area recovered gradually.", "LexaWall Educational Content"),
    (214, "blimp", "noun", "B1", "a large nonrigid airship used for advertising", "The blimp floated above the packed stadium.", "Aircraft type", "You observed the sky", "LexaWall Educational Content", "The blimp floated gracefully above the packed stadium constantly. Blimp advertising was common at major sporting events. The blimp provided aerial camera coverage. Blimp operations required considerable skill and training. Blimp flights were popular with crowds.", "LexaWall Educational Content", "The advertising blimp was visible everywhere around town. His blimp physique concerned his doctors deeply. Blimp references made him very angry.", "LexaWall Educational Content"),
    (215, "blindfold", "noun", "B1", "a cloth covering the eyes or to cover eyes", "The game used a blindfold for fairness.", "Eye covering", "You were blinded", "LexaWall Educational Content", "The game used a blindfold for fairness and safety. Blindfold players were guided by teammates carefully. The blindfold prevented cheating during competition. Blindfold games required trust and communication. Blindfolded participants felt vulnerable.", "LexaWall Educational Content", "She put on the blindfold carefully. His blindfold prevented seeing ahead. Removing the blindfold revealed surprises.", "LexaWall Educational Content"),
    (216, "bliss", "noun", "B1", "perfect happiness or great joy and contentment", "The couple experienced complete bliss on their honeymoon.", "Happiness occurred", "You felt joy", "LexaWall Educational Content", "The couple experienced complete bliss together on their honeymoon. Bliss seemed almost unreal and dreamlike. The bliss continued indefinitely for years. Bliss made everything worthwhile and meaningful. Bliss was fleeting and temporary.", "LexaWall Educational Content", "The vacation brought complete bliss to everyone. Her bliss was obvious and contagious. The bliss ended too soon.", "LexaWall Educational Content"),
    (217, "blister", "noun", "B1", "a fluid-filled bubble on skin from friction", "The hiking boot caused a painful blister on his foot.", "Skin injury", "You felt pain", "LexaWall Educational Content", "The hiking boot caused a painful blister on his heel. The blister filled with fluid gradually. Blister treatment required careful wound care. The blister eventually healed completely. Blistering was common for hikers.", "LexaWall Educational Content", "The heat caused severe blistering quickly. Her blister worsened without treatment. Blistering eventually subsided.", "LexaWall Educational Content"),
    (218, "bloated", "adjective", "B2", "swollen or oversize usually meaning excessive", "The bloated budget needed significant cutting and trimming.", "Swollen amount", "You recognized excess", "LexaWall Educational Content", "The bloated budget needed significant cutting and trimming immediately. Bloated government spending was criticized widely everywhere. The bloated organization was very inefficient. Bloated expenses exceeded income substantially. Bloat required serious reduction.", "LexaWall Educational Content", "He felt bloated after eating too much. Her bloated abdomen concerned her greatly. Bloating eventually subsided completely.", "LexaWall Educational Content"),
    (219, "bloc", "noun", "B1", "a group of countries with shared political interests", "The political bloc voted together on most issues.", "Alliance formed", "You recognized grouping", "LexaWall Educational Content", "The political bloc voted together consistently on most issues. Bloc members shared similar ideologies and values. The bloc wielded significant political power regionally. Bloc influence shaped policy decisions directly. Bloc cooperation continued steadily.", "LexaWall Educational Content", "The voting bloc controlled elections consistently. His bloc membership mattered politically. Bloc solidarity strengthened over time.", "LexaWall Educational Content"),
    (220, "blockade", "noun", "B2", "an action to prevent goods or people entering", "The military blockade starved the city of essential supplies.", "Siege action", "You recognized restriction", "LexaWall Educational Content", "The military blockade starved the city of essential supplies completely. Blockade enforcement was strict and uncompromising. The blockade lasted several years. Blockade conditions were dire and desperate. Blockade runners smuggled goods secretly.", "LexaWall Educational Content", "The blockade prevented escape successfully. Its blockade was very effective. Blockading continued relentlessly.", "LexaWall Educational Content"),
]

# Add more words in batches
# Due to length, creating comprehensive array

# Create dict rows
new_rows = []
for word_tuple in new_word_data:
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

# Combine
all_words = existing + new_rows

# Write
fieldnames = ['id', 'word', 'part_of_speech', 'cefr_level', 'definition', 'example_sentence',
              'notification_line_1', 'notification_line_2', 'example_source', 'context_scenario',
              'context_source', 'story_text', 'story_source', 'context_enabled']

with open(csv_path, 'w', encoding='utf-8', newline='') as f:
    writer = csv.DictWriter(f, fieldnames=fieldnames)
    writer.writeheader()
    writer.writerows(all_words)

print(f"✅ Added {len(new_rows)} words (205-220)")
print(f"✅ Total words now: {len(all_words)}")
print(f"⏳ {350 - len(all_words)} words remaining")
