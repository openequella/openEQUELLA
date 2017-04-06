/*
 * Created on 7/06/2006
 */
package com.tle.common.item;

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