# Writing stateful browser property tests

Property based tests are a style of testing which relies on randomly 
generating test data and verifying that the properties of the system hold. 

I won't go into why this style of testing is better than your traditional unit 
testing as there are plenty of other people who have discussed that:

* See the presentations on the [Scalacheck documentation](https://scalacheck.org/documentation.html)
* [PropEr Testing](https://propertesting.com/) - a book about property based testing in Erlang. The concepts still apply here.

## Stateful tests

Normally property tests shine when they're testing pure functions as you can generate 
a relatively large number of random test cases, typically 100, and test the properties in quick succession.

Unfortunately this is not always practical when you are testing a stateful system 
as the cost of resetting the system for each case can be prohibitive. 
(E.g. dropping and re-creating a database). Generally the solution to this problem is
to just drop the number of test cases to a lower number, say 5 or 10, that won't take as long but 
still give you confidence that the tested properties hold.

There is a particular technique of stateful testing that is really suited to browser UI testing 
which is described in [PropEr](https://propertesting.com/book_stateful_properties.html).

To quote `PropEr`:

*Stateful property tests are particularly useful when "what the code should do" - what 
the user perceives-is simple, but "how the code does it" -how it is implemented- is complex.*

In a nutshell the technique involves:

* Defining a simplified model of the system you are testing
* Defining commands which represent the execution flow
* Verifying that the system matches the model as commands are executed

## How this works here

These suites live in `src/test/scala/equellatests`, and the tests themselves in
`equellatests/tests`. They used to run under ScalaCheck's own runner; they now run under ScalaTest,
which drives ScalaCheck through the `scalatestplus/scalacheck-1-19` bridge. The generators and
`Prop`s are unchanged — ScalaTest supplies discovery and reporting, and ScalaCheck still supplies
the data.

Extend `PropertyBasedBrowserTest` and `check` your properties. It sets `minSuccessful = 1`, because
each evaluation drives a real browser: one successful run is the useful unit of work here, not the
usual hundred.

`StatefulTest` is where the technique above is implemented — a model, a set of commands, and the
machinery to run them against a browser and compare. `ShotTest` adds screenshots on failure.

Run them with:

```bash
./sbt "project autotest" ScalaTest/test
./sbt "project autotest" "ScalaTest/testOnly equellatests.tests.SanityTest"
```

They are discovered by test framework rather than by package, so a new ScalaTest suite is picked up
wherever you put it. See the [README](../README.md) for why the two frameworks have separate
configurations.


