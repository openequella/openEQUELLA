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

package com.tle.web.sections.standard.dialog.renderer;

import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.dialog.AbstractDialog;
import com.tle.web.sections.standard.dialog.model.DialogState;
import com.tle.web.sections.standard.model.HtmlComponentState;
import com.tle.web.sections.standard.model.HtmlLinkState;

/**
 * {@link AbstractDialog} renderer interface.
 *
 * <p>The {@link #setupButton(HtmlComponentState)} method was primarily introduced in order to
 * support ThickBox's automatic dialog opening via attributes. Since the same thing could be
 * achieved by using JavaScript handlers, this might be removed in the future.
 *
 * @author jmaginnis
 */
public interface DialogRenderer extends SectionRenderable {
  String AUTO_SIZE = "auto"; // $NON-NLS-1$

  /**
   * The Open function must open the dialog and display it's contents.
   *
   * @return The Open function, it has no arguments
   */
  JSCallable createOpenFunction();

  /**
   * The Close function must close the dialog and should be callable in the context of the dialog's
   * window.
   *
   * @return The close function, it has no arguments
   */
  JSCallable createCloseFunction();

  /**
   * Set the width of the dialog.
   *
   * @param width In CSS measurements (px, %)
   */
  void setWidth(String width);

  /**
   * Set the height of the dialog.
   *
   * @param height In CSS measurements (px, %)
   */
  void setHeight(String height);

  void setTitle(String title);

  /**
   * Sets up the "opener" link/button.
   *
   * <p>This will get called when the dialog is rendered, so in order for the button to work
   * properly, the dialog MUST be rendered before it.
   *
   * @param button Setup the opener button
   */
  void setupOpener(HtmlLinkState opener);

  DialogRenderer createNewRenderer(DialogState state);
}
