package com.tle.core.remoterepo.parser.mods.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Node;

import com.tle.common.util.XmlDocument;

/**
 * @author aholland
 */
public class ModsNotes extends ModsPart
{
	protected Map<String, Set<String>> notes;
	protected Set<String> allNotes;

	public ModsNotes(XmlDocument xml, Node context)
	{
		super(xml, context);
	}

	public Set<String> getNotes()
	{
		if( allNotes == null )
		{
			allNotes = new HashSet<String>();
			notes = new HashMap<String, Set<String>>();
			for( Node n : xml.nodeList("note", context) )
			{
				allNotes.add(XmlDocument.getTextContent(n));
			}
		}
		return allNotes;
	}
}
