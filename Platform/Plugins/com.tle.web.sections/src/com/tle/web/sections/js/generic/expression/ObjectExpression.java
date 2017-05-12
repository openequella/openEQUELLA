package com.tle.web.sections.js.generic.expression;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.web.sections.events.PreRenderContext;
import com.tle.web.sections.events.RenderContext;
import com.tle.web.sections.js.JSExpression;
import com.tle.web.sections.js.JSUtils;

@NonNullByDefault
public class ObjectExpression extends AbstractExpression
{
	private Map<String, JSExpression> properties = new LinkedHashMap<String, JSExpression>();

	public ObjectExpression()
	{
		// nothing
	}

	public ObjectExpression(Object... keyValues)
	{
		for( int i = 0; i < keyValues.length; i++ )
		{
			put(keyValues[i++].toString(), JSUtils.convertExpression(keyValues[i]));
		}
	}

	public void put(String key, @Nullable Object value)
	{
		properties.put(key, JSUtils.convertExpression(value));
	}

	@Override
	public String getExpression(RenderContext info)
	{
		StringBuilder sbuf = new StringBuilder();
		sbuf.append('{');
		boolean first = true;
		for( String key : properties.keySet() )
		{
			if( !first )
			{
				sbuf.append(',');
			}
			first = false;
			if( JSUtils.needsEscape(key) )
			{
				sbuf.append(JSUtils.toJSString(key));
			}
			else
			{
				sbuf.append(key);
			}
			sbuf.append(':');
			sbuf.append(properties.get(key).getExpression(info));
		}
		sbuf.append('}');
		return sbuf.toString();
	}

	@Override
	public void preRender(PreRenderContext info)
	{
		info.preRender(properties.values());
	}

	public Map<String, JSExpression> getValues()
	{
		return Collections.unmodifiableMap(properties);
	}

	public ObjectExpression merge(@Nullable ObjectExpression other)
	{
		if( other == null )
		{
			return this;
		}
		final Map<String, JSExpression> otherProps = other.getValues();
		for( Entry<String, JSExpression> entry : otherProps.entrySet() )
		{
			put(entry.getKey(), entry.getValue());
		}
		return this;
	}

	public boolean isEmpty()
	{
		return properties.isEmpty();
	}
}
