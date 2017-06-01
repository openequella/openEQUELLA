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

package com.tle.web.sections.render;

import java.util.ArrayList;
import java.util.List;

import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionsRuntimeException;
import com.tle.web.sections.events.RenderResultListener;

public class ResultListCollector implements RenderResultListener
{
	private final boolean combine;
	private final List<SectionRenderable> resultList = new ArrayList<SectionRenderable>();

	public ResultListCollector(boolean combine)
	{
		this.combine = combine;
	}

	public ResultListCollector()
	{
		this(false);
	}

	@Override
	public void returnResult(SectionResult result, String fromId)
	{
		if( result == null )
		{
			return;
		}
		if( !(result instanceof SectionRenderable) )
		{
			if( result instanceof TemplateResult )
			{
				throw new SectionsRuntimeException("Trying to collect SectionRenderable's but found a TemplateResult"); //$NON-NLS-1$
			}
			result = new PreRenderOnly((PreRenderable) result);
		}
		SectionRenderable renderable = (SectionRenderable) result;
		if( !combine || resultList.isEmpty() )
		{
			resultList.add(renderable);
		}
		else
		{
			resultList.set(0, CombinedRenderer.combineResults(resultList.get(0), renderable));
		}
	}

	public List<SectionRenderable> getResultList()
	{
		return resultList;
	}

	public SectionRenderable getFirstResult()
	{
		return resultList.isEmpty() ? null : resultList.get(0);
	}

}
