package com.tle.web.sections.registry.handler;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.web.sections.Section;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.annotations.Bookmarked;
import com.tle.web.sections.convert.Conversion;
import com.tle.web.sections.events.BookmarkEventListener;
import com.tle.web.sections.events.ParametersEventListener;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.RenderEventListener;
import com.tle.web.sections.generic.DummySectionInfo;

/**
 * Handler for the {@link Bookmarked} annotation.
 * <p>
 * This handler Scans the {@code Model} class ({@link Section#getModelClass()})
 * for {@code Bookmarked} annotations. If found, the handler will register a
 * {@link ParametersEventListener} and a {@link BookmarkEventListener}, and if
 * any of the @{code Bookmarked} annotations has the
 * {@link Bookmarked#rendered()} flag set, it also registers a
 * {@link RenderEventListener}.
 * 
 * @author jmaginnis
 */
@Bind
@Singleton
public class BookmarkRegistrationHandler extends CachedScannerHandler<AnnotatedBookmarkScanner>
{
	@Inject
	private Conversion conversion;

	@Override
	public void registered(String id, SectionTree tree, Section section)
	{
		Object model = section.instantiateModel(new DummySectionInfo());
		final AnnotatedBookmarkScanner bookmarkHandler = getForClass(model.getClass());
		if( bookmarkHandler.hasAnnotations() )
		{
			bookmarkHandler.registerConverters(id, tree);
			tree.addListener(null, ParametersEventListener.class, new AnnotationRequestListener(id, section, tree,
				bookmarkHandler));
			tree.addListener(id, BookmarkEventListener.class, new AnnotationBookmarkListener(id, section, tree,
				bookmarkHandler));
			if( bookmarkHandler.hasRendered() )
			{
				tree.addListener(id, RenderEventListener.class, new RenderEventListener()
				{
					@Override
					public void render(RenderEventContext context)
					{
						SectionUtils.registerRendered(context, context.getSectionId());
					}
				});
			}
		}
	}

	@Override
	protected AnnotatedBookmarkScanner newEntry(Class<?> clazz)
	{
		return new AnnotatedBookmarkScanner(clazz, this, conversion);
	}
}
