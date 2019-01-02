/*
 * Copyright 2019 Apereo
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

package com.tle.web.sections.render;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.js.ElementId;
import com.tle.web.sections.js.generic.SimpleElementId;

public abstract class AbstractWrappedElementId implements ElementId
{
	private ElementId elementId;
	private boolean used;
	private boolean changed;

	public AbstractWrappedElementId(ElementId elementId)
	{
		this.elementId = elementId;
	}

	@Override
	public String getElementId(SectionInfo info)
	{
		return elementId.getElementId(info);
	}

	public ElementId getWrappedElementId()
	{
		return elementId;
	}

	public String getId()
	{
		if( !elementId.isStaticId() )
		{
			throw new SectionsRuntimeException("Not a static id, use getElementId(SectionInfo) instead"); //$NON-NLS-1$
		}
		return elementId.getElementId(null);
	}

	public void setId(String id)
	{
		elementId = new SimpleElementId(id);
		registerUse();
	}

	public void setElementId(ElementId elementId)
	{
		this.elementId = elementId;
		if( used )
		{
			elementId.registerUse();
		}
		changed = true;
	}

	public boolean hasIdBeenSet()
	{
		return changed;
	}

	@Override
	public void registerUse()
	{
		used = true;
		elementId.registerUse();
	}

	@Override
	public boolean isElementUsed()
	{
		return used || elementId.isElementUsed();
	}

	@Override
	public boolean isStaticId()
	{
		return elementId.isStaticId();
	}

}
