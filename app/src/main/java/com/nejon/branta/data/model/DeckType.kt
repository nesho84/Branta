package com.nejon.branta.data.model

// ---------------------------------------------------------------------------
// DECK TYPE ENUM
// - The kinds of decks; every card in a deck follows its deck's type.
// - Each type carries the label shown in the app.
// - Types with isAvailable = false are planned (TODO) and hidden from the "New Deck" picker.
// ---------------------------------------------------------------------------
enum class DeckType(
    val label: String,
    val isAvailable: Boolean = true    // false = planned, hidden from the "New Deck" picker
) {
    NOTE("📝 Note"),                   // Cards are just text to learn
    QUESTION_ANSWER("❓ Q&A"),         // Cards have a question and an answer

    // @TODO: Planned deck types, not built yet (hidden from the picker until done):

    // @TODO: True/False: statement + correct answer (isTrue), True/False buttons in review,
    //       show right/wrong + Next, count wrong answers (wrongCount), no swiping
    TRUE_FALSE("✅ True/False", isAvailable = false),

    // @TODO: Multiple choice: question + 3-4 options, one of them is right
    MULTIPLE_CHOICE("🔤 Multiple choice", isAvailable = false),

    // @TODO: Type the answer: the learner types the answer and the app checks it
    TYPE_ANSWER("⌨️ Type the answer", isAvailable = false),

    // @TODO: Picture: picture + word (needs image storage)
    PICTURE("🖼️ Picture", isAvailable = false),

    // @TODO: Listen: play a word or sound (needs audio or text-to-speech)
    LISTEN("🔊 Listen", isAvailable = false)
}