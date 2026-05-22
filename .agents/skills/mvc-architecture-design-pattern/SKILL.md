---
name: mvc
description: This skill should be used when the user asks about "mvc pattern", "model view controller", "when to use mvc", "separate concerns", "ui architecture", or mentions needing to separate data, presentation, and user input handling.
version: 1.0.0
---

# MVC (Model-View-Controller) Pattern

## What Is It
MVC separates an application into three components: Model (data and business logic), View (presentation/UI), and Controller (handles user input and coordinates Model and View). This separation enables independent development, testing, and maintenance.

## When to Use
- **Web applications** - Traditional server-rendered apps
- **Desktop applications** - GUI applications with clear separation
- **Mobile apps** - iOS (UIKit) and Android development
- **Frameworks** - Ruby on Rails, Django, ASP.NET MVC
- **Testable UI** - Need to test logic without UI
- Building medium to large applications with complex business logic and data models
- Creating data-driven applications requiring multiple user interfaces (web + mobile + API)
- Team projects where designers and developers need to work independently
- Applications expected to grow over time with evolving requirements
- Systems requiring comprehensive automated testing of business logic
- Multi-platform products sharing core logic across different frontends
- When you need clear separation between data persistence, presentation, and user interaction

## When NOT to Use
- **Simple apps** - Overkill for basic applications
- **Real-time updates** - Consider MVP or MVVM instead
- **Modern SPAs** - React/Vue/Angular use component-based approach
- **Small teams** - May add unnecessary complexity

## The Process & How to Implement

### Step 1: Define the Model - Data and Business Logic
Create Model classes representing domain entities and encapsulating all business rules. Models should be completely independent of UI concerns.

**Ask:** "What are my core domain objects? What business rules govern their behavior?"

**Implementation:**
- Create classes for each domain entity (User, Order, Product, etc.)
- Encapsulate business logic methods (calculateTotal(), validateEmail(), etc.)
- Define data persistence interfaces (repository pattern)
- Ensure Models have no knowledge of Views or Controllers

**Example:** `UserModel` class with properties (id, email, hashedPassword) and methods (authenticate(), updateProfile(), validatePasswordStrength())

### Step 2: Design Views - Presentation Layer
Build Views that render Model data to users. Views should contain only presentation logic - no business rules or data manipulation.

**Ask:** "How will I display this Model data? What UI elements are needed?"

**Implementation:**
- Create templates/components for each screen or UI section
- Accept Model data as input (passed from Controller)
- Use templating syntax to display dynamic data
- Handle formatting and presentation concerns only
- Define UI events (button clicks, form submissions) but don't implement handlers

**Example:** `UserProfileView` template displaying user.email, user.name with an edit button that triggers a Controller action

### Step 3: Implement Controllers - Input Handling and Flow Control
Create Controllers that handle user input, coordinate between Models and Views, and control application flow.

**Ask:** "What user actions need handling? How do I coordinate Models and Views?"

**Implementation:**
- Define route handlers (web) or action methods (desktop/mobile)
- Parse and validate user input
- Call appropriate Model methods to execute business logic
- Select which View to render based on results
- Pass Model data to Views for display
- Handle errors and edge cases

**Example:** `UserController.updateProfile(request)` validates input, calls `userModel.updateProfile(data)`, then renders `UserProfileView` with updated user data

### Step 4: Establish Data Flow - Request-Response Cycle
Wire together the MVC components with clear data flow: User → Controller → Model → Controller → View.

**Ask:** "How does data flow from user action to screen update?"

**Standard Flow:**
1. User interacts with View (clicks button, submits form)
2. View sends event to Controller
3. Controller processes input and calls Model methods
4. Model executes business logic and updates data
5. Controller receives Model response
6. Controller selects appropriate View and passes Model data
7. View renders updated UI

**Example:** Form submission → FormController.submit() → OrderModel.createOrder() → returns Order object → OrderController passes to OrderConfirmationView → displays confirmation

### Step 5: Maintain Separation of Concerns
Continuously enforce boundaries: Models don't know about Views, Views don't contain business logic, Controllers coordinate but don't implement domain logic.

**Ask:** "Is each component focused on its single responsibility?"

**Red Flags:**
- Model calling View rendering methods (violates separation)
- View containing SQL queries or business calculations
- Controller implementing complex business rules (belongs in Model)
- Passing raw request objects to Models (Controller should parse first)

**Remediation:** Extract business logic to Model methods, move presentation logic to Views, keep Controllers thin (validate input, call Models, select Views)

### Step 6: Test Components Independently
Leverage MVC separation to write focused, fast tests for each component in isolation.

**Testing Strategy:**
- **Model Tests:** Unit tests for business logic without UI dependencies
- **View Tests:** Render tests with mock data, no business logic execution
- **Controller Tests:** Integration tests mocking Models and Views

**Example:** Test `OrderModel.calculateTotal()` with various product combinations without touching Controllers or Views - pure business logic validation

## Code Examples

### TypeScript Implementation

```typescript
// Model
class UserModel {
  private users: User[] = [];
  private observers: (() => void)[] = [];

  addUser(user: User): void {
    this.users.push(user);
    this.notify();
  }

  subscribe(observer: () => void): void {
    this.observers.push(observer);
  }

  private notify(): void {
    this.observers.forEach(o => o());
  }
}

// View
class UserView {
  render(users: User[]): string {
    return users.map(u => `<li>${u.name}</li>`).join('');
  }
}

// Controller
class UserController {
  constructor(private model: UserModel, private view: UserView) {}

  handleAddUser(name: string): void {
    this.model.addUser({ id: Date.now(), name });
    this.view.render(this.model.getUsers());
  }
}
```

See `examples/` directory for runnable TypeScript implementations:
- `examples/mvc.ts` - Basic implementation with user management
- `examples/mvc-advanced.ts` - Real-world example with todo app

## Example Application

**Situation:** Building an e-commerce platform requiring web interface, mobile app, and admin dashboard - all sharing the same product catalog and order processing.

**Application of MVC:**

**Models:**
- `ProductModel`: Manages product data (SKU, price, inventory), validates stock availability
- `OrderModel`: Handles order creation, calculates totals with tax/shipping, processes payments
- `UserModel`: Manages authentication, order history, wishlist functionality

**Views:**
- `WebProductListView`: HTML template displaying products in grid layout
- `MobileProductListView`: Native mobile UI with card-based design
- `AdminProductListView`: Data table with bulk editing capabilities
- All Views render the same `ProductModel` data in different formats

**Controllers:**
- `ProductController`: Handles search requests, filtering, sorting - coordinates between user input and ProductModel queries, passes results to appropriate View based on platform (web/mobile/admin)
- `OrderController`: Processes checkout flow, calls OrderModel.createOrder(), handles payment success/failure, renders confirmation or error Views

**Outcome:** Developers build business logic once in Models, designers create platform-specific Views independently, Controllers route requests appropriately - enabling parallel development and code reuse across three platforms.

## Common Pitfalls

**Fat Controllers:** Controllers containing business logic that belongs in Models - leads to duplication when multiple Controllers need the same logic. **Fix:** Extract to Model methods.

**Smart Views:** Views with embedded business calculations or data manipulation - makes testing difficult and violates separation. **Fix:** Move logic to Models, pass computed data from Controllers.

**Model-View Coupling:** Models directly triggering View updates or knowing about UI state. **Fix:** Use observer pattern or let Controllers mediate.

**Skipping the Model Layer:** Placing database queries directly in Controllers - eliminates testability and reusability. **Fix:** Always abstract data access through Model classes.

**Overly Thin Models:** Anemic domain models with only getters/setters, all logic in Controllers - wastes MVC benefits. **Fix:** Rich domain models with business logic encapsulated.

## Key Insights

**Parallel Development:** Designers work on Views using mock data while engineers implement Models - dramatically speeds development for medium+ teams.

**Framework Independence:** Well-designed Models can outlive UI frameworks - migrate from web to mobile without rewriting business logic.

**Testability Sweet Spot:** Testing Models (business logic) is fast and comprehensive without UI dependencies - catches most bugs where they matter.

**Not Always Optimal:** Simple CRUD applications may find MVC overhead excessive - consider simpler patterns. Very complex domains may need additional layers (services, repositories).

**Evolution Path:** MVC often evolves into more sophisticated patterns (MVVM for reactive UIs, Clean Architecture with additional layers) as applications grow - provides solid foundation for architectural progression.

## Related Patterns
- **Observer / Observer Pattern:** Enables Models to notify Controllers of changes without direct coupling (Model notifies View of changes)
- **Strategy:** View can use different rendering strategies
- **Composite:** Views often form composite structures
- **Repository Pattern:** Often used within Models to abstract data persistence
