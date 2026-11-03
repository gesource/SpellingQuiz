# SpellingQuiz

英単語を暗記するためのアプリです。日本語の意味を見て、対応する英単語を指で手書きすると、アプリが正解（○）か不正解（×）かを判定します。手書きの認識には ML Kit Digital Ink Recognition の英語モデル（en-US）を使い、認識は端末内で行います。

Android 版と iOS 版があります。

| | Android | iOS |
|---|---|---|
| 言語 / UI | Kotlin / Jetpack Compose | Swift 6 / SwiftUI |
| ML Kit | `com.google.mlkit:digital-ink-recognition:19.0.0`（Gradle） | `GoogleMLKit/DigitalInkRecognition` 9.0.0（CocoaPods） |
| 最低 OS | Android 7.0（API 24） | iOS 17.0 |
| 状況 | 実装済み | 実装済み |

## 画面の動き

- 上半分に日本語の意味、下半分に手書きの入力欄と「消す」「判定」ボタンを表示します。
- 「判定」を押すと、認識候補の上位5件のどれかが正解と一致すれば ○、しなければ × を表示します。大文字小文字と前後の空白は無視します。何も書かずに判定した場合は × になります。
- × のときは正解の綴りを表示し、「次へ」か「やり直す」を選べます。
- 最後の問題の後は「終了」と「最初から」を表示します。正解数は表示しません。
- 初回起動時に英語モデルをダウンロードします（2回目以降は不要）。ダウンロードが終わるまで、「判定」ボタンは「準備中」になります。

詳しい仕様は `openspec/changes/add-spelling-quiz-screen/specs/` にあります。

## ディレクトリ構成

```
android/   Android アプリ（Gradle プロジェクト）
ios/       iOS アプリ（xcodegen の project.yml と CocoaPods）
openspec/  仕様と設計（OpenSpec）
```

出題する単語は、各アプリに同梱した `word_cards.json` に書きます。ファイルに並んだ順に出題します。

- Android: `android/app/src/main/assets/word_cards.json`
- iOS: `ios/SpellingQuiz/word_cards.json`

```json
[
  { "meaning": "りんご", "spelling": "apple" },
  { "meaning": "大きい（サイズが）", "spelling": "big" }
]
```

## ビルドと実行

ビルド・テスト・実行の手順は、各ディレクトリの README を参照してください。

- Android: [android/README.md](android/README.md)
- iOS: [ios/README.md](ios/README.md)。Apple Silicon の Mac では、ML Kit の制約によりシミュレータで動かせないため、iPhone の実機が必要です

## 注意事項

- ML Kit は、端末内で処理した手書きのデータを Google に送りません。ただし、性能や利用状況のメトリクスは Google に送ります。アプリを公開する場合は、利用者にそのことを知らせる必要があります（[ML Kit Terms](https://developers.google.com/ml-kit/terms)）。
- 英語モデルのダウンロードにはネットワーク接続が必要です。ダウンロードに失敗した場合は「モデルのダウンロードに失敗しました」と表示されます。アプリを起動し直すと、もう一度ダウンロードを試みます。

## 仕様と設計

仕様・設計・タスクは OpenSpec で管理しています。

- 提案: `openspec/changes/add-spelling-quiz-screen/proposal.md`
- 仕様: `openspec/changes/add-spelling-quiz-screen/specs/`
- 設計: `openspec/changes/add-spelling-quiz-screen/design.md`
- タスク: `openspec/changes/add-spelling-quiz-screen/tasks.md`
