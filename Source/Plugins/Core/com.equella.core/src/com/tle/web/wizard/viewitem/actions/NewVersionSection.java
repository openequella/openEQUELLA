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

package com.tle.web.wizard.viewitem.actions;

import com.tle.beans.item.Item;
import com.tle.beans.workflow.WorkflowStatus;
import com.tle.core.guice.Bind;
import com.tle.core.item.service.ItemService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.js.generic.Js;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.viewurl.ItemSectionInfo;
import javax.inject.Inject;

@Bind
@SuppressWarnings("nls")
public class NewVersionSection extends AbstractWizardViewItemActionSection {
  @PlugKey("viewitem.actions.newversion.title")
  private static Label LABEL;

  @PlugKey("summary.sidebar.actions.newversion.moderationconflict")
  private static Label VERSION_IN_MODERATION_ALERT;

  @Override
  protected Label getLinkLabel() {
    return LABEL;
  }

  @Inject private ItemService itemService;

  @Override
  protected boolean canView(SectionInfo info, ItemSectionInfo itemInfo, WorkflowStatus status) {
    return !status.isLocked()
        && itemInfo.hasPrivilege("NEWVERSION_ITEM")
        && itemInfo.hasPrivilege("CREATE_ITEM");
  }

  @Override
  protected void execute(SectionInfo info) throws Exception {
    forwardToWizard(info, false, false, true);
  }

  @Override
  public SectionResult renderHtml(RenderEventContext context) {
    Item selectedItem = getItemInfo(context).getItem();
    // An item may have one or more versions.
    String selectedItemUuid = selectedItem.getUuid();
    boolean isItemInModeration = itemService.isItemInModeration(selectedItemUuid);
    if (isItemInModeration) {
      // Display a warning when creating a new version of this item because there is a current
      // version awaiting moderation.
      getComponent()
          .setClickHandler(context, new OverrideHandler(Js.alert_s(VERSION_IN_MODERATION_ALERT)));
    }
    return super.renderHtml(context);
  }

  @Override
  public String getLinkText() {
    return LABEL.getText();
  }
}
