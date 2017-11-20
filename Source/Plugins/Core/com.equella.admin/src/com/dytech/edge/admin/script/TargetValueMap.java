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

package com.dytech.edge.admin.script;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dytech.edge.wizard.TargetNode;
import com.dytech.edge.wizard.beans.DefaultWizardPage;
import com.dytech.edge.wizard.beans.WizardPage;
import com.dytech.edge.wizard.beans.control.ContainerControl;
import com.dytech.edge.wizard.beans.control.Group;
import com.dytech.edge.wizard.beans.control.GroupItem;
import com.dytech.edge.wizard.beans.control.WizardControl;
import com.dytech.edge.wizard.beans.control.WizardControlItem;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.tle.common.Check;

public class TargetValueMap
{
	private final Map<String, Set<String>> targetMap = new HashMap<String, Set<String>>();

	public void addPages(List<WizardPage> pages)
	{
		if( !Check.isEmpty(pages) )
		{
			for( WizardPage page : pages )
			{
				if( page instanceof DefaultWizardPage )
				{
					addControls(((DefaultWizardPage) page).getControls());
				}
			}
		}
	}

	public void addControls(List<WizardControl> controls)
	{
		if( Check.isEmpty(controls) )
		{
			return;
		}

		for( WizardControl control : controls )
		{
			List<String> vs = Lists.newArrayList(Lists.transform(control.getItems(),
				new Function<WizardControlItem, String>()
				{
					@Override
					public String apply(WizardControlItem item)
					{
						return item.getValue();
					}
				}));

			if( !vs.isEmpty() )
			{
				for( TargetNode target : control.getTargetnodes() )
				{
					String targetPath = target.getXoqlPath();
					if( targetMap.containsKey(targetPath) )
					{
						targetMap.get(targetPath).addAll(vs);
					}
					else
					{
						targetMap.put(targetPath, new HashSet<String>(vs));
					}
				}
			}

			if( control instanceof ContainerControl )
			{
				addControls(((ContainerControl) control).getControls());
			}
			else if( control instanceof Group )
			{
				for( GroupItem gi : ((Group) control).getGroups() )
				{
					addControls(gi.getControls());
				}
			}
		}
	}

	public Set<String> getValuesForTarget(String target)
	{
		Set<String> rv = targetMap.get(target);
		if( rv == null )
		{
			rv = Collections.emptySet();
		}
		return rv;
	}
}
