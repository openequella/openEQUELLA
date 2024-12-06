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

package com.tle.admin.service

import com.tle.admin.rest.{AuthApi, LegacyContentApi, RestConfiguration, StatusApi}
import org.slf4j.{Logger, LoggerFactory}

import java.util.Optional
import javax.inject.{Inject, Singleton}

@Singleton
class AdminTLELoginServiceImpl @Inject()(implicit cfg: RestConfiguration)
    extends AdminTLELoginService {
  private val LOGGER: Logger = LoggerFactory.getLogger(classOf[AdminTLELoginServiceImpl])

  override def keepAlive(): Unit = StatusApi.heartbeat

  override def logout(): Unit = {
    LOGGER.info("Logging out")
    AuthApi.logout
  }

  override def getLoggedInUserId: Optional[String] = {
    LegacyContentApi.currentUserDetails match {
      case Left(_) =>
        LOGGER.error("Failed to get current user details")
        Optional.empty()
      case Right(userDetails) => Optional.of(userDetails.id)
    }
  }
}
