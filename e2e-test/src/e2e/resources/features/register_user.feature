Feature: Register user

  Scenario: A user registers a new account
    Given a user which doesnt exist yet
    When the user registers its info
    Then the user receives a personal auth token
    And the user can query its info
