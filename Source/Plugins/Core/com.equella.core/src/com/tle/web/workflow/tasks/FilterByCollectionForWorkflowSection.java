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

package com.tle.web.workflow.tasks;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.common.workflow.Workflow;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.resources.ResourcesService;
import com.tle.web.search.filter.AbstractFilterByCollectionSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.annotations.TreeLookup;
import com.tle.web.sections.equella.utils.KeyOption;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.Option;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("nls")
@NonNullByDefault
public class FilterByCollectionForWorkflowSection
    extends AbstractFilterByCollectionSection<
        FilterByCollectionForWorkflowSection.FilterByCollectionForWorkflowModel> {

  private static PluginResourceHelper r =
      ResourcesService.getResourceHelper(FilterByCollectionForWorkflowSection.class);
  @TreeLookup private WorkflowSelection workflowSelection;

  @Override
  public DynamicHtmlListModel<WhereEntry> getCollectionModel() {
    return new WorkflowCollectionFilterModel();
  }

  public class WorkflowCollectionFilterModel extends DynamicHtmlListModel<WhereEntry> {
    @Nullable
    @Override
    public WhereEntry getValue(SectionInfo info, @Nullable String value) {
      if (value == null || ALL_KEY.equals(value)) {
        return null;
      }
      return new WhereEntry(value);
    }

    @Override
    protected Option<WhereEntry> getTopOption() {
      return new KeyOption<WhereEntry>(r.key("filter.query.collection.all"), ALL_KEY, null);
    }

    @Override
    protected Iterable<WhereEntry> populateModel(SectionInfo info) {
      List<WhereEntry> collectionOptions = new ArrayList<WhereEntry>();

      List<BaseEntityLabel> listSearchable = itemDefinitionService.listSearchable();

      Workflow workflow = getWorkflow(info);

      List<ItemDefinition> workflowCollections;

      if (workflow != null) {
        workflowCollections = itemDefinitionService.enumerateForWorkflow(workflow.getId());

        for (ItemDefinition itemDef : workflowCollections) {
          collectionOptions.add(new WhereEntry(itemDef.getUuid()));
        }
      } else {
        for (BaseEntityLabel bel : listSearchable) {
          collectionOptions.add(new WhereEntry(bel));
        }
      }

      return collectionOptions;
    }

    @Override
    protected Option<WhereEntry> convertToOption(SectionInfo info, WhereEntry obj) {
      return obj.convert();
    }
  }

  @Nullable
  private Workflow getWorkflow(SectionInfo info) {
    FilterByCollectionForWorkflowModel model = getModel(info);
    Workflow workflow = model.getWorkflow();
    if (workflow == null) {
      workflow = workflowSelection.getWorkflow(info);
      model.setWorkflow(workflow);
    }

    return workflow;
  }

  @Override
  public Class<FilterByCollectionForWorkflowModel> getModelClass() {
    return FilterByCollectionForWorkflowModel.class;
  }

  @Override
  public Object instantiateModel(SectionInfo info) {
    return new FilterByCollectionForWorkflowModel(false);
  }

  protected static class FilterByCollectionForWorkflowModel
      extends AbstractFilterByCollectionSection.AbstractFilterByCollectionModel {
    public FilterByCollectionForWorkflowModel(boolean disabled) {
      super(disabled);
    }

    private Workflow workflow;

    public Workflow getWorkflow() {
      return workflow;
    }

    public void setWorkflow(Workflow workflow) {
      this.workflow = workflow;
    }
  }
}
