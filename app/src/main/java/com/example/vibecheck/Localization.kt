package com.example.vibecheck

object Localization {
    data class UiStrings(
        val appSubtitle: String,
        val selectLanguage: String,
        val refreshButton: String,
        val loading: String,
        val fetching: String,
        val trendReasonLabel: String,
        val trendRatingLabel: String,
        val shareText: (String, String) -> String,
        val shareChooserTitle: String,
        val languageNames: Map<String, String>
    )

    private val commonLanguageNames = mapOf(
        "Russian" to "Русский",
        "English" to "English",
        "Spanish" to "Español",
        "German" to "Deutsch",
        "French" to "Français"
    )

    private val russian = UiStrings(
        appSubtitle = "Твой ежедневный гид по сленгу",
        selectLanguage = "Язык перевода:",
        refreshButton = "Обновить слово дня сейчас",
        loading = "Загрузка...",
        fetching = "Получение сленга...",
        trendReasonLabel = "Трендовость:",
        trendRatingLabel = "Рейтинг:",
        shareText = { word, trans -> "Слово дня от VibeCheck: $word — $trans" },
        shareChooserTitle = "Поделиться через",
        languageNames = commonLanguageNames
    )

    private val english = UiStrings(
        appSubtitle = "Your daily guide to slang",
        selectLanguage = "Translation language:",
        refreshButton = "Update word of the day now",
        loading = "Loading...",
        fetching = "Fetching slang...",
        trendReasonLabel = "Trending because:",
        trendRatingLabel = "Rating:",
        shareText = { word, trans -> "Word of the Day from VibeCheck: $word — $trans" },
        shareChooserTitle = "Share via",
        languageNames = commonLanguageNames
    )

    private val spanish = UiStrings(
        appSubtitle = "Tu guía diaria de argot",
        selectLanguage = "Idioma de traducción:",
        refreshButton = "Actualizar palabra del día ahora",
        loading = "Cargando...",
        fetching = "Buscando argot...",
        trendReasonLabel = "Tendencia por:",
        trendRatingLabel = "Calificación:",
        shareText = { word, trans -> "Palabra del día de VibeCheck: $word — $trans" },
        shareChooserTitle = "Compartir vía",
        languageNames = commonLanguageNames
    )

    private val german = UiStrings(
        appSubtitle = "Dein täglicher Slang-Guide",
        selectLanguage = "Übersetzungssprache:",
        refreshButton = "Wort des Tages jetzt aktualisieren",
        loading = "Laden...",
        fetching = "Slang abrufen...",
        trendReasonLabel = "Trendig weil:",
        trendRatingLabel = "Bewertung:",
        shareText = { word, trans -> "Wort des Tages von VibeCheck: $word — $trans" },
        shareChooserTitle = "Teilen",
        languageNames = commonLanguageNames
    )

    private val french = UiStrings(
        appSubtitle = "Votre guide quotidien de l'argot",
        selectLanguage = "Langue de traduction :",
        refreshButton = "Mettre à jour le mot du jour maintenant",
        loading = "Chargement...",
        fetching = "Récupération de l'argot...",
        trendReasonLabel = "Tendance car :",
        trendRatingLabel = "Note :",
        shareText = { word, trans -> "Mot du jour de VibeCheck : $word — $trans" },
        shareChooserTitle = "Partager via",
        languageNames = commonLanguageNames
    )

    fun getStrings(language: String): UiStrings {
        return when (language) {
            "English" -> english
            "Spanish" -> spanish
            "German" -> german
            "French" -> french
            else -> russian
        }
    }
}
