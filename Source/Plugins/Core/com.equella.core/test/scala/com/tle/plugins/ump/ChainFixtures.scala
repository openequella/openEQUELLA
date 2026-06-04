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

package com.tle.plugins.ump

import org.mockito.Mockito.mock
import scala.jdk.CollectionConverters._
import java.util

/** Shared test fixtures for [[UserDirectory]] plugin chain tests.
  *
  * Mix this trait into any test class that needs a pre-built empty or single-plugin chain.
  */
trait ChainFixtures {
  // A single mocked user directory plugin.
  val ud: UserDirectory = mock(classOf[UserDirectory])

  // A chain containing no plugins.
  val emptyChain: util.List[UserDirectory] = List.empty[UserDirectory].asJava

  // A chain containing a single plugin.
  val singlePluginChain: util.List[UserDirectory] = List(ud).asJava
}
