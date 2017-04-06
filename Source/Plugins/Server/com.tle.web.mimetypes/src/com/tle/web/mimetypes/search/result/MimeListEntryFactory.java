package com.tle.web.mimetypes.search.result;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.google.inject.Provider;
import com.tle.beans.mime.MimeEntry;
import com.tle.core.guice.Bind;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.events.js.JSHandler;

/**
 * @author aholland
 */
@Bind
@Singleton
public class MimeListEntryFactory
{
	@Inject
	private Provider<MimeListEntry> entryProvider;

	public MimeListEntry createMimeListEntry(SectionInfo info, MimeEntry mime, JSHandler editHandler,
		JSHandler deleteHandler)
	{
		MimeListEntry listItem = entryProvider.get();
		listItem.setEditHandler(editHandler);
		listItem.setDeleteHandler(deleteHandler);
		listItem.setMime(mime);
		return listItem;
	}
}
