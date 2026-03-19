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

package com.tle.web.freemarker.methods;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.render.SectionRenderable;
import java.util.StringJoiner;

@NonNullByDefault
/**
 * Immutable diagnostic payload for logging nested FreeMarker body {@link NullPointerException}
 * failures.
 *
 * <p>The payload captures fallback-chain metadata and a condensed single-line throwable summary so
 * log statements remain concise while still being actionable.
 */
record BodyRenderNpeDiagnostics(
    boolean fallbackRenderablePresent,
    String fallbackRenderableClass,
    int fallbackDepth,
    String fallbackIdentity,
    boolean npeMessagePresent,
    int stackFrameCount,
    String firstFrame,
    boolean causePresent,
    String details,
    @Nullable String resolvedFallbackClass,
    @Nullable String resolvedFallbackIdentity) {

  /**
   * Build diagnostics for a recoverable body-render NPE where rendering can continue via a resolved
   * fallback renderable.
   *
   * @param npe the exception raised while rendering the directive body
   * @param fallbackRenderable the original fallback renderable (possibly null)
   * @param resolvedFallback the terminal fallback renderable actually used for recovery
   * @param maxFrames maximum number of stack frames to include per throwable in the summary
   */
  static BodyRenderNpeDiagnostics forRecoverable(
      NullPointerException npe,
      @Nullable SectionRenderable fallbackRenderable,
      SectionRenderable resolvedFallback,
      int maxFrames) {
    return new BodyRenderNpeDiagnostics(
        fallbackRenderable != null,
        renderableClass(fallbackRenderable),
        fallbackDepth(fallbackRenderable),
        renderableIdentity(fallbackRenderable),
        npe.getMessage() != null,
        stackFrameCount(npe),
        firstFrame(npe),
        npe.getCause() != null,
        summariseThrowableSingleLine(npe, maxFrames),
        renderableClass(resolvedFallback),
        renderableIdentity(resolvedFallback));
  }

  /**
   * Build diagnostics for an unexpected body-render NPE that cannot be recovered and will be
   * rethrown.
   *
   * @param npe the exception raised while rendering the directive body
   * @param fallbackRenderable the current fallback renderable (possibly null)
   * @param maxFrames maximum number of stack frames to include per throwable in the summary
   */
  static BodyRenderNpeDiagnostics forUnexpected(
      NullPointerException npe, @Nullable SectionRenderable fallbackRenderable, int maxFrames) {
    return new BodyRenderNpeDiagnostics(
        fallbackRenderable != null,
        renderableClass(fallbackRenderable),
        fallbackDepth(fallbackRenderable),
        renderableIdentity(fallbackRenderable),
        npe.getMessage() != null,
        stackFrameCount(npe),
        firstFrame(npe),
        npe.getCause() != null,
        summariseThrowableSingleLine(npe, maxFrames),
        null,
        null);
  }

  @Override
  public String toString() {
    StringBuilder sb =
        new StringBuilder("fallbackRenderablePresent=")
            .append(fallbackRenderablePresent)
            .append(", fallbackRenderableClass=")
            .append(fallbackRenderableClass)
            .append(", fallbackDepth=")
            .append(fallbackDepth)
            .append(", fallbackIdentity=")
            .append(fallbackIdentity);
    if (resolvedFallbackClass != null) {
      sb.append(", resolvedFallbackClass=").append(resolvedFallbackClass);
    }
    if (resolvedFallbackIdentity != null) {
      sb.append(", resolvedFallbackIdentity=").append(resolvedFallbackIdentity);
    }
    sb.append(", npeMessagePresent=")
        .append(npeMessagePresent)
        .append(", stackFrameCount=")
        .append(stackFrameCount)
        .append(", firstFrame=")
        .append(firstFrame)
        .append(", causePresent=")
        .append(causePresent)
        .append(", details=")
        .append(details);
    return sb.toString();
  }

  private static int stackFrameCount(Throwable throwable) {
    StackTraceElement[] frames = throwable.getStackTrace();
    return frames == null ? -1 : frames.length;
  }

  private static String firstFrame(Throwable throwable) {
    StackTraceElement[] frames = throwable.getStackTrace();
    if (frames == null || frames.length == 0) {
      return "<none>";
    }
    return frames[0].toString();
  }

  private static String summariseThrowableSingleLine(Throwable throwable, int maxFrames) {
    StringJoiner causeJoiner = new StringJoiner(" || caused by: ");
    for (Throwable current = throwable; current != null; current = current.getCause()) {
      causeJoiner.add(formatThrowableSegment(current, maxFrames));
    }
    return causeJoiner.toString();
  }

  private static String formatThrowableSegment(Throwable throwable, int maxFrames) {
    return throwableHeader(throwable) + frameSummary(throwable.getStackTrace(), maxFrames);
  }

  private static String throwableHeader(Throwable throwable) {
    String message = throwable.getMessage() != null ? throwable.getMessage() : "<no-message>";
    return throwable.getClass().getName() + ": " + message;
  }

  private static String frameSummary(@Nullable StackTraceElement[] frames, int maxFrames) {
    if (frames == null || frames.length == 0) {
      return " | <no-stack-frames>";
    }
    StringJoiner frameJoiner = new StringJoiner(" | ");
    int limit = Math.min(maxFrames, frames.length);
    for (int i = 0; i < limit; i++) {
      frameJoiner.add("at " + frames[i]);
    }
    String summary = " | " + frameJoiner;
    if (frames.length > limit) {
      summary += " | ... " + (frames.length - limit) + " more";
    }
    return summary;
  }

  private static String renderableClass(@Nullable SectionRenderable renderable) {
    return renderable == null ? "<none>" : renderable.getClass().getName();
  }

  private static String renderableIdentity(@Nullable SectionRenderable renderable) {
    if (renderable == null) {
      return "<none>";
    }
    return renderable.getClass().getName()
        + "@"
        + Integer.toHexString(System.identityHashCode(renderable));
  }

  private static int fallbackDepth(@Nullable SectionRenderable renderable) {
    if (renderable instanceof BodyDirectiveRenderable) {
      return ((BodyDirectiveRenderable) renderable).fallbackDepth();
    }
    return 0;
  }
}
