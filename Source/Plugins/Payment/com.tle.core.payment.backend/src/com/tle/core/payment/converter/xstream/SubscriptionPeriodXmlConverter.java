package com.tle.core.payment.converter.xstream;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.tle.common.payment.entity.SubscriptionPeriod;
import com.tle.core.payment.dao.SubscriptionPeriodDao;

/**
 * @author Aaron
 */
@SuppressWarnings("nls")
public class SubscriptionPeriodXmlConverter implements Converter
{
	private final SubscriptionPeriodDao periodDao;

	public SubscriptionPeriodXmlConverter(SubscriptionPeriodDao periodDao)
	{
		this.periodDao = periodDao;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class clazz)
	{
		return clazz == SubscriptionPeriod.class;
	}

	@Override
	public void marshal(Object obj, HierarchicalStreamWriter writer, MarshallingContext context)
	{
		final SubscriptionPeriod period = (SubscriptionPeriod) obj;
		if( period != null )
		{
			writer.addAttribute("uuid", period.getUuid());
		}
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
	{
		final String uuidFromStream = reader.getAttribute("uuid");
		return periodDao.getByUuid(uuidFromStream);
	}
}
