# Project Plan

The user wants to add an improvement: 
1. Place a filter icon (funnel icon) next to the 'Search Note' bar.
2. Clicking this filter icon should open a pop-up window/dialog containing the filter components.
3. The popup filter window hierarchy must be:
   - Title: "filter components"
   - Transaction Type Dropdown ("All types")
   - Category Dropdown ("All categories")
   - Date range picker button ("Date range")
The filters should be removed from the main screen layout and moved entirely inside this new pop-up window.

## Project Brief

# Money Tracker - Android App Project Brief

## Goal
The primary objective is to streamline the Money Tracker user interface by moving existing filtering controls into a dedicated, accessible pop-up dialog. This optimization reduces visual clutter on the main screen while maintaining powerful transaction refinement tools.

## Features
1.  **Consolidated Search & Filter UI**: A unified search bar at the top of the transaction list with a funnel icon for one-tap access to advanced filters.
2.  **Centralized Filter Dialog**: A pop-up window that houses all refinement controls, featuring a clean hierarchy with a "filter components" title.
3.  **Advanced Transaction Refinement**: Users can filter their financial data through:
    *   **Transaction Type**: A dropdown to switch between "All types," Income, or Expenses.
    *   **Category Selection**: A dropdown to filter by specific transaction categories (e.g., Food, Rent, Salary).
    *   **Date Range Picker**: A button-triggered picker to filter transactions within a custom timeframe.
4.  **Adaptive Transaction Layout**: A responsive UI that automatically adjusts between a single-pane list and a multi-pane list-detail view based on device screen size.

## High-Level Tech Stack
*   **Kotlin**: The primary language for all application logic and data handling.
*   **Jetpack Compose**: The modern declarative toolkit used to build the UI, including the new filter pop-up and custom dialogs.
*   **Coroutines**: For managing asynchronous operations such as filtering large lists of transactions without blocking the UI thread.
*   **Jetpack Navigation 3**: A state-driven navigation architecture used to manage the app's backstack and adaptive transitions.
*   **Compose Material 3 Adaptive Library**: Used to implement the `ListDetailPaneScaffold` and ensure the UI scales seamlessly across phones, tablets, and foldables.
*   **Architectural Pattern**: MVVM (Model-View-ViewModel) to ensure a clean separation between the filtering logic and the UI state.

---

> [!NOTE]
> The "UI Design Image" section was omitted as the image generation tool is currently unavailable.

## Implementation Steps
**Total Duration:** 22h 38m 28s

### Task_1_CoreInfrastructure: Implement Room database, Entities, Repository with sample data seeding, and setup Navigation 3 with an Adaptive Scaffold for responsive layout.
- **Status:** COMPLETED
- **Updates:** Implemented Room DB, Transaction entity, DAO, Repository with seeding, Navigation 3, and Adaptive Scaffold (ListDetailPaneScaffold). Build is successful.
- **Acceptance Criteria:**
  - Room DB and Repository functional
  - Sample data seeding logic implemented
  - Navigation 3 and Adaptive Scaffold setup
  - Build pass

### Task_2_AnalyticsDashboard: Develop the Dashboard screen featuring summary cards (Income, Expense, Balance) and visual analytics (Monthly Bar Chart, Category Donut Chart) using the adaptive layout.
- **Status:** COMPLETED
- **Updates:** Implemented Dashboard screen with custom dark theme. Added Summary Cards (Income, Expense, Balance) and custom Bar and Donut charts. Ensured responsiveness using BoxWithConstraints and Navigation 3 integration. Build successful.
- **Acceptance Criteria:**
  - Dashboard displays correct totals from DB
  - Bar and Donut charts render correctly
  - The implemented UI must match the dark-themed side-by-side design provided in the project description
  - Build pass

### Task_3_TransactionManagement: Implement the Transaction List with pagination, search, and filters (type, category, date range), along with the Add/Edit transaction dialog/form.
- **Status:** COMPLETED
- **Updates:** Second refinement complete: Fixed required width for transaction table to ensure horizontal scrolling works on all screens. Forced dark theme globally in both Theme and MainActivity. Build successful.
- **Acceptance Criteria:**
  - Paginated transaction list working
  - Filters and Search functional
  - Add/Edit form persists data
  - The implemented UI must match the design provided in the project description
  - Build pass
- **Duration:** 15h 2s

### Task_4_RunAndVerify: Final integration check, app icon setup, and critical stability verification. critic_agent to verify stability and requirement alignment.
- **Status:** COMPLETED
- **Updates:** Final verification successful. The app is stable, follows the dark theme design consistently across devices, and the transaction table is now fully readable on mobile via horizontal scrolling. All features are functional.
- **Acceptance Criteria:**
  - All existing tests pass
  - Build pass
  - App does not crash
  - UI matches user requirements and design
  - Final verification by critic_agent complete

### Task_5_VerticalTransactionForm: Replace the existing horizontal transaction form with a vertical hierarchy containing Title, Transaction Type Dropdown, Amount Input, Category Dropdown, Note Input, Date Picker, and '+ Add' Button.
- **Status:** COMPLETED
- **Updates:** Replaced the horizontal transaction form with a vertical hierarchy in TransactionListScreen. Added 'Add new transaction' title, dropdowns for Type and Category, input fields for Amount and Note, a Date picker, and a '+ Add' button. Ensured persistence to Room and dark theme consistency. Build successful.
- **Acceptance Criteria:**
  - Transaction entry form updated to a vertical layout
  - All fields (Title, Type, Amount, Category, Note, Date, Add button) are present and functional stacked vertically
  - Form successfully validates input and persists new transaction to Room database
  - Build pass

### Task_6_RunAndVerifyVerticalForm: Final integration check and stability verification for the vertical transaction form update. Instruct critic_agent to verify stability and requirement alignment.
- **Status:** COMPLETED
- **Updates:** Fixed scroll issues on the transaction list screen by enabling vertical scroll on the root container and resolving nested scroll conflicts with the transaction table. Critic agent verified that scrolling is now working perfectly, the layout is correct, and the app is stable.
- **Acceptance Criteria:**
  - make sure all existing tests pass
  - build pass
  - app does not crash
  - UI matches user requirements and vertical form sketch
  - Final verification by critic_agent complete
- **Duration:** 7h 38m 26s

### Task_7_ImplementPopupFilter: Move transaction filters from the main screen layout to a new pop-up filter window triggered by a funnel icon next to the search bar. Implement Title, Transaction Type Dropdown, Category Dropdown, and Date Range Picker button. Ensure persistent filter state in ViewModel.
- **Status:** COMPLETED
- **Updates:** Fixed filter pop-up visibility by hoisting the showFilterDialog state to the screen container level and ensuring correct click callback mapping from the filter icon button. Build successful.
- **Acceptance Criteria:**
  - Filter icon next to search bar triggers the pop-up dialog
  - Pop-up window matches hierarchy: Title 'filter components', Type Dropdown, Category Dropdown, and Date Range Picker button
  - Filters are removed from the main screen layout
  - Filter state is preserved in ViewModel when pop-up opens/closes
  - build pass

### Task_8_RunAndVerifyPopupFilter: Final integration check and stability verification for the pop-up filter window update. Instruct critic_agent to verify application stability (no crashes), confirm alignment with user requirements, and report critical UI issues.
- **Status:** IN_PROGRESS
- **Updates:** User reports that Edit and Delete actions take too long to update the UI and wants them to be instantaneous with a toast notification. Reopening Task 7 to address responsiveness and add toast feedback.
- **Acceptance Criteria:**
  - make sure all existing tests pass
  - build pass
  - app does not crash
  - UI matches user requirements and pop-up filter hierarchy
  - Final verification by critic_agent complete
- **StartTime:** 2026-09-15 21:52:51 IST

