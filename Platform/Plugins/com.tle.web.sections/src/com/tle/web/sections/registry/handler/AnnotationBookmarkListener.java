package com.tle.web.sections.registry.handler;

import com.tle.web.sections.Section;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.events.BookmarkEvent;
import com.tle.web.sections.events.BookmarkEventListener;
import com.tle.web.sections.events.DocumentParamsEvent;

public class AnnotationBookmarkListener extends TargetedListener implements BookmarkEventListener
{
	private AnnotatedBookmarkScanner handler;

	public AnnotationBookmarkListener(String id, Section section, SectionTree tree, AnnotatedBookmarkScanner handler)
	{
		super(id, section, tree);
		this.handler = handler;
	}

	@Override
	public void bookmark(SectionInfo info, BookmarkEvent event)
	{
		handler.bookmark(info, id, event);
	}

	@Override
	public void document(SectionInfo info, DocumentParamsEvent event)
	{
		handler.document(info, id, event);
	}

}
