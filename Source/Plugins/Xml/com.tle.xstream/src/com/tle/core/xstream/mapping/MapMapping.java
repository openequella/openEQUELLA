/*
 * Created on May 24, 2005
 */
package com.tle.core.xstream.mapping;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.tle.core.xstream.XMLDataConverter;
import com.tle.core.xstream.XMLDataMappings;

/**
 * 
 */
public class MapMapping extends AbstractTypeMapping
{
	private String keyPath;
	private String valuePath;
	private XMLDataConverter converter;

	public MapMapping(String name, String node, String keyPath, String valuePath)
	{
		this(name, node, null, keyPath, valuePath);
	}

	public MapMapping(String name, String node, Class<?> type, String keyPath, String valuePath)
	{
		super(name, node, type);
		converter = new XMLDataConverter();
		this.valuePath = valuePath;
		this.keyPath = keyPath;
	}

	// Presumably the intent is to return the implementation class, so we
	// ignore Sonar's "loose coupling" warning
	@Override
	public Class<?> getDefaultType()
	{
		return HashMap.class; // NOSONAR
	}

	@Override
	public Class<?> getRequiredType()
	{
		return Map.class;
	}

	@SuppressWarnings("nls")
	@Override
	public void unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context, Object object)
	{
		Map<Object, Object> map = getInstantiatedMap(object);
		MapHolder holder = new MapHolder(map);
		XMLDataMappings mappings = createDataMappings(holder);
		converter.recurseUnmarshal(object, reader, context, "", mappings);
	}

	@Override
	public void marshal(HierarchicalStreamWriter writer, MarshallingContext context, Object object)
	{
		Map<?, ?> map = getMap(object);
		if( map != null )
		{
			for( Entry<?, ?> entry : map.entrySet() )
			{
				MapKeyValue kv = new MapKeyValue(entry.getKey(), entry.getValue());
				marshallValue(writer, context, kv);
			}
		}
	}

	protected void marshallValue(HierarchicalStreamWriter writer, MarshallingContext context, Object value)
	{
		XMLDataMappings mappings = createDataMappings(null);

		MapKeyValue kv = (MapKeyValue) value;

		converter.marshal(kv, mappings, writer, context);
	}

	protected AbstractMapping createMapNodeMapping(String path, MapHolder holder, boolean isKey)
	{
		return new MapNodeMapping(path, holder, isKey);
	}

	protected XMLDataMappings createDataMappings(MapHolder holder)
	{
		XMLDataMappings mappings = new XMLDataMappings();

		AbstractMapping valueMap = createMapNodeMapping(valuePath, holder, false);
		AbstractMapping keyMap = createMapNodeMapping(keyPath, holder, true);
		mappings.addNodeMapping(keyMap);
		mappings.addNodeMapping(valueMap);
		return mappings;
	}

	protected static class MapHolder
	{
		int count = 0;
		Map<Object, Object> map;
		MapKeyValue kv;

		public MapHolder(Map<Object, Object> map)
		{
			this.map = map;
			this.kv = new MapKeyValue();
		}

		public void add(Object value, boolean key)
		{
			if( key )
			{
				kv.key = value;
			}
			else
			{
				kv.value = value;
			}
			count++;
			if( count == 2 )
			{
				count = 0;
				map.put(kv.key, kv.value);
			}
		}
	}

	private static class MapNodeMapping extends NodeMapping
	{
		private boolean key;
		private final MapHolder holder;

		@SuppressWarnings("nls")
		public MapNodeMapping(String node, MapHolder holder, boolean key)
		{
			super("", node);
			this.holder = holder;
			this.key = key;
		}

		@Override
		public void unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context, Object object)
		{
			Object value = getUnmarshalledValue(object, reader, context);
			holder.add(value, key);
		}

		@Override
		protected Object getMarshalledValue(Object object)
		{
			return ((MapKeyValue) object).getValue(key);
		}
	}

	protected static class MapKeyValue
	{
		Object key;
		Object value;

		public MapKeyValue(Object key, Object value)
		{
			this.key = key;
			this.value = value;
		}

		public MapKeyValue()
		{
			// EMPTY
		}

		public Object getValue(boolean isKey)
		{
			return isKey ? key : value;
		}
	}

	private Map<?, ?> getMap(Object object)
	{
		return (Map<?, ?>) getField(object);
	}

	@SuppressWarnings("unchecked")
	private Map<Object, Object> getInstantiatedMap(Object object)
	{
		return (Map<Object, Object>) getInstantiatedField(object);
	}

	@Override
	public boolean hasValue(Object object)
	{
		Map<?, ?> map = getMap(object);
		return map != null && map.size() > 0;
	}
}
