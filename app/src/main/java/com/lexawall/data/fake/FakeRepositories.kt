package com.lexawall.data.fake

import com.lexawall.domain.model.*
import com.lexawall.domain.repository.*
import java.util.Calendar

object FakeWords {
    val ephemeral = Word(
        id = "ephemeral", text = "ephemeral", partOfSpeech = "Adjective", level = "B2",
        definition = "Lasting for a very short time.",
        exampleSentence = "Fashions are ephemeral, changing with every season.",
        pronunciation = "/əˈfem(ə)rəl/",
        etymology = "From Greek ephēmeros 'lasting only a day'."
    )
    val ambiguous = Word(
        id = "ambiguous", text = "Ambiguous", partOfSpeech = "Adjective", level = "B2",
        definition = "Open to more than one interpretation; having a double meaning.",
        exampleSentence = "The results of the experiment were ambiguous.",
        pronunciation = "/amˈbɪɡjuəs/",
        etymology = "From Latin ambiguus 'doubtful'."
    )
    val serendipity = Word(
        id = "serendipity", text = "Serendipity", partOfSpeech = "Noun", level = "C1",
        definition = "The occurrence of events by chance in a happy way.",
        exampleSentence = "A fortunate stroke of serendipity brought them together.",
        pronunciation = "/ˌsɛr.ənˈdɪp.ɪ.ti/",
        etymology = "Coined by Horace Walpole in 1754."
    )
    val resilient = Word(
        id = "resilient", text = "Resilient", partOfSpeech = "Adjective", level = "B2",
        definition = "Able to withstand or recover quickly from difficult conditions.",
        exampleSentence = "The community remained resilient after the storm.",
        pronunciation = "/rɪˈzɪl.i.ənt/",
        etymology = "From Latin resilire 'to recoil'."
    )
    val nuance = Word(
        id = "nuance", text = "Nuance", partOfSpeech = "Noun", level = "C1",
        definition = "A subtle difference in or shade of meaning, expression, or sound.",
        exampleSentence = "The nuances of the language are difficult to master.",
        pronunciation = "/ˈnjuː.ɑːns/",
        etymology = "From French nuer 'to shade'."
    )
    val allWords = listOf(ambiguous, ephemeral, serendipity, resilient, nuance)
    val flashcardWords = listOf(resilient, ephemeral, nuance, serendipity)
}

class FakeWordRepository : WordRepository {
    override fun getWallpaperWord(): Word = FakeWords.ephemeral
    override fun getWordById(id: String): Word =
        FakeWords.allWords.firstOrNull { it.id == id } ?: FakeWords.ephemeral
    override fun getFlashcardWords(): List<Word> = FakeWords.flashcardWords
}

class FakeQuizRepository : QuizRepository {
    override fun getQuizQuestions(): List<QuizQuestion> = listOf(
        QuizQuestion(word = FakeWords.ambiguous, options = listOf(
            "Open to more than one interpretation; having a double meaning.",
            "Clear and precise; without any doubt or confusion.",
            "Relating to the surrounding area or environment."
        ), correctIndex = 0),
        QuizQuestion(word = FakeWords.ephemeral, options = listOf(
            "Extremely large or great.",
            "Lasting for a very short time.",
            "Existing since the beginning of time."
        ), correctIndex = 1),
        QuizQuestion(word = FakeWords.serendipity, options = listOf(
            "A feeling of intense longing.",
            "A tendency to see the worst in things.",
            "The occurrence of events by chance in a happy way."
        ), correctIndex = 2),
        QuizQuestion(word = FakeWords.resilient, options = listOf(
            "Able to withstand or recover quickly from difficult conditions.",
            "Tending to give in to pressure easily.",
            "Showing no emotion or feeling."
        ), correctIndex = 0),
        QuizQuestion(word = FakeWords.nuance, options = listOf(
            "A loud and obvious statement.",
            "A subtle difference in meaning, expression, or sound.",
            "An extreme reaction to a situation."
        ), correctIndex = 1)
    )
    override fun getQuizResult(score: Int, total: Int): QuizResult = QuizResult(
        score = score, total = total,
        headline = when {
            score == total -> "Flawless."
            score >= total * 0.8 -> "Honestly impressive."
            score >= total * 0.5 -> "Solid effort."
            else -> "Keep going."
        }
    )
}

class FakeUserRepository : UserRepository {
    override fun getUserStats(): UserStats = UserStats(
        streakDays = 12, wordsMastered = 42, quizScore = 11, quizTotal = 12
    )
    override fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> "GOOD MORNING"
            hour < 17 -> "GOOD AFTERNOON"
            else -> "GOOD EVENING"
        }
    }
}

class FakeThemeRepository : ThemeRepository {
    override fun getThemes(): List<ThemeOption> = listOf(
        ThemeOption(id = "midnight", name = "Midnight", isActive = true),
        ThemeOption(id = "savanna", name = "Savanna"),
        ThemeOption(id = "lagoon", name = "Lagoon"),
        ThemeOption(id = "harmattan", name = "Harmattan"),
    )
}
