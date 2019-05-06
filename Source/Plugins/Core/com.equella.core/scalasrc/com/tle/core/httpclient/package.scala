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

package com.tle.core

import java.util.concurrent.Executors

import cats.effect.IO
import com.softwaremill.sttp.SttpBackendOptions
import com.softwaremill.sttp.SttpBackendOptions.{Proxy, ProxyType}
import com.softwaremill.sttp.asynchttpclient.fs2.AsyncHttpClientFs2Backend
import com.tle.legacy.LegacyGuice

import scala.concurrent.ExecutionContext

package object httpclient {
  val blockingEC            = ExecutionContext.fromExecutorService(Executors.newFixedThreadPool(5))
  implicit val contextShift = IO.contextShift(blockingEC)

  implicit lazy val sttpBackend = {
    val proxy     = LegacyGuice.configService.getProxyDetails
    val sttpProxy = Option(proxy.getHost).map(h => Proxy(h, proxy.getPort, ProxyType.Http))
    AsyncHttpClientFs2Backend[IO](SttpBackendOptions.Default.copy(proxy = sttpProxy))
  }
}
