Feature: Update User Info
  Scenario Outline: Update a user's info given a system status
    Given a update user info <system_status> system
    When I attempt to update a user info
    Then the system should <action> to the changed user

    Examples:
      | system_status | action    |
      | registered    | update    |
      | not found     | not found |
      | invalid       | fail      |