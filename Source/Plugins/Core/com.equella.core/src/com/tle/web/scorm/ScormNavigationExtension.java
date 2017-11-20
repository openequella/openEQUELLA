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

package com.tle.web.scorm;

import java.util.Iterator;
import java.util.List;

import javax.inject.Singleton;

import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.beans.item.attachments.ItemNavigationNode;
import com.tle.beans.item.attachments.ItemNavigationTab;
import com.tle.core.guice.Bind;
import com.tle.web.viewitem.treeviewer.ModifyNavigationExtension;

@Bind
@Singleton
public class ScormNavigationExtension implements ModifyNavigationExtension
{
	@Override
	public void process(List<ItemNavigationNode> nodes, List<Attachment> nodedAttachments)
	{
		Iterator<ItemNavigationNode> iterator = nodes.iterator();
		boolean hasScorm = false;
		while( iterator.hasNext() )
		{
			ItemNavigationNode node = iterator.next();
			for( ItemNavigationTab tab : node.getTabs() )
			{
				Attachment attachment = tab.getAttachment();
				if( attachment != null )
				{
					if( attachment instanceof CustomAttachment
						&& ((CustomAttachment) attachment).getType().equalsIgnoreCase(
							ScormUtils.ATTACHMENT_RESOURCE_TYPE) )
					{
						hasScorm = true;
						iterator.remove();
					}
				}
			}
		}

		// remove empty scorm folders
		if( hasScorm )
		{
			iterator = nodes.iterator();
			while( iterator.hasNext() )
			{
				ItemNavigationNode node = iterator.next();
				List<ItemNavigationTab> tabs = node.getTabs();
				if( tabs == null || tabs.size() == 0 )
				{
					iterator.remove();
				}
			}
		}
	}

}
