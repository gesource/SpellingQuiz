# Design

## Context

- `android/` と `ios/` は空。今回の変更でプロジェクトを作る（動機は proposal.md の Why を参照）。
- 要件は `specs/spelling-quiz/spec.md` と `specs/handwriting-recognition/spec.md` を参照。
- 開発環境: Xcode 27.2、CocoaPods 1.16.2、`android` CLI 1.1（テンプレート `empty-activity`。Compose と AGP 9 を使う）、JDK 21。Mac は Apple Silicon で、Rosetta が入っている。
- 依頼者の指示: 仕様を満たす最小限のコードにし、指定のない抽象化（インターフェース／プロトコル、DI、Repository、ViewModel）は加えない。命名は指定の名前（WordCard、SpellingQuizScreen、SpellingQuizState、HandwritingPad、SpellingRecognizer、matchesSpelling、loadWordCards）を使う。

## Goals / Non-Goals

**Goals:**
- 両プラットフォームで同じ画面挙動と同じ照合ルールにする。
- `matchesSpelling` を純粋関数にし、UI を動かさずにユニットテストできるようにする（iOS ではテスト時も ML Kit のリンクが必要。「テスト」の項を参照）。

**Non-Goals:**
- 正解数の集計、出題のランダム化、学習履歴の保存。
- モデルダウンロードの再試行 UI（暫定仕様のため）。
- 画面遷移のフレームワーク（Navigation など）。画面は1つだけ。
- 手書き画面の UI テストや E2E テスト（手動で確認する）。

## Decisions

### プロジェクトの作成
- Android は `android create empty-activity --name=SpellingQuiz --application-id=jp.gesource.sample.spellingquiz --namespace=jp.gesource.sample.spellingquiz -o android` で作る。公式 CLI のテンプレートで、Gradle の設定を一から書かずに済む。minSdk はテンプレートの既定値を使い、23 以上であることを確認する。
- iOS は xcodegen の `project.yml` から `.xcodeproj` を生成する。採らなかった案: `project.pbxproj` を手書きする（壊れやすい）、Xcode の GUI で作る（再現しにくい）。生成物の `.xcodeproj` は `project.yml` から作り直せる。
- アプリ ID は、最終的に Android が `jp.gesource.sample.spellingquiz.android`、iOS が `jp.gesource.sample.spellingquiz.ios` （末尾にプラットフォーム名を付ける形）になっている。当初はどちらも `jp.gesource.sample.spellingquiz` だった。iOS では、無料の Apple ID（Personal Team）でこの ID のまま自動署名を試みたところ「他の開発者に使われていて登録できない」エラーになったため `.ios` を付けて回避し、Android 側も命名を揃えるために `.android` を付けて実装後にリネームした。

### 依存関係の導入
- Android: `com.google.mlkit:digital-ink-recognition:19.0.0` を version catalog（`gradle/libs.versions.toml`）経由で追加する。非同期処理は ML Kit の `Task` のリスナーで書き、`kotlinx-coroutines-play-services` は追加しない（依存を最小にするため）。
- iOS: CocoaPods の `pod 'GoogleMLKit/DigitalInkRecognition', '9.0.0'`。公式の導入方法が CocoaPods だけで、Swift Package Manager は公式に記載がない。8.0.0 ではなく 9.0.0 を選ぶのは、App Store の審査でリジェクトされる原因（ドメインへの参照）が修正されているため。deployment target は 17.0（ML Kit の要件 15.5 を満たし、直近3年の iOS を対象にでき、`@Observable` が使える）。

### 状態の持ち方
- 画面の状態は `SpellingQuizState`（Kotlin: `WRITING / CORRECT / INCORRECT / FINISHED` の enum class、Swift: `writing / correct / incorrect / finished` の enum）と、問題番号・ストローク・認識中フラグ・エラーメッセージで表す。これらは画面の中で持つ（Compose は `remember { mutableStateOf }`、SwiftUI は `@State`）。採らなかった案: ViewModel（指定外の抽象化になるので使わない）。
- Android で画面を回転しても進行状況が消えないように、問題番号と `SpellingQuizState` は `rememberSaveable` で持つ（enum は Bundle に保存できる）。ストロークは `remember` のままにし、回転すると消える（spec で許容している）。縦画面に固定する案は、端末の使い方を制限するので採らない。SwiftUI の `@State` は回転しても保たれるので、iOS では対応は不要。
- モデルの準備状態（準備中 / 完了 / 失敗）は `SpellingRecognizer` が持ち、画面はそれを観察する（Compose は `mutableStateOf`、Swift は `@Observable`）。`SpellingRecognizer` はアプリ起動時に1つだけ作り、画面に渡す。

### 認識と照合
- `SpellingRecognizer` は en-US の `DigitalInkRecognitionModel` を持ち、`prepareModel()` で `isModelDownloaded` を確認し、未取得なら `download` する。iOS ではダウンロードの完了・失敗を `NotificationCenter` の `.mlkitModelDownloadDidSucceed` / `.mlkitModelDownloadDidFail` で受け取る。
- 認識時は `WritingArea(パッドの幅, パッドの高さ)` と空の preContext を渡す。座標とパッドの大きさは同じ単位で扱う（Android は px、iOS は pt）。
- `matchesSpelling(candidates, spelling)` はトップレベルの純粋関数にし、`SpellingRecognizer` と同じファイルに置く。候補の先頭5件それぞれを `trim` して小文字にし、同じく正規化した spelling と比べる。
- ストロークが0本のときは ML Kit を呼ばず、候補が空とみなして incorrect にする。空の Ink を渡したとき ML Kit が空の候補を返すのかエラーになるのかは、公式ドキュメントで確認できていないため。こうしておけば、ML Kit の挙動に左右されない。
- 認識に成功したら、候補の文字列リスト（空でもよい）を `matchesSpelling` に渡して correct か incorrect を決める。認識が失敗（例外やエラー）したら、エラーメッセージを入れて writing のままにする。
- エラーメッセージを消すタイミングは `specs/handwriting-recognition/spec.md` の「認識処理の失敗を知らせる」に従う。消すのはエラーメッセージ用の状態を null（nil）に戻す処理で、「判定」「消す」と、問題が変わる操作のそれぞれで行う。

### 手書き入力
- Android: `Modifier.pointerInput` + `awaitEachGesture` で down → move → up を受け取り、`(x, y, uptimeMillis)` を記録する。描画は `Canvas` の `drawPath`。入力欄の大きさは `onSizeChanged` で取得する。
- iOS: `DragGesture(minimumDistance: 0)` の `onChanged` で `value.location` と `value.time` を記録し、`onEnded` でストロークを確定する。描画は `Canvas`。大きさは `GeometryReader` で取得する。ML Kit の `StrokePoint` に渡す t は `time.timeIntervalSince1970 * 1000` のミリ秒。
- ストロークの点のリストは画面の状態として持ち、「消す」「やり直す」「次へ」「最初から」で空にする。
- correct / incorrect では、画面の下半分（入力欄と「消す」「判定」）全体の上に ○× とボタンを `Box`／`ZStack` で重ねて表示する。さらに、入力欄のジェスチャーと「消す」「判定」は、状態が writing のときだけ有効にする（Compose は `enabled` と、ジェスチャー処理の中での状態チェック、SwiftUI は `.disabled` と `.allowsHitTesting`）。重ねて表示するだけでは、下のボタンにタッチが届く可能性があるため。
- 横向きでは下半分の高さが足りず、縦に並べるとボタンが画面外に出る（Pixel 6 の実機で確認した）。そのため、横向きのときだけ、結果（○×と正解の spelling）を左、ボタンを右に横並びにし、○×の文字を 96sp（pt）から 56sp（pt）に小さくする。判定には画面の向きを使う（Android は `LocalConfiguration.current.orientation`、iOS は `verticalSizeClass == .compact`）。iOS も同じレイアウトにする。採らなかった案: 縦画面に固定する（端末の使い方を制限する）、結果表示をスクロールできるようにする（ボタンがすぐに見えない）。

### テスト
- `matchesSpelling` のユニットテストを、Android は JUnit4（`app/src/test`）、iOS は Swift Testing（`import Testing`、`SpellingQuizTests`）で書く。iOS は Swift 6 の言語モード（厳格な並行性チェック）でビルドする。
- iOS の `matchesSpelling` は ML Kit を import するファイルにあるので、テストターゲットはアプリをホストにする（xcodegen の test target に、アプリを `dependencies` として指定し、`TEST_HOST` / `BUNDLE_LOADER` が設定されるようにする）。Podfile では、テストターゲットをアプリターゲットの中に入れ子にし、`inherit! :search_paths` を指定する。テストからは `@testable import SpellingQuiz` で参照する。
- Android の JVM ユニットテストでは、`matchesSpelling` が ML Kit のクラスを参照しないので、ML Kit の初期化なしで実行できる。

### 単語データ
- Android は `assets/word_cards.json` を `org.json.JSONArray` で読む（シリアライズ用ライブラリは追加しない）。iOS は `Bundle.main` から読み、`JSONDecoder` で `[WordCard]` にデコードする。
- サンプル5件: りんご/apple、大きい（サイズが）/big、犬/dog、水/water、幸せな/happy。

## Risks / Trade-offs

- [ML Kit iOS がシミュレータ向けに arm64 を除外している] → x86_64（Rosetta）のシミュレータでビルドと確認をする。動かなければ実機で確認する。
- [モデルのダウンロードにはネットワークが必要で、失敗すると判定できない] → 暫定仕様としてエラー表示だけにとどめる。再起動すると再びダウンロードを試みる。
- [手書きの認識精度は書き方に左右される] → 上位5件まで正解として扱い、取りこぼしを減らす。
- [`xcodegen generate` は `.xcodeproj` を丸ごと作り直すので、`pod install` が書き込んだ設定が消える] → `project.yml` を変えて `xcodegen generate` をやり直したら、必ず続けて `pod install` を実行する。開発には `.xcworkspace` を使う。
- [CocoaPods trunk は今後、読み取り専用になる予定] → 既存の pod は引き続きインストールできる見込み。ML Kit が SPM に対応したら移行を検討する。
- [Android の `android create` テンプレートの内容は CLI の版で変わりうる] → 生成後に minSdk と依存関係を確認してから実装に進む。
