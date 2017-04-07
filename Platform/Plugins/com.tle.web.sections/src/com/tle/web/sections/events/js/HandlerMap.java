package com.tle.web.sections.events.js;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import com.tle.web.sections.js.JSStatements;
import com.tle.web.sections.js.generic.StatementHandler;
import com.tle.web.sections.js.generic.statement.StatementBlock;

public class HandlerMap
{
	private HandlerMap fallbackMap;
	private Map<String, JSHandler> handlerMap;

	public void setEventHandler(String event, JSHandler handler)
	{
		ensureHandlerMap();
		handlerMap.put(event, handler);
	}

	private void ensureHandlerMap()
	{
		if( handlerMap == null )
		{
			handlerMap = new LinkedHashMap<String, JSHandler>();
		}
	}

	public void addEventStatements(String event, JSStatements... statements)
	{
		if( handlerMap != null && handlerMap.containsKey(event) )
		{
			handlerMap.get(event).addStatements(StatementBlock.get(statements));
		}
		else
		{
			JSHandler newHandler = null;
			if( fallbackMap != null )
			{
				newHandler = fallbackMap.getHandler(event);
			}
			if( newHandler != null )
			{
				newHandler = new StatementHandler(newHandler, StatementBlock.get(statements));
			}
			else
			{
				newHandler = new StatementHandler(statements);
			}
			ensureHandlerMap();
			handlerMap.put(event, newHandler);
		}
	}

	public Set<String> getEventKeys()
	{
		if( handlerMap == null && fallbackMap != null )
		{
			return fallbackMap.getEventKeys();
		}
		if( fallbackMap == null && handlerMap != null )
		{
			return handlerMap.keySet();
		}
		if( fallbackMap == null && handlerMap == null )
		{
			return Collections.emptySet();
		}
		// else neither fallbackMap nor handlerMap is null
		Set<String> allKeys = new HashSet<String>(handlerMap.keySet());

		// sonar can't figure out that fallbackMap cannot be null at this point
		if( fallbackMap != null )
		{
			allKeys.addAll(fallbackMap.getEventKeys());
		}

		return allKeys;
	}

	public JSHandler getHandler(String event)
	{
		if( handlerMap != null && handlerMap.containsKey(event) )
		{
			return handlerMap.get(event);
		}
		if( fallbackMap != null )
		{
			return fallbackMap.getHandler(event);
		}
		return null;
	}

	public void setFallbackMap(HandlerMap fallbackMap)
	{
		this.fallbackMap = fallbackMap;
	}

}
