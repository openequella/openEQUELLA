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