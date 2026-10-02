Feature: Munchies user stories
  Acceptance scenarios derived from docs/user-stories/user-stories.md.
  Steps follow the vocabulary of the e2e-test module: "an authenticated client"
  is a client that already registered and holds a valid personal auth token.

  # ---------------------------------------------------------------
  # Authentication and Account Management
  # ---------------------------------------------------------------

  Scenario: User registration
    Given a user which doesnt exist yet
    When the user registers its info
    Then the user receives a personal auth token
    And the user can query its info

  Scenario: User login
    Given a user which is registered
    When the user logs in with its credentials
    Then the user receives a personal auth token
    And the user can access the protected features

  Scenario: Update user profile
    Given a user which is registered
    When the user updates its profile
    Then only the correct changes are allowed
    And the user has its profile updated

  # ---------------------------------------------------------------
  # Restaurant Browsing
  # ---------------------------------------------------------------

  Scenario: Browse restaurant menu
    Given an authenticated client
    And a restaurant with a menu exists
    When the client browses the restaurants
    And the client opens the restaurant menu
    Then the client sees the restaurant and its dishes
    And each dish shows its details and availability

  # ---------------------------------------------------------------
  # Menu Management
  # ---------------------------------------------------------------

  Scenario: Restaurant staff marks a dish as unavailable
    Given an authenticated restaurant staff member
    And a restaurant with an available dish
    When the staff member marks the dish as unavailable
    Then the dish is shown as unavailable in the menu
    And clients cannot order the dish

  # ---------------------------------------------------------------
  # Order Management
  # ---------------------------------------------------------------

  Scenario: Place order
    Given an authenticated client
    And a valid delivery order
    When the client places the order
    Then order is created successfully

  Scenario: Pay order
    Given an authenticated client
    And a valid delivery order
    And the client places the order
    When the client pays for the order
    Then the payment is completed successfully
    And the order is confirmed

  Scenario: Track order status
    Given an authenticated client
    And a valid delivery order
    When the client places the order
    And the client advances the order status
    Then order status is advanced successfully

  # ---------------------------------------------------------------
  # Notifications
  # ---------------------------------------------------------------

  Scenario: Payment success notification
    Given an authenticated client
    And a valid delivery order
    And the client places the order
    When the client pays for the order
    Then the payment is completed successfully
    And notification-service should have logged the payment confirmation

  # ---------------------------------------------------------------
  # Table Reservations
  # ---------------------------------------------------------------

  Scenario: Reserve a table
    Given an authenticated client
    And a restaurant with a free table
    When the client reserves a table at the restaurant
    Then the reservation is created successfully
