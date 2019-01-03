/*
 * Licensed to the Apereo Foundation under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package com.tle.web.sections.equella.search;

import java.util.ArrayList;
import java.util.List;

import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.TreeIndexed;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.generic.AbstractPrototypeSection;
import com.tle.web.sections.render.SectionRenderable;

@TreeIndexed
@SuppressWarnings("nls")
public abstract class AbstractSearchActionsSection<M extends AbstractSearchActionsSection.AbstractSearchActionsModel>
	extends
		AbstractPrototypeSection<M>
{
	public static final String AREA_SAVE = "save";
	public static final String AREA_SELECT = "select";

	protected final List<SectionId> topSections = new ArrayList<SectionId>();
	protected final List<SectionId> saveSections = new ArrayList<SectionId>();
	protected final List<SectionId> selectSections = new ArrayList<SectionId>();

	@Override
	public void treeFinished(String id, SectionTree tree)
	{
		List<SectionId> children = tree.getChildIds(id);
		for( SectionId child : children )
		{
			String layout = tree.getLayout(child.getSectionId());
			if( AREA_SELECT.equals(layout) )
			{
				selectSections.add(child);
			}
			else if( AREA_SAVE.equals(layout) )
			{
				saveSections.add(child);
			}
			else
			{
				topSections.add(child);
			}
		}
		super.treeFinished(id, tree);
	}

	protected void renderSectionsToModel(RenderEventContext context)
	{
		final AbstractSearchActionsModel model = getModel(context);

		model.setSaveSections(SectionUtils.renderSectionIds(context, saveSections));
		model.setSelectSections(SectionUtils.renderSectionIds(context, selectSections));
	}

	public abstract String[] getResetFilterAjaxIds();

	public void disableSearch(SectionInfo info)
	{
		getModel(info).setSearchDisabled(true);
	}

	public void disableSaveAndShare(SectionInfo info)
	{
		getModel(info).setSaveAndShareDisabled(true);
	}

	public static class AbstractSearchActionsModel
	{
		private List<SectionRenderable> saveSections;
		private List<SectionRenderable> selectSections;
		private boolean searchDisabled;
		private boolean saveAndShareDisabled;

		public List<SectionRenderable> getSelectSections()
		{
			return selectSections;
		}

		public void setSelectSections(List<SectionRenderable> selectSections)
		{
			this.selectSections = selectSections;
		}

		public boolean isSearchDisabled()
		{
			return searchDisabled;
		}

		public void setSearchDisabled(boolean searchDisabled)
		{
			this.searchDisabled = searchDisabled;
		}

		public List<SectionRenderable> getSaveSections()
		{
			return saveSections;
		}

		public void setSaveSections(List<SectionRenderable> saveSections)
		{
			this.saveSections = saveSections;
		}

		public boolean isSaveAndShareDisabled()
		{
			return saveAndShareDisabled;
		}

		public void setSaveAndShareDisabled(boolean saveAndShareDisabled)
		{
			this.saveAndShareDisabled = saveAndShareDisabled;
		}
	}
}
