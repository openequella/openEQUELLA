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

package com.tle.web.cloneormove.model;

import java.util.ArrayList;
import java.util.List;

import com.tle.common.NameValue;
import com.tle.core.i18n.BundleNameValue;
import com.tle.core.plugins.AbstractPluginService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;

/**
 * @author aholland
 */
public class CloneOptionsModel extends DynamicHtmlListModel<NameValue>
{
	private static String KEY_PFX = AbstractPluginService.getMyPluginId(SchemaTransformsModel.class)+".";
	public enum CloneOption
	{
		CLONE()
		{
			@Override
			public String toString()
			{
				return Integer.toString(ordinal());
			}
		},
		CLONE_NO_ATTACHMENTS()
		{
			@Override
			public String toString()
			{
				return Integer.toString(ordinal());
			}
		},
		MOVE()
		{
			@Override
			public String toString()
			{
				return Integer.toString(ordinal());
			}
		}
	}

	private final boolean canMove;
	private final boolean canClone;
	private final boolean canCloneNoAttachments;

	public CloneOptionsModel(final boolean canMove, final boolean canClone, final boolean canCloneNoAttachments)
	{
		this.canMove = canMove;
		this.canClone = canClone;
		this.canCloneNoAttachments = canCloneNoAttachments;
	}

	@Override
	protected Iterable<NameValue> populateModel(SectionInfo info)
	{
		final List<NameValue> values = new ArrayList<NameValue>();
		if( isCanClone(info) )
		{
			values.add(new BundleNameValue(
					KEY_PFX+"selectcollection.option.clone.clone", CloneOption.CLONE.toString())); //$NON-NLS-1$
		}
		if( isCanCloneNoAttachments(info) )
		{
			values
				.add(new BundleNameValue(
						KEY_PFX+"selectcollection.option.clone.clonenoattachments", CloneOption.CLONE_NO_ATTACHMENTS.toString())); //$NON-NLS-1$
		}
		if( isCanMove(info) )
		{
			values.add(new BundleNameValue(
					KEY_PFX+"selectcollection.option.clone.move", CloneOption.MOVE.toString())); //$NON-NLS-1$
		}
		return values;
	}

	protected boolean isCanMove(final SectionInfo info)
	{
		return canMove;
	}

	protected boolean isCanClone(final SectionInfo info)
	{
		return canClone;
	}

	protected boolean isCanCloneNoAttachments(final SectionInfo info)
	{
		return canCloneNoAttachments;
	}
}
