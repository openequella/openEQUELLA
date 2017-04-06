package com.tle.web.sections.standard;

import java.util.Collections;
import java.util.List;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.standard.model.HtmlListState;
import com.tle.web.sections.standard.model.Option;

public class MappedLists extends AbstractMappedValues<String>
{
	public HtmlListState getListState(SectionInfo info, String key, List<Option<?>> options, String defaultSelectedValue)
	{
		final HtmlListState state = new HtmlListState();
		state.setId(getParameterId() + '_' + SectionUtils.getPageUniqueId(info));
		state.setName(getParameterId() + '(' + keyEscape(key) + ')');
		state.setMultiple(false);
		state.setOptions(options);

		final MappedValuesModel<String> model = getModel(info);

		String selectedValue = model.getMap().get(key);
		if( selectedValue == null )
		{
			selectedValue = defaultSelectedValue;
		}

		if( selectedValue != null )
		{
			state.setSelectedValues(Collections.singleton(selectedValue));
		}

		model.getRendered().add(key);
		return state;
	}

	public String getSelectedValue(SectionInfo info, String key)
	{
		final MappedValuesModel<String> model = getModel(info);
		return model.getMap().get(key);
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
