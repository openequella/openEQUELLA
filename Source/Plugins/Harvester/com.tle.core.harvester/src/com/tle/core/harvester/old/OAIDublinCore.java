/*
 * Created on Apr 15, 2005
 */
package com.tle.core.harvester.old;

import java.util.Iterator;

import com.dytech.devlib.PropBagEx;

/**
 *
 */
public class OAIDublinCore
{
	public void processMetadataToLO(PropBagEx dc, PropBagEx xml)
	{
		xml = xml.getSubtree("item"); //$NON-NLS-1$
		String title = getFirst(dc.iterateAllValues("title")); //$NON-NLS-1$
		String description = getFirst(dc.iterateAllValues("description")); //$NON-NLS-1$

		xml.setNode("itembody/name", title); //$NON-NLS-1$
		xml.setNode("itembody/description", description); //$NON-NLS-1$

		PropBagEx dcxml = xml.aquireSubtree("itembody/dc"); //$NON-NLS-1$
		setNode(dcxml, "title", dc.iterateAllValues("title")); //$NON-NLS-1$ //$NON-NLS-2$
		setNode(dcxml, "creator", dc.iterateAllValues("creator")); //$NON-NLS-1$ //$NON-NLS-2$
		setNode(dcxml, "subject", dc.iterateAllValues("subject")); //$NON-NLS-1$ //$NON-NLS-2$
		setNode(dcxml, "description", dc.iterateAllValues("description")); //$NON-NLS-1$ //$NON-NLS-2$
		setNode(dcxml, "contributor", dc.iterateAllValues("contributor")); //$NON-NLS-1$ //$NON-NLS-2$
		setNode(dcxml, "publisher", dc.iterateAllValues("publisher")); //$NON-NLS-1$ //$NON-NLS-2$
		setNode(dcxml, "date", dc.iterateAllValues("date")); //$NON-NLS-1$ //$NON-NLS-2$
		setNode(dcxml, "type", dc.iterateAllValues("type")); //$NON-NLS-1$ //$NON-NLS-2$
		setNode(dcxml, "format", dc.iterateAllValues("format")); //$NON-NLS-1$ //$NON-NLS-2$
		setNode(dcxml, "identifier", dc.iterateAllValues("identifier")); //$NON-NLS-1$ //$NON-NLS-2$
		setNode(dcxml, "source", dc.iterateAllValues("source")); //$NON-NLS-1$ //$NON-NLS-2$
		setNode(dcxml, "language", dc.iterateAllValues("language")); //$NON-NLS-1$ //$NON-NLS-2$
		setNode(dcxml, "relation", dc.iterateAllValues("relation")); //$NON-NLS-1$ //$NON-NLS-2$
		setNode(dcxml, "coverage", dc.iterateAllValues("coverage")); //$NON-NLS-1$ //$NON-NLS-2$
		setNode(dcxml, "rights", dc.iterateAllValues("rights")); //$NON-NLS-1$ //$NON-NLS-2$
	}

	protected void setNode(PropBagEx xml, String node, Iterator<String> col)
	{
		String first = getFirst(col);
		if( first.length() > 0 )
		{
			xml.setNode(node, first);
		}
	}

	protected String getFirst(Iterator<String> col)
	{
		String first = null;
		if( col.hasNext() )
		{
			first = col.next();
		}
		else
		{
			first = ""; //$NON-NLS-1$
		}
		return first;
	}

}
