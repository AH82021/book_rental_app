# Book Service

A comprehensive microservice for managing books and categories in the Book Rental Application.

## Features

### Book Management

- ✅ **CRUD Operations**: Create, read, update, and soft delete books
- ✅ **Advanced Search**: Search books by title, author, ISBN, category, price range, and more
- ✅ **Inventory Management**: Track total and available copies, reserve/release functionality
- ✅ **Category Association**: Manage book-category relationships
- ✅ **Status Management**: Handle different book statuses (AVAILABLE, OUT_OF_STOCK, DISCONTINUED)

### Category Management

- ✅ **Hierarchical Categories**: Support parent-child category relationships
- ✅ **Slug-based URLs**: SEO-friendly category URLs
- ✅ **Category Navigation**: Get ancestors, descendants, and hierarchy views
- ✅ **Validation**: Prevent circular references in category hierarchy

### API Features

- ✅ **RESTful Endpoints**: Comprehensive REST API with proper HTTP methods
- ✅ **Pagination**: All list endpoints support pagination and sorting
- ✅ **Validation**: Input validation with meaningful error messages
- ✅ **Exception Handling**: Global exception handling with standardized error responses
- ✅ **API Documentation**: Swagger/OpenAPI 3.0 documentation

## Architecture

### Layers

1. **Controller Layer**: REST endpoints with validation and error handling
2. **Service Layer**: Business logic implementation
3. **Repository Layer**: Data access with JPA and custom queries
4. **Model Layer**: JPA entities with relationships and constraints
5. **DTO Layer**: Data transfer objects for API communication
6. **Mapper Layer**: MapStruct-based entity-DTO mapping

### Key Components

#### Entities

- `Book`: Core book entity with inventory tracking
- `Category`: Hierarchical category structure
- `BookStatus`: Enum for book status management

#### Services

- `BookService`: Comprehensive book management operations
- `CategoryService`: Category and hierarchy management

#### Controllers

- `BookController`: 25+ endpoints for book operations
- `CategoryController`: 15+ endpoints for category management

#### Repositories

- `BookRepository`: Advanced search queries and inventory operations
- `CategoryRepository`: Hierarchy navigation and category queries

## API Endpoints

### Books

- `POST /api/v1/books` - Create book
- `GET /api/v1/books/{id}` - Get book by ID
- `GET /api/v1/books/isbn/{isbn}` - Get book by ISBN
- `PUT /api/v1/books/{id}` - Update book
- `DELETE /api/v1/books/{id}` - Delete book
- `GET /api/v1/books` - List all books
- `GET /api/v1/books/search` - Search books
- `GET /api/v1/books/search/advanced` - Advanced search
- `GET /api/v1/books/category/{categoryId}` - Books by category
- `GET /api/v1/books/available` - Available books
- `PUT /api/v1/books/{id}/inventory` - Update inventory
- `POST /api/v1/books/{id}/reserve` - Reserve copies
- `POST /api/v1/books/{id}/release` - Release copies

### Categories

- `POST /api/v1/categories` - Create category
- `GET /api/v1/categories/{id}` - Get category by ID
- `GET /api/v1/categories/slug/{slug}` - Get category by slug
- `PUT /api/v1/categories/{id}` - Update category
- `DELETE /api/v1/categories/{id}` - Delete category
- `GET /api/v1/categories` - List all categories
- `GET /api/v1/categories/hierarchy` - Category hierarchy
- `PUT /api/v1/categories/{id}/move` - Move category

## Data Model

### Book Fields

- Basic info: title, author, isbn, description
- Publishing: publisher, publication date, pages, language
- Pricing: price, rental price
- Inventory: total copies, available copies
- Media: cover image URL
- Relationships: categories (many-to-many)

### Category Fields

- Basic info: name, slug, description
- Hierarchy: parent, children relationships
- Timestamps: created date
- Relationships: books (many-to-many)

## Business Rules

### Book Management

- ISBN uniqueness validation
- Available copies cannot exceed total copies
- Soft delete for data integrity
- Automatic slug generation for categories

### Category Hierarchy

- No circular references allowed
- Categories with books cannot be deleted
- Categories with subcategories cannot be deleted
- Automatic slug generation from names

## Error Handling

- Standardized error responses with timestamps
- Validation error details included
- Proper HTTP status codes
- Comprehensive exception hierarchy

## Technology Stack

- **Spring Boot 3.5.12**: Core framework
- **Spring Data JPA**: Data persistence
- **PostgreSQL 14+**: Primary database
- **MapStruct**: Object mapping
- **Lombok**: Boilerplate reduction
- **Jakarta Validation**: Input validation
- **Swagger/OpenAPI**: API documentation
- **Docker**: Containerization support

## Implementation Status



- Models with relationships and validation ✅
- Repositories with custom queries
- Services with comprehensive business logic
- Controllers with full REST API
- Exception handling and validation
- MapStruct mappers for clean separation
- Configuration classes for proper setup ✅

## Testing the API

### 🚀 Service Status

The book-service is now running on: **http://localhost:8082/book-service**

### 🔍 Health Check & Monitoring

#### Health Check Endpoint

```bash
# Basic health check
GET http://localhost:8082/book-service/actuator/health

# Detailed health information
curl http://localhost:8082/book-service/actuator/health
```

#### Actuator Endpoints

```bash
# Service information
GET http://localhost:8082/book-service/actuator/info

# Metrics (Prometheus format)
GET http://localhost:8082/book-service/actuator/metrics

# All available endpoints
GET http://localhost:8082/book-service/actuator
```

### 📚 API Documentation

#### Swagger UI

Access the interactive API documentation at:

```
http://localhost:8082/book-service/swagger-ui.html
```

#### OpenAPI Specification

```bash
# JSON format
GET http://localhost:8082/book-service/api-docs

# YAML format
GET http://localhost:8082/book-service/api-docs.yaml
```

### 🧪 Postman Testing

#### Import Collection

Use these base configurations for Postman:

**Base URL**: `http://localhost:8082/book-service`

#### Sample API Tests

##### 1. Health Check

```http
GET http://localhost:8082/book-service/actuator/health
```

##### 2. Get All Books

```http
GET http://localhost:8082/book-service/api/v1/books
```

##### 3. Get All Categories

```http
GET http://localhost:8082/book-service/api/v1/categories
```

##### 4. Create a New Book

```http
POST http://localhost:8082/book-service/api/v1/books
Content-Type: application/json

{
  "title": "Spring Boot in Action",
  "author": "Craig Walls",
  "isbn": "9781617292545",
  "description": "A comprehensive guide to Spring Boot framework",
  "price": 39.99,
  "rentalPrice": 12.99,
  "publicationDate": "2015-12-01",
  "publisher": "Manning Publications",
  "pages": 424,
  "language": "English",
  "coverImageUrl": "https://example.com/spring-boot-action.jpg",
  "totalCopies": 15,
  "categoryIds": [4]
}
```

##### 5. Search Books

```http
GET http://localhost:8082/book-service/api/v1/books/search?keyword=spring
```

##### 6. Advanced Search

```http
GET http://localhost:8082/book-service/api/v1/books/search/advanced?title=spring&minPrice=20&maxPrice=50
```

##### 7. Create a Category

```http
POST http://localhost:8082/book-service/api/v1/categories
Content-Type: application/json

{
  "name": "Spring Framework",
  "description": "Books about Spring Framework and related technologies",
  "parentId": 4
}
```

##### 8. Update Book Inventory

```http
PUT http://localhost:8082/book-service/api/v1/books/1/inventory
Content-Type: application/json

{
  "totalCopies": 20,
  "availableCopies": 15
}
```

##### 9. Reserve Book Copies

```http
POST http://localhost:8082/book-service/api/v1/books/1/reserve
Content-Type: application/json

{
  "quantity": 2
}
```

##### 10. Get Books by Category

```http
GET http://localhost:8082/book-service/api/v1/books/category/1
```

### 📊 Postman Collection Setup

#### Environment Variables

Create a Postman environment with these variables:

```json
{
  "baseUrl": "http://localhost:8082/book-service",
  "bookId": "1",
  "categoryId": "1",
  "isbn": "9780743273565"
}
```

#### Common Headers

Set these as collection-level headers:

```
Content-Type: application/json
Accept: application/json
```

### 🔧 cURL Examples

#### Quick Test Commands

```bash
# Health check
curl http://localhost:8082/book-service/actuator/health

# Get all books with pagination
curl "http://localhost:8082/book-service/api/v1/books?page=0&size=10"

# Get book by ID
curl http://localhost:8082/book-service/api/v1/books/1

# Search books
curl "http://localhost:8082/book-service/api/v1/books/search?keyword=gatsby"

# Get categories
curl http://localhost:8082/book-service/api/v1/categories

# Get category hierarchy
curl http://localhost:8082/book-service/api/v1/categories/hierarchy
```

### 📈 Load Testing

#### Artillery.js Example

```yaml
config:
  target: "http://localhost:8082/book-service"
  phases:
    - duration: 60
      arrivalRate: 10

scenarios:
  - name: "Get all books"
    requests:
      - get:
          url: "/api/v1/books"
  - name: "Health check"
    requests:
      - get:
          url: "/actuator/health"
```

### 🐛 Troubleshooting

#### Common Issues

1. **Connection Refused**

    - Ensure PostgreSQL is running
    - Check if service started successfully: `mvn spring-boot:run`

2. **Database Connection Issues**

    - Verify database exists: `psql -U postgres -l`
    - Test connection: `psql -U postgres -d bookstore_db`

3. **Port Already in Use**
    - Check what's running on port 8082: `lsof -i :8082`
    - Kill process or change port in `application.yml`

#### Logs

Check application logs for detailed error information:

```bash
tail -f logs/application.log
# or check console output from mvn spring-boot:run
```

## Database Migration

This service has been migrated from H2 to PostgreSQL for production-ready deployment.

### Quick Setup

1. **Local PostgreSQL Setup**:

   ```bash
   # Create database
   psql -U postgres -c "CREATE DATABASE bookstore_db;"

   # Start the application
   cd book-service
   mvn spring-boot:run
   ```

2. **Docker Setup (Alternative)**:
   ```bash
   # Run the setup script
   ./setup-postgres.sh
   ```

### Configuration

- **Development**: `jdbc:postgresql://localhost:5432/bookstore_db`
- **Username**: `postgres`
- **Password**: `paassword`
- **Production**: Environment variables supported (`${DB_USERNAME}`, `${DB_PASSWORD}`)

This book service is production-ready and provides a solid foundation for the book rental application's book and category management needs.

## Git Workflow & Branching Strategy

This project follows a structured **Git Flow** branching strategy with clear naming conventions and pull request guidelines.

### 🌿 Branch Types & Naming Conventions

#### 1. **Main Branches**

- **`main`** - Production-ready code. Only merged via PRs with approvals.
  - Protected branch with required code reviews
  - All commits are tagged with version numbers
  - Only receives merges from `release` branches

- **`develop`** - Integration branch for feature development
  - Contains latest development changes
  - All features are merged here before release
  - Base branch for feature development

#### 2. **Supporting Branches**

##### Feature Branches
- **Naming**: `feature/<feature-name>`
- **Base**: `develop`
- **Merge back to**: `develop`
- **Examples**:
  - `feature/book-search-enhancement`
  - `feature/category-hierarchy-optimization`
  - `feature/inventory-management`

##### Bugfix Branches
- **Naming**: `bugfix/<bug-name>`
- **Base**: `develop`
- **Merge back to**: `develop`
- **Examples**:
  - `bugfix/isbn-validation-fix`
  - `bugfix/category-delete-issue`
  - `bugfix/pagination-sorting-bug`

##### Hotfix Branches
- **Naming**: `hotfix/<issue-name>`
- **Base**: `main`
- **Merge back to**: `main` and `develop`
- **Examples**:
  - `hotfix/critical-security-patch`
  - `hotfix/database-connection-leak`

##### Release Branches
- **Naming**: `release/<version>`
- **Base**: `develop`
- **Merge back to**: `main` and `develop`
- **Examples**:
  - `release/v1.0.0`
  - `release/v1.1.0`

##### Documentation Branches
- **Naming**: `docs/<topic>`
- **Base**: `develop`
- **Examples**:
  - `docs/api-documentation`
  - `docs/deployment-guide`

#### 3. **Branch Naming Rules**

✅ **DO**:
- Use lowercase letters
- Separate words with hyphens
- Be descriptive and concise
- Reference issue number if applicable: `feature/ISSUE-123-book-search`

❌ **DON'T**:
- Use underscores in branch names
- Use uppercase letters
- Create branches like `my-branch`, `test`, `temp`
- Commit directly to `main` or `develop`

### 📝 Creating a New Branch

```bash
# Update develop branch
git checkout develop
git pull origin develop

# Create and switch to new branch
git checkout -b feature/your-feature-name

# Push branch to remote
git push -u origin feature/your-feature-name
```

### 📋 Commit Message Conventions

Follow the **Conventional Commits** specification:

```
<type>(<scope>): <subject>

<body>

<footer>
```

#### Commit Types
- **feat**: A new feature
- **fix**: A bug fix
- **docs**: Documentation changes
- **style**: Code style changes (formatting, semicolons, etc.)
- **refactor**: Code refactoring without feature changes
- **perf**: Performance improvements
- **test**: Adding or updating tests
- **chore**: Build process, dependencies, or tooling changes
- **ci**: CI/CD configuration changes

#### Examples

```bash
# Feature
git commit -m "feat(book): add advanced search by price range"

# Bug fix
git commit -m "fix(category): prevent circular reference in hierarchy"

# Documentation
git commit -m "docs(readme): update API endpoint documentation"

# Performance
git commit -m "perf(search): optimize book search query with indexing"
```

### 🔄 Pull Request Guidelines

#### Before Creating a PR

1. **Update your branch with latest develop**
   ```bash
   git fetch origin
   git rebase origin/develop
   ```

2. **Run tests locally**
   ```bash
   mvn clean test
   ```

3. **Build the project**
   ```bash
   mvn clean install
   ```

4. **Check code quality**
   ```bash
   mvn checkstyle:check
   ```

#### PR Title Format

```
<type>(<scope>): <description> [#issue-number]
```

Examples:
- `feat(book): add advanced search by price range #123`
- `fix(inventory): correct available copies calculation #456`
- `docs(readme): update deployment instructions`

#### PR Description Template

Use this template for all pull requests:

```markdown
## Description
Brief description of what this PR accomplishes.

## Type of Change
- [ ] Feature (new feature, non-breaking)
- [ ] Bug Fix (fixes an issue, non-breaking)
- [ ] Breaking Change (feature or fix that would cause existing functionality to change)
- [ ] Documentation Update
- [ ] Refactoring
- [ ] Performance Improvement

## Related Issues
Closes #123 (if applicable)

## Changes Made
- Change 1
- Change 2
- Change 3

## Testing Done
- [ ] Unit tests added/updated
- [ ] Integration tests added/updated
- [ ] Manual testing completed
- [ ] API endpoints tested with Postman/cURL

## Screenshots/Logs (if applicable)
Add screenshots or logs demonstrating the changes.

## Database Changes
- [ ] No database changes
- [ ] Migration added
- [ ] Schema modified (describe below)

Database changes:
```

## Performance Impact
- [ ] No performance impact
- [ ] Improves performance (describe below)
- [ ] May impact performance (describe mitigation)

Impact description:

## Checklist
- [ ] Code follows style guidelines
- [ ] I have performed a self-review
- [ ] Comments added for complex logic
- [ ] Documentation updated
- [ ] No new warnings generated
- [ ] Tests pass locally
- [ ] Ready for code review

## Reviewer Notes
Any additional notes for reviewers.
```

#### PR Review Process

1. **Author** creates PR from feature branch to `develop` (or `main` for hotfixes)
2. **Reviewers** (minimum 2 for production changes):
   - Review code quality and logic
   - Check for breaking changes
   - Verify tests are included
   - Request changes if needed
3. **Author** addresses feedback and updates PR
4. **Approver** merges PR after approval
5. **CI/CD** pipeline runs automated tests

#### PR Review Checklist for Reviewers

- [ ] Code follows project standards
- [ ] No security vulnerabilities
- [ ] Tests cover the changes
- [ ] Documentation is updated
- [ ] No hardcoded values
- [ ] Error handling is proper
- [ ] Database queries are optimized
- [ ] Backward compatibility maintained
- [ ] No breaking changes

### 🚀 Release Process

1. **Create Release Branch**
   ```bash
   git checkout -b release/v1.2.0 develop
   ```

2. **Update Version Numbers**
   - Update `pom.xml` version
   - Update version in documentation
   - Create CHANGELOG entry

3. **Create PR** from `release/v1.2.0` to `main`

4. **After Approval**
   ```bash
   git checkout main
   git merge --no-ff release/v1.2.0
   git tag -a v1.2.0 -m "Version 1.2.0 release"
   git push origin main
   git push origin v1.2.0
   ```

5. **Merge back to develop**
   ```bash
   git checkout develop
   git merge --no-ff release/v1.2.0
   git push origin develop
   git branch -d release/v1.2.0
   ```

### 🔥 Hotfix Process

1. **Create Hotfix Branch from main**
   ```bash
   git checkout -b hotfix/critical-bug main
   ```

2. **Fix the issue and test thoroughly**

3. **Create PR** to `main` with detailed explanation

4. **After Approval**
   ```bash
   git checkout main
   git merge --no-ff hotfix/critical-bug
   git tag -a v1.2.1 -m "Hotfix: critical-bug"
   git push origin main
   ```

5. **Merge back to develop**
   ```bash
   git checkout develop
   git merge --no-ff hotfix/critical-bug
   git push origin develop
   git branch -d hotfix/critical-bug
   ```

### 📊 Git Aliases (Optional)

Add these aliases to your `.gitconfig` for convenience:

```bash
git config --global alias.co checkout
git config --global alias.br branch
git config --global alias.ci commit
git config --global alias.st status
git config --global alias.unstage 'reset HEAD --'
git config --global alias.last 'log -1 HEAD'
git config --global alias.visual 'log --graph --oneline --all'
```

### 🔐 Best Practices

1. **Keep branches short-lived** - Merge within 3-5 days to avoid conflicts
2. **Rebase before merge** - Keep history clean with `git rebase develop`
3. **Never force push** - To `main` or `develop` branches
4. **Use meaningful commits** - One change per commit
5. **Write clear PR descriptions** - Help reviewers understand context
6. **Review your own code first** - Before requesting reviews
7. **Keep commits atomic** - Each commit should be logically complete
8. **Test locally before pushing** - Avoid CI/CD pipeline failures
9. **Delete merged branches** - Keep repository clean
10. **Use issue references** - Link commits to issues/tasks

### 🆘 Common Git Commands

```bash
# Create and checkout feature branch
git checkout -b feature/new-feature develop

# View all branches
git branch -a

# Delete local branch
git branch -d feature/old-feature

# Delete remote branch
git push origin --delete feature/old-feature

# Sync with latest develop
git fetch origin && git rebase origin/develop

# View commit history
git log --oneline --graph --all

# Undo last commit (keep changes)
git reset --soft HEAD~1

# Discard local changes
git checkout -- file.txt

# Stash changes temporarily
git stash
git stash pop
```

### 📞 Need Help?

For questions about git workflow, branching, or pull requests:
1. Check existing documentation
2. Review similar PRs
3. Ask in team communication channels
4. Create an issue with `help-wanted` label

