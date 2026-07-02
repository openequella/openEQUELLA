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

package com.tle.core.url;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;
import static java.net.HttpURLConnection.HTTP_MULT_CHOICE;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_PAYMENT_REQUIRED;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.ReferencedURL;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.common.util.BlindSSLSocketFactory;
import com.tle.core.guice.Bind;
import com.tle.core.services.HttpService;
import com.tle.core.url.dao.URLCheckerDao;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.time.Duration;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.inject.Inject;
import javax.inject.Singleton;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLParameters;
import javax.net.ssl.TrustManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@NonNullByDefault
@Bind
@Singleton
@SuppressWarnings("nls")
public class URLCheckerService {
  private static final Log LOGGER = LogFactory.getLog(URLCheckerService.class);

  public static enum URLCheckMode {
    /**
     * Does not check the URL and relies entirely on past checks. This should be your default option
     * for fast operation.
     */
    RECORDS_ONLY(true, true /* Doesn't matter for this enum value */),
    /**
     * Does not check the URL and relies entirely on past checks. Does not invoke the clustered task
     * so that it can happen in the same transaction.
     */
    IMPORT(true, true /* Doesn't matter for this enum value */),
    /**
     * Returns result from past checks if still valid, else if checks the URL first. <b>Warning</b>
     * - this will block on URL checking for up to 3 seconds.
     */
    RECORDS_FIRST(true, true),
    /** Same as RECORDS_FIRST, but it will not timeout. */
    RECORDS_FIRST_NON_ITERACTIVE(true, false),
    /**
     * Checks a URL regardless of when it was last checked. <b>Warning</b> - this could block for
     * some time if a URL is timing out. Only use this when you really, really, really need a URL to
     * be checked immediately.
     */
    ALWAYS_CHECK(false, false);

    private boolean useRecords;
    private boolean timeoutCheck;

    private URLCheckMode(boolean useRecords, boolean timeoutChecking) {
      this.useRecords = useRecords;
      this.timeoutCheck = timeoutChecking;
    }
  }

  private static final String USER_AGENT =
      "Mozilla/5.0 (compatible; equellaurlbot/1.0; +http://support.equella.com/)";

  // Per-request (per redirect hop) timeout. The whole-round-trip bound that the legacy
  // async-http-client value provided is restored separately via the overallTimeout field (see
  // checkUrl).
  private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(60);

  // The legacy async-http-client was configured with setMaxRedirects(25). We follow redirects
  // manually (see sendFollowingRedirects) and preserve that limit purely for transparency with the
  // previous behaviour - the java.net.http.HttpClient default is only 5.
  private static final int MAX_REDIRECTS = 25;

  // Pre-emptive empty basic authentication in case some of the URLs require it.
  // http://jira.pearsoncmg.com/jira/browse/EQ-411
  // Base64 of ":" (empty user and password) - matches the header the legacy async-http-client
  // "preemptive BASIC Realm" with empty credentials would have sent.
  private static final String PREEMPTIVE_AUTH =
      "Basic " + Base64.getEncoder().encodeToString(":".getBytes(StandardCharsets.UTF_8));

  @Inject private URLCheckerDao dao;
  @Inject private HttpService httpService;
  @Inject private URLCheckerPolicy policy;

  private final HttpClient client;

  /** Decides whether a URL is checkable at all - see {@link URLValidator}. */
  private final URLValidator urlValidator;

  /**
   * The overall wall-clock bound applied to a single {@link #checkUrl(ReferencedURL)} call,
   * covering all redirect hops. This restores the async-http-client whole-round-trip request
   * timeout - the per-hop {@link #REQUEST_TIMEOUT} only bounds a single request.
   */
  private final Duration overallTimeout;

  /** Production constructor, used by Guice (collaborators arrive via field injection). */
  public URLCheckerService() {
    this(URLCheckerService::isURL, REQUEST_TIMEOUT);
  }

  /**
   * Constructs the service with an explicit URL validator and overall check timeout, so tests can
   * supply a permissive validator (embedded test servers live on ephemeral ports the production
   * pattern rejects) and a short timeout. Production behaviour comes from the no-arg constructor's
   * defaults.
   */
  URLCheckerService(URLValidator urlValidator, Duration overallTimeout) {
    this.urlValidator = urlValidator;
    this.overallTimeout = overallTimeout;

    final SSLContext sslContext = blindTrustSslContext();
    final SSLParameters sslParameters = sslContext.getDefaultSSLParameters();
    // Disable hostname verification as well: the previous async-http-client/Netty backend did not
    // verify hostnames, and a blind trust manager alone does not reproduce that (the JDK client
    // verifies hostnames independently of the TrustManager).
    sslParameters.setEndpointIdentificationAlgorithm(null);

    // Note on connection pooling: the legacy async-http-client capped connections per host (2) and
    // in total (200). java.net.http.HttpClient exposes no equivalent tuning; the overall check
    // concurrency is instead bounded by CheckURLsScheduledTask.MAX_CONCURRENT_CHECKS (200). The
    // absence of a per-host cap is an accepted deviation for this use case.
    client =
        HttpClient.newBuilder()
            .connectTimeout(REQUEST_TIMEOUT)
            // Redirects are followed manually - see sendFollowingRedirects for why.
            .followRedirects(HttpClient.Redirect.NEVER)
            .sslContext(sslContext)
            .sslParameters(sslParameters)
            .proxy(ProxySelector.getDefault())
            .build();
  }

  /**
   * Builds an {@link SSLContext} that trusts all certificates. Just because a URL uses a
   * self-signed certificate doesn't mean it isn't a working URL, and we don't care about MITM
   * attacks since we're only checking that the URL resolves.
   *
   * <p>This deliberately does not reuse {@link BlindSSLSocketFactory#createBlindSSLContext()}: that
   * helper uses the "SSL" algorithm and does not disable endpoint identification, both of which we
   * need to control here.
   */
  private static SSLContext blindTrustSslContext() {
    try {
      final SSLContext sslContext = SSLContext.getInstance("TLS");
      sslContext.init(
          null,
          new TrustManager[] {BlindSSLSocketFactory.createTrustManager()},
          new SecureRandom());
      return sslContext;
    } catch (GeneralSecurityException e) {
      throw new RuntimeException("Failed to set up SSL context for the URL checker HTTP client", e);
    }
  }

  /**
   * Test constructor: explicit collaborators (so unit tests can supply mocks without Guice field
   * injection) plus the validator/timeout overrides.
   */
  URLCheckerService(
      URLCheckerDao dao,
      HttpService httpService,
      URLCheckerPolicy policy,
      URLValidator urlValidator,
      Duration overallTimeout) {
    this(urlValidator, overallTimeout);
    this.dao = dao;
    this.httpService = httpService;
    this.policy = policy;
  }

  public boolean isUrlDisabled(String url) {
    if (!Check.isEmpty(url)) {
      return policy.isUrlDisabled(getUrlStatus(url, URLCheckMode.RECORDS_ONLY));
    }
    return true;
  }

  public ReferencedURL getUrlStatus(final String url, final URLCheckMode mode) {
    boolean httpUrl = false;
    try {
      URI uri = new URI(url);
      String scheme = uri.getScheme();
      if ("http".equals(scheme) || "https".equals(scheme)) {
        httpUrl = true;
      }
    } catch (URISyntaxException e) {
      // ignore
    }

    final ReferencedURL rurl =
        dao.retrieveOrCreate(
            url, httpUrl, mode == URLCheckMode.IMPORT, mode != URLCheckMode.RECORDS_ONLY);

    if (!httpUrl) {
      return rurl;
    }

    // Return immediately if we relying solely on the records or we don't
    // have internet access
    if (mode == URLCheckMode.RECORDS_ONLY
        || mode == URLCheckMode.IMPORT
        || !httpService.canAccessInternet()) {
      return rurl;
    }

    // Also return immediately if we can use our records and the URL does
    // not need checking.
    if (mode.useRecords && !policy.requiresChecking(rurl)) {
      return rurl;
    }

    try {
      final CompletableFuture<ReferencedURL> f = checkUrl(rurl);
      final ReferencedURL newRurl = mode.timeoutCheck ? f.get(3, TimeUnit.SECONDS) : f.get();

      // Don't forget to save the results of this check
      dao.evict(rurl);
      dao.updateWithTransaction(newRurl);

      return newRurl;
    } catch (TimeoutException ex) {
      // Just use the record
      return rurl;
    } catch (Exception ex) {
      throw new RuntimeException("Error checking URL: " + url, ex);
    }
  }

  /**
   * Checks the referenced URL and returns a future to an updated referenced URL. The future
   * referenced URL is not persisted, but the caller should do that to ensure the checking records
   * are correct.
   */
  CompletableFuture<ReferencedURL> checkUrl(final ReferencedURL rurl) {
    return checkUrl(rurl, true)
        .thenApply(Pair::getFirst)
        // Bound the whole check - including every redirect hop - to overallTimeout, restoring the
        // async-http-client whole-round-trip request timeout. Placed before exceptionally(...) so a
        // timeout is turned into a normal failure record rather than surfacing to the caller.
        .orTimeout(overallTimeout.toMillis(), TimeUnit.MILLISECONDS)
        // If checking throws an exception, it's probably because we haven't been able to reach the
        // URL (eg, java.nio.channels.UnresolvedAddressException) so return an updated ReferencedURL
        // based on the old one.
        .exceptionally(t -> createFailureRecord(rurl, t));
  }

  /** Maps a failed check onto a new failure-state {@link ReferencedURL} based on the old one. */
  private ReferencedURL createFailureRecord(final ReferencedURL rurl, final Throwable t) {
    final Throwable cause =
        (t instanceof CompletionException && t.getCause() != null) ? t.getCause() : t;
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Exception checking " + rurl.getUrl(), cause);
    }
    ReferencedURL newRurl = new ReferencedURL();
    newRurl.setId(rurl.getId());
    newRurl.setUrl(rurl.getUrl());
    newRurl.setSuccess(false);
    newRurl.setStatus(0);
    newRurl.setTries(rurl.getTries() + 1);
    newRurl.setLastChecked(new Date(System.currentTimeMillis()));
    newRurl.setLastIndexed(new Date());
    newRurl.setMessage(cause.getClass().getName() + ": " + cause.getMessage());

    return newRurl;
  }

  /**
   * Sends a single HEAD or GET request and interprets the response, then - if a HEAD needs retrying
   * as a GET - performs that follow-up request. The resulting pair's second value is always false
   * (any required GET retry has already been performed).
   */
  private CompletableFuture<Pair<ReferencedURL, Boolean>> checkUrl(
      final ReferencedURL rurl, final boolean head) {
    if (!urlValidator.isValid(rurl.getUrl())) {
      // Most likely because the URL is not a URL at all, like "http://" or "beatlejuice".
      LOGGER.debug("Invalid URL: " + rurl.getUrl());
      return CompletableFuture.failedFuture(new IllegalArgumentException("Invalid URL"));
    }

    final URI uri;
    try {
      uri = URI.create(rurl.getUrl());
    } catch (RuntimeException e) {
      // e.g. IllegalArgumentException for a URL that passed the validator but is not a legal URI
      // (such as a bad percent-escape). Surface it through the future so it becomes a failure
      // record instead of throwing synchronously and aborting the whole scheduled batch.
      LOGGER.debug("Malformed URL: " + rurl.getUrl());
      return CompletableFuture.failedFuture(e);
    }

    LOGGER.debug("Valid URL: " + rurl.getUrl());
    return sendFollowingRedirects(uri, head, MAX_REDIRECTS)
        .thenCompose(
            response -> {
              final Pair<ReferencedURL, Boolean> result = interpretResponse(rurl, head, response);
              if (result.getSecond()) {
                // The HEAD request needs retrying with a GET.
                return checkUrl(result.getFirst(), false);
              }
              return CompletableFuture.completedFuture(result);
            });
  }

  /**
   * Sends a single HEAD or GET request to {@code uri} and, when the response is a redirect, follows
   * its {@code Location} header - resolving relative targets and <em>including HTTPS-&gt;HTTP
   * downgrades</em> - up to {@code remainingRedirects} more hops.
   *
   * <p>This replaces {@link HttpClient}'s built-in redirect handling ({@link HttpClient.Redirect})
   * for two reasons, both of which restore the previous async-http-client behaviour:
   *
   * <ul>
   *   <li>To preserve the legacy 25-hop limit ({@link #MAX_REDIRECTS}) without setting the
   *       JVM-global, undocumented {@code jdk.httpclient.redirects.retrylimit} property - that
   *       property is cached process-wide by the JDK on first use and would also change the
   *       redirect tolerance of the shared sttp {@code HttpClientFs2Backend}.
   *   <li>To follow HTTPS-&gt;HTTP redirects, which {@code Redirect.NORMAL} refuses. This checker
   *       already blind-trusts certificates and does not verify hostnames, so refusing the
   *       downgrade would protect nothing while newly flagging downgrade-only URLs as broken.
   * </ul>
   *
   * <p>The request method is preserved across hops (the checker only issues bodyless HEAD/GET
   * requests, so 303-style method rewriting is irrelevant); the empty pre-emptive Authorization
   * header is re-sent on every hop, matching async-http-client's non-host-scoped realm. When the
   * hop limit is exhausted the final redirect response is returned and interpreted as a failure,
   * the same observable outcome async-http-client produced by throwing "maximum redirect reached".
   */
  private CompletableFuture<HttpResponse<String>> sendFollowingRedirects(
      final URI uri, final boolean head, final int remainingRedirects) {
    final HttpRequest request;
    try {
      request = buildRequest(uri, head);
    } catch (RuntimeException e) {
      // e.g. IllegalArgumentException if the URI (possibly a redirect target) has no host or a
      // non-http scheme. Surface it through the future rather than throwing synchronously.
      return CompletableFuture.failedFuture(e);
    }

    return client
        .sendAsync(request, new BoundedBodyHandler(head))
        .thenCompose(response -> followRedirectIfNeeded(uri, head, response, remainingRedirects));
  }

  private HttpRequest buildRequest(final URI uri, final boolean head) {
    return HttpRequest.newBuilder(uri)
        .timeout(REQUEST_TIMEOUT)
        .header("User-Agent", USER_AGENT)
        // Pre-emptive basic auth for EQ-411.
        .header("Authorization", PREEMPTIVE_AUTH)
        .method(head ? "HEAD" : "GET", HttpRequest.BodyPublishers.noBody())
        .build();
  }

  /**
   * Continues the redirect chain when {@code response} is a redirect that should be followed (see
   * {@link #redirectTarget}), otherwise completes with the response as-is.
   */
  private CompletableFuture<HttpResponse<String>> followRedirectIfNeeded(
      final URI current,
      final boolean head,
      final HttpResponse<String> response,
      final int remainingRedirects) {
    final Optional<URI> next = redirectTarget(current, response, remainingRedirects);
    if (next.isEmpty()) {
      return CompletableFuture.completedFuture(response);
    }
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Following redirect " + current + " -> " + next.get());
    }
    return sendFollowingRedirects(next.get(), head, remainingRedirects - 1);
  }

  /**
   * @return the redirect target to follow (resolved against {@code current}, possibly downgrading
   *     the scheme), or empty if the response is not a redirect we should follow (not a 3xx, no
   *     Location header, or the hop limit is exhausted).
   */
  private static Optional<URI> redirectTarget(
      final URI current, final HttpResponse<String> response, final int remainingRedirects) {
    if (remainingRedirects <= 0 || !isRedirect(response.statusCode())) {
      return Optional.empty();
    }
    return response.headers().firstValue("Location").map(current::resolve);
  }

  /** True when the status code is a redirect (3xx). */
  static boolean isRedirect(final int code) {
    return code >= HTTP_MULT_CHOICE && code < HTTP_BAD_REQUEST;
  }

  /**
   * True when the status code means the URL exists: any 2xx, plus 401/402. For the latter two we
   * make an educated guess that if we're told we're not allowed to look at something (eg, behind
   * basic authentication or we haven't paid for the thing) the thing does actually exist, but we
   * can't truly verify it.
   */
  static boolean isTreatedAsExists(final int code) {
    return (code >= HTTP_OK && code < HTTP_MULT_CHOICE)
        || code == HTTP_UNAUTHORIZED
        || code == HTTP_PAYMENT_REQUIRED;
  }

  /**
   * Interprets an HTTP response against the referenced URL, mutating it with the outcome.
   *
   * @return a pair whose second value is true if this was a HEAD request that should be retried as
   *     a GET request.
   */
  private Pair<ReferencedURL, Boolean> interpretResponse(
      final ReferencedURL rurl, final boolean head, final HttpResponse<String> response) {
    final String url = rurl.getUrl();
    final int code = response.statusCode();
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Response code " + code + " for " + url);
    }

    // Redirects have already been followed by sendFollowingRedirects (up to MAX_REDIRECTS); a
    // redirect status reaching here means the hop limit was exhausted. Retry with GET on anything
    // other than 2xx. http://jira.pearsoncmg.com/jira/browse/EQ-561
    if (head && (code < HTTP_OK || code >= HTTP_MULT_CHOICE)) {
      // Technically we should only need to look out for HTTP_BAD_METHOD and retry with a GET, but
      // apparently not everyone has read the spec.
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Retry with GET for " + url);
      }
      return new Pair<>(rurl, true);
    }

    rurl.setStatus(code);
    rurl.setLastChecked(new Date());

    if (isTreatedAsExists(code)) {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("Found to be OK " + url);
      }
      rurl.setSuccess(true);
      rurl.setMessage(null);
      rurl.setTries(0);
      return new Pair<>(rurl, false);
    }

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("Failed " + url);
    }
    rurl.setSuccess(false);
    rurl.setTries(rurl.getTries() + 1);
    // The body is already bounded to MAX_MESSAGE_LENGTH by BoundedBodyHandler; setMessage also
    // truncates defensively.
    rurl.setMessage(response.body());
    return new Pair<>(rurl, false);
  }

  public static boolean isURL(String url) {
    if (url == null) {
      return false;
    }
    String urlPattern =
        "^http(s{0,1})://[a-zA-Z0-9_\\-\\.]+\\.([A-Za-z/]{2,5})[a-zA-Z0-9_/\\&\\?\\=\\-\\.\\~\\%]*";
    return url.matches(urlPattern);
  }
}
