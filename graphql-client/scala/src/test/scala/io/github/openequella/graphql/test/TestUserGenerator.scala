/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional
 * information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0, (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.openequella.graphql.test

import scala.util.Random

/** Test user details used when creating temporary internal users. */
case class TestUser(firstName: String, lastName: String, password: String)

/** Generates sensible randomized user details for tests. */
object TestUserGenerator {
  private val firstNames =
    Vector("Alex", "Jordan", "Taylor", "Morgan", "Sam", "Jamie", "Casey", "Riley", "Avery", "Quinn")
  private val lastNames =
    Vector(
      "Smith",
      "Jones",
      "Miller",
      "Davis",
      "Wilson",
      "Taylor",
      "Brown",
      "Moore",
      "Clark",
      "Hall"
    )

  private def randomElement[A](elements: IndexedSeq[A]): A =
    elements(Random.nextInt(elements.length))

  /** Generates a single test user with sensible randomized fields. */
  def next(): TestUser = {
    val firstName = randomElement(firstNames)
    val lastName  = randomElement(lastNames)
    val password  = s"$firstName#${Random.alphanumeric.take(8).mkString}"

    TestUser(firstName, lastName, password)
  }
}
