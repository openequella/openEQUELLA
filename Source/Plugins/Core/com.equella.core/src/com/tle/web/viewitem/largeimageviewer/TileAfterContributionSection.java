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

package com.tle.web.viewitem.largeimageviewer;

import java.util.Map;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import com.tle.core.guice.Bind;
import com.tle.core.item.operations.WorkflowOperation;
import com.tle.core.item.standard.operations.DuringSaveOperation;
import com.tle.core.plugins.ClassBeanLocator;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.annotations.DirectEvent;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.wizard.section.WizardSection;
import com.tle.web.wizard.section.WizardSectionInfo;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
@Bind
public class TileAfterContributionSection extends WizardSection<Object>
{
	private static final String TILE_SAVE_OP_KEY = "TILE_SAVE_OPS";
	private static final EnsureTilingOperation ENSURE_TILING_OP = new EnsureTilingOperation();

	public static enum Mode
	{
		AUTO_TILE_AFTER_CONTRIBUTION, PROMPT_ONLY_WHEN_VIEWING, @Deprecated PROMPT_AFTER_CONTRIBUTION
	}

	@Inject(optional = true)
	@Named("tileAfterContribution.mode")
	private Mode mode = Mode.PROMPT_ONLY_WHEN_VIEWING;

	@Override
	public SectionResult renderHtml(RenderEventContext context)
	{
		return null;
	}

	@DirectEvent
	public void ensureTilingOperation(SectionInfo info) throws Exception
	{
		if( getMode() == Mode.AUTO_TILE_AFTER_CONTRIBUTION )
		{
			final WizardSectionInfo winfo = getWizardInfo(info);
			final Map<String, DuringSaveOperation> saveOps = winfo.getWizardState().getSaveOperations();
			final DuringSaveOperation saveOp = saveOps.get(TILE_SAVE_OP_KEY);
			if( saveOp == null )
			{
				saveOps.put(TILE_SAVE_OP_KEY, ENSURE_TILING_OP);
			}
		}
	}

	private Mode getMode()
	{
		return mode == Mode.PROMPT_AFTER_CONTRIBUTION ? Mode.PROMPT_ONLY_WHEN_VIEWING : mode;
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "tac";
	}

	@Override
	public Class<Object> getModelClass()
	{
		return Object.class;
	}

	public static class EnsureTilingOperation extends ClassBeanLocator<StartTilingOperation>
		implements
			DuringSaveOperation
	{
		private static final long serialVersionUID = 1L;

		public EnsureTilingOperation()
		{
			super(StartTilingOperation.class);
		}

		@Override
		public WorkflowOperation createPreSaveWorkflowOperation()
		{
			return null;
		}

		@Override
		public WorkflowOperation createPostSaveWorkflowOperation()
		{
			return get();
		}

		@Override
		public String getName()
		{
			return null;
		}
	}
}
