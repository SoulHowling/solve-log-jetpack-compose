# SolveLog (SolveLog-JetpackCompose)

![Android](https://img.shields.io/badge/Platform-Android-green.svg)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg)
![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose%20%2F%20Material%203-blue.svg)
![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)

**SolveLog（ソルブログ）** は、学習や問題演習の解答記録、メモ作成、時間計測（ストップウォッチ・タイマー）を一体化して効率的な学習をサポートする Android アプリケーションです。
最新の **Jetpack Compose** と **Material Design 3** を採用して開発されています。

---

## 📱 主な機能

### 1. 解答記録機能 (Solve Log)
- **問題と解答ログの管理**: 各問題の解答記録を作成・記録・閲覧・編集できます。
- **フォルダ分類**: 問題や解答ログをカテゴリごとに整理するためのフォルダ管理機能。
- **多様な解答形式**: 選択肢形式や自由記述形式に対応し、正誤判定を記録可能です。

### 2. メモ機能 (Memo)
- **Markdown 対応**: リッチな表現が可能な Markdown レンダリングを搭載したメモエディタ＆ビューア。
- **フォルダ整理**: メモをフォルダごとに整理して分類・検索可能。

### 3. 時間計測機能 (Measurement)
- **ストップウォッチ & タイマー**: 勉強時間や演習時間を正確に計測。
- **バックグラウンド実行**: Foreground Service により、アプリを閉じてもバックグラウンドで安定動作・通知連携。

### 4. 設定 & データ管理 (Settings)
- **データ初期化**: 必要に応じてローカルデータを安全に消去。
- **ライセンス & ポリシー確認**: 使用しているオープンソースライブラリのライセンス一覧（AboutLibraries 連携）やプライバシーポリシーの表示。

---

## 🛠 使用技術・アーキテクチャ (Tech Stack)

* **UI**: [Jetpack Compose](https://developer.android.com/jetpack/compose) / [Material Design 3](https://m3.material.io/)
* **画面遷移**: [Navigation Compose](https://developer.android.com/jetpack/compose/navigation)
* **アーキテクチャ**: MVVM (Model-View-ViewModel) / StateFlow / Unidirectional Data Flow (UDF)
* **データベース**: [Room Persistence Library](https://developer.android.com/training/data-storage/room)
* **非同期処理**: Kotlin Coroutines & StateFlow
* **Markdown表示**: [Compose Markdown](https://github.com/huarangmeng/ComposeMarkdown)
* **ライセンス表示**: [AboutLibraries](https://github.com/mikepenz/AboutLibraries)
* **テスト**: JUnit 4, Robolectric, KotlinX Coroutines Test

---

## 🚀 ビルド & 動作環境 (Getting Started)

### 動作環境
* **Android OS**: Android 11 (API Level 30) 以上
* **JDK**: JDK 17 以上
* **Android Studio**: Android Studio Ladybug (2024.2.1) 以降推奨

### ビルド手順

1. 本リポジトリをクローンします。
   ```bash
   git clone https://github.com/your-username/solve-log-jetpack-compose.git
   cd solve-log-jetpack-compose
   ```

2. Android Studio でプロジェクトを開くか、コマンドラインからデバッグビルドを実行します。
   ```bash
   ./gradlew assembleDebug
   ```

3. ユニットテストを実行する場合:
   ```bash
   ./gradlew testDebugUnitTest
   ```

---

## 📄 ライセンス (License)

このプロジェクトのソースコードは **[Apache License 2.0](LICENSE.txt)** のもとで公開されています。

```text
Copyright 2024-2026 soulhowling / SolveLog-JetpackCompose

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```

### サードパーティライブラリのライセンス
本アプリで使用している外部ライブラリおよびそのライセンス条文の一覧については、**[THIRD_PARTY_LICENSES.txt](THIRD_PARTY_LICENSES.txt)** をご確認ください。
