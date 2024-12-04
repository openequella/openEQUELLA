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

package com.tle.mycontent.service;

import static scala.jdk.javaapi.OptionConverters.toScala;

import com.dytech.devlib.PropBagEx;
import com.google.inject.Provider;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.common.Check;
import com.tle.core.collection.service.ItemDefinitionService;
import com.tle.core.guice.Bind;
import com.tle.core.item.operations.WorkflowOperation;
import com.tle.core.item.service.ItemService;
import com.tle.core.item.standard.ItemOperationFactory;
import com.tle.core.plugins.PluginService;
import com.tle.core.plugins.PluginTracker;
import com.tle.mycontent.ContentHandler;
import com.tle.mycontent.MyContentConstants;
import com.tle.mycontent.web.section.ContributeMyContentAction;
import com.tle.mycontent.web.section.MyContentContributeSection;
import com.tle.mycontent.workflow.operations.OperationFactory;
import com.tle.web.myresources.RootMyResourcesSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.selection.SelectionService;
import com.tle.web.template.NewUiRoutes;
import com.tle.web.template.RenderNewTemplate;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import javax.inject.Inject;
import javax.inject.Singleton;
import org.java.plugin.registry.Extension;
import scala.Option;

@SuppressWarnings("nls")
@Bind(MyContentService.class)
@Singleton
public class MyContentServiceImpl implements MyContentService {
  private PluginTracker<ContentHandler> handlerTracker;

  @Inject private ItemDefinitionService itemDefinitionService;
  @Inject private ItemService itemService;
  @Inject private Provider<ContributeMyContentAction> contributeProvider;
  @Inject private OperationFactory editOpFactory;
  @Inject private ItemOperationFactory workflowFactory;
  @Inject private Provider<SelectionService> selectionService;

  // Prepare a SectionInfo for MyContentContributeSection and forward to it.
  // If this method is invoked from New UI and ID of a New UI SearchOptions is provided, save the
  // ID to the model of MyContentContributeSection.
  private void prepareContribution(SectionInfo info, String handlerId, String newUIStateId) {
    SectionInfo forward = MyContentContributeSection.createForForward(info);
    MyContentContributeSection contributeSection =
        forward.lookupSection(MyContentContributeSection.class);

    contributeSection.contribute(forward, handlerId);

    if (RenderNewTemplate.isNewUIEnabled() && newUIStateId != null) {
      contributeSection.getModel(forward).setNewUIStateId(newUIStateId);
    }

    info.forwardAsBookmark(forward);
  }

  // Return ID of a Scrapbook editing handler.
  private String getEditingHandler(ItemId itemId) {
    PropBagEx itemXml = itemService.getItemXmlPropBag(itemId);
    return itemXml.getNode(MyContentConstants.CONTENT_TYPE_NODE);
  }

  @Override
  public boolean isMyContentContributionAllowed() {
    return !Check.isEmpty(
        itemDefinitionService.getMatchingCreatableUuid(
            Collections.singleton(MyContentConstants.MY_CONTENT_UUID)));
  }

  @Override
  public ItemDefinition getMyContentItemDef() {
    ItemDefinition itemdef = itemDefinitionService.getByUuid(MyContentConstants.MY_CONTENT_UUID);
    if (itemdef == null) {
      throw new RuntimeException(
          "My Content collection is missing or does not have a UUID of "
              + MyContentConstants.MY_CONTENT_UUID
              + ".  This collection and UUID are mandatory for the Scrapbook to function"
              + " correctly.");
    }
    return itemdef;
  }

  @Override
  public void forwardToEditor(SectionInfo info, ItemId itemId) {
    MyContentContributeSection.forwardToEdit(info, getEditingHandler(itemId), itemId, null);
  }

  @Override
  public void forwardToEditorFromNewUI(SectionInfo info, ItemId itemId, String newUIStateId) {
    MyContentContributeSection.forwardToEdit(info, getEditingHandler(itemId), itemId, newUIStateId);
  }

  @Override
  public boolean returnFromContribute(SectionInfo info) {
    MyContentContributeSection myContribute = info.lookupSection(MyContentContributeSection.class);
    if (myContribute != null) {
      myContribute.contributionFinished(info);
    }

    Optional<String> newUIStateId =
        Optional.ofNullable(myContribute)
            .map(section -> section.getModel(info))
            .flatMap(
                model -> {
                  Optional<String> sid = Optional.ofNullable(model.getNewUIStateId());
                  sid.ifPresent(ignored -> model.setNewUIStateId(null));

                  return sid;
                });

    // Only return to '/page/myresources' when New UI is enabled and the page is NOT in Selection
    // Session.
    if (RenderNewTemplate.isNewUIEnabled()
        && selectionService.get().getCurrentSession(info) == null) {
      info.forwardToUrl(
          NewUiRoutes.myResources("scrapbook", Option.empty(), toScala(newUIStateId)));
      return true;
    }

    // 'type` must be 'scrapbook' as the page should return to the view of Scrapbook.
    String path =
        RootMyResourcesSection.buildForwardUrl(
            "scrapbook",
            newUIStateId
                .map(sid -> Collections.singletonMap("newUIStateId", sid))
                .orElse(Collections.emptyMap()));

    SectionInfo fwd = info.createForwardForUri(path);
    info.forwardAsBookmark(fwd);
    return true;
  }

  @Override
  public Set<String> getContentHandlerIds() {
    return handlerTracker.getExtensionMap().keySet();
  }

  @Override
  public String getContentHandlerNameKey(String handlerId) {
    Extension extension = handlerTracker.getExtension(handlerId);
    return extension.getParameter("nameKey").valueAsString();
  }

  @Override
  public ContentHandler getHandlerForId(String handlerId) {
    return handlerTracker.getBeanMap().get(handlerId);
  }

  @Override
  public WorkflowOperation getEditOperation(
      MyContentFields fields,
      String filename,
      String stagingUuid,
      boolean removeExistingAttachments,
      boolean useExistingAttachments) {
    return editOpFactory.create(
        fields, filename, stagingUuid, removeExistingAttachments, useExistingAttachments);
  }

  @Override
  public MyContentFields getFieldsForItem(ItemId itemId) {
    MyContentFields fields = new MyContentFields();
    PropBagEx itemXml = itemService.getItemXmlPropBag(itemId);
    fields.setTitle(itemXml.getNode(MyContentConstants.NAME_NODE));
    fields.setTags(itemXml.getNode(MyContentConstants.KEYWORDS_NODE));
    fields.setResourceId(itemXml.getNode(MyContentConstants.CONTENT_TYPE_NODE));
    return fields;
  }

  @Override
  public void delete(ItemId itemId) {
    // always unlock locked MyContent (you are the only one that can lock it
    // after all...)
    itemService.forceUnlock(itemService.get(itemId));
    itemService.operation(itemId, workflowFactory.delete(), workflowFactory.save());
  }

  @Override
  public void restore(ItemId itemId) {
    // always unlock locked MyContent (you are the only one that can lock it
    // after all...)
    itemService.forceUnlock(itemService.get(itemId));
    itemService.operation(itemId, workflowFactory.restore(), workflowFactory.save());
  }

  @Override
  public boolean isMyContentItem(Item item) {
    return item.getItemDefinition().getUuid().equals(MyContentConstants.MY_CONTENT_UUID);
  }

  @Override
  public ContributeMyContentAction createActionForHandler(String handlerId) {
    ContributeMyContentAction action = contributeProvider.get();
    action.setHandlerId(handlerId);
    action.setButtonLabel(new KeyLabel(getContentHandlerNameKey(handlerId)));
    return action;
  }

  @Inject
  public void setPluginService(PluginService pluginService) {
    handlerTracker =
        new PluginTracker<ContentHandler>(
            pluginService,
            "com.tle.mycontent",
            "contentHandler",
            "id",
            new PluginTracker.ExtensionParamComparator("order"));
    handlerTracker.setBeanKey("contributeBean");
  }

  @Override
  public void forwardToContribute(SectionInfo info, String handlerId) {
    prepareContribution(info, handlerId, null);
  }

  @Override
  public void forwardToContributeFromNewUI(
      SectionInfo info, String handlerId, String newUIStateId) {
    prepareContribution(info, handlerId, newUIStateId);
  }
}
