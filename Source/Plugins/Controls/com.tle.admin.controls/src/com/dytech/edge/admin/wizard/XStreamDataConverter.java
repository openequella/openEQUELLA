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
