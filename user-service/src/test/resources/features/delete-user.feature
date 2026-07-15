Feature: Delete User

  Scenario Outline: Delete a user given a system status
    Given a delete <system_status> system
    When I attempt to delete a user
    Then the system should <action> the user

    Examples:
      | system_status | action    |
      | registered    | delete    |
      | not found     | not delete |
