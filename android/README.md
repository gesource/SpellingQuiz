# SpellingQuiz（Android）

英単語手書きクイズの Android 版です。Kotlin と Jetpack Compose で書いており、手書きの認識には ML Kit Digital Ink Recognition を使います。アプリ全体の説明は [ルートの README](../README.md) を参照してください。

## 構成

パッケージは `jp.gesource.sample.spellingquiz.android` です（iOS 版の Bundle Identifier `jp.gesource.sample.spellingquiz.ios` と命名を揃えています）。

| ファイル | 役割 |
|---|---|
| `app/src/main/kotlin/.../MainActivity.kt` | 単語データを読み込み、`SpellingRecognizer` を作ってモデルを準備し、出題画面を表示する |
| `app/src/main/kotlin/.../SpellingQuizScreen.kt` | 出題画面 `SpellingQuizScreen` と画面の状態 `SpellingQuizState`（`WRITING` / `CORRECT` / `INCORRECT` / `FINISHED`） |
| `app/src/main/kotlin/.../HandwritingPad.kt` | 手書き入力欄 `HandwritingPad`。指の軌跡を描画し、ML Kit の `Ink` に変換する |
| `app/src/main/kotlin/.../SpellingRecognizer.kt` | 英語モデルの準備と認識を行う `SpellingRecognizer`、候補と正解を照合する `matchesSpelling()` |
| `app/src/main/kotlin/.../WordCard.kt` | 単語データ `WordCard` と、JSON を読み込む `loadWordCards()` |
| `app/src/main/assets/word_cards.json` | 出題する単語 |
| `app/src/test/kotlin/.../MatchesSpellingTest.kt` | `matchesSpelling()` のユニットテスト |

ViewModel や Repository は使っていません。画面の状態は `SpellingQuizScreen` の中で `remember` / `rememberSaveable` を使って持っています。

## 必要なもの

- JDK 17 以上
- Android SDK（compileSdk 37、minSdk 24）

`local.properties` に Android SDK の場所（`sdk.dir`）を書いてください。Android Studio で開くと自動で作られます。

## ビルドとテスト

このディレクトリ（`android/`）で実行します。

```sh
./gradlew assembleDebug
```

```sh
./gradlew testDebugUnitTest
```

## 端末で動かす

USB で接続した端末か、起動中のエミュレータにインストールします。

```sh
./gradlew installDebug
```

初回起動時に英語モデルをダウンロードするので、端末をネットワークにつないでおいてください。ダウンロードが終わるまで「判定」ボタンは「準備中」になります。Pixel 6 では約 12 秒かかりました。

## 単語を変更する

`app/src/main/assets/word_cards.json` を編集します。`meaning` が画面に出す日本語、`spelling` が正解の英単語です。ファイルに並んだ順に出題します。

```json
[
  { "meaning": "りんご", "spelling": "apple" }
]
```

## 動作確認の状況

Pixel 6（Android 17）の実機で、次のことを確認しました。

- モデルのダウンロード中は「準備中」になり、終わると「判定」が押せる
- 「apple」と書いて判定すると ○ になる（縦向き・横向きとも）
- 何も書かずに判定すると × になり、正解と「次へ」「やり直す」が表示される
- ○×の表示中は、入力欄・「消す」・「判定」が反応しない
- やり直す、次へ、出題順、終了画面、最初から
- 画面を回転しても、出題中の問題と状態が保たれる（書いた線は消える）
- 横向きのときは、結果とボタンが横に並ぶ

## 補足

- プロジェクトは `android create empty-activity` で作りました。テンプレートに入っていたサンプル（Navigation3、ViewModel、Repository など）とその依存は削除しています。
- ビルドすると「Android Gradle plugin 9.0.1 は compileSdk 36.1 までしか検証されていない」という警告が出ます。compileSdk 37 はテンプレートが設定した値で、ビルドと実機での動作に問題はありません。
