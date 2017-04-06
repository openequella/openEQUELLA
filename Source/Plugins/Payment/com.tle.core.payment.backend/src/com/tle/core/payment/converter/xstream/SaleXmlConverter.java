package com.tle.core.payment.converter.xstream;

import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.tle.common.payment.entity.Sale;
import com.tle.core.payment.dao.SaleDao;

/**
 * @author Aaron
 */
public class SaleXmlConverter implements Converter
{
	private final SaleDao saleDao;

	public SaleXmlConverter(SaleDao saleDao)
	{
		this.saleDao = saleDao;
	}

	@SuppressWarnings("rawtypes")
	@Override
	public boolean canConvert(Class clazz)
	{
		return clazz == Sale.class;
	}

	@Override
	public void marshal(Object obj, HierarchicalStreamWriter writer, MarshallingContext context)
	{
		final Sale sale = (Sale) obj;
		writer.addAttribute("uuid", sale.getUuid());
	}

	@Override
	public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context)
	{
		final String uuidFromStream = reader.getAttribute("uuid");
		return saleDao.get(uuidFromStream);
	}
}
