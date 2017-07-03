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

package com.tle.core.remoterepo.parser.mods.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Node;

import com.tle.core.xml.XmlDocument;

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
