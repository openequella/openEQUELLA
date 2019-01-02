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

package com.tle.web.sections.standard;

import java.util.Collection;
import java.util.Collections;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.ParametersEvent;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.standard.model.HtmlListState;
import com.tle.web.sections.standard.renderers.list.DropDownRenderer;

/**
 * A single selection list component.
 * <p>
 * It provides methods for getting and setting the single value. <br>
 * The default renderer is usually {@link DropDownRenderer}.
 * 
 * @author jmaginnis
 */
public class SingleSelectionList<T> extends MultiSelectionList<T>
{
	private boolean grouped;

	@Override
	protected void extraHtmlRender(SectionInfo info)
	{
		HtmlListState state = getState(info);
		state.setMultiple(false);
		state.setGrouped(grouped);
	}

	@Override
	protected void handleListParameters(SectionInfo info, ParametersEvent event)
	{
		String param = event.getParameter(getParameterId(), false);
		if( param != null )
		{
			setValuesInternal(info, Collections.singleton(param));
		}
	}

	public T getSelectedValue(SectionInfo info)
	{
		Collection<T> vals = getSelectedValues(info);
		if( !vals.isEmpty() )
		{
			return vals.iterator().next();
		}
		return null;
	}

	public void setSelectedValue(SectionInfo info, T type)
	{
		if( type == null )
		{
			setSelectedStringValue(info, null);
		}
		else
		{
			setSelectedStringValue(info, listModel.getStringValue(info, type));
		}
	}

	public String getSelectedValueAsString(SectionInfo info)
	{
		Collection<String> vals = getSelectedValuesAsStrings(info);
		if( vals != null && !vals.isEmpty() )
		{
			return vals.iterator().next();
		}
		return null;
	}

	public void addChangeEventHandler(JSHandler handler)
	{
		setEventHandler(JSHandler.EVENT_CHANGE, handler);
	}

	public void addChangeEventHandler(JSCallable callable, Object... args)
	{
		setEventHandler(JSHandler.EVENT_CHANGE, new StatementHandler(callable, args));
	}

	@Override
	protected String getValueType()
	{
		return String.class.getName();
	}

	public void setGrouped(boolean grouped)
	{
		this.grouped = grouped;
	}
}
