# Writing stateful browser property tests

Property based tests are a style of testing which relies on randomly 
generating test data and verifying that the properties of the system hold. 

I won't go into why this style of testing is better than your traditional unit 
testing as there are plenty of other people who have discussed that:

* See the presentations on the [Scalacheck documentation](http://scalacheck.org/documentation.html)
* [PropEr Testing](http://propertesting.com/) - a book about property based testing in Erlang. The concepts still apply here.

## Stateful tests

Normally property tests shine when they're testing pure functions as you can generate 
a relatively large number of random test cases, typically 100, and test the properties in quick succession.

Unfortunately this is not always practical when you are testing a stateful system 
as the cost of resetting the system for each case can be prohibitive. 
(E.g. dropping and re-creating a database). Generally the solution to this problem is
to just drop the number of test cases to a lower number, say 5 or 10, that won't take as long but 
still give you confidence that the tested properties hold.

There is a particular technique of stateful testing that is really suited to browser UI testing 
which is described in [PropEr](http://propertesting.com/book_stateful_properties.html).

To quote `PropEr`:

*Stateful property tests are particularly useful when "what the code should do" - what 
the user perceives-is simple, but "how the code does it" -how it is implemented- is complex.*

In a nutshell the technique involves:

* Defining a simplified model of the system you are testing
* Defining commands which represent the execution flow
* Verifying that the system matches the model as commands are executed


