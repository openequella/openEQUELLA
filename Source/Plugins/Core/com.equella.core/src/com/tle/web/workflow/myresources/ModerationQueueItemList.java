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

package com.tle.web.workflow.myresources;

import com.google.common.collect.Sets;
import com.google.inject.Provider;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemStatus;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.services.item.FreetextResult;
import com.tle.core.workflow.service.WorkflowService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.itemlist.item.AbstractItemList;
import com.tle.web.search.base.ContextableSearchSection;
import com.tle.web.searching.SearchIndexModifier;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.render.DateRendererFactory;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.SimpleSectionResult;
import com.tle.web.sections.standard.Table;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.TableState;
import com.tle.web.sections.standard.model.TableState.TableCell;
import com.tle.web.sections.standard.model.TableState.TableHeaderRow;
import java.util.Date;
import java.util.List;
import java.util.Set;
import javax.inject.Inject;

@SuppressWarnings("nls")
@Bind
public class ModerationQueueItemList
    extends AbstractItemList<ModerationQueueEntry, AbstractItemList.Model<ModerationQueueEntry>> {
  @PlugKey("modqueue.comment")
  private static Label LABEL_COMMENT;

  @PlugKey("listhead.title")
  private static Label LABEL_TITLE;

  @PlugKey("listhead.status")
  private static Label LABEL_STATUS;

  @PlugKey("listhead.submitted")
  private static Label LABEL_SUBMITTED;

  @PlugKey("listhead.lastaction")
  private static Label LABEL_LAST_ACTION;

  @ViewFactory private FreemarkerFactory viewFactory;
  @Inject private Provider<ModerationQueueEntry> factory;
  @Inject private WorkflowService workflowService;
  @Component @Inject private ModerationQueueCommentDialog commentDialog;

  @Component(name = "mt")
  private Table table;

  @Inject private DateRendererFactory dateRendererFactory;

  @Override
  public void registered(String id, SectionTree tree) {
    super.registered(id, tree);

    TableHeaderRow header =
        table.setColumnHeadings(LABEL_TITLE, LABEL_STATUS, LABEL_SUBMITTED, LABEL_LAST_ACTION);
    header.getCells().get(0).addClass("modqueue-title");
  }

  @Override
  protected SectionRenderable getRenderable(RenderEventContext context) {
    TableState tableState = table.getState(context);
    tableState.setFilterable(false);
    List<ModerationQueueEntry> items = getModel(context).getItems();
    for (ModerationQueueEntry item : items) {
      TableCell statusCell = new TableCell(item.getStatusLabel());
      statusCell.addClass("nowrap");
      if (item.getRejectMessage() != null) {
        statusCell.addContent(" (");
        statusCell.addContent(item.getRejectMessage());
        statusCell.addContent(")");
      }

      Date submittedDate = item.getSubmittedDate();
      SectionRenderable lastActionRenderer;
      Date lastActionDate = item.getLastActionDate();
      if (lastActionDate == null) {
        lastActionRenderer = new SimpleSectionResult("");
      } else {
        lastActionRenderer = dateRendererFactory.createDateRenderer(lastActionDate);
      }

      tableState.addRow(
          item.getTitle(),
          statusCell,
          dateRendererFactory.createDateRenderer(submittedDate),
          lastActionRenderer);
    }

    return viewFactory.createResult("queuelist.ftl", this);
  }

  @Override
  protected Set<String> getExtensionTypes() {
    return Sets.<String>newHashSet();
  }

  @Override
  protected ModerationQueueEntry createItemListEntry(
      SectionInfo info, Item item, FreetextResult result, int index, int available) {
    final ModerationQueueEntry entry = factory.get();
    entry.addModifier(
        new SearchIndexModifier(
            info.getTreeAttribute(ContextableSearchSection.SEARCHPAGE_ATTR), index, available));
    entry.setItem(item);
    entry.setInfo(info);
    return entry;
  }

  @Override
  protected void customiseListEntries(RenderContext context, List<ModerationQueueEntry> entries) {
    super.customiseListEntries(context, entries);
    for (ModerationQueueEntry entry : entries) {
      final Item item = entry.getItem();
      if (item.getStatus() == ItemStatus.REJECTED) {
        String message = workflowService.getLastRejectionMessage(item);
        if (!Check.isEmpty(message)) {
          final HtmlLinkState hcs =
              new HtmlLinkState(
                  new OverrideHandler(
                      commentDialog.getOpenFunction(), item.getItemId().toString()));
          hcs.setLabel(LABEL_COMMENT);
          entry.setRejectMessage(hcs);
        }
      }
    }
  }

  public Table getTable() {
    return table;
  }
}
