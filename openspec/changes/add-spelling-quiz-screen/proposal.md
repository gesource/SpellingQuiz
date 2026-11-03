# Proposal

## Why

英単語を覚えるときは、意味を見て綴りを自分で書くと効果が高い。そこで、日本語の意味を見て英単語を指で手書きし、アプリがその場で○×を判定する出題画面を Android と iOS の両方で作る。`android/` と `ios/` は空なので、今回の変更がアプリ全体の最初の機能になる。

## What Changes

- Android（Kotlin / Jetpack Compose）と iOS（Swift / SwiftUI）のプロジェクトを新しく作る。
- アプリに同梱する単語データ `word_cards.json`（WordCard の配列、サンプル5件）を追加し、ファイルに並んだ順に出題する。
- 出題画面 SpellingQuizScreen を追加する。状態は writing / correct / incorrect / finished の4つ。
  - writing: 画面の上半分に意味、下半分に手書き入力欄と「消す」「判定」ボタン
  - correct / incorrect: 意味と入力欄を残したまま、○× とボタンを重ねて表示する
  - finished: 「終了」と「最初から」ボタン（正解数は表示しない）
- 手書き入力欄 HandwritingPad を追加する。指でなぞった軌跡を描画し、座標と時刻を記録する。
- ML Kit Digital Ink Recognition の英語モデル（en-US）を使い、端末内で手書きを認識する。認識候補の上位5件のどれかが正解と一致すれば正解とする（大文字小文字と前後の空白は無視する）。
- 英語モデルは起動時に、未取得の場合だけダウンロードする（暫定仕様）。完了するまで「判定」は押せず「準備中」と表示し、失敗したら「モデルのダウンロードに失敗しました」と表示する。
- 照合関数 matchesSpelling のユニットテストを両プラットフォームに追加する。

## Capabilities

### New Capabilities

- `spelling-quiz`: 単語データの読み込み、出題順、画面状態（writing / correct / incorrect / finished）と状態遷移、各状態での表示とボタン操作、認識候補と正解の照合ルール
- `handwriting-recognition`: 指による手書き入力（描画・消去）、英語認識モデルの準備（ダウンロードと状態表示）、手書きの認識と失敗時の扱い

### Modified Capabilities

なし（既存の spec はない）

## Impact

- 新規コード
  - `android/` 一式（Gradle プロジェクト。`android create empty-activity` で作成）
  - `ios/` 一式（xcodegen の `project.yml` と CocoaPods の `Podfile`）
- 新規依存関係
  - Android: `com.google.mlkit:digital-ink-recognition:19.0.0`（minSdk 23 以上が必要）
  - iOS: `GoogleMLKit/DigitalInkRecognition` 9.0.0（CocoaPods、iOS 15.5 以上が必要。deployment target は 17.0 にする）
- 開発環境: iOS 側は `xcodegen` のインストールが必要。ML Kit iOS はシミュレータ向けに arm64 を除外しているので、Apple Silicon の Mac では x86_64（Rosetta）のシミュレータか実機で確認する。
- ネットワーク: 初回起動時にモデルのダウンロードで通信が発生する。
