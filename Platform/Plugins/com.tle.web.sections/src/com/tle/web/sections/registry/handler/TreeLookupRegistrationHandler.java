package com.tle.web.sections.registry.handler;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Singleton;

import com.tle.core.guice.Bind;
import com.tle.web.sections.Section;
import com.tle.web.sections.SectionTree;

@Bind
@Singleton
public class TreeLookupRegistrationHandler extends CachedScannerHandler<AnnotatedTreeLookupScanner>
{

	private static final String KEY_TREELOOKUPS = "$TREE-LOOKUPS$"; //$NON-NLS-1$

	@Override
	protected AnnotatedTreeLookupScanner newEntry(Class<?> clazz)
	{
		return new AnnotatedTreeLookupScanner(clazz, this);
	}

	@Override
	public void registered(String id, SectionTree tree, Section section)
	{
		List<String> ids = tree.getAttribute(KEY_TREELOOKUPS);
		if( ids == null )
		{
			ids = new ArrayList<String>();
			tree.setAttribute(KEY_TREELOOKUPS, ids);
		}
		AnnotatedTreeLookupScanner scanner = getForClass(section.getClass());
		if( scanner.hasLookups() )
		{
			ids.add(id);
		}
	}

	@Override
	public void treeFinished(SectionTree tree)
	{
		List<String> ids = tree.getAttribute(KEY_TREELOOKUPS);
		// can happen in the case of an empty tree (see
		// RenderSummarySectionViewItem)
		if( ids != null )
		{
			for( String id : ids )
			{
				Section section = tree.getSectionForId(id);
				AnnotatedTreeLookupScanner scanner = getForClass(section.getClass());
				scanner.doLookup(tree, section);
			}
		}
	}
}
