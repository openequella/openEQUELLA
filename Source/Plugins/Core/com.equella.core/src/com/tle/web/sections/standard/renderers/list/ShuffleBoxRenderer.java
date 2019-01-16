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

package com.tle.web.sections.standard.renderers.list;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import com.tle.common.Check;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.AppendedElementId;
import com.tle.web.sections.js.generic.expression.ArrayExpression;
import com.tle.web.sections.js.generic.expression.CombinedExpression;
import com.tle.web.sections.js.generic.expression.FunctionCallExpression;
import com.tle.web.sections.js.generic.expression.PropertyExpression;
import com.tle.web.sections.js.generic.function.AnonymousFunction;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.js.generic.function.PrependedParameterFunction;
import com.tle.web.sections.js.generic.statement.FunctionCallStatement;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.RendererConstants;
import com.tle.web.sections.standard.RendererFactory;
import com.tle.web.sections.standard.RendererFactoryAware;
import com.tle.web.sections.standard.js.JSDisableable;
import com.tle.web.sections.standard.js.JSListComponent;
import com.tle.web.sections.standard.js.modules.SelectModule;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.HtmlListState;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.renderers.AbstractElementRenderer;

@SuppressWarnings("nls")
public class ShuffleBoxRenderer extends AbstractElementRenderer
    implements JSListComponent, JSDisableable, RendererFactoryAware {
  private static final PluginResourceHelper URL_HELPER =
      ResourcesService.getResourceHelper(ShuffleBoxRenderer.class);
  private static final IncludeFile SHUFFLE_JS =
      new IncludeFile(URL_HELPER.url("js/shufflebox.js"), SelectModule.INCLUDE);
  private static final String CSS_URL = URL_HELPER.url("css/shufflebox.css");

  private static final JSCallable SETUP_FUNC =
      new ExternallyDefinedFunction("setupShuffleBox", 4, SHUFFLE_JS);
  private static final JSCallable DISABLE_FUNC =
      new ExternallyDefinedFunction("disableShuffleBox", SHUFFLE_JS);

  private final HtmlListState listState;

  private RendererFactory rendererFactory;

  private int size;
  private String leftLabel;
  private String rightLabel;
  private String allRightButtonText;
  private String rightButtonText;
  private String allLeftButtonText;
  private String leftButtonText;
  private String boxWidth;

  private ShuffleDropDownRenderer dropDownRight;
  private ShuffleDropDownRenderer dropDownLeft;

  private SectionRenderable leftButton;
  private SectionRenderable allLeftButton;
  private SectionRenderable rightButton;
  private SectionRenderable allRightButton;

  public ShuffleBoxRenderer(HtmlListState state) {
    super(state);
    this.listState = state;
  }

  @Override
  public void preRender(PreRenderContext info) {
    setupDropDowns(info);
    super.preRender(info);

    info.addCss(CSS_URL);
    info.preRender(dropDownLeft, dropDownRight);
  }

  private void setupDropDowns(PreRenderContext info) {
    HtmlListState dropState = new HtmlListState();
    dropState.setSelectedValues(listState.getSelectedValues());
    dropState.setOptions(listState.getOptions());
    dropState.setElementId(this);
    dropState.setMultiple(true);
    dropState.setName(listState.getName());
    if (!Check.isEmpty(boxWidth)) {
      dropState.setStyle("width:" + boxWidth);
    }
    dropDownLeft = new ShuffleDropDownRenderer(dropState, ShuffleDropDownRenderer.LEFT);
    dropDownRight = new ShuffleDropDownRenderer(dropState, ShuffleDropDownRenderer.RIGHT);
    dropDownLeft.setSize(size);
    dropDownRight.setSize(size);
    allRightButton = setupButton(info, allRightButtonText, "ar");
    rightButton = setupButton(info, rightButtonText, "r");
    allLeftButton = setupButton(info, allLeftButtonText, "al");
    leftButton = setupButton(info, leftButtonText, "l");
  }

  @Override
  protected void writeStart(SectionWriter writer, Map<String, String> attrs) throws IOException {
    // Do not write anything
  }

  @Override
  protected void writeMiddle(SectionWriter writer) throws IOException {
    writer.preRender(allRightButton, rightButton, leftButton, allLeftButton);
    JSStatements changeHandler = tagState.getHandler(JSHandler.EVENT_CHANGE);
    ArrayExpression buttons =
        new ArrayExpression(allRightButton, rightButton, leftButton, allLeftButton);

    if (changeHandler == null) {
      changeHandler =
          new FunctionCallStatement("updateButtons", dropDownLeft, dropDownRight, buttons);
    }

    writer.addReadyStatements(
        new FunctionCallStatement(
            SETUP_FUNC,
            dropDownLeft,
            dropDownRight,
            buttons,
            new AnonymousFunction(changeHandler)));

    writer.write("<div class=\"shuffle-box\">\n");

    writer.write("<div class=\"shuffle-box-inner\">\n");

    writer.write("<div class=\"label-box\">\n");
    if (!Check.isEmpty(leftLabel)) {
      writer.write("<label class=\"label-left\">" + leftLabel + "</label>");
    }
    if (!Check.isEmpty(rightLabel)) {
      writer.write("<label class=\"label-right\">" + rightLabel + "</label>");
    }
    writer.write("</div>");

    dropDownLeft.realRender(writer);

    writer.write("<div class=\"shuffle-box-controls\">\n");

    writer.write("<div class=\"shuffle-box-controls-right\">\n");
    allRightButton.realRender(writer);
    rightButton.realRender(writer);
    writer.write("</div>");
    writer.write("<div class=\"shuffle-box-controls-left\">\n");
    leftButton.realRender(writer);
    allLeftButton.realRender(writer);
    writer.write("</div>");

    writer.write("</div>");

    dropDownRight.realRender(writer);
    writer.write("</div>");
    writer.write("<div style='clear: both;'></div>");
    writer.write("</div>");
  }

  private SectionRenderable setupButton(SectionInfo info, String text, String postfix) {
    HtmlComponentState button = new HtmlComponentState(RendererConstants.BUTTON);
    button.setLabel(new TextLabel(text));
    button.setElementId(new AppendedElementId(this, postfix));

    return rendererFactory.getRenderer(info, button);
  }

  protected static class ShuffleDropDownRenderer extends DropDownRenderer {
    public static final int LEFT = 1;
    public static final int RIGHT = 2;

    protected int side;

    public ShuffleDropDownRenderer(HtmlListState state, int side) {
      super(state);
      this.side = side;
      if (side == LEFT) {
        setElementId(new AppendedElementId(state, "_left"));
      }
    }

    @Override
    protected boolean isDontSelect() {
      return true;
    }

    @Override
    protected boolean isSelectAll() {
      return side == RIGHT;
    }

    @Override
    protected boolean isIncluded(Option<?> option, Set<String> selectedValues) {
      return selectedValues.contains(option.getValue()) ^ side == LEFT;
    }

    @Override
    protected String getName(SectionInfo info) {
      if (side == LEFT) {
        return null;
      }
      return super.getName(info);
    }
  }

  @Override
  protected Map<String, String> prepareAttributes(SectionWriter writer) throws IOException {
    return null;
  }

  public void setSize(int size) {
    this.size = size;
  }

  public void setLeftLabel(String leftLabel) {
    this.leftLabel = leftLabel;
  }

  public void setRightLabel(String rightLabel) {
    this.rightLabel = rightLabel;
  }

  public void setAllRightButtonText(String allRightButtonText) {
    this.allRightButtonText = allRightButtonText;
  }

  public void setRightButtonText(String rightButtonText) {
    this.rightButtonText = rightButtonText;
  }

  public void setAllLeftButtonText(String allLeftButtonText) {
    this.allLeftButtonText = allLeftButtonText;
  }

  public void setLeftButtonText(String leftButtonText) {
    this.leftButtonText = leftButtonText;
  }

  public void setBoxWidth(String boxWidth) {
    this.boxWidth = boxWidth;
  }

  @Override
  public JSExpression createGetExpression() {
    return new FunctionCallExpression(SelectModule.ALL_VALUES, this);
  }

  @Override
  public JSExpression createGetNameExpression() {
    throw new UnsupportedOperationException();
  }

  @Override
  public JSCallable createSetFunction() {
    throw new UnsupportedOperationException("Not supported (yet)");
  }

  @Override
  public JSCallable createResetFunction() {
    throw new UnsupportedOperationException("Not supported (yet)");
  }

  @Override
  public JSExpression createNotEmptyExpression() {
    return new CombinedExpression(createGetExpression(), new PropertyExpression("length != 0"));
  }

  @Override
  protected void writeEnd(SectionWriter writer) throws IOException {
    // nothing
  }

  @Override
  protected String getTag() {
    return null;
  }

  @Override
  public JSCallable createDisableFunction() {
    ArrayExpression buttons =
        new ArrayExpression(allRightButton, rightButton, leftButton, allLeftButton);
    return new PrependedParameterFunction(DISABLE_FUNC, dropDownLeft, dropDownRight, buttons);
  }

  @Override
  public void setRenderFactory(RendererFactory rendererFactory) {
    this.rendererFactory = rendererFactory;
  }

  @Override
  public JSCallable createSetAllFunction() {
    throw new UnsupportedOperationException("Not supported (yet)");
  }
}
