@example @publicapis
Feature: Testing

  Background:
    Given a base uri "https://api.publicapis.org"
    Given a base path "entries"
    And a port 443


  Scenario: [1] Example Scenario
     Given query parameters
       |category|animals|
       |https|true|
     #Given headers
     #    |fake|heading|
    When the system requests GET
      # Note that with the base uri and path this will GET https://api.publicapis.org/entries?category=animals&https=true
    Then the response code is 200
    And the response body contains
      |count|equals|11|int|
      |entries[0].API|equals|Cat Facts|text|
    # Options:
      ## is,equals,hasItem,hasItems,contains,containsAnyOrder,hasSize,isNull,startsWith,endsWith,containsString
      ## All can be nulled by appending ! to start
      ## int, num (float), anything else is treated as string
    And the response body is a valid JSON
    # Options:
      ## JSON, XML, CVS (note CVS check is very bad)
    And the response time is less than 10000 milliseconds