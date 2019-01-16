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

package com.tle.web.sections.equella.dialog;

import java.util.ArrayList;
import java.util.List;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.dialog.model.ControlsState;
import com.tle.web.sections.standard.dialog.model.DialogControl;
import com.tle.web.sections.standard.dialog.model.DialogModel;
import com.tle.web.sections.standard.impl.RenderFactoryRenderer;
import com.tle.web.template.DialogTemplate;

/**
 * A dialog for displaying a list of form controls.
 *
 * <p>This is a base class for displaying a dialog with a list of controls ( {@link DialogControl}),
 * and a list of buttons.
 *
 * @author jmaginnis
 */
@NonNullByDefault
public abstract class FormDialog extends EquellaDialog<DialogModel> {
  protected List<DialogControl> controls = new ArrayList<DialogControl>();

  @Override
  protected SectionRenderable getRenderableContents(RenderContext context) {
    return new RenderFactoryRenderer(getControls(context), renderFactory);
  }

  public ControlsState getControls(SectionInfo info) {
    String controlsId = getSectionId() + "controls"; // $NON-NLS-1$
    ControlsState state = info.getAttribute(controlsId);
    if (state == null) {
      state = new ControlsState();
      state.setElementId(getState(info));
      state.setControls(controls);
      info.setAttribute(controlsId, state);
    }
    return state;
  }

  @Override
  public DialogModel instantiateDialogModel(SectionInfo info) {
    return new DialogModel();
  }

  public void setTemplate(DialogTemplate template) {
    this.template = template;
  }
}
