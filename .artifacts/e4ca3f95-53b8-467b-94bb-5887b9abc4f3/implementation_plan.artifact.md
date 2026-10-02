# Implementation Plan - Transaction Management

Implement a comprehensive transaction management system including a paginated list with search and filters, and a form for adding/editing transactions.

## User Review Required

> [!IMPORTANT]
> The provided image URL (https://cai-oss-public.oss-cn-beijing.aliyuncs.com/image_1726160677567_466185834.png) resulted in a 404 error. I will implement the UI based on the detailed textual description provided in the task.

## Proposed Changes

### Data Layer

#### [MODIFY] [TransactionDao.kt](file:///C:/Users/Venceslas Hyacinthe/AndroidStudioProjects/Moneytracker/app/src/main/java/com/example/moneytracker/data/local/TransactionDao.kt)
- Add `getTransactionsFilteredPaged` for searching and filtering.
- Add `getTransactionsCount` for pagination metadata.
- Add `getCategories` to provide unique categories for filters.

#### [NEW] [Converters.kt](file:///C:/Users/Venceslas Hyacinthe/AndroidStudioProjects/Moneytracker/app/src/main/java/com/example/moneytracker/data/local/Converters.kt)
- Add Room type converters for `TransactionType`.

#### [MODIFY] [AppDatabase.kt](file:///C:/Users/Venceslas Hyacinthe/AndroidStudioProjects/Moneytracker/app/src/main/java/com/example/moneytracker/data/local/AppDatabase.kt)
- Register `Converters`.

#### [MODIFY] [TransactionRepository.kt](file:///C:/Users/Venceslas Hyacinthe/AndroidStudioProjects/Moneytracker/app/src/main/java/com/example/moneytracker/data/repository/TransactionRepository.kt)
- Update interface and implementation to support filtering and pagination metadata.

### UI Layer

#### [MODIFY] [MainViewModel.kt](file:///C:/Users/Venceslas Hyacinthe/AndroidStudioProjects/Moneytracker/app/src/main/java/com/example/moneytracker/ui/MainViewModel.kt)
- Add states for:
    - Search query
    - Type filter (Income/Expense/All)
    - Category filter
    - Date range (Start/End)
    - Current page and total pages
- Add methods for:
    - CRUD operations (Add, Update, Delete)
    - Updating filters and search
    - Paginating (Next/Prev)

#### [MODIFY] [TransactionListScreen.kt](file:///C:/Users/Venceslas Hyacinthe/AndroidStudioProjects/Moneytracker/app/src/main/java/com/example/moneytracker/ui/screen/TransactionListScreen.kt)
- Re-implement the screen with:
    - `AddTransactionForm`: Horizontal form at the top.
    - `FilterSection`: Search bar and dropdowns/date pickers.
    - `TransactionTable`: Table-like header and rows showing transaction details.
    - `PaginationControls`: Buttons to navigate between pages.

#### [NEW] [TransactionDialogs.kt](file:///C:/Users/Venceslas Hyacinthe/AndroidStudioProjects/Moneytracker/app/src/main/java/com/example/moneytracker/ui/screen/TransactionDialogs.kt)
- Create `EditTransactionDialog` for updating existing transactions.

## Verification Plan

### Automated Tests
- Build the project: `./gradlew :app:assembleDebug`
- Run unit tests: `./gradlew :app:testDebugUnitTest`

### Manual Verification
- Verify the horizontal form adds new transactions correctly.
- Verify filters (search, type, category, date) update the list in real-time.
- Verify pagination correctly fetches data from Room.
- Verify Edit and Delete actions persist changes to the database.
