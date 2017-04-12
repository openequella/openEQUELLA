package com.tle.mycontent.web.selection;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.mycontent.ContentHandler;
import com.tle.mycontent.service.MyContentService;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.generic.CachedData.CacheFiller;
import com.tle.web.selection.SelectionService;
import com.tle.web.selection.SelectionSession;

@Bind
@Singleton
public class SelectionAllowedHandlers implements CacheFiller<Set<String>>
{
	@Inject
	private MyContentService myContentService;
	@Inject
	private SelectionService selectionService;

	@Override
	public Set<String> get(SectionInfo info)
	{
		Set<String> handlerIds = myContentService.getContentHandlerIds();
		MyContentSelectionSettings settings = getSelectionSettings(info);
		if( settings != null )
		{
			handlerIds = new HashSet<String>(handlerIds);
			Collection<String> types = settings.getRestrictToHandlerTypes();
			if( types != null )
			{
				handlerIds.retainAll(types);
			}
			if( settings.isRawFilesOnly() )
			{
				Iterator<String> iter = handlerIds.iterator();
				while( iter.hasNext() )
				{
					String handlerId = iter.next();
					ContentHandler handler = myContentService.getHandlerForId(handlerId);
					if( !handler.isRawFiles() )
					{
						iter.remove();
					}
				}
			}
			return handlerIds;
		}
		return null;
	}

	protected MyContentSelectionSettings getSelectionSettings(SectionInfo info)
	{
		SelectionSession session = selectionService.getCurrentSession(info);
		return (MyContentSelectionSettings) (session != null ? session.getAttribute(MyContentSelectionSettings.class)
			: null);
	}
}
