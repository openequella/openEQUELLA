/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
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

import static java.net.HttpURLConnection.HTTP_MULT_CHOICE;
import static java.net.HttpURLConnection.HTTP_OK;
import static java.net.HttpURLConnection.HTTP_PAYMENT_REQUIRED;
import static java.net.HttpURLConnection.HTTP_UNAUTHORIZED;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.common.util.concurrent.AsyncFunction;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.ning.http.client.AsyncHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.AsyncHttpClientConfig;
import com.ning.http.client.AsyncHttpClientConfig.Builder;
import com.ning.http.client.HttpResponseBodyPart;
import com.ning.http.client.HttpResponseHeaders;
import com.ning.http.client.HttpResponseStatus;
import com.ning.http.client.Realm;
import com.ning.http.client.Realm.AuthScheme;
import com.ning.http.client.RequestBuilder;
import com.tle.annotation.NonNullByDefault;
import com.tle.beans.ReferencedURL;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.common.util.BlindSSLSocketFactory;
import com.tle.core.guice.Bind;
import com.tle.core.services.HttpService;
import com.tle.core.url.dao.URLCheckerDao;

@NonNullByDefault
@Bind
@Singleton
@SuppressWarnings("nls")
public class URLCheckerService
{
	private static final Log LOGGER = LogFactory.getLog(URLCheckerService.class);

	public static enum URLCheckMode
	{
		/**
		 * Does not check the URL and relies entirely on past checks. This
		 * should be your default option for fast operation.
		 */
		RECORDS_ONLY(true, true/* Doesn't matter for this enum value */), /**
																			 * Does not check the URL and relies entirely on past checks. Does not invoke
																			 * the clustered task so that it can happen in the same transaction.
																			 */
		IMPORT(true, true/* Doesn't matter for this enum value */), /**
																	 * Returns result from past checks if still valid, else if checks the
																	 * URL first. <b>Warning</b> - this will block on URL checking for up to
																	 * 3 seconds.
																	 */
		RECORDS_FIRST(true, true), /**
									 * Same as RECORDS_FIRST, but it will not timeout.
									 */
		RECORDS_FIRST_NON_ITERACTIVE(true, false), /**
													 * Checks a URL regardless of when it was last checked. <b>Warning</b> -
													 * this could block for some time if a URL is timing out. Only use this
													 * when you really, really, really need a URL to be checked immediately.
													 */
		ALWAYS_CHECK(false, false);

		private boolean useRecords;
		private boolean timeoutCheck;

		private URLCheckMode(boolean useRecords, boolean timeoutChecking)
		{
			this.useRecords = useRecords;
			this.timeoutCheck = timeoutChecking;
		}
	}

	@Inject
	private URLCheckerDao dao;
	@Inject
	private HttpService httpService;
	@Inject
	private URLCheckerPolicy policy;

	private final AsyncHttpClient client;

	public URLCheckerService()
	{
		Builder bc = new AsyncHttpClientConfig.Builder();
		bc.setAllowPoolingConnections(false); // Avoid keep-alive
		bc.setCompressionEnforced(true);
		bc.setUseProxyProperties(true);
		bc.setFollowRedirect(true);
		bc.setMaxConnectionsPerHost(2);
		bc.setMaxConnections(200);
		bc.setMaxRedirects(25);
		bc.setUserAgent("Mozilla/5.0 (compatible; equellaurlbot/1.0; +http://support.equella.com/)");

		// These are actually the defaults, but let's specify in case they
		// change
		bc.setConnectTimeout(60000);
		bc.setRequestTimeout(60000);
		// ^^ note the massive request timeout, this not a per-request timeout,
		// it's the whole redirection round trip

		// Just because a URL uses a self-signed certificate doesn't mean it's
		// not a working URL. We also don't care about MITM attacks since we're
		// just checking the URL.
		bc.setSSLContext(BlindSSLSocketFactory.createBlindSSLContext());

		// Configure a fake authentication in case some the URLs are using it.
		// http://jira.pearsoncmg.com/jira/browse/EQ-411
		Realm realm = new Realm.RealmBuilder().setPrincipal("").setPassword("").setUsePreemptiveAuth(true)
			.setScheme(AuthScheme.BASIC).build();
		bc.setRealm(realm);

		client = new AsyncHttpClient(bc.build());
	}

	public boolean isUrlDisabled(String url)
	{
		if( !Check.isEmpty(url) )
		{
			return policy.isUrlDisabled(getUrlStatus(url, URLCheckMode.RECORDS_ONLY));
		}
		return true;
	}

	public ReferencedURL getUrlStatus(final String url, final URLCheckMode mode)
	{
		boolean httpUrl = false;
		try
		{
			URI uri = new URI(url);
			String scheme = uri.getScheme();
			if( "http".equals(scheme) || "https".equals(scheme) )
			{
				httpUrl = true;
			}
		}
		catch( URISyntaxException e )
		{
			// ignore
		}

		final ReferencedURL rurl = dao.retrieveOrCreate(url, httpUrl, mode == URLCheckMode.IMPORT, mode != URLCheckMode.RECORDS_ONLY);

		if( !httpUrl )
		{
			return rurl;
		}

		// Return immediately if we relying solely on the records or we don't
		// have internet access
		if( mode == URLCheckMode.RECORDS_ONLY || mode == URLCheckMode.IMPORT || !httpService.canAccessInternet() )
		{
			return rurl;
		}

		// Also return immediately if we can use our records and the URL does
		// not need checking.
		if( mode.useRecords && !policy.requiresChecking(rurl) )
		{
			return rurl;
		}

		try
		{
			final ListenableFuture<ReferencedURL> f = checkUrl(rurl);
			final ReferencedURL newRurl = mode.timeoutCheck ? f.get(3, TimeUnit.SECONDS) : f.get();

			// Don't forget to save the results of this check
			dao.evict(rurl);
			dao.updateWithTransaction(newRurl);

			return newRurl;
		}
		catch( TimeoutException ex )
		{
			// Just use the record
			return rurl;
		}
		catch( Exception ex )
		{
			throw new RuntimeException("Error checking URL: " + url, ex);
		}
	}

	/**
	 * Checks the referenced URL and returns a future to an updated referenced
	 * URL. The future referenced URL is not persisted, but the caller should do
	 * that to ensure the checking records are correct.
	 */
	ListenableFuture<ReferencedURL> checkUrl(final ReferencedURL rurl)
	{
		final ListenableFuture<Pair<ReferencedURL, Boolean>> checkUrlFuture = checkUrl(rurl, true);

		// Map the Pair to just the ReferencedUrl
		final ListenableFuture<ReferencedURL> mapToRurl = Futures.transformAsync(checkUrlFuture,
			new AsyncFunction<Pair<ReferencedURL, Boolean>, ReferencedURL>()
			{
				@Override
				public ListenableFuture<ReferencedURL> apply(Pair<ReferencedURL, Boolean> input)
				{
					return Futures.immediateFuture(input.getFirst());
				}
			});

		// If the above futures throw an exception, it's probably because we
		// haven't been able to check the URL (eg,
		// java.nio.UnresolvedAddressException) so return an updated
		// ReferencedURL based on the old one.
		return Futures.catchingAsync(mapToRurl, Throwable.class, new AsyncFunction<Throwable, ReferencedURL>()
		{
			@Override
			public ListenableFuture<ReferencedURL> apply(Throwable t)
			{
				final String url = rurl.getUrl();
				if( LOGGER.isDebugEnabled() )
				{
					LOGGER.debug("Exception checking " + url, t);
				}
				ReferencedURL newRurl = new ReferencedURL();
				newRurl.setId(rurl.getId());
				newRurl.setUrl(rurl.getUrl());
				newRurl.setSuccess(false);
				newRurl.setStatus(0);
				newRurl.setTries(rurl.getTries() + 1);
				newRurl.setLastChecked(new Date(System.currentTimeMillis()));
				newRurl.setLastIndexed(new Date());
				newRurl.setMessage(t.getClass().getName() + ": " + t.getMessage());

				return Futures.immediateFuture(newRurl);
			}
		});
	}

	/**
	 * @return second value will be true if it failed and needs trying with a
	 *         GET request.
	 */
	private ListenableFuture<Pair<ReferencedURL, Boolean>> checkUrl(final ReferencedURL rurl, final boolean head)
	{
		AsyncHandler<Pair<ReferencedURL, Boolean>> handler = new AsyncHandler<Pair<ReferencedURL, Boolean>>()
		{
			boolean retryWithGet = false;
			StringBuilder body = new StringBuilder();

			@Override
			public STATE onStatusReceived(HttpResponseStatus responseStatus)
			{
				final String url = rurl.getUrl();
				if( LOGGER.isDebugEnabled() )
				{
					LOGGER.debug("Checking " + url);
				}

				final int code = responseStatus.getStatusCode();
				if( LOGGER.isDebugEnabled() )
				{
					LOGGER.debug("Response code " + code + " for " + url);
				}

				// Note: Redirection is taken care of automatically. We don't
				// need to explicitly check for redirection codes here.
				// Retry with GET on anything other than 2xx
				// http://jira.pearsoncmg.com/jira/browse/EQ-561
				if( head && (code < HTTP_OK || code >= HTTP_MULT_CHOICE) )
				{
					// Technically we should only need to look out for
					// HTTP_BAD_METHOD and retry with a GET, but apparently not
					// everyone has read the spec.
					retryWithGet = true;
					if( LOGGER.isDebugEnabled() )
					{
						LOGGER.debug("Retry with GET for " + url);
					}

					// And abort the current request
					return STATE.ABORT;
				}

				// Retrieved status code is valid here
				rurl.setStatus(code);
				rurl.setLastChecked(new Date());

				// NOTE: Make an educated guess that if we're told we're not
				// allowed to look at something (eg, behind basic authentication
				// or we haven't paid for the thing) that the thing does
				// actually exist, but we can't truely verify it.
				if( (code >= HTTP_OK && code < HTTP_MULT_CHOICE) || code == HTTP_UNAUTHORIZED
					|| code == HTTP_PAYMENT_REQUIRED )
				{
					if( LOGGER.isDebugEnabled() )
					{
						LOGGER.debug("Found to be OK " + url);
					}
					rurl.setSuccess(true);
					rurl.setMessage(null);
					rurl.setTries(0);

					return STATE.ABORT;
				}

				if( LOGGER.isDebugEnabled() )
				{
					LOGGER.debug("Failed " + url);
				}

				// Don't abort - we need to capture the error message
				rurl.setSuccess(false);
				rurl.setTries(rurl.getTries() + 1);
				return STATE.CONTINUE;
			}

			@Override
			public STATE onHeadersReceived(HttpResponseHeaders headers)
			{
				// Don't abort - we are probably trying to capture the error
				// message
				return STATE.CONTINUE;
			}

			@Override
			public STATE onBodyPartReceived(HttpResponseBodyPart content)
			{
				body.append(new String(content.getBodyPartBytes()));
				if( body.length() < ReferencedURL.MAX_MESSAGE_LENGTH )
				{
					return STATE.CONTINUE;
				}

				return STATE.ABORT;
			}

			@Override
			public Pair<ReferencedURL, Boolean> onCompleted()
			{
				final String url = rurl.getUrl();
				if( LOGGER.isDebugEnabled() )
				{
					LOGGER.debug("onCompleted " + url);
				}
				// Only save if we're not retrying the URL
				if( !retryWithGet )
				{
					rurl.setMessage(rurl.isSuccess() ? null : body.toString());
				}

				return new Pair<ReferencedURL, Boolean>(rurl, retryWithGet);
			}

			@Override
			public void onThrowable(Throwable t)
			{
				// Ignore
			}
		};

		try
		{			
			RequestBuilder requestBuilder;
			if (isURL(rurl.getUrl())){
				LOGGER.debug("Valid URL: "+rurl.getUrl());
				requestBuilder = new RequestBuilder(head ? "HEAD" : "GET").setUrl(rurl.getUrl());
			}
			else{
				LOGGER.debug("Invalid URL: "+rurl.getUrl());
				throw new IllegalArgumentException("Invalid URL");
			}
			return Futures.transformAsync(
				new AsyncHttpToGuavaAdapter<Pair<ReferencedURL, Boolean>>(
					client.executeRequest(requestBuilder.build(), handler)),
				new AsyncFunction<Pair<ReferencedURL, Boolean>, Pair<ReferencedURL, Boolean>>()
				{
					@Override
					public ListenableFuture<Pair<ReferencedURL, Boolean>> apply(Pair<ReferencedURL, Boolean> result)
						throws Exception
					{
						if( result.getSecond() )
						{
							// Return a new future that will instead check using
							// the GET method.
							return checkUrl(result.getFirst(), false);
						}
						return Futures.immediateFuture(result);
					}
				});
		}
		catch( IllegalArgumentException ex )
		{
			// Most likely because the URL is not a URL at all, like "http://"
			// or "beatlejuice".
			return Futures.immediateFailedFuture(ex);
		}
	}

	public static boolean isURL(String url)
	{
		if(url == null){
			return false;
		}
		String urlPattern = "^http(s{0,1})://[a-zA-Z0-9_\\-\\.]+\\.([A-Za-z/]{2,5})[a-zA-Z0-9_/\\&\\?\\=\\-\\.\\~\\%]*";
		return url.matches(urlPattern);
	}
	
	/**
	 * Stupidity! AsyncHttpClient copied the Guava implementation so we now need
	 * adapters to delegate between them, all because Java doesn't provide the
	 * ability to add listeners to Future or map/transform the result.
	 */
	private static class AsyncHttpToGuavaAdapter<T> implements ListenableFuture<T>
	{
		private final com.ning.http.client.ListenableFuture<T> delegate;

		public AsyncHttpToGuavaAdapter(com.ning.http.client.ListenableFuture<T> delegate)
		{
			this.delegate = delegate;
		}

		@Override
		public boolean cancel(boolean mayInterruptIfRunning)
		{
			return delegate.cancel(mayInterruptIfRunning);
		}

		@Override
		public boolean isCancelled()
		{
			return delegate.isCancelled();
		}

		@Override
		public boolean isDone()
		{
			return delegate.isDone();
		}

		@Override
		public T get() throws InterruptedException, ExecutionException
		{
			return delegate.get();
		}

		@Override
		public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException
		{
			return delegate.get(timeout, unit);
		}

		@Override
		public void addListener(Runnable listener, Executor executor)
		{
			delegate.addListener(listener, executor);
		}
	}
}
