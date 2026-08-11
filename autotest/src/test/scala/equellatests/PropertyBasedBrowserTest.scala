package equellatests

import org.scalatest.funsuite.AnyFunSuite
import org.scalatestplus.scalacheck.Checkers

/** Base for the property-based suites in this package: a ScalaTest suite that can `check`
  * ScalaCheck `Prop`s, so the generators stay in ScalaCheck while ScalaTest provides discovery and
  * reporting.
  *
  * Each property drives a real browser, so one successful evaluation is the useful unit of work
  * rather than the usual hundred. `minSuccessful = 1` preserves the `-s 1` argument these suites
  * were given when they ran under the ScalaCheck runner directly.
  */
trait PropertyBasedBrowserTest extends AnyFunSuite with Checkers {
  implicit override val generatorDrivenConfig: PropertyCheckConfiguration =
    PropertyCheckConfiguration(minSuccessful = 1)
}
