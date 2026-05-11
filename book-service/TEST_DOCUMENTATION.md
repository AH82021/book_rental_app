# Book Service Test Suite Documentation

This document provides comprehensive information about the test suite for the Book Service.

## Overview

The test suite includes:
- **Unit Tests**: Testing individual components in isolation using Mockito
- **Integration Tests**: Testing complete workflows across layers
- **Model Tests**: Testing entity behavior and validation
- **Controller Tests**: Testing REST API endpoints with MockMvc
- **Service Tests**: Testing business logic

## Test Structure

```
src/test/java/com/bookstore/book_service/
├── BookServiceIntegrationTest.java          # Integration tests for Book operations
├── CategoryServiceIntegrationTest.java      # Integration tests for Category operations
├── TestFixtures.java                        # Test data builders and factories
├── RestApiTestUtils.java                    # REST API testing utilities
├── model/
│   └── BookAndCategoryModelTest.java       # Entity model tests
├── service/impl/
│   ├── BookServiceImplTest.java            # Unit tests for BookService
│   └── CategoryServiceImplTest.java        # Unit tests for CategoryService
└── controller/
    ├── BookControllerTest.java             # Unit tests for BookController
    └── CategoryControllerTest.java         # Unit tests for CategoryController

src/test/resources/
└── application-test.properties             # Test configuration
```

## Test Configuration

### Test Properties File (application-test.properties)

The test suite uses an in-memory H2 database for fast, isolated testing:

```properties
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.cache.type=none
```

## Running Tests

### Run all tests
```bash
mvn clean test
```

### Run specific test class
```bash
mvn test -Dtest=BookServiceImplTest
```

### Run specific test method
```bash
mvn test -Dtest=BookServiceImplTest#shouldCreateBookSuccessfully
```

### Run with coverage report
```bash
mvn clean test jacoco:report
```

### Run tests with verbose output
```bash
mvn test -X
```

## Test Naming Convention

Tests follow the naming pattern: `should[ExpectedBehavior]When[Condition]`

Examples:
- `shouldCreateBookSuccessfully`
- `shouldThrowExceptionWhenIsbnDuplicate`
- `shouldGetBookById`

## Best Practices Implemented

### 1. Unit Testing with Mockito
- **Isolation**: Each unit test is isolated using mocks
- **No Database Access**: Uses Mockito to mock repository calls
- **Fast Execution**: Typically completes in milliseconds

Example:
```java
@Test
void shouldCreateBookSuccessfully() {
    when(bookRepository.existsByIsbnAndDeletedFalse(anyString())).thenReturn(false);
    when(bookMapper.toEntity(validBookRequest)).thenReturn(testBook);
    
    BookResponse response = bookService.createBook(validBookRequest);
    
    assertThat(response).isNotNull();
    verify(bookRepository, times(1)).save(any(Book.class));
}
```

### 2. Integration Testing
- **Full Context**: Uses `@SpringBootTest` to load the entire Spring context
- **Real Database**: Uses H2 in-memory database for actual persistence
- **Transactional**: Uses `@Transactional` to rollback changes after each test

Example:
```java
@SpringBootTest
@Transactional
class BookServiceIntegrationTest {
    // Tests the entire flow from controller to database
}
```

### 3. Test Fixtures
- **Builders**: Use builder pattern for test data creation
- **Reusability**: Share common test fixtures across tests
- **Readability**: Clear factory methods for creating test objects

Example:
```java
Book testBook = TestFixtures.createValidBook();
BookCreateRequest request = TestFixtures.createValidBookRequest();
```

### 4. Assertions
- **AssertJ**: Uses fluent assertions for better readability
- **Descriptive Messages**: Clear assertion messages for failed tests

Example:
```java
assertThat(response).isNotNull();
assertThat(response.getTitle()).isEqualTo("The Great Gatsby");
assertThat(bookRepository.findByIdAndDeletedFalse(1L)).isPresent();
```

### 5. Nested Test Organization
- **Logical Grouping**: Tests organized in nested classes by functionality
- **Readability**: Clear hierarchy makes it easier to navigate tests
- **Reuse**: Shared setup in parent class

Example:
```java
@Nested
@DisplayName("Get Book Tests")
class GetBookTests {
    // Related tests grouped together
}
```

### 6. MockMvc for Controller Testing
- **Web Layer Testing**: Tests HTTP layer without starting server
- **Request/Response Validation**: Verifies status codes, headers, body content
- **JSON Assertions**: Validates JSON responses

Example:
```java
mockMvc.perform(post("/api/v1/books")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
    .andExpect(status().isCreated())
    .andExpect(jsonPath("$.id").exists());
```

## Test Coverage Goals

Current test coverage:
- **Controllers**: 100% of endpoints tested
- **Services**: 100% of business logic tested
- **Models**: Core entity logic tested
- **Repositories**: Basic CRUD operations validated in integration tests

## Common Testing Patterns

### Testing Exceptions
```java
@Test
void shouldThrowExceptionWhenBookNotFound() {
    when(bookRepository.findByIdAndDeletedFalse(999L))
        .thenReturn(Optional.empty());
    
    assertThatThrownBy(() -> bookService.getBookById(999L))
        .isInstanceOf(ResourceNotFoundException.class)
        .hasMessageContaining("Book not found");
}
```

### Testing Pagination
```java
@Test
void shouldGetAllBooksWithPagination() {
    Page<Book> bookPage = new PageImpl<>(
        List.of(testBook), 
        PageRequest.of(0, 20), 
        1
    );
    when(bookRepository.findByDeletedFalse(any())).thenReturn(bookPage);
    
    Page<BookResponse> response = bookService.findAllBooks(PageRequest.of(0, 20));
    
    assertThat(response.getTotalElements()).isEqualTo(1);
}
```

### Testing Transactions
```java
@Test
@Transactional
void shouldPersistChangesInTransaction() {
    Book book = bookRepository.save(testBook);
    
    // Verify persisted
    Optional<Book> saved = bookRepository.findById(book.getId());
    assertThat(saved).isPresent();
}
```

## Continuous Integration

Tests run automatically on:
- Push to repository
- Pull request creation
- Scheduled nightly builds

Ensure all tests pass before merging:
```bash
mvn clean test -DskipITs=false
```

## Debugging Tests

### Enable Debug Logging
```properties
logging.level.com.bookstore=DEBUG
logging.level.org.hibernate.SQL=DEBUG
```

### Use IDE Debugger
1. Set breakpoint in test
2. Run test with debugging enabled
3. Step through code to identify issues

### Print SQL Statements
```properties
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

## Performance Tips

1. **Use H2 in-memory database**: Fast database operations for tests
2. **Disable logging**: Reduce I/O overhead during tests
3. **Cache test fixtures**: Reuse expensive test data creation
4. **Mock external services**: Avoid slow HTTP calls in tests
5. **Use Transactional**: Automatic rollback of changes

## Adding New Tests

When adding new functionality:

1. Write unit tests first (TDD approach)
2. Mock dependencies to test in isolation
3. Add integration tests for end-to-end validation
4. Verify both success and failure scenarios
5. Use meaningful test names and descriptions
6. Group related tests in nested classes

## Test Maintenance

- Review tests when functionality changes
- Update test fixtures when DTOs change
- Remove duplicate test code
- Keep test data minimal and focused
- Document complex test scenarios

## Resources

- [JUnit 5 Documentation](https://junit.org/junit5/docs/current/user-guide/)
- [Mockito Documentation](https://javadoc.io/doc/org.mockito/mockito-core/latest/org/mockito/Mockito.html)
- [AssertJ Documentation](https://assertj.github.io/assertj-core-features-highlight.html)
- [Spring Boot Testing Guide](https://spring.io/guides/gs/testing-web/)
- [RestAssured Documentation](https://rest-assured.io/)

## Troubleshooting

### Tests timeout
- Increase test timeout in IDE settings
- Check for infinite loops or deadlocks
- Verify database connection settings

### Database constraints violated
- Clear database between tests using `@BeforeEach`
- Use unique test data in each test
- Check for missing cascade deletions

### Mock not working as expected
- Verify mock setup is correct
- Use `ArgumentMatcher` for complex arguments
- Check mock invocation with `verify()`

### Port already in use
- Use port 0 for random port: `server.port=0`
- Kill process using port: `lsof -ti:8080 | xargs kill`

