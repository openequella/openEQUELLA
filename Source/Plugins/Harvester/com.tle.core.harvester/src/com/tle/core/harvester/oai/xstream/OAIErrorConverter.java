/*
 * Created on Apr 12, 2005
 */
package com.tle.core.harvester.oai.xstream;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.tle.core.harvester.oai.data.OAIError;

/**
 * 
 */
public class OAIErrorConverter extends OAIAbstractConverter
{
	@Override
	public boolean canConvert(Class kclass)
	{
		return kclass.equals(OAIError.class);
	}

	@Override
	public void marshal(Object object, HierarchicalStreamWriter writer, MarshallingContext arg2)
	{
		OAIError error = (OAIError) object;
		writer.addAttribute("code", error.getCode());
		writer.setValue(error.getMessage());
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
	{
		String code = reader.getAttribute("code");
		String message = reader.getValue();
		return new OAIError(code, message);
	}

}
