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

package com.tle.web.viewitem.attachments;

import com.tle.beans.ReferencedURL;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.common.Check;
import com.tle.common.security.SecurityConstants;
import com.tle.core.item.ViewCountJavaDao;
import com.tle.core.security.TLEAclManager;
import com.tle.core.url.URLCheckerService;
import com.tle.core.url.URLCheckerService.URLCheckMode;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.render.WrappedLabel;
import com.tle.web.sections.result.util.CountLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import com.tle.web.viewurl.AttachmentDetail;
import com.tle.web.viewurl.ViewableResource;
import com.tle.web.viewurl.resource.SimpleUrlResource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DetailUrlResource extends SimpleUrlResource {
  static {
    PluginResourceHandler.init(DetailUrlResource.class);
  }

  @PlugKey("linkresource.details.type")
  private static Label TYPE;

  @PlugKey("linkresource.details.mimetype")
  private static Label MIMETYPE;

  @PlugKey("linkresource.details.url")
  private static Label URL;

  @PlugKey("linkresource.details.status")
  private static Label STATUS;

  @PlugKey("linkresource.details.status.bad")
  private static Label STATUS_BAD;

  @PlugKey("linkresource.details.status.unknown")
  private static Label STATUS_UNKNOWN;

  @PlugKey("linkresource.details.views")
  private static Label VIEWS;

  private final URLCheckerService urlCheckerService;
  private final TLEAclManager aclService;

  public DetailUrlResource(
      ViewableResource resource,
      String url,
      String description,
      URLCheckerService urlCheckerService,
      TLEAclManager aclService) {
    super(resource, url, description, urlCheckerService.isUrlDisabled(url));
    this.urlCheckerService = urlCheckerService;
    this.aclService = aclService;
  }

  @Override
  public List<AttachmentDetail> getCommonAttachmentDetails() {
    List<AttachmentDetail> commonDetails = new ArrayList<AttachmentDetail>();

    // Type
    commonDetails.add(makeDetail(TYPE, MIMETYPE));

    IAttachment attachment = getAttachment();

    // URL
    String url = attachment.getUrl();
    if (!Check.isEmpty(url)) {
      HtmlLinkState link = new HtmlLinkState(new SimpleBookmark(url));
      link.setLabel(new WrappedLabel(new TextLabel(url), -1, true, false));
      link.setDisabled(isDisabled());
      commonDetails.add(makeDetail(URL, new LinkRenderer(link)));

      // Bad Status
      ReferencedURL urlStatus = urlCheckerService.getUrlStatus(url, URLCheckMode.RECORDS_FIRST);
      if (!urlStatus.isSuccess()) {
        commonDetails.add(
            makeDetail(STATUS, urlStatus.getTries() == 0 ? STATUS_UNKNOWN : STATUS_BAD));
      }
    }

    if (attachment instanceof Attachment) {
      final Attachment att = (Attachment) attachment;
      if (!aclService
          .filterNonGrantedPrivileges(
              att.getItem(), Collections.singleton(SecurityConstants.VIEW_VIEWCOUNT))
          .isEmpty()) {
        Integer views =
            ViewCountJavaDao.getAttachmentViewCount(getViewableItem().getItemId(), att.getUuid());
        if (views != null) {
          commonDetails.add(makeDetail(VIEWS, new CountLabel(views)));
        }
      }
    }

    return commonDetails;
  }
}
