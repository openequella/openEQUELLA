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

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.Section;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.events.AbstractDirectEvent;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.RenderEventListener;

@NonNullByDefault
public class ModalListener extends AbstractDirectEvent implements RenderEventListener, SectionId
{
	private ModalRenderer modal;
	private SectionTree tree;

	public ModalListener(String id, ModalRenderer modal, SectionTree tree)
	{
		super(PRIORITY_MODAL_LOGIC, id);
		this.modal = modal;
		this.tree = tree;
	}

	@Override
	public void render(RenderEventContext context)
	{
		if( modal.isModal(context) )
		{
			try
			{
				SectionResult result = modal.renderModal(context);
				if( result != null )
				{
					context.getRenderEvent().returnResult(result);
				}
			}
			catch( Exception e )
			{
				SectionUtils.throwRuntime(e);
			}
		}
	}

	@Override
	public void fireDirect(SectionId sectionId, SectionInfo info) throws Exception
	{
		if( modal.isModal(info) )
		{
			RenderContext renderContext = info.getRootRenderContext();
			renderContext.setModalId(forSectionId.getSectionId());
			info.renderNow();
		}
	}

	@Override
	public String getSectionId()
	{
		return forSectionId.getSectionId();
	}

	@Override
	public Section getSectionObject()
	{
		return (Section) modal;
	}

	@Override
	public SectionTree getTree()
	{
		return tree;
	}
}
