package com.tle.web.sections.standard;

import java.util.Objects;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.standard.model.HtmlBooleanState;
import com.tle.web.sections.standard.model.HtmlValueState;

public class MappedStrings extends AbstractMappedValues<String>
{
	public HtmlBooleanState getBooleanState(SectionInfo info, String key, String value)
	{
		HtmlBooleanState state = new HtmlBooleanState();
		String name = getIdForKey(info, key);
		state.setName(name);
		state.setId(getParameterId() + '_' + SectionUtils.getPageUniqueId(info));
		MappedValuesModel<String> model = getModel(info);
		String text = model.getMap().get(key);
		if( Objects.equals(text, value) )
		{
			state.setChecked(true);
		}
		state.setValue(value);
		model.getRendered().add(key);
		return state;
	}

	public HtmlValueState getValueState(SectionInfo info, String key)
	{
		HtmlValueState value = new HtmlValueState();
		String nameId = getIdForKey(info, key);
		value.setId(getParameterId() + '_' + SectionUtils.getPageUniqueId(info));
		value.setName(nameId);
		MappedValuesModel<String> model = getModel(info);
		String text = model.getMap().get(key);
		model.getRendered().add(key);
		value.setValue(text != null ? text : ""); //$NON-NLS-1$
		return value;
	}

	public String getIdForKey(SectionInfo info, String key)
	{
		return getParameterId() + '(' + keyEscape(key) + ')';
	}

	@Override
	protected String convert(String val)
	{
		return val;
	}

	@Override
	protected String convertBack(String val)
	{
		return val;
	}

}
