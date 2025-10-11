# Contributing to TopoClimb Android App

Thank you for your interest in contributing to the TopoClimb Android app! This document provides guidelines and instructions for contributing.

## Table of Contents

1. [Code of Conduct](#code-of-conduct)
2. [Getting Started](#getting-started)
3. [Development Process](#development-process)
4. [Coding Standards](#coding-standards)
5. [Submitting Changes](#submitting-changes)
6. [Testing Guidelines](#testing-guidelines)
7. [Documentation](#documentation)

## Code of Conduct

### Our Standards

- Be respectful and inclusive
- Welcome newcomers and help them get started
- Focus on what is best for the community
- Show empathy towards other community members

## Getting Started

### Prerequisites

1. Familiarize yourself with:
   - Kotlin programming language
   - Android development
   - Jetpack Compose
   - MVVM architecture
   - Git version control

2. Read the documentation:
   - `README.md` - Project overview
   - `ARCHITECTURE.md` - Architecture details
   - `QUICKSTART.md` - Development setup
   - `API_INTEGRATION.md` - API documentation

### Setting Up Your Development Environment

1. Fork the repository on GitHub
2. Clone your fork locally:
   ```bash
   git clone https://github.com/your-username/topoclimb-android-app.git
   cd topoclimb-android-app
   ```
3. Add the upstream repository:
   ```bash
   git remote add upstream https://github.com/paulhenry46/topoclimb-android-app.git
   ```
4. Open the project in Android Studio
5. Configure your API endpoint in `AppConfig.kt`

## Development Process

### Branching Strategy

- `main` - Production-ready code
- `develop` - Integration branch for features
- `feature/feature-name` - Feature development
- `bugfix/bug-name` - Bug fixes
- `hotfix/fix-name` - Urgent production fixes

### Creating a Feature Branch

```bash
git checkout -b feature/your-feature-name
```

### Keeping Your Fork Updated

```bash
git fetch upstream
git checkout main
git merge upstream/main
```

## Coding Standards

### Kotlin Style Guide

Follow the [official Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html):

#### Naming Conventions

- **Classes**: PascalCase - `SitesViewModel`
- **Functions**: camelCase - `loadSites()`
- **Constants**: UPPER_SNAKE_CASE - `API_BASE_URL`
- **Variables**: camelCase - `siteId`
- **Composables**: PascalCase - `SitesScreen()`

#### Code Formatting

Use Android Studio's built-in formatter:
- **Format code**: `Ctrl + Alt + L` (Windows/Linux) or `Cmd + Option + L` (Mac)
- **Organize imports**: `Ctrl + Alt + O` (Windows/Linux) or `Cmd + Option + O` (Mac)

#### Best Practices

1. **Single Responsibility**: Each class/function should have one clear purpose
2. **DRY (Don't Repeat Yourself)**: Extract common code into reusable functions
3. **Immutability**: Prefer `val` over `var` when possible
4. **Null Safety**: Use nullable types appropriately and avoid `!!`

### Architecture Guidelines

#### MVVM Pattern

```kotlin
// ViewModel - Business logic and state management
class SitesViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SitesUiState())
    val uiState: StateFlow<SitesUiState> = _uiState.asStateFlow()
    
    fun loadSites() {
        // Implementation
    }
}

// UI - Composable functions
@Composable
fun SitesScreen(viewModel: SitesViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    // UI implementation
}
```

#### Repository Pattern

```kotlin
class TopoClimbRepository {
    suspend fun getSites(): Result<List<Site>> {
        return try {
            Result.success(api.getSites())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

### Compose Guidelines

1. **State Hoisting**: Keep state in the appropriate scope
2. **Recomposition**: Minimize recomposition by using `remember` and `derivedStateOf`
3. **Side Effects**: Use `LaunchedEffect`, `DisposableEffect` appropriately
4. **Modifiers**: Chain modifiers in logical order

Example:
```kotlin
@Composable
fun RouteCard(
    route: Route,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        // Content
    }
}
```

### Comments and Documentation

- Use KDoc for public APIs:
  ```kotlin
  /**
   * Loads sites from the API and updates the UI state.
   * 
   * @param forceRefresh If true, bypasses cache and fetches fresh data
   */
  fun loadSites(forceRefresh: Boolean = false)
  ```

- Add inline comments for complex logic:
  ```kotlin
  // Filter routes by grade and type, applying both filters simultaneously
  val filtered = routes.filter { route ->
      // Implementation
  }
  ```

## Submitting Changes

### Before Submitting

1. **Test your changes**:
   ```bash
   ./gradlew test
   ./gradlew connectedAndroidTest
   ```

2. **Run code formatter**:
   - Format all modified files
   - Organize imports
   - Remove unused imports

3. **Update documentation** if needed:
   - Update README if adding features
   - Update ARCHITECTURE if changing architecture
   - Update API_INTEGRATION if changing API

4. **Commit your changes**:
   ```bash
   git add .
   git commit -m "feat: add route detail screen"
   ```

### Commit Message Format

Follow [Conventional Commits](https://www.conventionalcommits.org/):

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types**:
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Build process or auxiliary tool changes

**Examples**:
```
feat(routes): add filtering by difficulty grade

Add filter chips in Routes screen to filter by grade.
Users can now select multiple grades to filter routes.

Closes #123
```

```
fix(network): handle timeout errors properly

Add timeout configuration to Retrofit client and display
user-friendly error messages when network requests timeout.
```

### Creating a Pull Request

1. Push your branch to your fork:
   ```bash
   git push origin feature/your-feature-name
   ```

2. Go to GitHub and create a Pull Request

3. Fill in the PR template:
   - **Title**: Clear, descriptive title
   - **Description**: What changed and why
   - **Screenshots**: For UI changes
   - **Testing**: How you tested the changes
   - **Checklist**: Complete the checklist

4. Wait for review and address feedback

### PR Checklist

- [ ] Code follows project style guidelines
- [ ] Self-review completed
- [ ] Comments added for complex code
- [ ] Documentation updated
- [ ] Tests added/updated
- [ ] All tests passing
- [ ] No new warnings
- [ ] Screenshots included (for UI changes)

## Testing Guidelines

### Unit Tests

Write unit tests for:
- ViewModels (state management, business logic)
- Repository (data handling, error cases)
- Utility functions

Example:
```kotlin
class SitesViewModelTest {
    @Test
    fun `loadSites updates state with sites on success`() {
        // Given
        val viewModel = SitesViewModel()
        
        // When
        viewModel.loadSites()
        
        // Then
        // Assert state is updated correctly
    }
}
```

### Instrumented Tests

Write instrumented tests for:
- UI interactions
- Navigation flows
- Integration scenarios

Example:
```kotlin
@Test
fun testNavigationToRoutesScreen() {
    // Test navigation between screens
}
```

### Test Coverage

Aim for:
- **ViewModels**: 80%+ coverage
- **Repository**: 80%+ coverage
- **UI**: Critical paths tested

## Documentation

### What to Document

1. **Public APIs**: All public functions and classes
2. **Complex Logic**: Non-obvious implementations
3. **Configuration**: Setup and configuration steps
4. **Architecture**: Significant architectural decisions

### Documentation Files

- **README.md**: User-facing features and setup
- **ARCHITECTURE.md**: Technical architecture
- **API_INTEGRATION.md**: API details
- **QUICKSTART.md**: Developer onboarding
- **UI_DESIGN.md**: UI specifications
- **CONTRIBUTING.md**: This file

### Updating Documentation

When making changes:
1. Update relevant documentation files
2. Keep examples up to date
3. Add screenshots for UI changes
4. Update version numbers if applicable

## Feature Requests and Bug Reports

### Reporting Bugs

Include:
- Steps to reproduce
- Expected behavior
- Actual behavior
- Screenshots/videos
- Device/OS version
- App version

### Requesting Features

Include:
- Use case description
- Proposed solution
- Alternative solutions considered
- Additional context

## Code Review Process

### As a Reviewer

- Be constructive and respectful
- Explain the "why" behind suggestions
- Distinguish between "must fix" and "nice to have"
- Acknowledge good work

### As an Author

- Respond to all comments
- Ask questions if unclear
- Don't take feedback personally
- Make requested changes promptly

## Getting Help

- **Questions**: Open a discussion on GitHub
- **Bugs**: Create an issue
- **Features**: Create a feature request
- **Chat**: Join project Discord/Slack (if available)

## Recognition

Contributors will be:
- Listed in CONTRIBUTORS.md
- Mentioned in release notes
- Credited in the app (for significant contributions)

## License

By contributing, you agree that your contributions will be licensed under the MIT License.

---

Thank you for contributing to TopoClimb Android App! 🧗‍♂️

Happy coding!
