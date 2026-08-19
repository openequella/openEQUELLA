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

package com.tle.core.url

import com.sun.net.httpserver.{
  HttpExchange,
  HttpHandler,
  HttpServer,
  HttpsConfigurator,
  HttpsParameters,
  HttpsServer
}
import com.tle.beans.ReferencedURL
import com.tle.core.services.HttpService
import com.tle.core.url.URLCheckerService.URLCheckMode
import com.tle.core.url.dao.URLCheckerDao
import org.mockito.ArgumentMatchers.{anyBoolean, anyString}
import org.mockito.Mockito.{mock, when}
import org.scalatest.BeforeAndAfterAll
import org.scalatest.funspec.AnyFunSpec
import org.scalatest.matchers.should.Matchers

import java.io.File
import java.net.{InetSocketAddress, ServerSocket}
import java.nio.charset.StandardCharsets
import java.time.Duration
import java.nio.file.Files
import java.security.KeyStore
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicReference
import javax.net.ssl.{KeyManagerFactory, SSLContext}

/** Characterization tests for [[URLCheckerService]]. These pin down the observable behaviour of the
  * URL checker (status interpretation, HEAD -> GET retry, redirect following, self-signed TLS
  * trust, connection-failure handling, message truncation and pre-emptive auth) against a real
  * embedded HTTP(S) server.
  *
  * They act as a control: they must pass against the current async-http-client implementation and
  * continue to pass unchanged after the backend is migrated to java.net.http.HttpClient.
  */
class URLCheckerServiceTest extends AnyFunSpec with Matchers with BeforeAndAfterAll {

  private var httpServer: HttpServer   = _
  private var httpsServer: HttpsServer = _
  private var baseHttp: String         = _
  private var baseHttps: String        = _

  // Records the Authorization header seen by the /checkauth handler.
  private val lastAuthHeader = new AtomicReference[String]("")

  private def handler(fn: HttpExchange => Unit): HttpHandler =
    (exchange: HttpExchange) =>
      try fn(exchange)
      finally exchange.close()

  private def respond(exchange: HttpExchange, code: Int, body: String): Unit = {
    val bytes = body.getBytes(StandardCharsets.UTF_8)
    if ("HEAD".equalsIgnoreCase(exchange.getRequestMethod) || bytes.isEmpty) {
      exchange.sendResponseHeaders(code, -1)
    } else {
      exchange.sendResponseHeaders(code, bytes.length.toLong)
      val os = exchange.getResponseBody
      os.write(bytes)
      os.close()
    }
  }

  private def registerHandlers(server: HttpServer): Unit = {
    // Simple 200 for HEAD.
    server.createContext("/ok", handler(respond(_, 200, "")))

    // HEAD fails with 405, GET succeeds with 200 - exercises the HEAD -> GET retry path.
    server.createContext(
      "/head405-get200",
      handler { ex =>
        if ("GET".equalsIgnoreCase(ex.getRequestMethod)) respond(ex, 200, "ok")
        else respond(ex, 405, "")
      }
    )

    // 404 with a body - the body should be captured as the failure message.
    server.createContext("/notfound", handler(respond(_, 404, "Not found here")))

    // 401 and 402 are treated as "exists but can't verify" (EQ-411).
    server.createContext("/unauthorized", handler(respond(_, 401, "nope")))
    server.createContext("/payment", handler(respond(_, 402, "pay up")))

    // A redirect chain longer than 5 hops ending in a 200.
    server.createContext(
      "/redirect",
      handler { ex =>
        val path = ex.getRequestURI.getPath
        val hop  = path.substring("/redirect/".length).toInt
        if (hop >= 8) {
          respond(ex, 200, "")
        } else {
          ex.getResponseHeaders.add("Location", s"/redirect/${hop + 1}")
          respond(ex, 302, "")
        }
      }
    )

    // 404 whose body exceeds ReferencedURL.MAX_MESSAGE_LENGTH - the message must be truncated.
    val bigBody = "x" * (ReferencedURL.MAX_MESSAGE_LENGTH + 100)
    server.createContext("/bigbody", handler(respond(_, 404, bigBody)))

    // Records the Authorization header so we can assert pre-emptive auth is sent.
    server.createContext(
      "/checkauth",
      handler { ex =>
        lastAuthHeader.set(Option(ex.getRequestHeaders.getFirst("Authorization")).getOrElse(""))
        respond(ex, 200, "")
      }
    )

    // 302 redirect to the plain-HTTP server. Registered on the HTTPS server it exercises the
    // HTTPS -> HTTP downgrade that Redirect.NORMAL refuses but async-http-client followed.
    server.createContext(
      "/downgrade",
      handler { ex =>
        ex.getResponseHeaders.add("Location", baseHttp + "/ok")
        respond(ex, 302, "")
      }
    )

    // Redirects forever - proves the 25-hop limit terminates the chain as a failure rather than
    // looping indefinitely.
    server.createContext(
      "/loopredirect",
      handler { ex =>
        val hop = ex.getRequestURI.getPath.substring("/loopredirect/".length).toInt
        ex.getResponseHeaders.add("Location", s"/loopredirect/${hop + 1}")
        respond(ex, 302, "")
      }
    )

    // HEAD -> 405 forces a GET; the GET returns 200 with a large body that stalls part-way. A client
    // that downloaded the whole (successful) body would block on the stall - the checker must not.
    server.createContext(
      "/blackhole",
      handler { ex =>
        if ("GET".equalsIgnoreCase(ex.getRequestMethod)) {
          ex.sendResponseHeaders(200, 10000000L) // claims a large body...
          val os = ex.getResponseBody
          os.write("x".getBytes(StandardCharsets.UTF_8))
          os.flush()
          Thread.sleep(5000) // ...then stalls
          try os.close()
          catch { case _: Throwable => () }
        } else {
          respond(ex, 405, "")
        }
      }
    )

    // 200 that sleeps before responding - used with a shortened overallTimeout to prove the
    // whole-trip timeout yields a failure record.
    server.createContext(
      "/slow",
      handler { ex =>
        Thread.sleep(2000)
        respond(ex, 200, "")
      }
    )
  }

  private def selfSignedSslContext(): SSLContext = {
    // Generate a throwaway self-signed keystore via keytool so the HTTPS server presents a cert a
    // normal client would reject - proving the checker's blind-trust behaviour.
    val ksFile: File = Files.createTempFile("oeq-urlchecker-test", ".p12").toFile
    ksFile.delete()
    val javaHome = System.getProperty("java.home")
    val keytool  = new File(new File(javaHome, "bin"), "keytool").getAbsolutePath
    val pb       = new ProcessBuilder(
      keytool,
      "-genkeypair",
      "-alias",
      "test",
      "-keyalg",
      "RSA",
      "-keysize",
      "2048",
      "-validity",
      "1",
      "-dname",
      "CN=localhost",
      "-ext",
      "SAN=ip:127.0.0.1",
      "-keystore",
      ksFile.getAbsolutePath,
      "-storepass",
      "changeit",
      "-keypass",
      "changeit",
      "-storetype",
      "PKCS12"
    )
    pb.redirectErrorStream(true)
    val proc = pb.start()
    val out  = new String(proc.getInputStream.readAllBytes(), StandardCharsets.UTF_8)
    val code = proc.waitFor()
    if (code != 0) throw new RuntimeException(s"keytool failed ($code): $out")

    val ks = KeyStore.getInstance("PKCS12")
    val in = Files.newInputStream(ksFile.toPath)
    try ks.load(in, "changeit".toCharArray)
    finally in.close()
    ksFile.delete()

    val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm)
    kmf.init(ks, "changeit".toCharArray)
    val ctx = SSLContext.getInstance("TLS")
    ctx.init(kmf.getKeyManagers, null, null)
    ctx
  }

  override def beforeAll(): Unit = {
    httpServer = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0)
    // A real thread pool so slow/stalling handlers (/blackhole, /slow) don't block the single
    // default dispatch thread and starve subsequent requests.
    httpServer.setExecutor(Executors.newCachedThreadPool())
    registerHandlers(httpServer)
    httpServer.start()
    baseHttp = s"http://127.0.0.1:${httpServer.getAddress.getPort}"

    httpsServer = HttpsServer.create(new InetSocketAddress("127.0.0.1", 0), 0)
    httpsServer.setExecutor(Executors.newCachedThreadPool())
    httpsServer.setHttpsConfigurator(new HttpsConfigurator(selfSignedSslContext()) {
      override def configure(params: HttpsParameters): Unit = {
        val engine = getSSLContext.createSSLEngine()
        params.setNeedClientAuth(false)
        params.setCipherSuites(engine.getEnabledCipherSuites)
        // Force TLSv1.2: the JDK's com.sun.net.httpserver HttpsServer is known to hang with some
        // TLSv1.3 clients (half-close / session-ticket handling), which would otherwise mask the
        // trust behaviour we are characterizing.
        params.setProtocols(Array("TLSv1.2"))
      }
    })
    registerHandlers(httpsServer)
    httpsServer.start()
    baseHttps = s"https://127.0.0.1:${httpsServer.getAddress.getPort}"
  }

  override def afterAll(): Unit = {
    if (httpServer != null) httpServer.stop(0)
    if (httpsServer != null) httpsServer.stop(0)
  }

  /** Builds a service wired to mocked collaborators. A permissive `URLValidator` is injected so
    * URLs pointing at the ephemeral test-server port (which the production regex would reject)
    * reach the HTTP logic.
    */
  private def serviceFor(
      rurl: ReferencedURL,
      overallTimeoutMillis: Long = -1
  ): URLCheckerService = {
    val dao         = mock(classOf[URLCheckerDao])
    val httpService = mock(classOf[HttpService])
    val policy      = mock(classOf[URLCheckerPolicy])

    when(
      dao.retrieveOrCreate(anyString(), anyBoolean(), anyBoolean(), anyBoolean())
    ).thenReturn(rurl)
    when(httpService.canAccessInternet()).thenReturn(true)

    val permissiveValidator: URLValidator = (_: String) => true
    val overallTimeout                    =
      if (overallTimeoutMillis >= 0) Duration.ofMillis(overallTimeoutMillis)
      else Duration.ofSeconds(60) // the production default
    new URLCheckerService(dao, httpService, policy, permissiveValidator, overallTimeout)
  }

  private def newRurl(url: String, tries: Int = 0): ReferencedURL = {
    val r = new ReferencedURL()
    r.setUrl(url)
    r.setTries(tries)
    r
  }

  private def check(path: String, initialTries: Int = 0, https: Boolean = false): ReferencedURL = {
    val base = if (https) baseHttps else baseHttp
    val url  = base + path
    val rurl = newRurl(url, initialTries)
    serviceFor(rurl).getUrlStatus(url, URLCheckMode.ALWAYS_CHECK)
  }

  describe("URLCheckerService.getUrlStatus (live check)") {
    it("marks a 200 HEAD response as success and resets the retry counter") {
      val result = check("/ok", initialTries = 3)
      result.isSuccess shouldBe true
      result.getStatus shouldBe 200
      result.getTries shouldBe 0
    }

    it("retries with GET when HEAD is rejected, then succeeds on a 200 GET") {
      val result = check("/head405-get200")
      result.isSuccess shouldBe true
      result.getStatus shouldBe 200
    }

    it("marks a 404 as failure, captures the body as the message and increments tries") {
      val result = check("/notfound", initialTries = 2)
      result.isSuccess shouldBe false
      result.getStatus shouldBe 404
      result.getMessage should include("Not found here")
      result.getTries shouldBe 3
    }

    it("treats 401 Unauthorized as success (URL exists but cannot be verified)") {
      val result = check("/unauthorized")
      result.isSuccess shouldBe true
      result.getStatus shouldBe 401
    }

    it("treats 402 Payment Required as success (URL exists but cannot be verified)") {
      val result = check("/payment")
      result.isSuccess shouldBe true
      result.getStatus shouldBe 402
    }

    it("follows a redirect chain longer than five hops to a final 200") {
      val result = check("/redirect/0")
      result.isSuccess shouldBe true
      result.getStatus shouldBe 200
    }

    // Now that URLCheckerService uses java.net.http.HttpClient (which handshakes correctly with the
    // embedded HTTPS test server), this is an active guarantee that self-signed certs are trusted.
    it("trusts a self-signed TLS certificate") {
      val result = check("/ok", https = true)
      result.isSuccess shouldBe true
      result.getStatus shouldBe 200
    }

    it("truncates the failure message to MAX_MESSAGE_LENGTH") {
      val result = check("/bigbody")
      result.isSuccess shouldBe false
      result.getMessage.length shouldBe ReferencedURL.MAX_MESSAGE_LENGTH
    }

    it("reports a connection failure as failure and increments tries") {
      // Grab a free port then release it so nothing is listening.
      val socket = new ServerSocket(0)
      val port   = socket.getLocalPort
      socket.close()
      val url    = s"http://127.0.0.1:$port/gone"
      val rurl   = newRurl(url, tries = 1)
      val result =
        serviceFor(rurl).getUrlStatus(url, URLCheckMode.ALWAYS_CHECK)
      result.isSuccess shouldBe false
      result.getStatus shouldBe 0
      result.getTries shouldBe 2
      result.getMessage should not be empty
    }

    it("sends pre-emptive basic authorization on the request (EQ-411)") {
      lastAuthHeader.set("")
      val result = check("/checkauth")
      result.isSuccess shouldBe true
      lastAuthHeader.get() should startWith("Basic ")
    }

    // A URL that passes validation but is not a legal URI (bad percent-escape) must be reported as a
    // failure via the future - not thrown synchronously, which would abort the whole scheduled batch
    // in CheckURLsScheduledTask (it calls checkUrl directly with no try/catch).
    it("reports a malformed URL as a failure without throwing synchronously") {
      val url     = baseHttp + "/%zz"
      val rurl    = newRurl(url, tries = 1)
      val service = serviceFor(rurl)

      // checkUrl (the path the scheduled task uses) must not throw; the future completes normally.
      val future = service.checkUrl(rurl)
      val result = future.get()
      result.isSuccess shouldBe false
      result.getStatus shouldBe 0
      result.getTries shouldBe 2
    }

    // async-http-client followed redirects regardless of scheme; Redirect.NORMAL refuses HTTPS->HTTP
    // downgrades, so the manual loop must restore that. (HTTPS-origin, so like the self-signed test
    // it only meaningfully runs against the JDK-client implementation.)
    it("follows an HTTPS -> HTTP redirect downgrade") {
      val result = check("/downgrade", https = true)
      result.isSuccess shouldBe true
      result.getStatus shouldBe 200
    }

    // Matches async-http-client's setMaxRedirects(25): a chain longer than the limit terminates as a
    // failure instead of looping forever.
    it("stops following redirects at the hop limit and reports failure") {
      val result = check("/loopredirect/0")
      result.isSuccess shouldBe false
      result.getStatus shouldBe 302
    }

    // A successful response body must not be downloaded (async-http-client aborted on a 2xx status).
    // The endpoint stalls for 5s after a couple of bytes; downloading the whole body would block.
    it("does not download the body of a successful response") {
      val start         = System.nanoTime()
      val result        = check("/blackhole")
      val elapsedMillis = (System.nanoTime() - start) / 1000000L

      result.isSuccess shouldBe true
      result.getStatus shouldBe 200
      elapsedMillis should be < 3000L
    }

    // async-http-client's 60s request timeout bounded the whole round trip; the overall timeout must
    // turn a slow check into a failure record rather than surfacing an exception.
    it("fails a check that exceeds the overall timeout, without throwing") {
      val url    = baseHttp + "/slow"
      val rurl   = newRurl(url)
      val result =
        serviceFor(rurl, overallTimeoutMillis = 500).getUrlStatus(url, URLCheckMode.ALWAYS_CHECK)
      result.isSuccess shouldBe false
      result.getStatus shouldBe 0
    }
  }
}
