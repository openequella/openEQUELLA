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

package com.dytech.edge.admin.wizard;

import com.dytech.gui.adapters.TablePasteAdapter.DataConverter;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.core.util.CompositeClassLoader;

public class XStreamDataConverter implements DataConverter
{
	private final XStream xstream;

	public XStreamDataConverter(Class<?> klass)
	{
		xstream = new XStream();
		ClassLoader cl = xstream.getClassLoader();
		// I think they always are
		if( cl instanceof CompositeClassLoader )
		{
			((CompositeClassLoader) cl).add(klass.getClassLoader());
		}
		else
		{
			CompositeClassLoader comp = new CompositeClassLoader();
			comp.add(cl);
			comp.add(klass.getClassLoader());
			xstream.setClassLoader(comp);
		}
	}

	@Override
	public Object deserialise(String value)
	{
		return xstream.fromXML(value);
	}

	@Override
	public String serialise(Object object)
	{
		return xstream.toXML(object).replaceAll("\\n", ""); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
