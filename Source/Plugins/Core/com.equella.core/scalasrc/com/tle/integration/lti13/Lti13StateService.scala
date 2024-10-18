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

package com.tle.integration.lti13

import com.tle.core.guice.Bind
import com.tle.core.replicatedcache.ReplicatedCacheService
import com.tle.integration.oauth2.{OAuth2StateService, StateConfig}
import java.net.URI
import javax.inject.{Inject, Singleton}

/**
  * Details for an entry being stored in state.
  *
  * @param platformId The ID of the platform which this state is linked to. (VALIDATION)
  * @param loginHint The initial login hint for the connection. (VALIDATION - maybe)
  * @param targetLinkUri The intended link to be launched as per the initial request. Once
  *                      authentication is complete, this link should be redirected to.
  */
@SerialVersionUID(1)
case class Lti13StateDetails(platformId: String, loginHint: String, targetLinkUri: URI)
    extends Serializable

/**
  * Manages the `state` values for LTI 1.3 Authentication processes where a state is valid for 10 seconds.
  */
@Bind
@Singleton
class Lti13StateService @Inject()(rcs: ReplicatedCacheService)
    extends OAuth2StateService[Lti13StateDetails](rcs, StateConfig(10, "lti13-state"))
