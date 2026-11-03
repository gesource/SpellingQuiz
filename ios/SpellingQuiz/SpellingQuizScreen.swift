import SwiftUI

enum SpellingQuizState {
    case writing, correct, incorrect, finished
}

struct SpellingQuizScreen: View {
    let cards: [WordCard]
    let recognizer: SpellingRecognizer

    @State private var index = 0
    @State private var state: SpellingQuizState = .writing
    @State private var strokes: [[InkPoint]] = []
    @State private var padSize: CGSize = .zero
    @State private var recognizing = false
    @State private var errorMessage: String?

    @Environment(\.verticalSizeClass) private var verticalSizeClass

    var body: some View {
        if state == .finished {
            VStack {
                Text("終了")
                    .font(.largeTitle)
                Button("最初から") { startWriting(0) }
                    .padding(.top, 24)
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        } else {
            quizBody
        }
    }

    private var quizBody: some View {
        let card = cards[index]
        let writing = state == .writing
        let modelState = recognizer.modelState

        return VStack(spacing: 0) {
            Text(card.meaning)
                .font(.largeTitle)
                .frame(maxWidth: .infinity, maxHeight: .infinity)

            ZStack {
                VStack {
                    HandwritingPad(strokes: $strokes, enabled: writing) { size in
                        padSize = size
                    }
                    .border(Color.gray)
                    .allowsHitTesting(writing)
                    .frame(maxWidth: .infinity, maxHeight: .infinity)

                    if modelState == .failed {
                        Text("モデルのダウンロードに失敗しました")
                            .foregroundColor(.red)
                    }
                    if let errorMessage {
                        Text(errorMessage)
                            .foregroundColor(.red)
                    }

                    HStack(spacing: 16) {
                        Button("消す") {
                            strokes = []
                            errorMessage = nil
                        }
                        .disabled(!writing)

                        Button(modelState == .preparing ? "準備中" : "判定") {
                            judge()
                        }
                        .disabled(!writing || modelState != .ready || recognizing)
                    }
                    .padding(.top, 8)
                }
                .padding(16)

                if state == .correct || state == .incorrect {
                    resultOverlay(card: card)
                }
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
        }
    }

    private func resultOverlay(card: WordCard) -> some View {
        let correct = state == .correct
        let landscape = verticalSizeClass == .compact
        let markSize: CGFloat = landscape ? 56 : 96

        let result = VStack {
            if correct {
                Text("○")
                    .font(.system(size: markSize))
                    .foregroundColor(Color(red: 0.18, green: 0.49, blue: 0.20))
            } else {
                Text("×")
                    .font(.system(size: markSize))
                    .foregroundColor(Color(red: 0.78, green: 0.16, blue: 0.16))
                Text(card.spelling)
                    .font(.title2)
                    .foregroundColor(.black)
            }
        }

        let buttons = HStack(spacing: 16) {
            Button("次へ") { goNext() }
            if !correct {
                Button("やり直す") { startWriting(index) }
            }
        }

        return Group {
            if landscape {
                HStack(spacing: 32) {
                    result
                    buttons
                }
            } else {
                VStack(spacing: 16) {
                    result
                    buttons
                }
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity)
        .background(Color.white.opacity(0.9))
    }

    private func startWriting(_ newIndex: Int) {
        index = newIndex
        state = .writing
        strokes = []
        errorMessage = nil
    }

    private func goNext() {
        if index + 1 < cards.count {
            startWriting(index + 1)
        } else {
            state = .finished
            strokes = []
            errorMessage = nil
        }
    }

    private func judge() {
        errorMessage = nil
        let spelling = cards[index].spelling
        guard !strokes.isEmpty else {
            state = .incorrect
            return
        }
        recognizing = true
        recognizer.recognize(
            ink: toInk(strokes),
            width: Float(padSize.width),
            height: Float(padSize.height),
            onResult: { candidates in
                Task { @MainActor in
                    recognizing = false
                    state = matchesSpelling(candidates, spelling) ? .correct : .incorrect
                }
            },
            onError: { _ in
                Task { @MainActor in
                    recognizing = false
                    errorMessage = "認識に失敗しました"
                }
            }
        )
    }
}
