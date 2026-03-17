package com.tle.web.freemarker.methods;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.NestedRenderable;
import com.tle.web.sections.render.PreRenderable;
import com.tle.web.sections.render.SectionRenderable;
import freemarker.core.Environment;
import freemarker.template.AdapterTemplateModel;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateDirectiveBody;
import freemarker.template.TemplateException;
import freemarker.template.TemplateModel;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;

public class AbstractRenderDirectiveTest {
  @Test
  public void executeRendersNestedBodyForNestedRenderable() throws Exception {
    StringWriter output = new StringWriter();
    Environment env = newEnvironment(output);
    RecordingNestedRenderable nestedRenderable = new RecordingNestedRenderable();
    TestRenderDirective directive = new TestRenderDirective(nestedRenderable);

    TemplateDirectiveBody body = mock(TemplateDirectiveBody.class);
    doAnswer(
            invocation -> {
              Writer writer = invocation.getArgument(0);
              writer.write("nested-body");
              return null;
            })
        .when(body)
        .render(any(Writer.class));

    Map<String, TemplateModel> params = new HashMap<>();
    params.put("section", new TestAdapterTemplateModel(nestedRenderable));

    directive.execute(env, params, new TemplateModel[0], body);

    assertNotNull(nestedRenderable.getNestedRenderable());
    assertEquals("nested-body", output.toString());
    verify(body, times(1)).render(any(Writer.class));
  }

  @Test
  public void executeDoesNotSetNestedRenderableWhenBodyIsNull() throws Exception {
    StringWriter output = new StringWriter();
    Environment env = newEnvironment(output);
    RecordingNestedRenderable nestedRenderable = new RecordingNestedRenderable();
    TestRenderDirective directive = new TestRenderDirective(nestedRenderable);

    Map<String, TemplateModel> params = new HashMap<>();
    params.put("section", new TestAdapterTemplateModel(nestedRenderable));

    directive.execute(env, params, new TemplateModel[0], null);

    assertNull(nestedRenderable.getNestedRenderable());
    assertEquals("", output.toString());
  }

  @Test
  public void executeDoesNotSetNestedRenderableWhenBodyRendersEmptyContent() throws Exception {
    StringWriter output = new StringWriter();
    Environment env = newEnvironment(output);
    RecordingNestedRenderable nestedRenderable = new RecordingNestedRenderable();
    nestedRenderable.setNestedRenderable(
        new BodyDirectiveRenderable(writer -> writer.write("fallback-label"), null));
    TestRenderDirective directive = new TestRenderDirective(nestedRenderable);

    TemplateDirectiveBody body = mock(TemplateDirectiveBody.class);
    doAnswer(
            invocation -> {
              Writer writer = invocation.getArgument(0);
              writer.write("");
              return null;
            })
        .when(body)
        .render(any(Writer.class));

    Map<String, TemplateModel> params = new HashMap<>();
    params.put("section", new TestAdapterTemplateModel(nestedRenderable));

    directive.execute(env, params, new TemplateModel[0], body);

    assertNotNull(nestedRenderable.getNestedRenderable());
    assertEquals("fallback-label", output.toString());
  }

  @Test
  public void executePreservesFallbackPreRenderWhenBodyOverridesOutput() throws Exception {
    StringWriter output = new StringWriter();
    Environment env = newEnvironment(output);
    RecordingNestedRenderable nestedRenderable = new RecordingNestedRenderable();
    SectionRenderable fallback = mock(SectionRenderable.class);
    nestedRenderable.setNestedRenderable(fallback);
    TestRenderDirective directive = new TestRenderDirective(nestedRenderable);

    TemplateDirectiveBody body = mock(TemplateDirectiveBody.class);
    doAnswer(
            invocation -> {
              Writer writer = invocation.getArgument(0);
              writer.write("override");
              return null;
            })
        .when(body)
        .render(any(Writer.class));

    Map<String, TemplateModel> params = new HashMap<>();
    params.put("section", new TestAdapterTemplateModel(nestedRenderable));

    directive.execute(env, params, new TemplateModel[0], body);

    assertEquals("override", output.toString());
    verify(fallback, times(1)).preRender(any(PreRenderContext.class));
  }

  @Test
  public void executeFallsBackWhenBodyRenderHitsMissingMacroContextNpe() throws Exception {
    StringWriter output = new StringWriter();
    Environment env = newEnvironment(output);
    RecordingNestedRenderable nestedRenderable = new RecordingNestedRenderable();
    nestedRenderable.setNestedRenderable(
        new BodyDirectiveRenderable(writer -> writer.write("fallback-label"), null));
    TestRenderDirective directive = new TestRenderDirective(nestedRenderable);

    TemplateDirectiveBody body = mock(TemplateDirectiveBody.class);
    doAnswer(
            invocation -> {
              NullPointerException npe =
                  new NullPointerException(
                      "Cannot read field \"nestedContentParameterNames\" because "
                          + "\"this.invokingMacroContext\" is null");
              npe.setStackTrace(
                  new StackTraceElement[] {
                    new StackTraceElement(
                        "freemarker.core.BodyInstruction$Context",
                        "<init>",
                        "BodyInstruction.java",
                        128)
                  });
              throw npe;
            })
        .when(body)
        .render(any(Writer.class));

    Map<String, TemplateModel> params = new HashMap<>();
    params.put("section", new TestAdapterTemplateModel(nestedRenderable));

    directive.execute(env, params, new TemplateModel[0], body);

    assertEquals("fallback-label", output.toString());
  }

  @Test
  public void executeSkipsBodyDirectiveFallbackChainForMissingMacroContextNpe() throws Exception {
    StringWriter output = new StringWriter();
    Environment env = newEnvironment(output);
    RecordingNestedRenderable nestedRenderable = new RecordingNestedRenderable();
    AtomicInteger skippedBodyInvocations = new AtomicInteger(0);
    SectionRenderable terminalFallback =
        new SectionRenderable() {
          @Override
          public void realRender(SectionWriter writer) throws IOException {
            writer.write("terminal-fallback");
          }

          @Override
          public void preRender(PreRenderContext info) {
            // no-op
          }
        };
    SectionRenderable nestedChain =
        new BodyDirectiveRenderable(
            writer -> skippedBodyInvocations.incrementAndGet(), terminalFallback);
    nestedRenderable.setNestedRenderable(
        new BodyDirectiveRenderable(writer -> writer.write("ignored"), nestedChain));
    TestRenderDirective directive = new TestRenderDirective(nestedRenderable);

    TemplateDirectiveBody body = mock(TemplateDirectiveBody.class);
    doAnswer(
            invocation -> {
              NullPointerException npe =
                  new NullPointerException(
                      "Cannot read field \"nestedContentParameterNames\" because "
                          + "\"this.invokingMacroContext\" is null");
              npe.setStackTrace(
                  new StackTraceElement[] {
                    new StackTraceElement(
                        "freemarker.core.BodyInstruction$Context",
                        "<init>",
                        "BodyInstruction.java",
                        128)
                  });
              throw npe;
            })
        .when(body)
        .render(any(Writer.class));

    Map<String, TemplateModel> params = new HashMap<>();
    params.put("section", new TestAdapterTemplateModel(nestedRenderable));

    directive.execute(env, params, new TemplateModel[0], body);

    assertEquals("terminal-fallback", output.toString());
    assertEquals(0, skippedBodyInvocations.get());
  }

  @Test
  public void executeFallsBackWhenBodyRenderHitsStacklessNpe() throws Exception {
    StringWriter output = new StringWriter();
    Environment env = newEnvironment(output);
    RecordingNestedRenderable nestedRenderable = new RecordingNestedRenderable();
    nestedRenderable.setNestedRenderable(
        new BodyDirectiveRenderable(writer -> writer.write("stackless-fallback"), null));
    TestRenderDirective directive = new TestRenderDirective(nestedRenderable);

    TemplateDirectiveBody body = mock(TemplateDirectiveBody.class);
    doAnswer(
            invocation -> {
              NullPointerException npe = new NullPointerException();
              npe.setStackTrace(new StackTraceElement[0]);
              throw npe;
            })
        .when(body)
        .render(any(Writer.class));

    Map<String, TemplateModel> params = new HashMap<>();
    params.put("section", new TestAdapterTemplateModel(nestedRenderable));

    directive.execute(env, params, new TemplateModel[0], body);

    assertEquals("stackless-fallback", output.toString());
  }

  private static Environment newEnvironment(StringWriter output)
      throws IOException, TemplateException {
    Configuration configuration = new Configuration(Configuration.VERSION_2_3_34);
    Template template = new Template("test", new StringReader(""), configuration);
    return template.createProcessingEnvironment(Collections.emptyMap(), output);
  }

  private static final class TestRenderDirective extends AbstractRenderDirective {
    private final SectionRenderable renderable;
    private final SectionWriter sectionWriter;

    private TestRenderDirective(SectionRenderable renderable) {
      this.renderable = renderable;
      RenderContext renderContext = mock(RenderContext.class);
      PreRenderContext preRenderContext = mock(PreRenderContext.class);
      when(renderContext.getPreRenderContext()).thenReturn(preRenderContext);
      doAnswer(
              invocation -> {
                PreRenderable preRenderable = invocation.getArgument(0);
                preRenderable.preRender(preRenderContext);
                return null;
              })
          .when(preRenderContext)
          .preRender(any(PreRenderable.class));
      this.sectionWriter = new SectionWriter(new StringWriter(), renderContext);
    }

    @Override
    public SectionWriter getSectionWriter() {
      return sectionWriter;
    }

    @Override
    protected SectionRenderable getRenderable(Object section, Map<String, TemplateModel> params) {
      return renderable;
    }
  }

  private record TestAdapterTemplateModel(Object wrapped)
      implements AdapterTemplateModel, TemplateModel {

    @Override
    public Object getAdaptedObject(Class hint) {
      return wrapped;
    }
  }

  private static final class RecordingNestedRenderable implements NestedRenderable {
    private SectionRenderable nestedRenderable;

    @Override
    public NestedRenderable setNestedRenderable(SectionRenderable nested) {
      this.nestedRenderable = nested;
      return this;
    }

    @Override
    public SectionRenderable getNestedRenderable() {
      return nestedRenderable;
    }

    @Override
    public void realRender(SectionWriter writer) throws IOException {
      if (nestedRenderable != null) {
        nestedRenderable.realRender(writer);
      }
    }

    @Override
    public void preRender(PreRenderContext info) {
      if (nestedRenderable != null) {
        nestedRenderable.preRender(info);
      }
    }
  }
}
