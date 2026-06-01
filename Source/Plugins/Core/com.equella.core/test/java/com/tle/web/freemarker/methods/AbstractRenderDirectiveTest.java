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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tle.annotation.NonNull;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateDirectiveModel;
import java.io.StringWriter;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.commons.lang.StringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Tests for the reflection-based nested content detection in {@link AbstractRenderDirective}.
 *
 * <p>These tests exercise {@code hasNestedContent()} by rendering real FreeMarker templates that
 * invoke a custom directive, capturing the Environment at the point of the directive call.
 */
class AbstractRenderDirectiveTest {

  private static final String PROBE_DIRECTIVE_NAME = "probe";

  private Configuration cfg;

  @BeforeEach
  void setUp() {
    cfg = new Configuration(Configuration.VERSION_2_3_34);
    cfg.setTemplateLoader(new StringTemplateLoader());
  }

  /**
   * When a macro wraps our directive and is called self-closing ({@code <@mymacro/>}), the
   * directive body is non-null (it wraps the {@code <#nested/>} instruction), but there is no
   * actual nested content. {@code hasNestedContent} must return false.
   */
  @Test
  void selfClosingMacroCallHasNoNestedContent() throws Exception {
    boolean hasNested =
        renderTemplateAndCaptureNestedContentFlag(
            "<#macro mymacro>" + probe("<#nested/>") + "</#macro><@mymacro/>");
    assertFalse(hasNested, "Self-closing macro call should have no nested content");
  }

  /**
   * When a macro wraps our directive and is called with nested content ({@code
   * <@mymacro>content</@mymacro>}), {@code hasNestedContent} must return true.
   */
  @Test
  void macroCallWithNestedContentDetected() throws Exception {
    boolean hasNested =
        renderTemplateAndCaptureNestedContentFlag(
            "<#macro mymacro>"
                + probe("<#nested/>")
                + "</#macro>"
                + "<@mymacro>some content</@mymacro>");
    assertTrue(hasNested, "Macro call with nested content should be detected");
  }

  /**
   * When our directive is called directly (not via a macro) without nested content, {@code
   * hasNestedContent} must return false and not throw.
   */
  @Test
  void directCallWithoutBodyReturnsFalse() throws Exception {
    boolean hasNested = renderTemplateAndCaptureNestedContentFlag(probe());
    assertFalse(hasNested, "Direct call without body should return false");
  }

  /** Verifies that BodyDirectiveRenderable correctly delegates to the template body writer. */
  @Test
  void bodyDirectiveRenderableRendersBody() {
    StringWriter output = new StringWriter();
    AtomicBoolean rendered = new AtomicBoolean(false);

    final String bodyContent = "hello from body";
    TemplateDirectiveBody body =
        writer -> {
          writer.write(bodyContent);
          rendered.set(true);
        };

    var renderable = new AbstractRenderDirective.BodyDirectiveRenderable(body);
    var mockContext = mock(com.tle.web.sections.events.RenderContext.class);
    when(mockContext.getPreRenderContext())
        .thenReturn(mock(com.tle.web.sections.events.PreRenderContext.class));
    var sectionWriter = new com.tle.web.sections.SectionWriter(output, mockContext);

    assertDoesNotThrow(() -> renderable.realRender(sectionWriter));
    assertTrue(rendered.get());
    assertEquals(bodyContent, output.toString());
  }

  private String probe(@NonNull String content) {
    // Matches PROBE_DIRECTIVE_NAME in createProbeDirective()
    return StringUtils.isEmpty(content) ? "<@probe/>" : "<@probe>" + content + "</@probe>";
  }

  private String probe() {
    return probe("");
  }

  /** Creates a directive that captures the result of {@code hasNestedContent()} when invoked. */
  private TemplateDirectiveModel createProbeDirective(AtomicReference<Boolean> result) {
    return (env, params, loopVars, body) -> {
      result.set(AbstractRenderDirective.hasNestedContent(env));
      if (body != null) {
        body.render(env.getOut());
      }
    };
  }

  private boolean renderTemplateAndCaptureNestedContentFlag(String template) throws Exception {
    final String TEMPLATE_NAME = "test.ftl";

    AtomicReference<Boolean> result = new AtomicReference<>();
    TemplateDirectiveModel probe = createProbeDirective(result);

    StringTemplateLoader loader = (StringTemplateLoader) cfg.getTemplateLoader();
    loader.putTemplate(TEMPLATE_NAME, template);
    cfg.setSharedVariable(PROBE_DIRECTIVE_NAME, probe);

    Template t = cfg.getTemplate(TEMPLATE_NAME);
    t.process(null, new StringWriter());

    return result.get();
  }
}
