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

package com.tle.web.viewitem.service;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.attachments.Attachments;
import com.tle.beans.item.attachments.ImsAttachment;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.common.URLUtils;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.common.usermanagement.util.UserXmlUtils;
import com.tle.core.guice.Bind;
import com.tle.core.item.helper.ItemHelper;
import com.tle.core.item.helper.ItemHelper.ItemHelperSettings;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.core.services.user.UserService;
import com.tle.core.xslt.service.XsltService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.events.BookmarkEvent;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.viewurl.ItemSectionInfo;
import java.util.List;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

@Bind(ItemXsltService.class)
@Singleton
public class ItemXsltServiceImpl implements ItemXsltService {
  private static final Log LOGGER = LogFactory.getLog(ItemXsltService.class);

  @Inject private ItemHelper itemHelper;
  @Inject private UserService userService;
  @Inject private XsltService xsltService;

  private PluginTracker<ItemXsltExtension> itemXsltTracker;

  @Override
  public String renderSimpleXsltResult(RenderContext info, ItemSectionInfo itemInfo, String xslt) {
    String result = executeTransformation(xslt, getXmlForXslt(info, itemInfo));

    List<ItemXsltExtension> itemXsltExtensions = itemXsltTracker.getBeanList();
    for (ItemXsltExtension extension : itemXsltExtensions) {
      info.preRender(extension);
    }
    return result;
  }

  private String executeTransformation(String xslt, PropBagEx itemXml) {
    try {
      return xsltService.transformFromXsltString(xslt, itemXml);
    } catch (RuntimeException e) {
      LOGGER.error("Error transforming xslt ", e); // $NON-NLS-1$
      return "ERROR running xslt"; //$NON-NLS-1$
    }
  }

  @Override
  public PropBagEx getStandardXmlForXslt(Item item, ItemSectionInfo itemInfo) {
    boolean isItemInfoProvided = itemInfo != null;
    PropBagEx basicXml =
        isItemInfoProvided ? itemInfo.getItemxml() : new PropBagEx(item.getItemXml().getXml());
    PropBagEx fullXml =
        itemHelper.convertToXml(new ItemPack(item, basicXml, ""), new ItemHelperSettings(true));

    // Include more information about the Owner.
    String owner = item.getOwner();
    try {
      // Retrieve the owner information
      UserBean userBean = userService.getInformationForUser(owner);
      PropBagEx ownerXml = UserXmlUtils.getUserAsXml(userBean);
      fullXml.append("item/owner", ownerXml);
    } catch (Exception e) {
      LOGGER.info("Error including owner '" + owner + "' for xslt");
    }
    // Include Item directory.
    fullXml.setNode(
        "itemdir",
        isItemInfoProvided
            ? itemInfo.getItemdir()
            : CurrentInstitution.get().getFilestoreId() + "/items/" + item.getItemId() + "/");

    // Include information for our templates and webdav information
    fullXml.setNode("template", "entity/" + item.getItemDefinition().getId() + "/displaytemplate/");

    // Include Collection name.
    fullXml.setNode("collection", CurrentLocale.get(item.getItemDefinition().getName()));

    // Include IMS
    Attachments attachments = new UnmodifiableAttachments(item);
    ImsAttachment ims = attachments.getIms();
    if (ims != null) {
      fullXml.setNode("viewims", "viewscorm.jsp");
      fullXml.setNode("imsdir", URLUtils.urlEncode(ims.getUrl()) + '/');
    }

    return fullXml;
  }

  @Override
  public PropBagEx getXmlForXslt(SectionInfo info, ItemSectionInfo itemInfo) {
    PropBagEx fullItemXml = getStandardXmlForXslt(itemInfo.getItem(), itemInfo);

    // There are more additional XML nodes can be added by 'info' and 'itemInfo'.
    // Include session params.
    BookmarkEvent bookmarkEvent = new BookmarkEvent(BookmarkEvent.CONTEXT_SESSION);
    info.processEvent(bookmarkEvent);
    fullItemXml.setNode(
        "sessionparams",
        SectionUtils.getParameterString(
            SectionUtils.getParameterNameValues(bookmarkEvent.getBookmarkState(), false)));

    // Include extensions.
    List<ItemXsltExtension> itemXsltExtensions = itemXsltTracker.getBeanList();
    for (ItemXsltExtension extension : itemXsltExtensions) {
      extension.addXml(fullItemXml, info);
    }

    return fullItemXml;
  }

  @Inject
  public void setPluginService(PluginService pluginService) {
    itemXsltTracker =
        new PluginTracker<ItemXsltExtension>(
            pluginService, "com.tle.web.viewitem", "itemXslt", null); // $NON-NLS-1$ //$NON-NLS-2$
    itemXsltTracker.setBeanKey("class"); // $NON-NLS-1$
  }
}
