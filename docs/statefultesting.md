# Writing stateful browser property tests

Property based tests are a style of testing which relies on randomly 
generating test data and verifying that the properties of the system hold. 

I won't go into why this style of testing is better than your traditional unit 
testing as there are plenty of other people who have discussed that:

* See the presentations on the [Scalacheck documentation](http://scalacheck.org/documentation.html)
* [PropEr Testing](http://propertesting.com/) - a book about property based testing in Erlang. The concepts still apply here.

## Stateful tests

Normally property tests shine when they're testing pure functions as they can throw 
large numbers, typically 100, of random test parameters at a function and test 
the properties in quick succession.

Unfortunately this is not always practical when you are testing a stateful system 
as the cost of resetting the system for each case can be prohibitive. 
(E.g. dropping and re-creating a database). Generally the solution to this problem is
to just drop the number of test cases to a lower number, say 5, that won't take as long.

TODO

