/*
 * Created on Apr 13, 2005
 */
package com.tle.core.harvester.oai.xstream;

import java.util.Collection;
import java.util.Iterator;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.tle.core.harvester.oai.data.List;
import com.tle.core.harvester.oai.data.ResumptionToken;

/**
 * 
 */
public abstract class OAIAbstractConverter implements Converter
{
	private XStream xstream;

	public OAIAbstractConverter()
	{
		xstream = XStreamFactory.getXStream();
	}

	protected int parseInt(String string, int defaultValue)
	{
		try
		{
			defaultValue = Integer.parseInt(string);
		}
		catch( Exception e )
		{
			// IGNORE
		}

		return defaultValue;
	}

	public Object convert(String name, UnmarshallingContext context)
	{
		return context.convertAnother(context, xstream.getMapper().realClass(name));
	}

	protected void addAttribute(HierarchicalStreamWriter writer, String name, String value)
	{
		if( value != null && value.length() > 0 )
		{
			writer.addAttribute(name, value);
		}
	}

	public void startNode(HierarchicalStreamWriter writer, String node, Object value)
	{
		if( value != null )
		{
			writer.startNode(node);
			xstream.marshal(value, writer);
			writer.endNode();
		}
	}

	public void startNode(HierarchicalStreamWriter writer, String node, String value)
	{
		if( value != null )
		{
			writer.startNode(node);
			writer.setValue(value);
			writer.endNode();
		}
	}

	public void startNode(HierarchicalStreamWriter writer, String node, Collection value)
	{
		this.startNode(writer, node, value, false);
	}

	public void startNode(HierarchicalStreamWriter writer, String node, Collection value, boolean oneNode)
	{
		if( value != null && value.size() > 0 )
		{
			Iterator i = value.iterator();
			if( oneNode )
			{
				writer.startNode(node);
			}

			while( i.hasNext() )
			{
				if( !oneNode )
				{
					writer.startNode(node);
				}

				Object o = i.next();
				if( o instanceof String )
				{
					writer.setValue(o.toString());
				}
				else
				{
					marshal(o, writer);
				}

				if( !oneNode )
				{
					writer.endNode();
				}
			}

			if( value instanceof List )
			{

				List list = (List) value;

				ResumptionToken token = list.getResumptionToken();
				if( token != null )
				{
					if( !oneNode )
					{
						writer.startNode(node);
					}

					marshal(token, writer);

					if( !oneNode )
					{
						writer.endNode();
					}
				}
			}

			if( oneNode )
			{
				writer.endNode();
			}
		}
	}

	public void marshal(Object object, HierarchicalStreamWriter writer)
	{
		if( object != null )
		{
			xstream.marshal(object, writer);
		}
	}
}
