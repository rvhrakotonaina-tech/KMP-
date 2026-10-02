# Money Tracker 💰📱

**Money Tracker** is a modern, privacy-focused Android personal finance application built with **Jetpack Compose**, **Room (SQLite)**, and an on-device offline **Large Language Model (Qwen2.5-0.5B-Instruct)** for local AI budget assistance.

---

## 🌟 Key Features

### 📊 1. Dashboard & Analytics
- **Financial Summaries**: Real-time summary cards for Total Income, Total Expenses, and Net Balance.
- **Category Expense Donut Chart**: Visual breakdown of spending by category with percentages and custom color coding.
- **Monthly Income vs Expense Bar Chart**: Chronological multi-month comparisons reflecting actual transaction activity.
- **Deterministic Advice Engine**: Instant, data-driven financial advice generated in Kotlin based on actual spending ratios, top categories, and savings opportunities without LLM hallucinations.

### 📝 2. Transaction Management
- **Full CRUD Operations**: Add, edit, view details, and delete income and expense transactions.
- **Paginated List View**: Smooth paginated transaction browsing powered by Room.
- **Advanced Filtering**: Filter transactions by note search keywords, transaction type (Income/Expense), category, and custom date ranges.
- **Date Picker**: Integrated Material 3 date picker for transaction entries.

### 🤖 3. Offline On-Device AI Assistant
- **100% Offline LLM Inference**: Runs `Qwen2.5-0.5B-Instruct` (8-bit quantized `.task` model) locally on-device using **Google MediaPipe Tasks GenAI**.
- **Intent Routing & Deterministic Reasoning**: Kotlin pre-calculates exact financial facts (totals, net balance, top spending categories, and savings goal feasibility) before prompt delivery, ensuring 100% numerical accuracy and zero math hallucinations.
- **Interactive Chat Interface**: Dedicated full-screen chat window with message bubbles, loading indicators, and smart conversational financial guidance.

### 🎨 4. Customization & Multi-Language Support
- **Custom Currency Selection**: Choose your primary currency symbol (`$`, `€`, `£`, `¥`, `₹`, etc.) with instant dashboard updates.
- **10 Localized Languages**: Full native translations supported across:
  - 🇺🇸 English
  - 🇫🇷 French (*Français*)
  - 🇩🇪 German (*Deutsch*)
  - 🇪🇸 Spanish (*Español*)
  - 🇮🇹 Italian (*Italiano*)
  - 🇵🇹 Portuguese (*Português*)
  - 🇷🇺 Russian (*Русский*)
  - 🇨🇳 Chinese (*中文*)
  - 🇯🇵 Japanese (*日本語*)
  - 🇰🇷 Korean (*한국어*)

### 🚀 5. First-Launch Onboarding & Sample Data Mode
- **Clean Empty Start**: Fresh installations start completely clean without forced mock records.
- **Onboarding Welcome Screen**: Smooth first-launch experience offering two choices:
  - **Get Started**: Jump straight into the empty app to enter your own transactions.
  - **Explore with Sample Data**: Load a demo dataset to test all charts, analytics, and AI features.
- **Sample Data Banner & Clear Option**: Easily purge demo data at any time from the Dashboard or Settings screen.

### 🔒 6. Privacy First
- **Zero Cloud Leakage**: All financial records, database queries, and AI assistant chats are processed 100% locally on your device.

---

## 🛠️ Tech Stack & Libraries

| Domain | Technology / Library | Description |
| :--- | :--- | :--- |
| **Language** | Kotlin `1.9+` | Modern, concise language for Android development |
| **UI Framework** | Jetpack Compose | Declarative UI toolkit with Material Design 3 |
| **Theme & Components** | Material Design 3 | `androidx.compose.material3`, Material Icons Extended |
| **Architecture** | MVVM | Model-View-ViewModel with `StateFlow`, `SharedFlow`, and Coroutines |
| **Database** | Room ORM + SQLite | `androidx.room`, KSP code generation |
| **Preferences** | DataStore Preferences | `androidx.datastore.preferences` for user settings |
| **Navigation** | Navigation 3 | `androidx.navigation3.runtime`, Adaptive List-Detail Pane Scaffold |
| **On-Device AI / LLM** | MediaPipe Tasks GenAI | `com.google.mediapipe.tasks.genai.llminference` running local `Qwen2.5-0.5B-Instruct` (Q8) |
| **Networking** | Retrofit + OkHttp + Moshi | Network connectivity monitoring & model downloading |
| **Permissions** | Accompanist Permissions | Lifecycle-aware permission handling |

---

## 🏗️ Architecture & Data Flow

```
[ Room Database (SQLite) ]
           │
           ▼
[ TransactionRepository ]
           │
           ▼
[ MainViewModel (StateFlow) ] ───► [ Intent Router & Deterministic Engine ]
           │                                      │
           ▼                                      ▼
[ Jetpack Compose UI ] ◄───────────────── [ Local LLM Engine (Qwen 0.5B) ]
 (Dashboard, Chat, Transactions)
```

1. **Deterministic Financial Engine**: Kotlin performs all arithmetic, income/expense totals, net balance calculations, category grouping, and savings goal feasibility checks.
2. **Intent Routing**: Deterministic financial queries (e.g., *"What is my largest expense?"* or *"How much did I spend on Food?"*) are answered directly by Kotlin in 0ms with 100% precision.
3. **Natural Language Explanation**: For conversational advice, Kotlin constructs a structured **Financial Facts Context** and passes it to the local Qwen model for concise natural-language explanations.

---

## 🚀 Getting Started & Building

### Prerequisites
- **Android Studio**: Jellyfish / Koala or newer
- **JDK**: Java 11 or Java 17
- **Min SDK**: 24 (Android 7.0)
- **Target SDK**: 35+ / Android 14+

### Build Steps
1. Clone the repository:
   ```bash
   git clone https://github.com/your-username/Moneytracker.git
   cd Moneytracker
   ```
2. Open the project in **Android Studio**.
3. Sync Gradle project files (`Gradle Sync`).
4. Build the project:
   ```bash
   ./gradlew :app:compileDebugKotlin
   ```
5. Run the app on an Android device or emulator (API 24+).

---

## 📄 License
This project is licensed under the MIT License - see the `LICENSE` file for details.
