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

package com.tle.web.sections.standard.dialog.model;

import com.tle.web.sections.Bookmark;
import com.tle.web.sections.js.JSBookmarkModifier;
import com.tle.web.sections.js.JSFunction;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.dialog.AbstractDialog;
import com.tle.web.sections.standard.model.HtmlComponentState;

/**
 * The State class for Dialog {@code Section}s and Renderers.
 *
 * <p>Much of the grunt work is in the {@link AbstractDialog} Section class.
 *
 * @author jmaginnis
 */
public class DialogState extends HtmlComponentState {
  private SectionRenderable contents;
  private Bookmark contentsUrl;
  private JSBookmarkModifier openModifier;
  private String height;
  private String width;
  private boolean modal;
  private boolean ajax;
  private boolean inline;
  private JSFunction dialogOpenedCallback;
  private JSFunction dialogClosedCallback;

  public SectionRenderable getContents() {
    return contents;
  }

  public void setContents(SectionRenderable contents) {
    this.contents = contents;
  }

  public boolean isInline() {
    return inline;
  }

  public void setInline(boolean inline) {
    this.inline = inline;
  }

  public String getHeight() {
    return height;
  }

  public void setHeight(String height) {
    this.height = height;
  }

  public String getWidth() {
    return width;
  }

  public void setWidth(String width) {
    this.width = width;
  }

  public Bookmark getContentsUrl() {
    return contentsUrl;
  }

  public void setContentsUrl(Bookmark contentsUrl) {
    this.contentsUrl = contentsUrl;
  }

  public boolean isModal() {
    return modal;
  }

  public void setModal(boolean modal) {
    this.modal = modal;
  }

  public boolean isAjax() {
    return ajax;
  }

  public void setAjax(boolean ajax) {
    this.ajax = ajax;
  }

  public JSBookmarkModifier getOpenModifier() {
    return openModifier;
  }

  public void setOpenModifier(JSBookmarkModifier openModifier) {
    this.openModifier = openModifier;
  }

  public JSFunction getDialogOpenedCallback() {
    return dialogOpenedCallback;
  }

  public void setDialogOpenedCallback(JSFunction dialogOpenedCallback) {
    this.dialogOpenedCallback = dialogOpenedCallback;
  }

  public JSFunction getDialogClosedCallback() {
    return dialogClosedCallback;
  }

  public void setDialogClosedCallback(JSFunction dialogClosedCallback) {
    this.dialogClosedCallback = dialogClosedCallback;
  }
}
