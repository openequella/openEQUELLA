/*
 * Created on 6/12/2005
 */
package com.tle.core.settings.property.serialise;

import java.util.HashMap;
import java.util.Map;

import com.tle.common.settings.ConfigurationProperties;
import com.tle.common.settings.annotation.PropertyDataList;
import com.tle.core.settings.property.PropertyBeanFactory;

public class DataListPropertySerialiser
	extends
		AbstractListPropertySerialiser<PropertyDataList, Map<String, String>, ConfigurationProperties>
{
	@Override
	Class<PropertyDataList> getType()
	{
		return PropertyDataList.class;
	}

	@Override
	protected Class<? extends ConfigurationProperties> getType(PropertyDataList t)
	{
		return t.type();
	}

	@Override
	protected String getKey(PropertyDataList t)
	{
		return t.key();
	}

	@Override
	protected void load(Map<String, Map<String, String>> list, Map<String, String> properties, String nkey, String num,
		String value)
	{
		Map<String, String> subset = list.get(num);
		if( subset == null )
		{
			subset = new HashMap<String, String>();
			list.put(num, subset);
		}
		subset.put(nkey, value);
	}

	@Override
	protected ConfigurationProperties initialise(Class<? extends ConfigurationProperties> type, Map<String, String> val)
		throws InstantiationException, IllegalAccessException
	{
		ConfigurationProperties n = type.newInstance();
		PropertyBeanFactory.load(n, val);
		return n;
	}

	@Override
	protected void save(String key, ConfigurationProperties config, Map<String, String> properties)
	{
		Map<String, String> map = new HashMap<String, String>();
		PropertyBeanFactory.save(config, map);
		for( Map.Entry<String, String> entry : map.entrySet() )
		{
			properties.put(key + '.' + entry.getKey(), entry.getValue());
		}
	}
}
