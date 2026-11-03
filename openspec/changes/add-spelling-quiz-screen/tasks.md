# Tasks

## 1. Android プロジェクトの準備

- [x] 1.1 `android create empty-activity --name=SpellingQuiz --application-id=jp.gesource.sample.spellingquiz --namespace=jp.gesource.sample.spellingquiz -o android` でプロジェクトを作り、`android/app/build.gradle.kts` があることと、minSdk が 23 以上であることを確認する
- [x] 1.2 `gradle/libs.versions.toml` と `app/build.gradle.kts` に `com.google.mlkit:digital-ink-recognition:19.0.0` を追加し、`./gradlew assembleDebug` が成功することを確認する（テンプレートのサンプル（Navigation3・DataRepository・ViewModel・MainScreen とそのテスト）と、それらにしか使わない依存は、ユーザーの了承を得て削除した）

## 2. Android の単語データと照合

- [x] 2.1 `app/src/main/assets/word_cards.json` にサンプル5件を作り、`WordCard.kt` に `WordCard` と `loadWordCards()` を実装する。`./gradlew assembleDebug` が成功することを確認する
- [x] 2.2 `SpellingRecognizer.kt` にトップレベル関数 `matchesSpelling(candidates, spelling)` を実装し、`app/src/test/.../MatchesSpellingTest.kt` を書く（1位一致・5位一致・6位のみ一致は false・大文字小文字と前後の空白の無視・空の候補は false）。`./gradlew testDebugUnitTest` が通ることを確認する

## 3. Android の手書き認識

- [x] 3.1 `SpellingRecognizer` にモデルの準備（`isModelDownloaded` を確認し、未取得なら `download`）と、準備状態（準備中 / 完了 / 失敗）の公開を実装する。ビルドが通ることを確認する
- [x] 3.2 `SpellingRecognizer` に `recognize`（`WritingArea` と空の preContext を渡し、候補の文字列リストかエラーを返す）を実装する。ビルドが通ることを確認する
- [x] 3.3 `HandwritingPad.kt` を実装する（`pointerInput` で点と時刻を記録し、`Canvas` で描画し、大きさを親に伝え、`Ink` に変換する）。ビルドが通ることを確認する

## 4. Android の出題画面

- [x] 4.1 `SpellingQuizScreen.kt` に `SpellingQuizState` と画面を実装し、`MainActivity.kt` から表示する。画面に含めるもの: writing / correct / incorrect / finished の表示とボタン操作、準備中・エラーの表示とエラーの消去、認識中の判定ボタンの無効化、ストロークが0本なら ML Kit を呼ばず incorrect にする処理、writing 以外では入力欄・「消す」・「判定」を無効にする処理、問題番号と状態の `rememberSaveable`。`./gradlew assembleDebug testDebugUnitTest` が成功することを確認する
- [x] 4.2 エミュレータか実機で起動し、spec のシナリオを手動で確認する（モデルの準備 → 手書き → 判定 → ○/× → やり直す／次へ → 終了 → 最初から）。あわせて次の3つも確認する: 何も書かずに判定すると × になること、○× の表示中に入力欄・「消す」・「判定」が反応しないこと、回転しても問題と状態が保たれること
- [x] 4.3 横向きのとき、結果表示（○×と正解）とボタンを横並びにする（`LocalConfiguration` の orientation で判定し、○×は 56sp）。`./gradlew assembleDebug` が成功し、実機を横向きにして correct と incorrect でボタンがすべて画面内に表示され、操作できることを確認する

## 5. iOS プロジェクトの準備

- [x] 5.1 `brew install xcodegen` を実行し、`ios/project.yml` を作る。アプリ `SpellingQuiz` は iOS 17.0、bundle ID `jp.gesource.sample.spellingquiz`。テスト `SpellingQuizTests` はアプリを `dependencies` に指定し、アプリをホストにする。`xcodegen generate` で `SpellingQuiz.xcodeproj` ができ、テストターゲットに `TEST_HOST` が設定されていることを確認する（`xcodebuild -showBuildSettings -target SpellingQuizTests` で見る）
- [x] 5.2 `ios/Podfile` を作る（`platform :ios, '17.0'`。アプリターゲットに `pod 'GoogleMLKit/DigitalInkRecognition', '9.0.0'` を指定し、その中に `SpellingQuizTests` を入れ子にして `inherit! :search_paths` を指定する）。`pod install` で `SpellingQuiz.xcworkspace` ができることを確認する。以降、`xcodegen generate` をやり直したときは、必ず続けて `pod install` を実行する

## 6. iOS の単語データと照合

- [x] 6.1 `ios/SpellingQuiz/word_cards.json` にサンプル5件を作り、`WordCard.swift` に `WordCard` と `loadWordCards()` を実装する。x86_64 のシミュレータ向けに `xcodebuild build` が成功することを確認する
- [x] 6.2 `SpellingRecognizer.swift` にトップレベル関数 `matchesSpelling(_:_:)` を実装し、`SpellingQuizTests/MatchesSpellingTests.swift` に Android と同じケースを書く。`xcodebuild test` が通ることを確認する

## 7. iOS の手書き認識

- [x] 7.1 `SpellingRecognizer`（`@Observable`）に、モデルの準備（`isModelDownloaded` の確認、`download`、成功・失敗の通知の受け取り）と準備状態を実装する。ビルドが通ることを確認する
- [x] 7.2 `SpellingRecognizer` に `recognize`（`WritingArea` と空の preContext を渡し、候補の文字列リストかエラーを返す）を実装する。ビルドが通ることを確認する
- [x] 7.3 `HandwritingPad.swift` を実装する（`DragGesture` で位置と時刻を記録し、`Canvas` で描画し、`GeometryReader` で大きさを取り、ML Kit の `Stroke` に変換する）。ビルドが通ることを確認する

## 8. iOS の出題画面

- [x] 8.1 `SpellingQuizScreen.swift` に `SpellingQuizState` と画面を実装し（Android と同じ挙動。ストロークが0本の扱い、writing 以外での `.disabled` / `.allowsHitTesting(false)`、エラーの消去、横向き（`verticalSizeClass == .compact`）での結果表示の横並びを含む）、`SpellingQuizApp.swift` で起動時に `prepareModel()` を呼んで表示する。`xcodebuild build test` が成功することを確認する
- [x] 8.2 x86_64 のシミュレータ（動かなければ実機）で起動し、spec のシナリオを手動で確認する（モデルの準備 → 手書き → 判定 → ○/× → やり直す／次へ → 終了 → 最初から）。あわせて次の3つも確認する: 何も書かずに判定すると × になること、○× の表示中に入力欄・「消す」・「判定」が反応しないこと、回転しても問題と状態が保たれること

## Workflow follow-up

- 実装とレビューが済んだら change をアーカイブする（`/opsx:archive`）。
