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

package com.tle.web.itemlist;

import com.tle.web.itemlist.item.ListSettings;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.standard.model.HtmlBooleanState;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.renderers.DivRenderer;
import java.util.Collection;
import java.util.List;

public interface ListEntry {
  HtmlLinkState getTitle();

  Label getDescription();

  HtmlBooleanState getCheckbox();

  boolean isHilighted();

  void setHilighted(boolean highlighted);

  List<MetadataEntry> getMetadata();

  void addMetadata(MetadataEntry meta);

  void addDelimitedMetadata(Label label, Object... data);

  void addDelimitedMetadata(Label label, Collection<?> data);

  void setAttribute(Object key, Object value);

  boolean isFlagSet(String flagKey);

  void init(RenderContext context, ListSettings<? extends ListEntry> settings);

  void setInfo(SectionInfo info);

  void addRatingAction(int order, Object... ratingData);

  void addRatingAction(Object... ratingData);

  void addRatingMetadata(Object... ratingData);

  void addRatingMetadataWithOrder(int order, Object... ratingData);

  void setThumbnailCount(DivRenderer count);

  void addThumbnail(SectionRenderable renderable);
}
