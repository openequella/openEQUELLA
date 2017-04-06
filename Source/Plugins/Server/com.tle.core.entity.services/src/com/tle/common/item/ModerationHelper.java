/*
 * Created on 7/06/2006
 */
package com.tle.common.item;

import java.util.Set;

import javax.inject.Singleton;

import com.dytech.devlib.PropBagEx;
import com.dytech.edge.common.Constants;
import com.tle.beans.item.Item;
import com.tle.beans.item.ModerationStatus;
import com.tle.core.guice.Bind;

@Bind
@Singleton
public class ModerationHelper extends AbstractHelper
{
	@Override
	public void load(PropBagEx itemxml, Item bean)
	{
		itemxml = itemxml.aquireSubtree("moderation"); //$NON-NLS-1$
		itemxml.deleteAll(Constants.XML_WILD);

		ModerationStatus moderation = bean.getModeration();
		if( moderation != null )
		{
			if( moderation.getLiveApprovalDate() != null )
			{
				itemxml.setNode("liveapprovaldate", formatDate(moderation //$NON-NLS-1$
					.getLiveApprovalDate()));
			}
		}
	}

	@Override
	public void save(PropBagEx xml, Item item, Set<String> handled)
	{
		// nothing
	}
}