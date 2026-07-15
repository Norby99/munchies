Feature: Register User

  Scenario Outline: Register a user given a system status
    Given a register <system_status> system
    When I fill in the registration form with valid information
    Then the system should <action> a new user account

    Examples:
      | system_status | action     |
      | blank         | create     |
      | registered    | not create |