package com.tle.core.qti.converter.xstream;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.tle.common.qti.entity.QtiAssessmentItemRef;
import com.tle.core.qti.dao.QtiAssessmentItemRefDao;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
public class QtiAssessmentItemRefXmlConverter implements Converter
{
	private final QtiAssessmentItemRefDao questionRefDao;

	public QtiAssessmentItemRefXmlConverter(QtiAssessmentItemRefDao questionRefDao)
	{
		this.questionRefDao = questionRefDao;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class clazz)
	{
		return clazz == QtiAssessmentItemRef.class;
	}

	@Override
	public void marshal(Object obj, HierarchicalStreamWriter writer, MarshallingContext context)
	{
		final QtiAssessmentItemRef questionRef = (QtiAssessmentItemRef) obj;
		if( questionRef != null )
		{
			writer.addAttribute("uuid", questionRef.getUuid());
		}
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
	{
		final String uuidFromStream = reader.getAttribute("uuid");
		return questionRefDao.getByUuid(uuidFromStream);
	}
}
