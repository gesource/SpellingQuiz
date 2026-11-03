import Foundation

struct WordCard: Decodable, Sendable {
    let meaning: String
    let spelling: String
}

func loadWordCards() -> [WordCard] {
    // 同梱ファイルが読めないのは開発時のミスなので、その場で停止させる
    guard let url = Bundle.main.url(forResource: "word_cards", withExtension: "json") else {
        fatalError("word_cards.json がバンドルにありません")
    }
    do {
        let data = try Data(contentsOf: url)
        return try JSONDecoder().decode([WordCard].self, from: data)
    } catch {
        fatalError("word_cards.json を読み込めません: \(error)")
    }
}
