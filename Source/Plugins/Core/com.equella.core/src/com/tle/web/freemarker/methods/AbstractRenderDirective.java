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
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.NestedRenderable;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.StyleableRenderer;
import freemarker.core.Environment;
import freemarker.template.AdapterTemplateModel;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
public abstract class AbstractRenderDirective extends SectionsTemplateModel
    implements TemplateDirectiveModel {
  private static final Logger LOGGER = LoggerFactory.getLogger(AbstractRenderDirective.class);

  @NonNullByDefault(false)
  @SuppressWarnings({"unchecked", "nls"})
  @Override
  public void execute(Environment env, Map params, TemplateModel[] arg2, TemplateDirectiveBody body)
      throws TemplateException, IOException {
    RenderContext info = getSectionWriter();
    try {
      Object model = params.get("section"); // $NON-NLS-1$
      if (model instanceof AdapterTemplateModel) {
        Object wrapped = ((AdapterTemplateModel) model).getAdaptedObject(Object.class);
        if (wrapped != null) {
          SectionRenderable renderable = getRenderable(wrapped, params);
          if (renderable == null) {
            return;
          }
          if (renderable instanceof StyleableRenderer) {
            ((StyleableRenderer) renderable)
                .setStyles(
                    getParam("style", params), getParam("class", params), getParam("id", params));
          }
          if (renderable instanceof NestedRenderable nestedRenderable && body != null) {
            nestedRenderable.setNestedRenderable(
                new BodyDirectiveRenderable(body, nestedRenderable.getNestedRenderable()));
          }
          SectionWriter writer = new SectionWriter(env.getOut(), info);
          writer.preRender(renderable);
          renderable.realRender(writer);
        }
      } else {
        throw new RuntimeException(
            "'section' parameter to render macro was not of the appropriate type (was "
                + model.getClass()
                + ").  Perhaps you named your section property the same as a method name?");
      }
    } catch (Exception e) {
      throw (e instanceof RuntimeException ? (RuntimeException) e : new RuntimeException(e));
    }
  }

  public static class BodyDirectiveRenderable implements SectionRenderable {

    private final TemplateDirectiveBody body;
    @Nullable private final SectionRenderable fallbackRenderable;

    public BodyDirectiveRenderable(
        TemplateDirectiveBody body, @Nullable SectionRenderable fallbackRenderable) {
      this.body = body;
      this.fallbackRenderable = fallbackRenderable;
    }

    /**
     * Renders nested FreeMarker directive content into Sections rendering.
     *
     * <p>Under FreeMarker 2.3.34, rendering nested directive bodies can throw {@link
     * NullPointerException} in two known edge-cases:
     *
     * <ul>
     *   <li>missing macro invocation context (`BodyInstruction$Context` with `invokingMacroContext`
     *       null), and
     *   <li>a stackless/message-less NPE emitted by FreeMarker internals.
     * </ul>
     *
     * <p>For those recoverable cases we intentionally fall back to the original nested renderable
     * chain to preserve legacy rendering behaviour (including Ajax/dialog paths). Any other NPE is
     * treated as unexpected and rethrown after logging diagnostics.
     *
     * <p>This came about when migrating to FreeMarker 2.3.34. The original legacy implementation
     * relied on accessing internal methods via reflection. But in about version 2.3.24 these were
     * removed, so {@code AbstractRenderDirective} was changed to use only public APIs.
     */
    @Override
    public void realRender(SectionWriter writer) throws IOException {
      String renderedBody = renderBodyOrFallback(writer);
      if (renderedBody == null) {
        return;
      }
      writeRenderedBodyOrFallback(renderedBody, writer);
    }

    @Nullable
    private String renderBodyOrFallback(SectionWriter writer) throws IOException {
      StringWriter bodyBuffer = new StringWriter();
      try {
        body.render(bodyBuffer);
        return bodyBuffer.toString();
      } catch (TemplateException e) {
        throw new SectionsRuntimeException(e);
      } catch (NullPointerException e) {
        return recoverFromBodyNpeOrRethrow(writer, e);
      }
    }

    @Nullable
    private String recoverFromBodyNpeOrRethrow(SectionWriter writer, NullPointerException e)
        throws IOException {
      if (canFallbackFromBodyNpe(e) && fallbackRenderable != null) {
        renderRecoverableFallback(writer, e);
        return null;
      }
      logUnexpectedBodyNpe(e);
      throw e;
    }

    private void renderRecoverableFallback(SectionWriter writer, NullPointerException npe)
        throws IOException {
      SectionRenderable resolvedFallback = unwrapBodyDirectiveFallback(fallbackRenderable);
      if (LOGGER.isDebugEnabled()) {
        BodyRenderNpeDiagnostics diagnostics =
            BodyRenderNpeDiagnostics.forRecoverable(npe, fallbackRenderable, resolvedFallback, 20);
        LOGGER.debug(
            "FreeMarker nested body rendering hit recoverable body NPE; using fallback. {}",
            diagnostics);
      }
      resolvedFallback.realRender(writer);
    }

    private void logUnexpectedBodyNpe(NullPointerException npe) {
      BodyRenderNpeDiagnostics diagnostics =
          BodyRenderNpeDiagnostics.forUnexpected(npe, fallbackRenderable, 30);
      LOGGER.error(
          "Unexpected NullPointerException while rendering FreeMarker nested body. {}",
          diagnostics);
    }

    private void writeRenderedBodyOrFallback(String renderedBody, SectionWriter writer)
        throws IOException {
      if (renderedBody.isBlank() && fallbackRenderable != null) {
        fallbackRenderable.realRender(writer);
        return;
      }
      writer.write(renderedBody);
    }

    @Override
    public void preRender(PreRenderContext info) {
      if (fallbackRenderable != null) {
        fallbackRenderable.preRender(info);
      }
    }

    int fallbackDepth() {
      int depth = 1;
      SectionRenderable current = fallbackRenderable;
      while (current instanceof BodyDirectiveRenderable) {
        depth++;
        current = ((BodyDirectiveRenderable) current).fallbackRenderable;
      }
      return depth;
    }
  }

  @Nullable
  protected String getParam(String param, Map<?, ?> params) {
    Object val = params.get(param);
    if (val != null) {
      return val.toString();
    }
    return null;
  }

  @Nullable
  protected abstract SectionRenderable getRenderable(
      Object section, Map<String, TemplateModel> params);

  private static boolean isMissingMacroContextNpe(NullPointerException npe) {
    String message = npe.getMessage();
    if (message == null || !message.contains("invokingMacroContext")) {
      return false;
    }
    for (StackTraceElement frame : npe.getStackTrace()) {
      if ("freemarker.core.BodyInstruction$Context".equals(frame.getClassName())) {
        return true;
      }
    }
    return false;
  }

  private static boolean isStacklessNpe(NullPointerException npe) {
    StackTraceElement[] frames = npe.getStackTrace();
    return npe.getMessage() == null
        && (frames == null || frames.length == 0)
        && npe.getCause() == null;
  }

  private static boolean canFallbackFromBodyNpe(NullPointerException npe) {
    return isMissingMacroContextNpe(npe) || isStacklessNpe(npe);
  }

  private static SectionRenderable unwrapBodyDirectiveFallback(SectionRenderable renderable) {
    SectionRenderable current = renderable;
    while (current instanceof BodyDirectiveRenderable) {
      SectionRenderable next = ((BodyDirectiveRenderable) current).fallbackRenderable;
      if (next == null) {
        break;
      }
      current = next;
    }
    return current;
  }
}
