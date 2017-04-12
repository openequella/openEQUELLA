/*
 * Created on Apr 12, 2005
 */
package com.tle.core.harvester.oai.xstream;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.tle.core.harvester.oai.data.ResumptionToken;

/**
 * 
 */
public class OAIResumptionTokenConverter extends OAIAbstractConverter
{
	private static final String CURSOR = "cursor";
	private static final String COMPLETELISTSIZE = "completeListSize";

	@Override
	public boolean canConvert(Class kclass)
	{
		return kclass.equals(ResumptionToken.class);
	}

	@Override
	public void marshal(Object object, HierarchicalStreamWriter writer, MarshallingContext arg2)
	{
		ResumptionToken token = (ResumptionToken) object;
		writer.addAttribute(COMPLETELISTSIZE, "" + token.getCompleteListSize());
		writer.addAttribute(CURSOR, "" + token.getCursor());
		writer.setValue(token.getToken());
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
	{
		ResumptionToken token = new ResumptionToken();
		String size = reader.getAttribute(COMPLETELISTSIZE);
		String cursor = reader.getAttribute(CURSOR);
		token.setCompleteListSize(parseInt(size, 0));
		token.setCursor(parseInt(cursor, 0));
		token.setToken(reader.getValue());
		return token;
	}

}
