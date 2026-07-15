Feature: Verify Email

  Scenario Outline: Verify a user's email given a system status
    Given a verify email <system_status> system
    When I attempt to verify the user email with <otk>
    Then the system should <action> the email

    Examples:
      | system_status | otk     | action         |
      | registered    | correct | confirm        |
      | registered    | wrong   | invalid request |
      | not found     | correct | invalid request |
