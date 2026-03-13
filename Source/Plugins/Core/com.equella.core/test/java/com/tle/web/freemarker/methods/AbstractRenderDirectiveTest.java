package com.tle.web.freemarker.methods;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.NestedRenderable;
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

    assertNull(nestedRenderable.getNestedRenderable());
    assertEquals("", output.toString());
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

  private static final class TestAdapterTemplateModel
      implements AdapterTemplateModel, TemplateModel {
    private final Object wrapped;

    private TestAdapterTemplateModel(Object wrapped) {
      this.wrapped = wrapped;
    }

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
      // no-op for tests
    }
  }
}
