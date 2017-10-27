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

package com.tle.core.item.helper;

import java.util.Set;

import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.Constants;
import com.tle.beans.item.HistoryEvent;
import com.tle.beans.item.Item;
import com.tle.core.guice.Bind;

@Bind
@Singleton
public class HistoryEventHelper extends AbstractHelper
{
	@Override
	public void load(PropBagEx itemxml, Item bean)
	{
		itemxml = itemxml.aquireSubtree("history"); //$NON-NLS-1$
		itemxml.deleteAll(Constants.XML_WILD);
		for( HistoryEvent h : bean.getHistory() )
		{
			String nodeName;
			boolean attribute = false;

			switch( h.getType() )
			{
				case resetworkflow:
					nodeName = "resetworkflow"; //$NON-NLS-1$
					break;
				case approved:
					nodeName = "approved"; //$NON-NLS-1$
					break;
				case edit:
					nodeName = "edit"; //$NON-NLS-1$
					break;
				case promoted:
					nodeName = "promoted"; //$NON-NLS-1$
					break;
				case comment:
					nodeName = "comment"; //$NON-NLS-1$
					attribute = true;
					break;
				case rejected:
					nodeName = "rejected"; //$NON-NLS-1$
					break;
				case newversion:
					nodeName = "newversion"; //$NON-NLS-1$
					break;
				case clone:
					nodeName = "clone"; //$NON-NLS-1$
					break;
				case contributed:
					nodeName = "contributed"; //$NON-NLS-1$
					break;
				case workflowremoved:
					nodeName = "workflowremoved"; //$NON-NLS-1$
					break;
				case scriptComplete:
					nodeName = "scripttask";
					break;
				case scriptError:
					nodeName = "scripttask";
					break;
				case statechange:
				default:
					nodeName = "statechange"; //$NON-NLS-1$
			}

			PropBagEx history = itemxml.newSubtree(nodeName);

			setNode(history, attribute ? "@comment" : "comment", h.getComment()); //$NON-NLS-1$ //$NON-NLS-2$
			setNode(history, "@step", h.getStep()); //$NON-NLS-1$
			setNode(history, "@stepName", h.getStepName()); //$NON-NLS-1$
			setNode(history, "@state", h.getState()); //$NON-NLS-1$
			setNode(history, "@tostep", h.getToStep()); //$NON-NLS-1$
			setNode(history, "@toStepName", h.getToStepName()); //$NON-NLS-1$
			setNode(history, "@applies", h.isApplies()); //$NON-NLS-1$
			setNode(history, "@date", formatDate(h.getDate())); //$NON-NLS-1$
			String user = h.getUser();
			if( user != null && user.length() > 0 )
			{
				setNode(history, "@user", user); //$NON-NLS-1$
				setNode(history, Constants.XML_ROOT, user);
			}
		}
	}

	@Override
	public void save(PropBagEx xml, Item item, Set<String> handled)
	{
		// nothing
	}

}