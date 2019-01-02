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

package com.tle.web.sections.generic;

import java.util.EventListener;
import java.util.Map;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.events.SectionEvent;

/**
 * Use in places where you don't have a section info (eg. 'registered' methods)
 * 
 * @author Aaron
 */
@NonNullByDefault
public class DummySectionInfo extends DefaultSectionInfo
{
	@SuppressWarnings("null")
	public DummySectionInfo()
	{
		// No controller, but that's ok, we won't use it
		super(null);
	}

	@Override
	public <L extends EventListener> void processEvent(SectionEvent<L> event, @Nullable SectionTree tree)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void forward(SectionInfo forward)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void forwardToUrl(String url, int code)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void forwardToUrl(String url)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void forwardAsBookmark(SectionInfo forward)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public SectionInfo createForward(String path, @Nullable Map<Object, Object> attributes)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isReal()
	{
		return false;
	}
}
