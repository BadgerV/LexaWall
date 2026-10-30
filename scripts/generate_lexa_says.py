#!/usr/bin/env python3
"""
Generate the 'lexa_says' column for every word in the CSV.

Each entry is a short literary passage (4-6 sentences) that uses the word
naturally, as if excerpted from a novel or essay, attributed to '— Lexa Says'.

The passages read like real prose — no tutoring, no tips, just the word
living inside a story.
"""

import csv, random, os

SRC = os.path.join(os.path.dirname(__file__), "..",
                   "app", "src", "main", "assets", "lexawall_words_v2.csv")
DST = SRC

random.seed(42)

# ──────────────────────────────────────────────────────────
# Scene templates by part-of-speech.
# {w} = word, {d} = definition
# Each is a 4-6 sentence mini-passage that uses the word naturally.
# ──────────────────────────────────────────────────────────

NOUN_PASSAGES = [
    lambda w, d: f"The letter arrived on a Tuesday, folded twice and sealed with red wax. Inside was a single word she did not expect. It spoke of {w} — the kind that changes the air in a room. She read it three times, then set it on the windowsill where the light could find it. By evening, she understood what it meant to carry that knowledge quietly.",
    lambda w, d: f"He had seen {w} before, but never like this. It appeared at the edge of the market square, half-hidden beneath the morning fog. The old bookseller pointed at it and said nothing. Some things, he believed, explained themselves. The boy disagreed, but kept that opinion folded in his coat pocket.",
    lambda w, d: f"There was a word the professor used every autumn, when the campus turned amber and the lectures grew long. {w.capitalize()}, she called it. The students wrote it down but rarely understood. Years later, one of them would stand in a foreign city and finally feel its weight. That is how language works — it waits for you.",
    lambda w, d: f"The museum had a room no one visited. In it hung a painting that captured {w} in oils so dark they seemed to breathe. A guard once told me the artist had envisioned it in a dream. Whether or not that was true, the painting made you pause. And pausing, in a museum, is the whole point.",
    lambda w, d: f"She kept a journal of words she did not yet own. {w.capitalize()} was on page forty-three, underlined twice. Beside it she had written: 'heard on the train, spoken by a stranger to no one.' The journal was not organised. Neither was the world it tried to describe.",
    lambda w, d: f"In the village, they had a name for everything the city forgot. {w.capitalize()} was one such name. The elders used it when the river changed direction, when the sky turned the colour of iron. The children listened without understanding. Understanding, they were told, would come with weather.",
    lambda w, d: f"There are words you learn from books and words you learn from silence. {w.capitalize()} belonged to the second kind. My grandmother never defined it; she simply lived as though it were obvious. When I finally looked it up, the dictionary felt inadequate. Some meanings refuse to sit still on a page.",
    lambda w, d: f"The architect sketched {w} into the blueprint without explaining why. His assistant asked twice. Both times he answered with a different question. The building was completed in November, and everyone who entered it felt something they could not name. The architect smiled. That was the point.",
    lambda w, d: f"I found the word '{w}' circled in red in a library book, page two hundred and seven. Whoever had been there before me had also drawn an arrow to the margin, where they wrote simply: 'yes.' I never learned who they were. But I agreed with them completely. Some annotations are better than essays.",
    lambda w, d: f"The old woman at the cafe used {w} three times in one story, each time with a different weight. The first was casual, the second mournful, the third almost triumphant. Her audience — a single cat and a waiter pretending not to listen — missed none of it. Words, in the right mouth, become music.",
    lambda w, d: f"When the flood receded, the town discovered things it had forgotten. A bicycle. A wedding ring. And, scratched into the wall of the post office, the word '{w}.' No one claimed it. But everyone who read it stood a little longer than necessary. Some graffiti is not vandalism. Some graffiti is confession.",
    lambda w, d: f"His thesis was rejected for being 'too poetic,' which he took as a compliment. Chapter four was entirely about {w} — not its definition, but its texture, its sound, the way it sat differently in English than in Portuguese. The committee wanted data. He gave them a love letter. They were not amused, but they remembered it.",
]

VERB_PASSAGES = [
    lambda w, d: f"She decided to {w} on a Wednesday, which surprised no one who knew her well. Wednesdays were for decisions. Mondays were for regret, and Fridays for forgetting. She had built her entire life around this calendar. It worked, mostly, until it didn't.",
    lambda w, d: f"To {w} requires a kind of patience most people only pretend to have. He learned this in a workshop above a bakery, where the flour dust settled on everything like quiet snow. The instructor never raised her voice. She simply demonstrated, and the room paid attention. That was the year he stopped rushing.",
    lambda w, d: f"They used to {w} by the harbour, back when the harbour still smelled of salt and engine oil. It was not a graceful act. But it was honest, and in that town, honesty mattered more than grace. The old fishermen watched from their boats and nodded. Some things don't need to be beautiful to be true.",
    lambda w, d: f"The first time he tried to {w}, the result was a disaster. The second time was worse. By the fourth attempt, something shifted — not skill, exactly, but perspective. He stopped trying to be perfect and started trying to be present. The difference, it turned out, was everything.",
    lambda w, d: f"You cannot {w} without first understanding what you stand to lose. That was the lesson of the summer, written in sunburn and late-night conversations on a porch that faced the wrong direction. Nobody planned it. The best lessons rarely announce themselves. They just arrive, uninvited, and sit down.",
    lambda w, d: f"If you {w} with enough conviction, people will follow. She proved this in a boardroom on the ninth floor, with nothing but a whiteboard and an idea that had no business working. It worked anyway. Conviction, she told her team afterwards, is just stubbornness with better posture. They laughed, but they remembered.",
    lambda w, d: f"He wanted to {w}, but the timing was wrong. It was always wrong, according to someone. The trick, his mother once said, is to stop waiting for the timing and start trusting the impulse. He did, eventually, on an unremarkable afternoon. Nothing dramatic happened. That was the beauty of it.",
    lambda w, d: f"The instruction manual said to {w} gently, but she had never been gentle with anything that mattered. She pressed hard, leaned in, and let the edges blur. The result was imperfect and alive. Her teacher would have disapproved. Her teacher was not here.",
    lambda w, d: f"In the dream, she could {w} without consequence. She did it recklessly, joyfully, the way children run before they learn about gravity. When she woke, the feeling lingered — a phantom permission. She carried it with her all morning, and by noon, she had done the thing for real. Dreams, occasionally, are rehearsals.",
    lambda w, d: f"The manual never said you had to {w} alone, but everyone assumed so. It took a stranger in a yellow raincoat to prove otherwise. They met at a bus stop, and within ten minutes, the task was done. Neither exchanged names. Some collaborations are better anonymous.",
    lambda w, d: f"Her grandmother could {w} in three languages and apologise in none. It was a point of pride, passed down like the copper pot and the recipe for Sunday bread. The grandchildren inherited the skill unevenly. But they all inherited the stubbornness. That part was not optional.",
    lambda w, d: f"The sign on the door read: 'Do not {w} after midnight.' Nobody knew who had posted it, or why midnight specifically. The rule was broken on the first night, naturally. And then every night after. Some prohibitions exist only to confirm what people were already going to do.",
]

ADJ_PASSAGES = [
    lambda w, d: f"The morning was {w} in a way she had not expected. The light slanted differently, as if the sun had shifted its opinion overnight. She stood at the window with coffee growing cold in her hands. It was not a dramatic change. It was the kind only the attentive would notice.",
    lambda w, d: f"He described the city as {w}, and everyone in the room disagreed. That was the thing about him — his words never landed where you expected. But hours later, walking home through streets he had named so precisely, you understood. The word settled into the pavement like rain. You could not unsee it.",
    lambda w, d: f"The fabric was {w} to the touch, which the shopkeeper mentioned as though it were a selling point. Perhaps it was. She bought three metres and carried it home in a paper bag that tore at the corner. The walk was long, the sun was low, and the fabric against her arm felt like a decision she had already made.",
    lambda w, d: f"There was something {w} about the whole arrangement — the chairs angled just so, the candles lit at uneven heights. It looked accidental, but she knew better. Every detail had been argued over, reconsidered, and finally placed with the kind of care that disguises itself as ease. That is what mastery looks like from the outside.",
    lambda w, d: f"The silence between them was {w}, and neither tried to fill it. Some silences demand words; this one refused them. They sat on the bench until the streetlights came on, one by one, like slow applause. Then she stood, brushed the dust from her coat, and walked home. Nothing had been resolved. Everything had been understood.",
    lambda w, d: f"His writing was {w} — deliberately so. He trimmed every sentence until only the necessary bones remained. Critics called it cold. Readers called it honest. He called it Tuesday, because that was when he edited, and editing was where the real work lived.",
    lambda w, d: f"The landscape was unmistakably {w}, though no photograph could prove it. You had to stand there, boots in the mud, wind pulling at your collar, to feel what the word actually meant. She tried to explain it in a postcard. Four lines were not enough. She sent it anyway.",
    lambda w, d: f"They called the house {w}, but not unkindly. It had earned the word over decades — through storms, renovations, and one very stubborn wisteria vine that refused to grow where it was planted. The house did not mind the label. It had outlasted every opinion anyone had ever held about it.",
    lambda w, d: f"The meal was {w} in the best possible sense. Nothing competed for attention. The bread was warm, the olive oil was sharp, and every ingredient knew its place. She ate slowly, the way you read a sentence that deserves to be read twice. Simplicity, done well, is the hardest thing to imitate.",
    lambda w, d: f"Her voice was {w} that evening, as though the words themselves had been stored somewhere cool and dark before being released. The audience noticed. A few leaned forward. One closed their eyes. When she finished, the applause came not in a wave but in a breath — slow, collective, real.",
]

ADV_PASSAGES = [
    lambda w, d: f"She spoke {w}, as though the words themselves might bruise if handled carelessly. The room leaned in. Even the clock seemed to slow its ticking. When she finished, no one moved. It was the kind of quiet that means something has landed exactly where it should.",
    lambda w, d: f"He walked {w} through the corridor, aware that every door was slightly ajar. Someone had been here before him. The carpet held impressions of shoes larger than his own. He followed them to the end of the hall, where the window was open and the curtain moved like breath. Some exits explain themselves.",
    lambda w, d: f"The rain fell {w}, without the drama the forecast had promised. It was the kind of rain that makes you forget your umbrella on purpose. She stood under the awning and watched the street turn silver. A bus passed, half-empty. The city, for once, felt honest.",
    lambda w, d: f"They moved {w} through the crowd, two people who had learned long ago that speed is not the same as urgency. The market was loud, fragrant, full of hands reaching for things they did not need. They bought bread and plums and walked home in no particular hurry. The afternoon opened before them like an unread book.",
    lambda w, d: f"The conductor raised the baton {w}, and the orchestra understood. It was not a command — it was an invitation. The first note arrived like a door opening onto a garden you had forgotten existed. The audience held still. For three minutes and forty seconds, no one remembered where they had parked.",
    lambda w, d: f"She closed the book {w}, pressing the covers together as if to seal something inside. The story was finished but the feeling was not. She sat with it the way you sit with a guest who has said something important. Some books end on the last page. This one ended three days later, in the middle of a grocery store.",
]

DEFAULT_PASSAGES = [
    lambda w, d: f"There is a word that sits between knowing and feeling. {w.capitalize()} is that word. I first encountered it in a secondhand bookshop in Lisbon, underlined in blue ink by someone I would never meet. The margin note read: 'exactly this.' I bought the book for that sentence alone. The rest of it, I am still reading.",
    lambda w, d: f"The teacher wrote '{w}' on the blackboard and waited. Thirty seconds passed. Then a hand rose in the back row — not to answer, but to ask a better question. That, she said, is what this word does. It doesn't close a conversation. It opens one.",
    lambda w, d: f"Some words arrive in your life like weather — expected but still surprising. {w.capitalize()} came to me during a winter so mild it felt borrowed from another year. I was reading by a window, and the sentence stopped me mid-breath. The author had placed it perfectly, the way a stone is placed in a garden. You think it was always there.",
    lambda w, d: f"My father used the word '{w}' exactly once, at a dinner I almost did not attend. He said it quietly, between the salad and the main course, as though testing whether it still fit in his mouth. It did. The table went silent, not from shock but from recognition. Some words carry a family's entire history in two syllables.",
    lambda w, d: f"In translation, '{w}' loses something — a texture, a temperature. The French version is close but not the same. The Japanese rendering captures the feeling but not the shape. This is why we learn languages: not to decode, but to feel the difference between a word and its echo. {w.capitalize()} deserves to be felt in the original.",
    lambda w, d: f"The playwright crossed out the line, then wrote it again. The word '{w}' had to stay — it was the only honest thing in the scene. The actors would struggle with it, he knew. Good. Struggle is how a word stops being ink and starts being breath. Opening night proved him right.",
    lambda w, d: f"At seventeen, she tattooed '{w}' on her wrist in a language she was still learning. Her mother called it impulsive. Her teacher called it commitment. Twenty years later, she could conjugate it in four tenses and explain it in three. The tattoo had faded. The word had not.",
    lambda w, d: f"The crossword clue was: '{d}.' Seven letters. He stared at it for an hour before the answer arrived — not from thought, but from the smell of rain through an open window. {w.capitalize()}. Of course. Some answers hide in the senses, not the mind. He wrote it in pen, because he was certain, and because certainty deserves ink.",
]


def pick_passage(pos):
    p = pos.strip().lower()
    if p == "noun":
        return random.choice(NOUN_PASSAGES)
    elif p == "verb":
        return random.choice(VERB_PASSAGES)
    elif p == "adjective":
        return random.choice(ADJ_PASSAGES)
    elif p == "adverb":
        return random.choice(ADV_PASSAGES)
    else:
        return random.choice(DEFAULT_PASSAGES)


def main():
    rows = []
    with open(SRC, newline="", encoding="utf-8") as f:
        reader = csv.reader(f)
        header = next(reader)
        for row in reader:
            rows.append(row)

    if "lexa_says" not in header:
        header.append("lexa_says")

    lexa_col = header.index("lexa_says")

    for row in rows:
        while len(row) <= lexa_col:
            row.append("")

        word = row[1].strip()
        definition = row[4].strip()
        pos = row[2].strip()

        passage_fn = pick_passage(pos)
        passage = passage_fn(word, definition)
        # Append attribution
        passage += "\n— Lexa Says"

        row[lexa_col] = passage

    with open(DST, "w", newline="", encoding="utf-8") as f:
        writer = csv.writer(f)
        writer.writerow(header)
        writer.writerows(rows)

    print(f"✅  Generated lexa_says for {len(rows)} words → {DST}")
    # Show samples
    for i in [0, 5, 50, 100, 200]:
        if i < len(rows):
            print(f"\n{'='*60}")
            print(f"Word: {rows[i][1]}  ({rows[i][2]})")
            print(f"{rows[i][lexa_col]}")


if __name__ == "__main__":
    main()
