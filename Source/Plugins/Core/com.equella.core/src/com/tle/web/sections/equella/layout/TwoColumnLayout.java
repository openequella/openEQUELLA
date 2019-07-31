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

package com.tle.web.sections.equella.layout;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.CombinedRenderer;
import com.tle.web.sections.render.FallbackTemplateResult;
import com.tle.web.sections.render.GenericTemplateResult;
import com.tle.web.sections.render.TemplateResult;

@NonNullByDefault
public abstract class TwoColumnLayout<M extends TwoColumnLayout.TwoColumnModel>
    extends OneColumnLayout<M> {
  public static final String LEFT = "left"; // $NON-NLS-1$
  public static final String RIGHT = "right"; // $NON-NLS-1$

  @Override
  protected ContentLayout getDefaultLayout(SectionInfo info) {
    return ContentLayout.TWO_COLUMN;
  }

  @Override
  protected TemplateResult setupTemplate(RenderEventContext info) {
    TemplateResult templ = getTemplateResult(info);
    GenericTemplateResult bodyTemp = new GenericTemplateResult();
    bodyTemp.addNamedResult(
        LEFT,
        CombinedRenderer.combineMultipleResults(
            templ.getNamedResult(info, OneColumnLayout.BODY), templ.getNamedResult(info, LEFT)));
    return new FallbackTemplateResult(bodyTemp, templ);
  }

  @Override
  public Class<M> getModelClass() {
    return (Class<M>) TwoColumnModel.class;
  }

  public static class TwoColumnModel extends OneColumnLayout.OneColumnLayoutModel {
    private boolean receiptSpanBothColumns;

    public boolean isReceiptSpanBothColumns() {
      return receiptSpanBothColumns;
    }

    public void setReceiptSpanBothColumns(boolean receiptSpanBothColumns) {
      this.receiptSpanBothColumns = receiptSpanBothColumns;
    }
  }
}
