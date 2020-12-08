# Cucumber-api-tester

This tool allows you to run feature test files using cucumber using an API testing based runner in multiple ways. 

It is recomended that you run the example features first, then write your features. 

## Running the example
### Bash
To build
```sh
$ git clone git@github.com:DayneD89/cucumber-api-tester.git
$ cd cucumber-api-tester
$ mvn package
```
To run (console)
```sh
$ java -jar target/api-tester-0.0.6-Alpha-jar-with-dependencies.jar example
```
To run (user input)
```sh
$ java -jar target/api-tester-0.0.6-Alpha-jar-with-dependencies.jar
```

### IDEA
To build
 - Clone and open project
 - Import dependencies
 - Ensure you have latest cucumber and gherkin plugins

To run (specific test/scenario)
 - Go to the test or scenario you want to run and rick click -> run test/scenario

To run (header)
 - Run main.java. For example by rick clicking on main function in file -> run main();
 

## Writing feature files
Feature files take the form of:
```gherkin
@HEADER
Feature: FEATURE NAME
  Background:
    Given ...
    Given ...
  Scenario: SCENARIO NAME
     Given ...
     When ...
     Then ...
  Scenario: SCENARIO NAME
     Given ...
     And ...
     When ...
     Then ...
     And ...
```
Using the 'And' keyword will work as if using the last Given/When/Then keyword used.
### Header
This determines what tests will be run when a test case is selected by header. Headers take the form of @Foo to create a Foo header. @~ignore can be used to ignore files even if the headers match. Files can have as many headers as desired, and all scenarios in a file will share all of that files headers. 
### Background
Commands places in the background section will effect all scenarios in that file, while commands inside a scenario will only be in effect until the next scenario. 
### Given
| Command        | Effect     |
| :------------- | -----------: |
|  a base uri "\<URI>" | Sets the host to \<URL>    |
|  a base path "\<PATH>" | Sets the path to \<PATH>    |
|  a base port \<PORT> | Sets the host to \<PORT>    |
|  (form/query/path)? parameters | Sets parameters. Takes in a table. *see below*   |
|  headers | Sets headers. Takes in a table. *see below*   |
|  a JSON body "\<JSON-STR>" | Sets the body to \<JSON-STR> and sets the content-type header   |

### When
| Command        | Effect     |
| :------------- | -----------: |
|  the system requests \<METHOD> | Gets response using \<METHOD>    |
|  the system requests \<METHOD> "\<PATH>" | Sets \<PATH> and gets response using \<METHOD>    |


### Then
| Command        | Effect     |
| :------------- | -----------: |
|  the response code is \<CODE> | Verifies response code matches \<CODE>    |
|  the response body is (not)? a valid \<JSON\CSV\XML> | Verifies response body is a valid \<JSON\CSV\XML>    |
|  the response time is less than \<TIME> | Verifies response time is under \<TIME> ms   |
|  the response header contains | Verifies response headers match. Takes in a table *see below*  |
|  the response body contains  | Verifies response body matches. Takes in a datatable *see below*  |

### (Data)?Tables
Tables allow you to pass data into the code. For example, to pass in headers you could use;
```gherkin
Given headers
    |Content-Encoding|gzip|
    |Connection|keep-alive|
```
to set the Content-Encoding and Connection request headers.

DataTables allow you to pass even more data into the 'Then the response body contains' step. Here is an example;
```gherkin
Then the response body contains
    |count|equals|11|int|
    |entries[0].API|equals|Cat Facts|text|
```
This takes in 4 items per row, the lookup-element, the matcher, the expected-value and the type. 
This step will pass if it is
1. Able to find $lookup-element in the response body.
2. $response[$lookup-element] is of type $type
3. Hamcrest can validate $response[$lookup-element] and $expected-value using $matcher

lookup-element may use [n] to retrieve the nth object from an array, or object.nestedobject to test nestedobject that is isside object

### Matchers
Matchers can be made negative by placing '!' as the first character.
Some Matchers can be made if-ey by placing '~' as the first character.
 - equals,equal,is
 - hasitem
 - hasitems (comma delim)
 - contains
 - hassize
 - startswith
 - endswith