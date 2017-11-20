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

package com.tle.web.cloneormove.section;

import java.util.Collections;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Qualifier;

import com.tle.beans.entity.Schema;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.core.guice.Bind;
import com.tle.core.item.service.ItemService;
import com.tle.core.security.TLEAclManager;
import com.tle.web.cloneormove.model.CloneOptionsModel;
import com.tle.web.cloneormove.model.CloneOptionsModel.CloneOption;
import com.tle.web.cloneormove.model.RootCloneOrMoveModel;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.sections.SectionConstants;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.render.Label;
import com.tle.web.viewitem.section.ParentViewItemSectionUtils;
import com.tle.web.viewitem.summary.content.AbstractContentSection;
import com.tle.web.wizard.WebWizardService;

/**
 * @author aholland
 */
@Bind
public class RootCloneOrMoveSection extends AbstractContentSection<RootCloneOrMoveModel>
{
	@PlugKey("moveonly.title")
	private static Label MOVE_TITLE_LABEL;

	@PlugKey("cloneonly.title")
	private static Label CLONE_TITLE_LABEL;

	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Inject
	private ItemService itemService;
	@Inject
	private TLEAclManager aclManager;
	@Inject
	private WebWizardService webWizardService;

	@Inject
	@Qualifier("main")
	private CloneOrMoveSection cloneOrMoveSection;

	@Override
	public SectionResult renderHtml(final RenderEventContext context) throws Exception
	{
		final RootCloneOrMoveModel model = getModel(context);

		final Item srcItem = itemService.get(new ItemId(model.getUuid(), model.getVersion()));
		final Schema sourceSchema = srcItem.getItemDefinition().getSchema();

		Schema destSchema = null;
		final ItemDefinition destCollection = cloneOrMoveSection.getCurrentSelectedItemdef(context);
		if( destCollection != null )
		{
			destSchema = destCollection.getSchema();
		}
		cloneOrMoveSection.setSchemas(context, sourceSchema, destSchema);
		cloneOrMoveSection.setAllowCollectionChange(context, true);
		if( isMove(context) )
		{
			addDefaultBreadcrumbs(context, ParentViewItemSectionUtils.getItemInfo(context), MOVE_TITLE_LABEL);
			cloneOrMoveSection.setMove(context, true);
		}
		else if( isClone(context) )
		{
			addDefaultBreadcrumbs(context, ParentViewItemSectionUtils.getItemInfo(context), CLONE_TITLE_LABEL);
			cloneOrMoveSection.setMove(context, false);
		}
		else
		{
			throw new RuntimeException("User does not have sufficient privileges for the requested operation"); //$NON-NLS-1$
		}

		model.setSections(Collections.singletonList(renderSection(context, cloneOrMoveSection)));

		displayBackButton(context);
		return viewFactory.createResult("cloneormove.ftl", context); //$NON-NLS-1$
	}

	@Override
	public void registered(final String id, final SectionTree tree)
	{
		super.registered(id, tree);
		tree.registerInnerSection(cloneOrMoveSection, id);

		cloneOrMoveSection.setClientSideCallbacks(events.getSubmitValuesFunction("proceed")); //$NON-NLS-1$
		cloneOrMoveSection.setCloneOptionsModel(new WizCloneOptionsModel());
	}

	@EventHandlerMethod
	public void proceed(final SectionInfo info, final int cloneOption, final String newCollectionUuid,
		final String transform, String submit)
	{
		final CloneOption cloneOp = CloneOption.values()[cloneOption];
		final boolean cloneAttachments = (cloneOp != CloneOption.CLONE_NO_ATTACHMENTS);
		final boolean isMove = (cloneOp == CloneOption.MOVE);

		// forward to wizard entry cloneitem method with item def set to
		// selected
		final RootCloneOrMoveModel model = getModel(info);

		webWizardService.forwardToCloneItemWizard(info, newCollectionUuid, model.getUuid(), model.getVersion(),
			transform, isMove, cloneAttachments);
	}

	protected boolean isMove(final SectionInfo info)
	{
		final RootCloneOrMoveModel model = getModel(info);
		if( model.getUuid() == null )
		{
			return false;
		}
		final Item srcItem = itemService.get(new ItemId(model.getUuid(), model.getVersion()));
		return model.getIsMove()
			&& !aclManager.filterNonGrantedPrivileges(srcItem, Collections.singletonList(CloneOrMoveSection.MOVE_ITEM))
				.isEmpty();
	}

	protected boolean isClone(final SectionInfo info)
	{
		final RootCloneOrMoveModel model = getModel(info);
		if( model.getUuid() == null )
		{
			return false;
		}
		final Item srcItem = itemService.get(new ItemId(model.getUuid(), model.getVersion()));
		return !model.getIsMove()
			&& !aclManager
				.filterNonGrantedPrivileges(srcItem, Collections.singletonList(CloneOrMoveSection.CLONE_ITEM))
				.isEmpty();
	}

	public void setCurrentItemdef(final SectionInfo info, final String itemdefUuid)
	{
		cloneOrMoveSection.setCurrentSelectedItemdef(info, itemdefUuid);
	}

	@Override
	public Class<RootCloneOrMoveModel> getModelClass()
	{
		return RootCloneOrMoveModel.class;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return SectionConstants.ROOT_SECTION_ID;
	}

	private class WizCloneOptionsModel extends CloneOptionsModel
	{
		public WizCloneOptionsModel()
		{
			super(false, false, false);
		}

		@Override
		protected boolean isCanMove(final SectionInfo info)
		{
			return isMove(info);
		}

		@Override
		protected boolean isCanClone(final SectionInfo info)
		{
			return isClone(info);
		}

		@Override
		protected boolean isCanCloneNoAttachments(final SectionInfo info)
		{
			return isClone(info);
		}
	}
}
