Feature: Login User

  Scenario Outline: Login a user with login statuses
    Given a login <system_status> system
    When I attempt to login with <credentials>.
    Then the login system should respond with <expected_response>.

    Examples:
      | system_status | credentials | expected_response |
      | registered    | correct     | login             |
      | registered    | wrong       | failure           |
      | not found     | correct     | failure           |
      | blocked       | correct     | blocked           |
