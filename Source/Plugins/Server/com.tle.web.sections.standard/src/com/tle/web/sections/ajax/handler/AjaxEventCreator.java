package com.tle.web.sections.ajax.handler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import com.tle.annotation.NonNullByDefault;
import com.tle.web.sections.SectionId;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.ajax.AjaxRenderContext;
import com.tle.web.sections.ajax.JSONRenderer;
import com.tle.web.sections.ajax.JSONResponseCallback;
import com.tle.web.sections.ajax.exception.AjaxException;
import com.tle.web.sections.convert.Conversion;
import com.tle.web.sections.events.AbstractDirectEvent;
import com.tle.web.sections.events.SectionEvent;
import com.tle.web.sections.events.js.ParameterizedEvent;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.SimpleSectionResult;

@NonNullByDefault
@SuppressWarnings("nls")
public class AjaxEventCreator implements ParameterizedEvent
{
	protected final Method method;
	protected final String sectionId;
	private final Class<?> firstParamType;
	private final int numParams;
	private final int priority;
	private String eventId;
	private Conversion conversion;

	public static class SimpleResult
	{
		private final Object result;

		public SimpleResult(Object result)
		{
			this.result = result;
		}

		public Object getResult()
		{
			return result;
		}
	}

	public AjaxEventCreator(String eventId, String id, Method method, int priority, Conversion conversion)
	{
		this.conversion = conversion;
		this.eventId = eventId;
		this.sectionId = id;
		this.method = method;
		this.priority = priority;
		firstParamType = method.getParameterTypes()[0];
		numParams = method.getParameterTypes().length - 1;
	}

	@Override
	public String getEventId()
	{
		return eventId;
	}

	@Override
	public SectionEvent<?> createEvent(SectionInfo info, final String[] params)
	{
		return new AbstractDirectEvent(priority, sectionId)
		{
			@Override
			public void fireDirect(SectionId sectionId, SectionInfo info)
			{
				info.preventGET();
				Object[] args = new Object[params.length + 1];
				AjaxRenderContext ajaxContext = null;
				if( firstParamType == AjaxRenderContext.class )
				{
					ajaxContext = new StandardAjaxRenderContext(info.getRootRenderContext());
					args[0] = ajaxContext;
				}
				else
				{
					args[0] = info;
				}
				Type[] paramTypes = method.getGenericParameterTypes();
				int i = 1;
				for( String param : params )
				{
					args[i] = conversion.convertFromString(param, paramTypes[i]);
					i++;
				}
				try
				{
					Object result = method.invoke(info.getSectionForId(sectionId), args);
					if( result == null )
					{
						info.getRootRenderContext().setRenderedResponse(new SimpleSectionResult(""));
						return;
					}
					if( result instanceof JSONResponseCallback )
					{
						if( ajaxContext == null )
						{
							throw new Error("JSONResponseCallback only supported for ajax methods");
						}
						ajaxContext.setJSONResponseCallback((JSONResponseCallback) result);
					}
					else
					{
						SectionRenderable renderable;
						if( result instanceof SectionRenderable )
						{
							renderable = (SectionRenderable) result;
						}
						else
						{
							renderable = new JSONRenderer(result, true);
						}
						info.getRootRenderContext().setRenderedResponse(renderable);
					}
				}

				catch( InvocationTargetException ite )
				{
					throw new AjaxException(ite.getCause());
				}
				catch( Exception t )
				{
					throw new AjaxException(t);
				}
			}
		};
	}

	@Override
	public int getParameterCount()
	{
		return numParams;
	}

	@Override
	public boolean isPreventXsrf()
	{
		return false;
	}
}
