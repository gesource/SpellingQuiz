# SpellingQuiz（iOS）

英単語手書きクイズの iOS 版です。Swift 6 と SwiftUI で書いており、手書きの認識には ML Kit Digital Ink Recognition を使います。アプリ全体の説明は [ルートの README](../README.md) を参照してください。

## 実装の状況

実装・実機での動作確認ともに完了しています。

| 部分 | 状況 |
|---|---|
| プロジェクト（xcodegen + CocoaPods） | 完了 |
| 単語データ `WordCard` / `loadWordCards()` | 完了 |
| 照合 `matchesSpelling()` とそのテスト | 完了。実機で `xcodebuild test` が通ることを確認済み |
| 英語モデルの準備と認識（`SpellingRecognizer`） | 完了 |
| 手書き入力欄（`HandwritingPad`） | 完了 |
| 出題画面（`SpellingQuizScreen`） | 完了。実機で spec のシナリオを手動確認済み |

## 構成

| ファイル | 役割 |
|---|---|
| `project.yml` | xcodegen の定義。`SpellingQuiz.xcodeproj` はここから生成する |
| `Podfile` | ML Kit（`GoogleMLKit/DigitalInkRecognition` 9.0.0）の導入 |
| `SpellingQuiz/SpellingQuizApp.swift` | アプリの入口 |
| `SpellingQuiz/WordCard.swift` | 単語データ `WordCard` と、JSON を読み込む `loadWordCards()` |
| `SpellingQuiz/SpellingRecognizer.swift` | 英語モデルの準備・認識を行う `SpellingRecognizer` と、候補と正解を照合する `matchesSpelling()` |
| `SpellingQuiz/HandwritingPad.swift` | 手書き入力欄 `HandwritingPad`。指の軌跡を描画し、ML Kit の `Ink` に変換する |
| `SpellingQuiz/SpellingQuizScreen.swift` | 出題画面 `SpellingQuizScreen` と画面の状態 `SpellingQuizState` |
| `SpellingQuiz/word_cards.json` | 出題する単語 |
| `SpellingQuizTests/MatchesSpellingTests.swift` | `matchesSpelling()` のテスト（Swift Testing） |

## 必要なもの

- Xcode 16 以上（確認は Xcode 27.1 で行いました）
- CocoaPods 1.17.0 以上（理由は下の「CocoaPods のバージョンについて」を参照）
- xcodegen
- iPhone の実機（理由は下の「シミュレータについて」を参照）

```sh
brew install xcodegen
```

## セットアップ

このディレクトリ（`ios/`）で実行します。

```sh
xcodegen generate
```

```sh
pod install
```

以後は `SpellingQuiz.xcworkspace` を開いてください（`.xcodeproj` ではありません）。

`project.yml` を変更したときや、ファイルを追加・削除したときは、`xcodegen generate` でプロジェクトを作り直します。作り直すと `pod install` が書き込んだ設定が消えるので、続けて必ず `pod install` も実行してください。

## CocoaPods のバージョンについて

Xcode 27 が書き出すプロジェクトファイルの形式（`objectVersion = 77`）に、CocoaPods 1.15.2 系の `xcodeproj` gem が対応していません（`pod install` が `Unable to find compatibility version string for object version` エラーで失敗します）。CocoaPods 1.17.0 以上（`xcodeproj` 1.28.1 以上を含む）であれば読み込めます。デフォルトの `pod` コマンドが古いバージョンの場合は、次のようにバージョンを指定して実行してください。

```sh
gem install cocoapods -v 1.17.0
pod _1.17.0_ install
```

## シミュレータについて

ML Kit の iOS 版は、シミュレータ向けに Apple Silicon（arm64）用のバイナリを提供していません。一方、Xcode 27 では Intel（x86_64）のシミュレータを実行先に選べません。そのため、Apple Silicon の Mac では、アプリの実行もテストもシミュレータではできません（実機での実行・確認が必要です）。

シミュレータ向けのビルドが通るかどうかだけは、次のコマンドで確認できます。

```sh
xcodebuild -workspace SpellingQuiz.xcworkspace -scheme SpellingQuiz -sdk iphonesimulator -arch x86_64 build-for-testing
```

## 実機で動かす

`project.yml` に開発チーム（`DEVELOPMENT_TEAM: T77QARX2F2`、自動署名）を設定済みです。別の Apple ID / チームで動かす場合は、Xcode の Signing & Capabilities で両ターゲット（`SpellingQuiz` と `SpellingQuizTests`）それぞれの Team を選び直してください。

- Bundle Identifier は `jp.gesource.sample.spellingquiz.ios` です。無料の Personal Team では `jp.gesource.sample.spellingquiz`（末尾に `.ios` がない形）の登録が「他の開発者に使われていて登録できない」エラーになったため、`.ios` を付けて回避しています。
- Xcode の Signing & Capabilities 画面を開いた直後は、サーバーへの登録（App ID の作成）がまだ行われておらず `xcodebuild` から `No Account for Team` エラーになることがあります。その場合は Xcode 上で一度 Team のドロップダウンを選び直すと、登録が実行されてエラーが消えます（CLI だけでは再現できません）。
- 初回インストール後、端末の「設定」→「一般」→「VPN とデバイス管理」で開発者アプリ（Apple ID）を信頼しないと、アプリが起動できません（`Developer App Certificate is not trusted` エラー）。

## 設定の補足

- `Podfile` の `post_install` で、依存する pod の deployment target をすべて 17.0 にそろえています。依存 pod の指定（9.0〜12.0）のままだと、Xcode 27 の下限 15.0 を下回ってビルドエラーになるためです。
- `project.yml` で `ENABLE_USER_SCRIPT_SANDBOXING` を `NO` にしています。CocoaPods が追加するビルドスクリプトが、Xcode のスクリプトサンドボックスで失敗するのを防ぐためです。
- Swift の言語モードは 6 です（厳格な並行性チェックが有効）。`SpellingRecognizer` は ML Kit のコールバックが任意のスレッドから呼ばれるため `@unchecked Sendable` にしています。
- テストは Swift Testing で書いています。`matchesSpelling()` が ML Kit を使うファイルにあるため、テストターゲットはアプリをホストにして動かします。
- `HandwritingPad` には明示的に白背景（`.background(Color.white)`）を付けています。背景を指定しないと、端末がダークモードのときに黒で描いた手書きの線が背景色と同化して見えなくなるためです。
