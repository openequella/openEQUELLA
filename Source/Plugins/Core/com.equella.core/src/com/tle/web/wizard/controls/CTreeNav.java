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

package com.tle.web.wizard.controls;

import java.util.List;
import java.util.Map;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.wizard.beans.control.WizardControl;
import com.tle.beans.item.attachments.ItemNavigationNode;
import com.tle.beans.item.attachments.ItemNavigationTree;
import com.tle.core.freetext.queries.BaseQuery;
import com.tle.core.wizard.controls.WizardPage;

public class CTreeNav extends AbstractHTMLControl
{
	private static final long serialVersionUID = 1L;

	public CTreeNav(WizardPage page, int controlNumber, int nestingLevel, WizardControl controlBean)
	{
		super(page, controlNumber, nestingLevel, controlBean);
	}

	@Override
	public BaseQuery getPowerSearchQuery()
	{
		return null;
	}

	@Override
	public boolean isEmpty()
	{
		return false;
	}

	@Override
	public void loadFromDocument(PropBagEx itemxml)
	{
		// specific to the web
	}

	@Override
	public void resetToDefaults()
	{
		// specific to the web
	}

	@Override
	public void saveToDocument(PropBagEx itemxml)
	{
		// specific to the web
	}

	@Override
	public void setValues(String... values)
	{
		// specific to the web
	}

	public List<ItemNavigationNode> getRootNodes()
	{
		return new ItemNavigationTree(getAllNavigation()).getRootNodes();
	}

	public Map<String, ItemNavigationNode> getNavigationMap()
	{
		return new ItemNavigationTree(getAllNavigation()).getNavigationMap();
	}

	public List<ItemNavigationNode> getAllNavigation()
	{
		return getRepository().getItem().getTreeNodes();
	}

	public ItemNavigationTree getItemNavigationTree()
	{
		return new ItemNavigationTree(getAllNavigation());
	}
}
