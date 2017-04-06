package com.tle.core.qti.converter.xstream;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.tle.common.qti.entity.QtiAssessmentItem;
import com.tle.core.qti.dao.QtiAssessmentItemDao;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
public class QtiAssessmentItemXmlConverter implements Converter
{
	private final QtiAssessmentItemDao questionDao;

	public QtiAssessmentItemXmlConverter(QtiAssessmentItemDao questionDao)
	{
		this.questionDao = questionDao;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class clazz)
	{
		return clazz == QtiAssessmentItem.class;
	}

	@Override
	public void marshal(Object obj, HierarchicalStreamWriter writer, MarshallingContext context)
	{
		final QtiAssessmentItem question = (QtiAssessmentItem) obj;
		if( question != null )
		{
			writer.addAttribute("uuid", question.getUuid());
		}
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
	{
		final String uuidFromStream = reader.getAttribute("uuid");
		return questionDao.getByUuid(uuidFromStream);
	}
}
