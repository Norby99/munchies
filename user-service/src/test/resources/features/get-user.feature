Feature: Get User

  Scenario Outline: Get user information with various registration states
    Given a <registration_status> user
    When I query the system with the user id
    Then the system should respond with <expected_response>

    Examples:
      | registration_status | expected_response |
      | registered          | user_info         |
      | unregistered        | not_found         |