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

package com.tle.web.cloud.search;

import com.dytech.devlib.PropBagEx;
import com.tle.annotation.NonNullByDefault;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.core.cloud.beans.converted.CloudItem;
import com.tle.core.guice.Bind;
import com.tle.web.cloud.view.CloudViewableItem;
import com.tle.web.cloud.viewable.CloudViewItemUrlFactory;
import com.tle.web.itemlist.StdMetadataEntry;
import com.tle.web.itemlist.item.AbstractItemlikeListEntry;
import com.tle.web.sections.Bookmark;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.equella.render.JQueryTimeAgo;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelRenderer;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.SimpleBookmark;
import com.tle.web.sections.standard.renderers.LinkRenderer;
import com.tle.web.viewable.ViewableItem;
import javax.inject.Inject;

/** @author Aaron */
@SuppressWarnings("nls")
@NonNullByDefault
@Bind
public class CloudSearchListEntry extends AbstractItemlikeListEntry<CloudItem> {
  static {
    PluginResourceHandler.init(CloudSearchListEntry.class);
  }

  @PlugKey("search.result.label.datemodified")
  private static Label LABEL_MODIFIED;

  @PlugKey("search.result.label.subject")
  private static Label LABEL_SUBJECT;

  @PlugKey("search.result.label.license")
  private static Label LABEL_LICENSE;

  @PlugKey("search.result.label.educationlevel")
  private static Label LABEL_EDUCATION_LEVEL;

  @PlugKey("search.result.label.format")
  private static Label LABEL_FORMAT;

  @Inject private CloudViewItemUrlFactory urlFactory;

  @Override
  protected Bookmark getTitleLink() {
    return urlFactory.createItemUrl(info, (CloudViewableItem) getViewableItem());
  }

  @Override
  public UnmodifiableAttachments loadAttachments() {
    return new UnmodifiableAttachments(getItem());
  }

  @Override
  protected ViewableItem<CloudItem> createViewableItem() {
    return new CloudViewableItem(getItem());
  }

  @Override
  protected void setupMetadata(RenderContext context) {
    CloudItem item = getItem();
    final PropBagEx pmeta = new PropBagEx(item.getMetadata());
    final String subject = pmeta.getNode("oer/dc/subject", null);
    if (subject != null) {
      addMetadata(new StdMetadataEntry(LABEL_SUBJECT, new LabelRenderer(new TextLabel(subject))));
    }
    final String eduLevel = pmeta.getNode("oer/dc/terms/educationLevel", null);
    if (eduLevel != null) {
      addMetadata(
          new StdMetadataEntry(LABEL_EDUCATION_LEVEL, new LabelRenderer(new TextLabel(eduLevel))));
    }
    final String license = pmeta.getNode("oer/eq/license_type", null);
    if (license != null) {
      final String licenseUrl = pmeta.getNode("oer/dc/terms/license", null);
      final SectionRenderable licRenderable;
      if (licenseUrl != null) {
        final HtmlLinkState linkState =
            new HtmlLinkState(new TextLabel(license), new SimpleBookmark(licenseUrl));
        linkState.setTarget("_blank");
        licRenderable = new LinkRenderer(linkState);
      } else {
        licRenderable = new LabelRenderer(new TextLabel(license));
      }
      addMetadata(new StdMetadataEntry(LABEL_LICENSE, licRenderable));
    }
    final String format = pmeta.getNode("oer/dc/format", null);
    if (format != null) {
      addMetadata(new StdMetadataEntry(LABEL_FORMAT, new LabelRenderer(new TextLabel(format))));
    }
    if (item.getDateModified() != null) {
      addMetadata(
          new StdMetadataEntry(LABEL_MODIFIED, JQueryTimeAgo.timeAgoTag(item.getDateModified())));
    }
  }
}
