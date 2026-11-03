import Testing
@testable import SpellingQuiz

struct MatchesSpellingTests {
    @Test("1位の候補が一致すれば正解")
    func firstCandidateMatches() {
        #expect(matchesSpelling(["apple", "appk"], "apple"))
    }

    @Test("5位の候補が一致すれば正解")
    func fifthCandidateMatches() {
        #expect(matchesSpelling(["a", "b", "c", "d", "apple"], "apple"))
    }

    @Test("6位以降でだけ一致する場合は不正解")
    func onlySixthCandidateMatchesIsIncorrect() {
        #expect(!matchesSpelling(["a", "b", "c", "d", "e", "apple"], "apple"))
    }

    @Test("大文字小文字と前後の空白を無視する")
    func ignoresCaseAndSurroundingWhitespace() {
        #expect(matchesSpelling([" Apple "], "apple"))
        #expect(matchesSpelling(["apple"], " APPLE "))
    }

    @Test("候補が空なら不正解")
    func emptyCandidatesIsIncorrect() {
        #expect(!matchesSpelling([], "apple"))
    }
}
