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

import com.tle.beans.ReferencedURL;
import java.net.http.HttpResponse.BodyHandler;
import java.net.http.HttpResponse.BodySubscriber;
import java.net.http.HttpResponse.ResponseInfo;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;

/**
 * A body handler that only downloads the response body when it is actually needed - i.e. to capture
 * the failure message of a GET request. HEAD responses carry no body, redirect responses are about
 * to be followed, and responses whose status the URL checker treats as "exists" ({@code 2xx}, plus
 * {@code 401}/{@code 402}) discard their body, so in those cases the body is aborted before any of
 * it is transferred. This reproduces async-http-client, whose {@code AsyncHandler} returned {@code
 * State.ABORT} on a successful status (never reading the body) and stopped reading once the
 * captured body reached {@link ReferencedURL#MAX_MESSAGE_LENGTH}.
 */
final class BoundedBodyHandler implements BodyHandler<String> {
  private final boolean head;

  BoundedBodyHandler(final boolean head) {
    this.head = head;
  }

  @Override
  public BodySubscriber<String> apply(final ResponseInfo responseInfo) {
    final int code = responseInfo.statusCode();
    final boolean bodyNeeded =
        !head && !URLCheckerService.isRedirect(code) && !URLCheckerService.isTreatedAsExists(code);
    return new BoundedStringSubscriber(bodyNeeded ? ReferencedURL.MAX_MESSAGE_LENGTH : 0);
  }

  /**
   * A {@link BodySubscriber} that accumulates at most {@code maxBytes} bytes of the response body
   * as a UTF-8 string and then cancels the subscription, so large or slow bodies are not downloaded
   * in full. A {@code maxBytes} of {@code 0} aborts before any body is transferred.
   *
   * <p>The Flow spec (rule 1.1) guarantees {@code onSubscribe} is signalled before any other
   * method; the {@code volatile} subscription field and the guards below are defensive hygiene
   * against non-compliant publishers (and implement rule 2.5's requirement to cancel a duplicate
   * subscription).
   */
  private static final class BoundedStringSubscriber implements BodySubscriber<String> {
    private final int maxBytes;
    private final CompletableFuture<String> result = new CompletableFuture<>();
    private final StringBuilder body = new StringBuilder();
    private volatile Flow.Subscription subscription;
    private int bytesRead;

    BoundedStringSubscriber(final int maxBytes) {
      this.maxBytes = maxBytes;
    }

    @Override
    public CompletionStage<String> getBody() {
      return result;
    }

    @Override
    public void onSubscribe(final Flow.Subscription subscription) {
      if (this.subscription != null) {
        // Flow spec rule 2.5: cancel any subscription after the first.
        subscription.cancel();
        return;
      }
      this.subscription = subscription;
      if (maxBytes <= 0) {
        subscription.cancel();
        result.complete("");
      } else {
        subscription.request(Long.MAX_VALUE);
      }
    }

    @Override
    public void onNext(final List<ByteBuffer> buffers) {
      if (result.isDone()) {
        // Already completed (bound reached and cancelled) - ignore any buffers still in flight.
        return;
      }
      for (final ByteBuffer buffer : buffers) {
        final byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        body.append(new String(bytes, StandardCharsets.UTF_8));
        bytesRead += bytes.length;
      }
      if (bytesRead >= maxBytes) {
        subscription.cancel();
        result.complete(body.toString());
      }
    }

    @Override
    public void onError(final Throwable throwable) {
      result.completeExceptionally(throwable);
    }

    @Override
    public void onComplete() {
      result.complete(body.toString());
    }
  }
}
