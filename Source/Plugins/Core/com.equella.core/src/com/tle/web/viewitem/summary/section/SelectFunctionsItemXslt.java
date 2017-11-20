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

package com.tle.web.viewitem.summary.section;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.tle.beans.item.ItemKey;
import com.tle.core.guice.Bind;
import com.tle.web.integration.service.IntegrationService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.selection.SelectedResource;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;
import com.tle.web.viewitem.service.ItemXsltExtension;
import com.tle.web.viewurl.ItemSectionInfo;

@Bind
@Singleton
public class SelectFunctionsItemXslt implements ItemXsltExtension
{
	private static final String SELECT_ITEM = "selectItem"; //$NON-NLS-1$
	private static final String SELECT_PATH = "selectPath"; //$NON-NLS-1$
	private static final String SELECT_ATTACHMENT_BY_UUID = "selectAttachmentByUuid"; //$NON-NLS-1$
	@Inject
	private IntegrationService integrationService;
	@Inject
	private SelectionService selectionService;

	@Override
	public void preRender(PreRenderContext info)
	{
		SelectionSession currentSelection = selectionService.getCurrentSession(info);
		if( currentSelection != null )
		{
			ItemSectionInfo itemInfo = info.getAttributeForClass(ItemSectionInfo.class);
			ItemKey itemId = itemInfo.getItemId();
			if( currentSelection.isSelectAttachments() )
			{
				SelectedResource attachResource = new SelectedResource(itemId, null, null);
				attachResource.setType(SelectedResource.TYPE_ATTACHMENT);
				SelectedResource pathResource = new SelectedResource(itemId, null, null);
				pathResource.setUrl("path"); //$NON-NLS-1$
				info.preRender(selectionService.getSelectFunction(info, SELECT_ATTACHMENT_BY_UUID, attachResource),
					selectionService.getSelectFunction(info, SELECT_PATH, pathResource));
			}
			if( currentSelection.isSelectItem() )
			{
				info.preRender(selectionService.getSelectFunction(info, SELECT_ITEM, new SelectedResource(itemId, null,
					null)));
			}
		}

	}

	@Override
	public void addXml(PropBagEx xml, SectionInfo info)
	{
		SelectionSession currentSelection = selectionService.getCurrentSession(info);
		if( currentSelection != null )
		{
			PropBagEx selectXml = xml.newSubtree("selection"); //$NON-NLS-1$
			if( currentSelection.isSelectAttachments() )
			{
				selectXml.setNode("selectAttachmentFunction", SELECT_ATTACHMENT_BY_UUID); //$NON-NLS-1$
				selectXml.setNode("selectPathFunction", SELECT_PATH); //$NON-NLS-1$
			}
			if( currentSelection.isSelectItem() )
			{
				selectXml.setNode("selectItemFunction", SELECT_ITEM); //$NON-NLS-1$
			}
		}
		if( integrationService.getIntegrationInterface(info) != null )
		{
			PropBagEx integrationXml = xml.newSubtree("integration"); //$NON-NLS-1$
			integrationXml.setNode("integrating", true); //$NON-NLS-1$
		}
	}

}
