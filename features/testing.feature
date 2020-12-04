@example
Feature: Testing

  Background:
    Given a base uri "http://jsonplaceholder.typicode.com"
    Given a base path "todos"
    And a port 80


  Scenario: [1] Example Scenario
     #Given query parameters
     #  |param|here|
     #Given headers
     #    |fake|heading|
    When the system requests GET "1"
      # Note that with the base uri and path this will GET http://jsonplaceholder.typicode.com/todos/1
    Then the response code is 200
    And the response body contains
      |userId|equals|1|int|
      |title|startsWith|del|text|
    # Options:
      ## is,equals,hasItem,hasItems,contains,containsAnyOrder,hasSize,isNull,startsWith,endsWith,containsString
      ## All can be nulled by appending ! to start
      ## int, num (float), anything else is treated as string
    And the response body is a valid JSON
    # Options:
      ## JSON, XML, CVS (note CVS check is very bad)
    And the response time is less than 10000 milliseconds
  Scenario: [2] Example Scenario
     #Given parameters
     #  |param|here|
     #Given headers
     #    |fake|heading|
    When the system requests GET "1"
      # Note that with the base uri and path this will GET http://jsonplaceholder.typicode.com/todos/1
    Then the response code is 200
    And the response body contains
      |userId|equals|1|int|
      |title|startsWith|del|text|
    # Options:
      ## is,equals,hasItem,hasItems,contains,containsAnyOrder,hasSize,isNull,startsWith,endsWith,containsString
      ## All can be nulled by appending ! to start
      ## int, num (float), anything else is treated as string
    And the response body is a valid JSON
    # Options:
      ## JSON, XML, CVS (note CVS check is very bad)
    And the response time is less than 10000 milliseconds