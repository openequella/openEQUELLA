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

package com.tle.web.itemlist.item;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.item.IItem;
import com.tle.core.services.item.FreetextResult;
import com.tle.web.itemlist.StandardListSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.RenderContext;
import java.util.List;
import java.util.Set;

@NonNullByDefault
public abstract class AbstractItemlikeList<
        I extends IItem<?>,
        LE extends ItemlikeListEntry<I>,
        M extends StandardListSection.Model<LE>>
    extends StandardListSection<LE, M> implements ItemlikeList<I, LE> {
  protected abstract Set<String> getExtensionTypes();

  protected abstract LE createItemListEntry(
      SectionInfo info, I item, @Nullable FreetextResult result, int index, int available);

  @Override
  protected void customiseListEntries(RenderContext context, List<LE> entries) {
    // Nada
  }

  @Override
  public LE addItem(
      SectionInfo info, I item, @Nullable FreetextResult result, int index, int available) {
    LE entry = createItemListEntry(info, item, result, index, available);
    entry.setAttribute(FreetextResult.class, result);
    addListItem(info, entry);
    return entry;
  }

  @Override
  public Object instantiateModel(SectionInfo info) {
    return new Model<I, LE>();
  }

  public static class Model<I extends IItem<?>, LE extends ItemlikeListEntry<I>>
      extends StandardListSection.Model<LE> {
    // nothing
  }
}
