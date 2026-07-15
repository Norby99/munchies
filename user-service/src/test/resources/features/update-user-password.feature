Feature: Update User Password

  Scenario Outline: Update a user's password given a system status
    Given an update password <system_status> system
    When I attempt to update the user password with <credentials>
    Then the system should <action> the password

    Examples:
      | system_status | credentials | action               |
      | registered    | correct     | update               |
      | registered    | wrong       | wrong credentials    |
      | blocked       | correct     | locked               |
      | unauthorized  | correct     | unauthorized         |
      | not found     | correct     | not found            |
