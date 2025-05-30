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

import cats.effect.IO
import com.tle.legacy.LegacyGuice
import sttp.capabilities.fs2.Fs2Streams
import sttp.client3.SttpBackendOptions.{Proxy, ProxyType}
import sttp.client3.asynchttpclient.fs2.AsyncHttpClientFs2Backend
import sttp.client3.{Request, Response, SttpBackendOptions}

package object httpclient {

  private val sttpBackend = {
    val proxy     = LegacyGuice.configService.getProxyDetails
    val sttpProxy = Option(proxy.getHost).map(h => Proxy(h, proxy.getPort, ProxyType.Http))
    AsyncHttpClientFs2Backend.resource[IO](SttpBackendOptions.Default.copy(proxy = sttpProxy))
  }

  /** Sends the given HTTP request using a `sttp` backend based on `AsyncHttpClientFs2Backend`.
    *
    * This method delegates to the configured backend to perform the request and return the
    * response. It uses `Fs2Streams[IO]` as the streaming capability, allowing streamed request or
    * response bodies.
    *
    * @param req
    *   The HTTP request to send, using `Fs2Streams[IO]` as the streaming implementation.
    * @tparam T
    *   The expected type of the response body.
    * @return
    *   An `IO` effect producing the HTTP response.
    */
  def sendRequest[T](req: Request[T, Fs2Streams[IO]]): IO[Response[T]] =
    sttpBackend.use(_.send(req))
}
