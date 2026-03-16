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
  @SuppressWarnings({"unchecked", "nls", "rawtypes"})
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
          if (renderable instanceof NestedRenderable && body != null) {
            NestedRenderable nestedRenderable = (NestedRenderable) renderable;
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

    @Override
    public void realRender(SectionWriter writer) throws IOException {
      StringWriter bodyBuffer = new StringWriter();
      try {
        body.render(bodyBuffer);
      } catch (TemplateException e) {
        throw new SectionsRuntimeException(e);
      } catch (NullPointerException e) {
        if (canFallbackFromBodyNpe(e) && fallbackRenderable != null) {
          SectionRenderable resolvedFallback = unwrapBodyDirectiveFallback(fallbackRenderable);
          LOGGER.warn(
              "FreeMarker nested body rendering hit recoverable body NPE. Falling back to "
                  + "original nested renderable. fallbackRenderableClass={}, fallbackDepth={}, "
                  + "fallbackIdentity={}, resolvedFallbackClass={}, resolvedFallbackIdentity={}, "
                  + "details={}",
              fallbackRenderable.getClass().getName(),
              fallbackDepth(fallbackRenderable),
              renderableIdentity(fallbackRenderable),
              resolvedFallback.getClass().getName(),
              renderableIdentity(resolvedFallback),
              summariseThrowableSingleLine(e, 20));
          resolvedFallback.realRender(writer);
          return;
        }
        LOGGER.error(
            "Unexpected NullPointerException while rendering FreeMarker nested body. "
                + "fallbackRenderablePresent={}, fallbackRenderableClass={}, fallbackDepth={}, "
                + "fallbackIdentity={}, npeMessagePresent={}, stackFrameCount={}, "
                + "firstFrame={}, causePresent={}, details={}",
            fallbackRenderable != null,
            fallbackRenderable == null ? "<none>" : fallbackRenderable.getClass().getName(),
            fallbackDepth(fallbackRenderable),
            renderableIdentity(fallbackRenderable),
            e.getMessage() != null,
            e.getStackTrace() == null ? -1 : e.getStackTrace().length,
            firstFrame(e),
            e.getCause() != null,
            summariseThrowableSingleLine(e, 30));
        throw e;
      }

      String renderedBody = bodyBuffer.toString();
      if (renderedBody.isBlank() && fallbackRenderable != null) {
        fallbackRenderable.realRender(writer);
      } else {
        writer.write(renderedBody);
      }
    }

    @Override
    public void preRender(PreRenderContext info) {
      if (fallbackRenderable != null) {
        fallbackRenderable.preRender(info);
      }
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

  private static String summariseThrowableSingleLine(Throwable throwable, int maxFrames) {
    StringBuilder sb = new StringBuilder();
    Throwable current = throwable;
    int depth = 0;
    while (current != null) {
      if (depth > 0) {
        sb.append(" || caused by: ");
      }
      sb.append(current.getClass().getName());
      if (current.getMessage() != null) {
        sb.append(": ").append(current.getMessage());
      } else {
        sb.append(": <no-message>");
      }
      StackTraceElement[] frames = current.getStackTrace();
      if (frames == null || frames.length == 0) {
        sb.append(" | <no-stack-frames>");
        current = current.getCause();
        depth++;
        continue;
      }
      int limit = Math.min(maxFrames, frames.length);
      for (int i = 0; i < limit; i++) {
        sb.append(" | at ").append(frames[i]);
      }
      if (frames.length > limit) {
        sb.append(" | ... ").append(frames.length - limit).append(" more");
      }
      current = current.getCause();
      depth++;
    }
    return sb.toString();
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
    int depth = 0;
    SectionRenderable current = renderable;
    while (current instanceof BodyDirectiveRenderable) {
      depth++;
      current = ((BodyDirectiveRenderable) current).fallbackRenderable;
    }
    return depth;
  }

  private static String firstFrame(Throwable throwable) {
    StackTraceElement[] frames = throwable.getStackTrace();
    if (frames == null || frames.length == 0) {
      return "<none>";
    }
    return frames[0].toString();
  }

  private static SectionRenderable unwrapBodyDirectiveFallback(SectionRenderable renderable) {
    SectionRenderable current = renderable;
    while (current instanceof BodyDirectiveRenderable
        && ((BodyDirectiveRenderable) current).fallbackRenderable != null) {
      current = ((BodyDirectiveRenderable) current).fallbackRenderable;
    }
    return current;
  }
}
