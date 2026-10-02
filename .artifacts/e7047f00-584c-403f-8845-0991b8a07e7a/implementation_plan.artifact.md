# Implementation Plan - Offline AI Budget Assistant

This plan outlines the integration of an offline Large Language Model (LLM) using the **Google AI Edge SDK (MediaPipe)** to provide proactive budget insights and a chatbot interface.

## User Review Required

> [!IMPORTANT]
> **Model Size:** Offline LLMs like Gemma 2B or Phi-3 are large (~1.5GB to 2.2GB). We will implement a download manager to avoid bloating the initial app size.
> **Hardware Requirements:** On-device AI performs best on devices with at least 6GB of RAM. We will add a check to disable or warn users on lower-end devices.

## Proposed Changes

### 1. Build & Dependency Setup
We need to add the MediaPipe GenAI dependencies to the project.

#### [MODIFY] [build.gradle.kts](file:///C:/Users/Venceslas Hyacinthe/AndroidStudioProjects/Moneytracker/app/build.gradle.kts)
- Add `implementation("com.google.mediapipe:tasks-genai:0.10.14")` (or latest stable).
- Configure `sourceSets` to include the model assets directory.

---

### 2. AI Engine & Data Layer
Create the core logic to handle offline inference and data preparation.

#### [NEW] [AIEngine.kt](file:///C:/Users/Venceslas Hyacinthe/AndroidStudioProjects/Moneytracker/app/src/main/java/com/example/moneytracker/data/ai/AIEngine.kt)
- Manage `LlmInference` lifecycle.
- Handle model downloading and initialization.

#### [NEW] [PromptManager.kt](file:///C:/Users/Venceslas Hyacinthe/AndroidStudioProjects/Moneytracker/app/src/main/java/com/example/moneytracker/data/ai/PromptManager.kt)
- Logic to convert `List<Transaction>` into a structured summary (e.g., "Food: $500, Rent: $1200").
- Templates for "Budget Advice", "Trend Analysis", and "Chat Context".

---

### 3. UI Components (Dashboard & Chat)
Add the visual elements for the "Proactive Assistant".

#### [MODIFY] [Route.kt](file:///C:/Users/Venceslas Hyacinthe/AndroidStudioProjects/Moneytracker/app/src/main/java/com/example/moneytracker/ui/navigation/Route.kt)
- Add `Route.Chat` for the full-screen chatbot experience.

#### [MODIFY] [DashboardScreen.kt](file:///C:/Users/Venceslas Hyacinthe/AndroidStudioProjects/Moneytracker/app/src/main/java/com/example/moneytracker/ui/screen/DashboardScreen.kt)
- Add an `InsightSection` at the top of the dashboard.
- Create `InsightCard` components that display AI-generated tips.
- Add a Floating Action Button (FAB) to trigger the chat.

#### [NEW] [ChatScreen.kt](file:///C:/Users/Venceslas Hyacinthe/AndroidStudioProjects/Moneytracker/app/src/main/java/com/example/moneytracker/ui/screen/ChatScreen.kt)
- A standard chat interface with message bubbles.
- Support for "Quick Actions" (buttons like "Summarize this week").

---

### 4. ViewModel Integration

#### [MODIFY] [MainViewModel.kt](file:///C:/Users/Venceslas Hyacinthe/AndroidStudioProjects/Moneytracker/app/src/main/java/com/example/moneytracker/ui/MainViewModel.kt)
- Expose `aiState` (Loading, Ready, Error).
- Expose `chatMessages` and `generatedInsights`.
- Trigger insight generation when transactions change (debounced).

## Verification Plan

### Automated Tests
- Unit tests for `PromptManager` to ensure transaction summaries are formatted correctly.
- Mock tests for `AIEngine` state transitions.

### Manual Verification
1. **Model Download:** Verify the app correctly downloads and verifies the LLM file.
2. **Insight Accuracy:** Check if the generated insights reflect the actual data in the database.
3. **Chat Responsiveness:** Measure the time-to-first-token for the offline LLM on a real device.
4. **Offline Mode:** Ensure the AI works while the device is in Airplane Mode.
