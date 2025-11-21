# Expense Tracker Android App

A modern, production-ready expense tracking Android application built with Jetpack Compose, MVVM architecture, Firebase Authentication, and REST API integration.

## Features

✅ **Firebase Authentication**
- Email/password sign up and login
- Form validation with user-friendly error messages
- Persistent authentication state

✅ **Expense Management**
- Create, read, update, and delete expenses
- Category-based organization
- Date tracking
- Amount validation

✅ **Dashboard & Analytics**
- Total spending overview
- Category-wise breakdown
- Visual progress indicators
- Percentage calculations

✅ **Modern UI/UX**
- Material Design 3
- Responsive layouts
- Dark mode support
- Smooth animations
- Pull-to-refresh

✅ **Best Practices**
- MVVM architecture with ViewModels
- Repository pattern for data layer
- Dependency injection with Hilt
- Kotlin Coroutines and Flow
- REST API integration with Retrofit
- Proper error handling
- Input validation

## Tech Stack

### Architecture & Design Patterns
- **MVVM** (Model-View-ViewModel)
- **Repository Pattern**
- **Clean Architecture principles**
- **Unidirectional Data Flow** with StateFlow

### Libraries & Frameworks

#### UI
- **Jetpack Compose** - Modern declarative UI
- **Material Design 3** - Latest Material components
- **Navigation Compose** - Type-safe navigation

#### Dependency Injection
- **Hilt** - Dependency injection framework

#### Networking
- **Retrofit** - REST API client
- **Moshi** - JSON serialization
- **OkHttp** - HTTP client with logging

#### Firebase
- **Firebase Authentication** - User authentication
- **Firebase Firestore** - NoSQL database (via backend)

#### Async & State Management
- **Kotlin Coroutines** - Asynchronous programming
- **StateFlow** - State management
- **Lifecycle** - Lifecycle-aware components

## Project Structure

```
app/src/main/java/xcom/niteshray/xapps/expensetracker_vivaahaverse/
├── data/
│   ├── model/              # Data classes
│   │   ├── Expense.kt
│   │   ├── ApiModels.kt
│   ├── remote/             # API services
│   │   └── ExpenseApiService.kt
│   └── repository/         # Repository layer
│       ├── ExpenseRepository.kt
│       └── AuthRepository.kt
├── di/                     # Dependency injection modules
│   └── AppModule.kt
├── ui/
│   ├── navigation/         # Navigation setup
│   │   └── Screen.kt
│   ├── screens/           # Composable screens
│   │   ├── auth/
│   │   │   ├── LoginScreen.kt
│   │   │   └── SignUpScreen.kt
│   │   ├── expenses/
│   │   │   ├── ExpenseListScreen.kt
│   │   │   └── ExpenseFormScreen.kt
│   │   └── dashboard/
│   │       └── DashboardScreen.kt
│   ├── viewmodel/         # ViewModels
│   │   ├── AuthViewModel.kt
│   │   ├── ExpenseListViewModel.kt
│   │   ├── ExpenseFormViewModel.kt
│   │   └── DashboardViewModel.kt
│   └── theme/             # Material theming
│       ├── Color.kt
│       ├── Theme.kt
│       └── Type.kt
├── MainActivity.kt
└── ExpenseTrackerApplication.kt
```

## Setup Instructions

### Prerequisites

1. **Android Studio** (Latest version - Hedgehog or newer)
2. **JDK 11** or higher
3. **Firebase Project** with:
   - Authentication enabled (Email/Password)
   - Firestore database created
4. **Backend API** running (see ExpenseTrackerBackend folder)

### Step 1: Firebase Setup

1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Create a new project or select existing one
3. Add an Android app:
   - Package name: `xcom.niteshray.xapps.expensetracker_vivaahaverse`
   - Download `google-services.json`
4. Place `google-services.json` in `app/` folder
5. Enable Email/Password authentication:
   - Go to Authentication → Sign-in method
   - Enable Email/Password

### Step 2: Configure Backend URL

Edit `app/build.gradle.kts` and update the `BASE_URL`:

```kotlin
defaultConfig {
    // For Android Emulator connecting to localhost
    buildConfigField("String", "BASE_URL", "\"http://10.0.2.2:8000/\"")
    
    // For physical device, use your computer's IP
    // buildConfigField("String", "BASE_URL", "\"http://192.168.x.x:8000/\"")
}
```

**Important URLs:**
- **Android Emulator**: Use `10.0.2.2` to connect to host machine's localhost
- **Physical Device**: Use your computer's local IP address
- **Production**: Use your deployed backend URL

### Step 3: Build and Run

1. Open project in Android Studio
2. Sync Gradle files
3. Make sure backend is running on port 8000
4. Run the app on emulator or physical device

```bash
# Sync and build
./gradlew clean build

# Install on connected device
./gradlew installDebug
```

## API Integration

The app communicates with the backend API using Retrofit. All requests include Firebase ID tokens for authentication.

### Example API Call Flow

1. User logs in via Firebase Auth
2. App retrieves Firebase ID token
3. Token included in `Authorization: Bearer <token>` header
4. Backend verifies token and processes request

### Network Configuration

- **Timeout**: 30 seconds
- **Logging**: Enabled in debug builds
- **Error Handling**: Centralized in Repository layer

## Features Details

### Authentication Flow

1. **Sign Up**
   - Email validation (must be valid email format)
   - Password validation (minimum 6 characters)
   - Password confirmation match
   - Firebase user creation
   - Auto-login after successful signup

2. **Login**
   - Email/password validation
   - Firebase authentication
   - Persistent session
   - Error messages for invalid credentials

### Expense Management

1. **List Expenses**
   - View all expenses
   - Filter by category
   - Pull to refresh
   - Swipe to delete confirmation
   - Edit via item click

2. **Add/Edit Expense**
   - Amount input with validation
   - Description (max 500 characters)
   - Category dropdown
   - Date selection
   - Real-time validation
   - Error messages

3. **Delete Expense**
   - Confirmation dialog
   - Optimistic UI update
   - Error handling with rollback

### Dashboard

1. **Overview Card**
   - Total spending amount
   - Total expense count
   - Prominent display

2. **Category Breakdown**
   - Amount per category
   - Item count per category
   - Percentage of total
   - Visual progress bars
   - Sorted by amount (descending)

## Validation Rules

### Email
- Must be valid email format
- Cannot be empty

### Password
- Minimum 6 characters
- Required for signup and login

### Amount
- Must be a valid number
- Must be greater than 0
- Maximum 999,999,999

### Description
- Cannot be empty
- Maximum 500 characters

## Error Handling

The app implements comprehensive error handling:

1. **Network Errors**
   - Connection timeout
   - No internet connection
   - Server unreachable
   - User-friendly messages

2. **Authentication Errors**
   - Invalid credentials
   - User not found
   - Email already exists
   - Weak password

3. **Validation Errors**
   - Field-specific error messages
   - Real-time validation
   - Form submission prevention

4. **API Errors**
   - 401 Unauthorized
   - 404 Not Found
   - 500 Server Error
   - Parsed error messages from backend

## State Management

The app uses modern Android state management:

- **StateFlow** for ViewModel state
- **collectAsStateWithLifecycle** for lifecycle-aware collection
- **Unidirectional data flow**
- **Immutable state objects**

### Example State

```kotlin
data class ExpenseListUiState(
    val expenses: List<Expense> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedCategory: String? = null,
    val isRefreshing: Boolean = false
)
```

## Dependency Injection

Hilt provides all dependencies:

```kotlin
@HiltViewModel
class ExpenseListViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository
) : ViewModel()
```

Modules:
- **NetworkModule**: Retrofit, OkHttp, Moshi
- **FirebaseModule**: Firebase Auth

## Testing

### Unit Tests Location
```
app/src/test/java/
```

### Instrumented Tests Location
```
app/src/androidTest/java/
```

### Running Tests
```bash
# Unit tests
./gradlew test

# Instrumented tests (requires device/emulator)
./gradlew connectedAndroidTest
```

## Build Variants

### Debug Build
- Logging enabled
- `BASE_URL` points to localhost
- Debug signing

### Release Build
- Logging disabled
- Update `BASE_URL` for production
- Proguard enabled (optional)
- Release signing required

## Responsive Design

The app adapts to different screen sizes:
- **Phone**: Optimized single-pane layout
- **Tablet**: Can be extended for two-pane layout
- **Landscape**: Proper orientation handling

## Accessibility

- Content descriptions for icons
- Semantic UI structure
- Screen reader compatible
- Keyboard navigation support

## Performance Optimizations

1. **LazyColumn** for efficient list rendering
2. **Stable keys** for list items
3. **Remember** for composable state
4. **Coroutines** for async operations
5. **Flow** for reactive data streams
6. **Lifecycle-aware** state collection

## Known Limitations

1. Date picker uses current date (can be enhanced with DatePickerDialog)
2. No offline mode (requires network connection)
3. No data caching (fetches from API each time)
4. No image attachments for expenses

## Future Enhancements

- [ ] Offline mode with local database (Room)
- [ ] Data synchronization
- [ ] Receipt photo attachments
- [ ] Export to PDF/Excel
- [ ] Recurring expenses
- [ ] Budget limits and notifications
- [ ] Multi-currency support
- [ ] Biometric authentication
- [ ] Dark mode customization
- [ ] Advanced filtering (date range, amount range)
- [ ] Search functionality
- [ ] Data visualization charts

## Troubleshooting

### App can't connect to backend

**Check:**
1. Backend is running on port 8000
2. `BASE_URL` in `build.gradle.kts` is correct
3. For emulator: use `10.0.2.2` not `localhost`
4. For device: use computer's IP address
5. Firewall allows connections

### Firebase Authentication not working

**Check:**
1. `google-services.json` is in `app/` folder
2. Firebase Authentication is enabled
3. Email/Password provider is enabled
4. SHA-1 fingerprint added (for release builds)

### Build Errors

**Solutions:**
1. Sync Gradle files
2. Clean and rebuild: `./gradlew clean build`
3. Invalidate caches and restart Android Studio
4. Check internet connection (Gradle downloads dependencies)

### Network Errors

**Check:**
1. Internet permission in AndroidManifest.xml
2. `usesCleartextTraffic="true"` for HTTP (development only)
3. Backend API is accessible
4. Firebase ID token is valid

## Contributing

This is an assessment project. For production use:
1. Add comprehensive unit tests
2. Add UI tests
3. Implement CI/CD pipeline
4. Add crash reporting (Firebase Crashlytics)
5. Add analytics
6. Security hardening

## License

This project is created for assessment purposes.

## Support

For issues:
1. Check backend logs
2. Check Android Logcat
3. Verify Firebase configuration
4. Test API endpoints with Postman

## Screenshots

(Add screenshots of your app here)

- Login Screen
- Expense List
- Add/Edit Expense
- Dashboard
- Category Filter

---

Built with ❤️ using Jetpack Compose and modern Android development practices.
