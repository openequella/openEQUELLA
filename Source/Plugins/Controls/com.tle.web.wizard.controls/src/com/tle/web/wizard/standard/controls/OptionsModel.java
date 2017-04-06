package com.tle.web.wizard.standard.controls;

import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.standard.model.DynamicHtmlListModel;
import com.tle.web.sections.standard.model.Option;
import com.tle.web.sections.standard.model.SimpleOption;
import com.tle.web.wizard.controls.Item;
import com.tle.web.wizard.controls.OptionCtrl;

public class OptionsModel extends DynamicHtmlListModel<Item>
{
	private Option<Item> topOption;
	private final OptionCtrl optionCtrl;

	public OptionsModel(OptionCtrl optionCtrl)
	{
		this.optionCtrl = optionCtrl;
	}

	@Override
	protected Iterable<Item> populateModel(SectionInfo info)
	{
		return optionCtrl.getItems();
	}

	@Override
	protected Option<Item> getTopOption()
	{
		return topOption;
	}

	@Override
	protected Option<Item> convertToOption(SectionInfo info, Item item)
	{
		return new SimpleOption<Item>(item.getName(), item.getValue(), item);
	}

	public void setTopOption(Option<Item> topOption)
	{
		this.topOption = topOption;
	}
}