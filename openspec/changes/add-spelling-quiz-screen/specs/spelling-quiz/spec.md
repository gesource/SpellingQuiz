# Spec Delta

## Purpose

日本語の意味を見て英単語を手書きし、その綴りが正しいかをその場で判定して、同梱の単語を順番に出題する暗記クイズの出題画面と判定ルールを定める。

## ADDED Requirements

### Requirement: 同梱の単語データを順番に出題する
アプリは同梱の単語データ（意味 meaning と綴り spelling の組の配列）を読み込み、ファイルに並んだ順に1問ずつ出題しなければならない（SHALL）。1問につき正解の綴りは1つとする。

#### Scenario: 1問目を表示する
- **WHEN** アプリを起動する
- **THEN** 単語データの先頭の meaning が出題される

#### Scenario: ファイル順に進む
- **GIVEN** 単語データが「りんご/apple」「大きい（サイズが）/big」の順に並んでいる
- **WHEN** 1問目で「次へ」を押す
- **THEN** 2問目として「大きい（サイズが）」が出題される

### Requirement: 回答中（writing）の画面
writing 状態では、画面の上半分に meaning を、下半分に手書き入力欄と「消す」「判定」ボタンを表示しなければならない（SHALL）。

#### Scenario: 回答中の表示
- **WHEN** 問題が writing 状態になる
- **THEN** 上半分に meaning、下半分に手書き入力欄・「消す」・「判定」が表示される
- **AND** 手書き入力欄は空である

### Requirement: 判定により correct か incorrect に移る
「判定」を押すと、手書きを認識し、その候補を正解の spelling と照合して、一致すれば correct、一致しなければ incorrect に移らなければならない（SHALL）。認識候補が空の場合は incorrect とする。

#### Scenario: 正しく書けた
- **GIVEN** 正解が "apple" である
- **WHEN** 「apple」と書いて「判定」を押し、候補に "apple" が含まれる
- **THEN** correct に移る

#### Scenario: 間違って書いた
- **GIVEN** 正解が "apple" である
- **WHEN** 「appel」と書いて「判定」を押し、上位5件の候補に "apple" が含まれない
- **THEN** incorrect に移る

#### Scenario: 何も書かずに判定した
- **WHEN** 入力欄が空のまま「判定」を押す
- **THEN** 認識エラーにはならず、incorrect に移る

### Requirement: 認識候補と正解の照合ルール
照合では、認識候補の上位5件のいずれかが正解の spelling と一致した場合に正解としなければならない（SHALL）。比較の前に、候補と spelling の両方を小文字にし、前後の空白を取り除く。6件目以降の候補は照合に使わない。

#### Scenario: 1位の候補が一致する
- **WHEN** 候補が ["apple", "appk"]、正解が "apple"
- **THEN** 正解と判定される

#### Scenario: 5位の候補が一致する
- **WHEN** 候補の5番目が "apple"、正解が "apple"
- **THEN** 正解と判定される

#### Scenario: 6位以降でだけ一致する
- **WHEN** 候補の6番目だけが "apple"、正解が "apple"
- **THEN** 不正解と判定される

#### Scenario: 大文字小文字と前後の空白を無視する
- **WHEN** 候補が [" Apple "]、正解が "apple"
- **THEN** 正解と判定される

#### Scenario: 候補が空
- **WHEN** 候補が []、正解が "apple"
- **THEN** 不正解と判定される

### Requirement: 正解（correct）の画面
correct 状態では、meaning と手書き入力欄を残したまま、その上に ○ と「次へ」ボタンを重ねて表示しなければならない（SHALL）。

#### Scenario: 正解時の表示
- **WHEN** correct に移る
- **THEN** meaning と入力欄の上に ○ と「次へ」が表示される

### Requirement: 不正解（incorrect）の画面
incorrect 状態では、meaning と手書き入力欄を残したまま、その上に ×、正解の spelling、「次へ」「やり直す」ボタンを重ねて表示しなければならない（SHALL）。

#### Scenario: 不正解時の表示
- **GIVEN** 正解が "apple" である
- **WHEN** incorrect に移る
- **THEN** ×、"apple"、「次へ」、「やり直す」が表示される

### Requirement: 結果表示中は入力と判定を受け付けない
correct または incorrect の状態では、手書き入力欄・「消す」・「判定」を操作できないようにしなければならない（SHALL）。操作できるのは、重ねて表示したボタンだけとする。

#### Scenario: 正解表示中に判定し直せない
- **GIVEN** correct になっている
- **WHEN** 「判定」ボタンや手書き入力欄に触れる
- **THEN** 何も起こらず、correct のままである

#### Scenario: 不正解表示中に書き足せない
- **GIVEN** incorrect になっている
- **WHEN** 手書き入力欄をなぞる、または「消す」に触れる
- **THEN** 線は追加も消去もされず、incorrect のままである

### Requirement: やり直す
incorrect 状態で「やり直す」を押すと、正解の spelling を隠し、手書き入力欄を空にして、同じ問題の writing に戻らなければならない（SHALL）。

#### Scenario: 同じ問題をやり直す
- **GIVEN** 1問目で incorrect になっている
- **WHEN** 「やり直す」を押す
- **THEN** 1問目の writing に戻り、正解の spelling は表示されず、入力欄は空である

### Requirement: 次へ
correct または incorrect の状態で「次へ」を押すと、次の問題の writing に移らなければならない（SHALL）。最後の問題で押した場合は finished に移る。

#### Scenario: 次の問題に進む
- **GIVEN** 最後ではない問題で correct になっている
- **WHEN** 「次へ」を押す
- **THEN** 次の問題の writing に移り、入力欄は空である

#### Scenario: 最後の問題で次へを押す
- **GIVEN** 最後の問題で incorrect になっている
- **WHEN** 「次へ」を押す
- **THEN** finished に移る

### Requirement: 終了（finished）の画面
finished 状態では「終了」と「最初から」ボタンを表示しなければならない（SHALL）。正解数は表示しない。「最初から」を押すと1問目の writing に戻る。

#### Scenario: 終了画面の表示
- **WHEN** finished に移る
- **THEN** 「終了」と「最初から」が表示され、正解数は表示されない

#### Scenario: 最初から解き直す
- **GIVEN** finished になっている
- **WHEN** 「最初から」を押す
- **THEN** 1問目の writing に移り、入力欄は空である

### Requirement: 画面の向きが変わっても進行状況を保つ
端末を回転して画面の向きが変わっても、出題中の問題と画面の状態（writing / correct / incorrect / finished）を保たなければならない（SHALL）。手書き入力欄の線は保たなくてよい。

#### Scenario: 回答の途中で回転する
- **GIVEN** 3問目で incorrect になっている
- **WHEN** 端末を縦から横に回転する
- **THEN** 3問目の incorrect の表示が続く

#### Scenario: 横向きでも結果表示のボタンを操作できる
- **GIVEN** 端末が横向きである
- **WHEN** correct または incorrect に移る
- **THEN** ○×（incorrect では正解の spelling も）と、「次へ」（incorrect では「やり直す」も）が横に並んで、すべて画面内に表示され、操作できる
