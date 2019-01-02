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

package com.tle.web.sections.events;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EventListener;
import java.util.List;

import com.tle.web.sections.MutableSectionInfo;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;

public class DocumentParamsEvent extends AbstractSectionEvent<BookmarkEventListener>
{
	private final List<DocumentedParam> supported;
	private final List<DocumentedParam> unsupported;
	private List<SectionId> childIds;
	private final SectionId sectionId;

	public DocumentParamsEvent()
	{
		this.sectionId = null;
		supported = new ArrayList<DocumentedParam>();
		unsupported = new ArrayList<DocumentedParam>();
	}

	private DocumentParamsEvent(DocumentParamsEvent parent, SectionId childId)
	{
		this.supported = parent.supported;
		this.unsupported = parent.unsupported;
		this.sectionId = childId;
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

	public void addParam(boolean supported, String param, String type, String... values)
	{
		DocumentedParam dparam = new DocumentedParam(param, type, Arrays.asList(values));
		if( supported )
		{
			this.supported.add(dparam);
		}
		else
		{
			this.unsupported.add(dparam);
		}
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

	@Override
	public void finishedFiring(SectionInfo info, SectionTree tree)
	{
		for( SectionId childId : childIds )
		{
			info.processEvent(new DocumentParamsEvent(this, childId));
		}
	}

	@Override
	public void fire(SectionId sectionId, SectionInfo info, BookmarkEventListener listener) throws Exception
	{
		listener.document(info, this);
	}

	@Override
	public Class<? extends EventListener> getListenerClass()
	{
		return BookmarkEventListener.class;
	}

	public static final class DocumentedParam
	{
		private final String param;
		private final String type;
		private final Collection<String> values;

		public DocumentedParam(String param, String type, Collection<String> values)
		{
			this.param = param;
			this.type = type;
			this.values = values;
		}

		public String getParam()
		{
			return param;
		}

		public String getType()
		{
			return type;
		}

		public Collection<String> getValues()
		{
			return values;
		}
	}

	public List<DocumentedParam> getSupported()
	{
		return supported;
	}

	public List<DocumentedParam> getUnsupported()
	{
		return unsupported;
	}
}
