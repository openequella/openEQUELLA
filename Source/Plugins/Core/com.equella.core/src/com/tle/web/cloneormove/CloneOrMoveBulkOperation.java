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

package com.tle.web.cloneormove;

import java.util.Collection;
import java.util.List;

import com.tle.beans.item.ItemPack;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.item.operations.WorkflowOperation;
import com.tle.core.item.standard.operations.CloneFactory;
import com.tle.core.item.standard.operations.MetadataTransformingOperation;
import com.tle.core.plugins.BeanLocator;
import com.tle.core.plugins.FactoryMethodLocator;
import com.tle.core.plugins.SerializedBeanLocator;
import com.tle.web.bulk.operation.BulkOperationExecutor;
import com.tle.web.bulk.operation.BulkOperationExtension;
import com.tle.web.cloneormove.model.CloneOptionsModel;
import com.tle.web.cloneormove.model.CloneOptionsModel.CloneOption;
import com.tle.web.cloneormove.section.CloneOrMoveSection;
import com.tle.web.itemadmin.operation.StandardOperations;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.utils.KeyOption;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.Button;
import com.tle.web.sections.standard.model.Option;

@Bind
public class CloneOrMoveBulkOperation extends CloneOrMoveSection implements BulkOperationExtension
{
	private static final String CLONE_VAL = "clone"; //$NON-NLS-1$
	private static final String MOVE_VAL = "move"; //$NON-NLS-1$

	@PlugKey("bulkop.title.")
	private static String KEY_TITLE;
	@PlugKey("bulkop.clone")
	private static String KEY_CLONE;
	@PlugKey("bulkop.move")
	private static String KEY_MOVE;
	@PlugKey("bulk.move.title")
	private static String KEY_MOVE_TITLE;
	@PlugKey("bulk.clone.title")
	private static String KEY_CLONE_TITLE;

	@Override
	public void registered(String id, SectionTree tree)
	{
		setForBulk(true);
		super.registered(id, tree);
		setCloneOptionsModel(new BulkCloneOptionsModel());
	}

	private class BulkCloneOptionsModel extends CloneOptionsModel
	{
		public BulkCloneOptionsModel()
		{
			super(false, false, false);
		}

		@Override
		protected boolean isCanClone(final SectionInfo info)
		{
			return !isHideClone(info);
		}

		@Override
		protected boolean isCanCloneNoAttachments(final SectionInfo info)
		{
			return !isHideCloneNoAttachments(info);
		}

		@Override
		protected boolean isCanMove(final SectionInfo info)
		{
			return !isHideMove(info);
		}
	}

	@Override
	public void register(SectionTree tree, String parentId)
	{
		tree.registerInnerSection(this, parentId);
	}

	@SuppressWarnings("nls")
	@Override
	public BeanLocator<CloneOrMoveExecutor> getExecutor(SectionInfo info, String operationId)
	{
		final CloneOption cloneOption = CloneOption.values()[Integer
			.parseInt(getCloneOptions().getSelectedValueAsString(info))];
		return new SerializedBeanLocator<CloneOrMoveExecutor>(new CloneOrMoveExecutor(
			getCollections().getSelectedValueAsString(info), getSchemaImports().getSelectedValueAsString(info),
			"submit".equals(getSubmitOptions().getSelectedValueAsString(info)), cloneOption));
	}

	@Override
	public Label getStatusTitleLabel(SectionInfo info, String operationId)
	{
		return new KeyLabel(StandardOperations.getStatusKey(), new KeyLabel(KEY_TITLE + operationId));
	}

	@Override
	public void addOptions(SectionInfo info, List<Option<OperationInfo>> options)
	{
		options.add(new KeyOption<OperationInfo>(KEY_CLONE, CLONE_VAL, new OperationInfo(this, CLONE_VAL)));
		options.add(new KeyOption<OperationInfo>(KEY_MOVE, MOVE_VAL, new OperationInfo(this, MOVE_VAL)));
	}

	@Override
	public SectionRenderable renderOptions(RenderContext context, String operationId)
	{
		boolean clone = operationId.equals(CLONE_VAL);
		setHideClone(context, !clone);
		setHideCloneNoAttachments(context, !clone);
		setHideMove(context, clone);
		setAllowCollectionChange(context, true);
		return renderSection(context, this);
	}

	@SuppressWarnings("nls")
	public static class CloneOrMoveExecutor extends FactoryMethodLocator<MetadataTransformingOperation>
		implements
			BulkOperationExecutor
	{
		private static final long serialVersionUID = 1L;

		private final String transform;
		private final String newCollectionUuid;
		private final CloneOption cloneOption;
		private final boolean submit;

		public CloneOrMoveExecutor(String newCollectionUuid, String transform, boolean submit, CloneOption cloneOption)
		{
			super(CloneFactory.class, null);
			this.newCollectionUuid = newCollectionUuid;
			this.transform = transform;
			this.submit = submit;
			this.cloneOption = cloneOption;
		}

		@Override
		protected String getMethodName()
		{
			if( cloneOption == CloneOption.MOVE )
			{
				return "moveDirect";
			}
			return "clone";
		}

		@Override
		protected Object[] getArgs()
		{
			if( cloneOption == CloneOption.MOVE )
			{
				return new Object[]{newCollectionUuid, true};
			}
			return new Object[]{newCollectionUuid, cloneOption == CloneOption.CLONE, submit};
		}

		@Override
		public WorkflowOperation[] getOperations()
		{
			CloneFactory factory = getFactory();
			final MetadataTransformingOperation cloneOperation = invokeFactoryMethod(factory);
			if( !Check.isEmpty(transform) )
			{
				cloneOperation.setTransform(transform);
			}

			return new WorkflowOperation[]{cloneOperation, factory.save()};
		}

		@Override
		public String getTitleKey()
		{
			if( cloneOption == CloneOption.MOVE )
			{
				return KEY_MOVE_TITLE;
			}
			return KEY_CLONE_TITLE;
		}
	}

	@Override
	public boolean hasExtraOptions(SectionInfo info, String operationId)
	{
		return true;
	}

	@Override
	public boolean validateOptions(SectionInfo info, String operationId)
	{
		return !Check.isEmpty(getCollections().getSelectedValueAsString(info));
	}

	@Override
	public boolean areOptionsFinished(SectionInfo info, String operationId)
	{
		return validateOptions(info, operationId);
	}

	@Override
	public void prepareDefaultOptions(SectionInfo info, String operationId)
	{
		// nothing
	}

	@Override
	public boolean hasExtraNavigation(SectionInfo info, String operationId)
	{
		return false;
	}

	@Override
	public Collection<Button> getExtraNavigation(SectionInfo info, String operationId)
	{
		return null;
	}

	@Override
	public boolean hasPreview(SectionInfo info, String operationId)
	{
		return false;
	}

	@Override
	public ItemPack runPreview(SectionInfo info, String operationId, long itemUuid)
	{
		return null;
	}

	@Override
	public boolean showPreviousButton(SectionInfo info, String opererationId)
	{
		return true;
	}
}
