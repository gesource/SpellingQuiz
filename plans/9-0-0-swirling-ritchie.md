# 英単語手書きクイズ（Android / iOS）実装計画

## Context

日本語の意味を見て英単語を指で手書きし、ML Kit Digital Ink Recognition（en-US、端末内）で○×判定する出題画面を、Android（Kotlin / Compose）と iOS（Swift / SwiftUI）で実装する。`android/` と `ios/` は空のため、プロジェクト作成から行う。仕様を満たす最小限のコードとし、指定外の抽象化（DI、Repository、プロトコル抽象化、ViewModel）は入れない。

## 確定事項

| 項目 | Android | iOS |
|---|---|---|
| ID | `jp.gesource.sample.spellingquiz` | 同左（bundle ID） |
| ML Kit | `com.google.mlkit:digital-ink-recognition:19.0.0`（Gradle） | `pod 'GoogleMLKit/DigitalInkRecognition', '9.0.0'`（CocoaPods） |
| 最低 OS | テンプレート既定値（23 以上であることを確認。ML Kit 要件 API 23） | iOS 17.0（ML Kit 要件 15.5。`@Observable` 使用） |
| プロジェクト作成 | `android create empty-activity` | xcodegen（`brew install xcodegen`）＋ `project.yml` |
| 状態名 | `enum class SpellingQuizState { WRITING, CORRECT, INCORRECT, FINISHED }` | `enum SpellingQuizState { case writing, correct, incorrect, finished }` |

## 共通の画面挙動

- 上半分に meaning、下半分に HandwritingPad と「消す」「判定」ボタン。
- correct / incorrect は meaning と手書き欄を残したまま、○× とボタンを重ねて表示（オーバーレイ）。
  - correct: ○、「次へ」
  - incorrect: ×、正解 spelling、「次へ」「やり直す」
- 「やり直す」: ストロークを消して同じ問題の writing へ。
- 「次へ」: 次の問題の writing へ（ストロークも消去）。最後なら finished。
- finished: 「終了」と「最初から」（1問目の writing へ）。正解数は出さない。
- 判定: ストロークを Ink に変換し、`WritingArea(パッド幅, パッド高さ)` を付けて認識し、`matchesSpelling(上位5件, spelling)` で判定。候補が空なら incorrect。
- 認識中は「判定」を無効にする。
- 認識で例外が出たら、判定ボタンの上に「認識に失敗しました」を表示し、writing のままにする。
- モデル: 起動時に `isModelDownloaded` を確認し、未取得のときだけダウンロードする。完了まで「判定」を無効にし、「準備中」と表示する。失敗したら判定ボタンの上に「モデルのダウンロードに失敗しました」を表示する（再試行機能はなし）。
- `matchesSpelling(candidates, spelling)`: `candidates.take(5)` のいずれかについて、`trim().lowercase()` した値が一致すれば true。
- `word_cards.json` のサンプル5件: apple / big / dog / water / happy（meaning は日本語）。

## Android（`android/`）

1. `android create empty-activity --name=SpellingQuiz --application-id=jp.gesource.sample.spellingquiz --namespace=jp.gesource.sample.spellingquiz -o android`
2. `gradle/libs.versions.toml` に `mlkit-digital-ink = "19.0.0"` を追加し、`app/build.gradle.kts` に `implementation(libs.mlkit.digital.ink)` を追加する。minSdk が 23 以上であることを確認する。
3. `app/src/main/java/jp/gesource/sample/spellingquiz/` に次のファイルを置く。
   - `WordCard.kt`: `data class WordCard(val meaning: String, val spelling: String)` と `fun loadWordCards(context: Context): List<WordCard>`（`assets` を `org.json.JSONArray` で読む）
   - `SpellingRecognizer.kt`: `class SpellingRecognizer`
     - Compose の `mutableStateOf` でモデル状態（準備中 / 完了 / 失敗）を持つ。
     - `prepareModel()`: `RemoteModelManager.isModelDownloaded` を確認し、必要なら `download` する。
     - `recognize(ink, width, height, onResult, onError)`: `RecognitionContext.builder().setPreContext("").setWritingArea(WritingArea(w, h))` を付けて呼ぶ。Task のリスナーを使い、coroutines 系の依存は追加しない。
     - トップレベル関数 `matchesSpelling(candidates: List<String>, spelling: String): Boolean`
   - `HandwritingPad.kt`: `@Composable HandwritingPad(strokes, onStrokesChange, modifier)`。`pointerInput` + `awaitEachGesture` で `(x, y, uptimeMillis)` を集めて `Canvas` に描く。`onSizeChanged` でサイズを親に伝える。点のリストを `Ink` に変換する関数 `toInk()` も置く。
   - `SpellingQuizScreen.kt`: `SpellingQuizState` と `@Composable SpellingQuizScreen(cards, recognizer)`。index / state / strokes / padSize / recognizing / errorMessage を `remember` で持つ。
   - `MainActivity.kt`（テンプレートを変更）: `loadWordCards` と `SpellingRecognizer` を生成し、`prepareModel()` を呼んで `SpellingQuizScreen` を表示する。
   - `app/src/main/assets/word_cards.json`
4. テスト `app/src/test/java/jp/gesource/sample/spellingquiz/MatchesSpellingTest.kt`（JUnit4、テンプレートの依存関係を使う）
   - 1位一致、5位一致、6位一致は false、大文字小文字・前後空白の吸収、空の候補は false

## iOS（`ios/`）

1. `brew install xcodegen`
2. `ios/project.yml`: アプリターゲット `SpellingQuiz`（iOS 17.0、bundle ID `jp.gesource.sample.spellingquiz`、ソースは `SpellingQuiz/`、`word_cards.json` はリソースとして含める）と、ユニットテストターゲット `SpellingQuizTests` を定義する。`cd ios && xcodegen generate` を実行する。
3. `ios/Podfile`: `platform :ios, '17.0'`、アプリターゲットに `pod 'GoogleMLKit/DigitalInkRecognition', '9.0.0'` を書く。テストターゲットは `inherit! :search_paths`。`pod install` を実行し、以降は `SpellingQuiz.xcworkspace` を使う。
4. `ios/SpellingQuiz/` に次のファイルを置く。
   - `word_cards.json`
   - `WordCard.swift`: `struct WordCard: Decodable` と `func loadWordCards() -> [WordCard]`（`Bundle.main`）
   - `SpellingRecognizer.swift`: `@Observable final class SpellingRecognizer`
     - モデル状態を持つ。
     - `prepareModel()`: `ModelManager.isModelDownloaded` を確認し、必要なら `download` する。完了と失敗は `.mlkitModelDownloadDidSucceed` / `.mlkitModelDownloadDidFail` の通知で受け取る。
     - `recognize(strokes:width:height:completion:)`: `DigitalInkRecognitionContext(preContext: "", writingArea: WritingArea(width:height:))` を付ける。
     - トップレベル関数 `matchesSpelling(_ candidates: [String], _ spelling: String) -> Bool`
   - `HandwritingPad.swift`: `DragGesture(minimumDistance: 0)` の `onChanged` で `value.location` と `value.time` を記録し、`onEnded` でストロークを確定する。`Canvas` で `Path` を描く。サイズは `GeometryReader` で取得する。ML Kit の `Stroke` / `StrokePoint`（t はミリ秒）への変換関数も置く。
   - `SpellingQuizScreen.swift`: 状態は `@State` で持つ。画面構成は Android と同じ。
   - `SpellingQuizApp.swift`: `@main`。起動時に `prepareModel()` を呼ぶ。
5. テスト `ios/SpellingQuizTests/MatchesSpellingTests.swift`（XCTest。Android と同じケース）

## 検証

- Android
  - `cd android && ./gradlew assembleDebug testDebugUnitTest`（終了コードを確認する）
  - 可能ならエミュレータで起動し、モデルのダウンロード → 手書き → 判定まで確認する（`android` CLI）。
- iOS
  - `xcodebuild -workspace ios/SpellingQuiz.xcworkspace -scheme SpellingQuiz -destination 'platform=iOS Simulator,name=<端末>,arch=x86_64' build test`
  - ML Kit がシミュレータ向けに arm64 を除外しているため、Rosetta の x86_64 シミュレータを使う。動かない場合はその旨を報告し、実機での確認をユーザーに依頼する。
- 手書きの認識精度と、モデルのダウンロード（ネットワークが必要）は、実機またはエミュレータでの手動確認とする。
