import SwiftUI

@main
struct SpellingQuizApp: App {
    private let cards = loadWordCards()
    private let recognizer = SpellingRecognizer()

    init() {
        recognizer.prepareModel()
    }

    var body: some Scene {
        WindowGroup {
            SpellingQuizScreen(cards: cards, recognizer: recognizer)
        }
    }
}
