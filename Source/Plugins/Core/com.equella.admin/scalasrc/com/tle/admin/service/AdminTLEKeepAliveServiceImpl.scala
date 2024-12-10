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

import com.tle.admin.rest.{RestConfiguration, StatusApi}
import org.slf4j.{Logger, LoggerFactory}

import java.util.concurrent.TimeUnit
import java.util.{Timer, TimerTask}
import javax.inject.{Inject, Singleton}

@Singleton
class AdminTLEKeepAliveServiceImpl @Inject() (implicit cfg: RestConfiguration)
    extends AdminTLEKeepAliveService {

  private val LOGGER: Logger = LoggerFactory.getLogger(classOf[AdminTLEKeepAliveServiceImpl])
  private var task: Option[TimerTask] = None

  override def start(): Unit = {
    LOGGER.info("Starting keep-alive service")
    if (task.isEmpty) {
      LOGGER.debug("Starting new background thread for keep-alive service")
      val daemon = new Timer("keep-alive", true)
      val tt = new TimerTask {
        override def run(): Unit = {
          StatusApi.heartbeat
        }
      }
      daemon.schedule(tt, 0, TimeUnit.MINUTES.toMillis(2))
      task = Some(tt)
    } else {
      // Proper usage should mean this never happens, so let's log it in case it does
      LOGGER.warn("Keep-alive service already running")
    }
  }

  override def stop(): Unit = {
    LOGGER.info("Stopping keep-alive service")
    task.foreach(_.cancel())
    task = None
  }
}
