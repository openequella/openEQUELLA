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

package com.tle.web.sections.events;

import java.util.List;

import com.tle.web.sections.MutableSectionInfo;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;

public abstract class AbstractTargettedEvent<E extends AbstractTargettedEvent<E, L>, L extends TargetedEventListener>
	extends
		AbstractSectionEvent<L>
{
	private List<? extends SectionId> childIds;
	private SectionId sectionId;

	public AbstractTargettedEvent(SectionId sectionId)
	{
		this.sectionId = sectionId;
	}

	@Override
	public String getListenerId()
	{
		if( sectionId != null )
		{
			return sectionId.getSectionId();
		}
		return null;
	}

	@Override
	public SectionId getForSectionId()
	{
		return sectionId;
	}

	@Override
	public void beforeFiring(SectionInfo info, SectionTree tree)
	{
		super.beforeFiring(info, tree);
		if( sectionId == null )
		{
			MutableSectionInfo minfo = info.getAttributeForClass(MutableSectionInfo.class);
			childIds = minfo.getRootIds();
		}
		else
		{
			childIds = info.getAllChildIds(sectionId);
		}
	}

	public List<? extends SectionId> getChildren()
	{
		return childIds;
	}

	public void setChildren(List<? extends SectionId> children)
	{
		childIds = children;
	}

	@Override
	public void finishedFiring(SectionInfo info, SectionTree tree)
	{
		for( SectionId childId : childIds )
		{
			sectionId = childId;
			info.processEvent(this);
		}
	}
}
