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
import freemarker.core.TemplateElement;
import freemarker.template.AdapterTemplateModel;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonNullByDefault
public abstract class AbstractRenderDirective extends SectionsTemplateModel
    implements TemplateDirectiveModel {

  private static final MacroContextIntrospector macroIntrospector = new MacroContextIntrospector();

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
          if (renderable instanceof NestedRenderable && body != null && hasNestedContent(env)) {
            ((NestedRenderable) renderable).setNestedRenderable(new BodyDirectiveRenderable(body));
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

    public BodyDirectiveRenderable(TemplateDirectiveBody body) {
      this.body = body;
    }

    @Override
    public void realRender(SectionWriter writer) throws IOException {
      try {

        body.render(writer);
      } catch (TemplateException e) {
        throw new SectionsRuntimeException(e);
      }
    }

    @Override
    public void preRender(PreRenderContext info) {
      // nothing
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

  /**
   * Checks whether the current macro call has nested content by reflecting on FreeMarker internals.
   *
   * <p>Reflection is required because the public {@code getCurrentDirectiveCallPlace()} API returns
   * the call site of <em>this</em> directive ({@code _render}), not the enclosing macro ({@code
   * render}). See {@code Dev/docs/freemarker-reflection.md} for full analysis.
   *
   * @return true if the macro was called with nested content, false otherwise
   */
  static boolean hasNestedContent(Environment env) {
    return macroIntrospector
        .getMacroCallPlace(env)
        .filter(callPlace -> callPlace.getChildCount() > 0)
        .isPresent();
  }

  /**
   * Encapsulates reflective access to FreeMarker's package-private macro context internals.
   *
   * <p>In FreeMarker 2.3.34, {@code Macro.Context} no longer has a {@code nestedContent} field.
   * Instead, the {@code callPlace} field references the {@code UnifiedCall} element, and nested
   * content is determined via the public {@code TemplateElement.getChildCount()} method.
   *
   * <p>This class handles lazy initialization of reflective handles, error recovery via a
   * circuit-breaker pattern, and provides a clean {@code Optional}-based API.
   */
  private static class MacroContextIntrospector {
    private static final Logger LOGGER = LoggerFactory.getLogger(MacroContextIntrospector.class);

    private final Method currentContextMethod;
    @Nullable private Field callPlaceField;
    private boolean reflectionFailed = false;

    MacroContextIntrospector() {
      try {
        currentContextMethod = Environment.class.getDeclaredMethod("getCurrentMacroContext");
        currentContextMethod.setAccessible(true);
      } catch (Exception e) {
        throw new SectionsRuntimeException(e);
      }
    }

    /**
     * Returns the {@link TemplateElement} representing the macro call site, if available.
     *
     * <p>This traverses: {@code Environment} → {@code Macro.Context} (via reflection) → {@code
     * callPlace} field (via reflection) → {@code TemplateElement} (public type).
     */
    Optional<TemplateElement> getMacroCallPlace(Environment env) {
      if (reflectionFailed) {
        return Optional.empty();
      }
      try {
        return getMacroContext(env).flatMap(this::getCallPlace);
      } catch (Exception e) {
        LOGGER.warn(
            "Failed to check nested content via reflection on FreeMarker internals. "
                + "Body directives will not be wrapped for nested rendering.",
            e);
        reflectionFailed = true;
        return Optional.empty();
      }
    }

    private Optional<Object> getMacroContext(Environment env) throws Exception {
      return Optional.ofNullable(currentContextMethod.invoke(env));
    }

    private Optional<TemplateElement> getCallPlace(Object context) {
      try {
        if (callPlaceField == null) {
          callPlaceField = context.getClass().getDeclaredField("callPlace");
          callPlaceField.setAccessible(true);
        }
        return Optional.of(callPlaceField.get(context))
            .filter(TemplateElement.class::isInstance)
            .map(TemplateElement.class::cast);
      } catch (Exception e) {
        throw new SectionsRuntimeException(e);
      }
    }
  }
}
