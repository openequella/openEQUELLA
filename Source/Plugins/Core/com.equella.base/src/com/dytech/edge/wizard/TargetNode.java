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

package com.dytech.edge.wizard;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import com.dytech.devlib.PropBagEx;
import com.dytech.devlib.PropBagEx.PropBagIterator;
import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.LanguageString;
import com.tle.common.Check;
import com.tle.common.i18n.LangUtils;

/**
 * A bean for holding information about a wizard target.
 */
@SuppressWarnings("nls")
public class TargetNode implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String target;
	private String attribute;
	@Deprecated
	@XStreamOmitField
	@SuppressWarnings("unused")
	private boolean noSearch;

	public TargetNode(String target, String attribute)
	{
		setTarget(target);
		setAttribute(attribute);
	}

	public PropBagEx addNode(PropBagEx bag, String value)
	{
		PropBagEx xml = bag;
		if( !isAttribute() )
		{
			if( target.length() > 0 )
			{
				xml = bag.newSubtree(target);
			}
			xml.setNode("/", value);
		}
		else
		{
			bag.setNode(getFullTarget(), value);
			xml = null;
		}
		return xml;
	}

	public PropBagEx addLangNodes(PropBagEx bag, Map<String, String> values)
	{
		if( values != null && !values.isEmpty() )
		{
			PropBagEx xml = bag;
			if( !isAttribute() )
			{
				if( target.length() > 0 )
				{
					xml = bag.newSubtree(target);
				}

				LanguageBundle bundle = new LanguageBundle();
				Map<String, LanguageString> strings = new HashMap<String, LanguageString>();
				for( Map.Entry<String, String> entry : values.entrySet() )
				{
					String key = entry.getKey();

					LanguageString langString = new LanguageString();
					langString.setBundle(bundle);
					langString.setLocale(key);
					langString.setText(entry.getValue());
					strings.put(key, langString);
				}
				bundle.setStrings(strings);
				LangUtils.setBundleToXml(bundle, xml);
			}
			return xml;
		}
		return null;
	}

	public String getXoqlPath()
	{
		return getFreetextField();
	}

	public String getFreetextField()
	{
		StringBuilder buffer = new StringBuilder();

		if( !target.startsWith("/") ) //$NON-NLS-1$
		{
			buffer.append('/');
		}
		buffer.append(target);

		if( isAttribute() )
		{
			if( buffer.charAt(buffer.length() - 1) != '/' )
			{
				buffer.append('/');
			}
			buffer.append('@');
			buffer.append(attribute);
		}

		return buffer.toString();
	}

	public void clear(PropBagEx bag)
	{
		if( !isAttribute() )
		{
			if( target.length() > 0 && !target.equals("/") )
			{
				bag.deleteAll(target);
			}
		}
		else
		{
			bag.deleteNode(getFullTarget());
		}
	}

	public int count(PropBagEx bag)
	{
		return bag.nodeCount(target);
	}

	public boolean nodeExists(PropBagEx bag, int i)
	{
		if( target.equals("/") )
		{
			return true;
		}
		else
		{
			return bag.nodeExists(getNode(i));
		}
	}

	public String getVal(PropBagEx bag, int i)
	{
		// Return the value
		return bag.getNode(getNode(i));
	}

	private String getNode(int i)
	{
		String combinedTarget = target;

		// Don't add index if looking at root node.
		if( target.length() > 0 && i > 0 )
		{
			// TLE-2323
			combinedTarget += "[" + i + "]";
		}

		// Add an attribute if we need to.
		if( isAttribute() )
		{
			if( !combinedTarget.endsWith("/") )
			{
				combinedTarget += '/';
			}
			combinedTarget += "@" + attribute;
		}
		return combinedTarget;
	}

	public Collection<String> getValues(PropBagEx bag)
	{
		return bag.getNodeList(getFullTarget());

	}

	public String getAttribute()
	{
		return attribute;
	}

	public void setAttribute(String attribute)
	{
		this.attribute = attribute;
	}

	public String getTarget()
	{
		return target;
	}

	public void setTarget(String target)
	{
		if( "/".equals(target) )
		{
			target = "";
		}
		this.target = target;
	}

	public PropBagIterator iterate(PropBagEx bag)
	{
		return bag.iterator(target);
	}

	public Map<String, String> getLangNode(PropBagEx itemxml, Locale defaultLocale)
	{
		Map<String, String> map = new HashMap<String, String>();
		PropBagEx node = itemxml.getSubtree(target);
		if( node != null )
		{
			LanguageBundle bundle = LangUtils.getBundleFromXml(node, defaultLocale);
			Map<String, LanguageString> strings = bundle.getStrings();
			for( LanguageString langString : strings.values() )
			{
				map.put(langString.getLocale(), langString.getText());
			}
		}
		return map;
	}

	public String getFullTarget()
	{
		if( !isAttribute() )
		{
			return target;
		}
		return target + '@' + attribute;
	}

	public boolean isAttribute()
	{
		return !Check.isEmpty(attribute);
	}

	@Override
	public String toString()
	{
		return getFullTarget();
	}
}
