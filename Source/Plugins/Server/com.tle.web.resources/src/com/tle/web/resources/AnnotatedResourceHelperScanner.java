package com.tle.web.resources;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import com.tle.web.sections.Section;
import com.tle.web.sections.SectionUtils;

public class AnnotatedResourceHelperScanner
{
	private static class ResourceData
	{
		Field field;
		Class<?> clazz;
	}

	private final Class<?> clazz;
	private PluginResourceHelper helper;
	private final List<ResourceData> fieldData = new ArrayList<ResourceData>();
	private final ResourceHelperHandler handler;

	public AnnotatedResourceHelperScanner(Class<?> clazz, ResourceHelperHandler handler)
	{
		this.handler = handler;
		this.clazz = clazz;
		try
		{
			Field[] fields = clazz.getDeclaredFields();
			for( Field field : fields )
			{
				ResourceHelper annotation = field.getAnnotation(ResourceHelper.class);
				if( annotation != null )
				{
					field.setAccessible(true);
					if( (field.getModifiers() & Modifier.STATIC) != 0 )
					{
						field.set(null, getHelper());
					}
					else
					{
						ResourceData data = new ResourceData();
						data.field = field;
						if( annotation.fixed() )
						{
							data.clazz = clazz;
						}
						fieldData.add(data);
					}
				}
			}
			clazz = clazz.getSuperclass();
			if( clazz != null )
			{
				AnnotatedResourceHelperScanner scanner = handler.getForClass(clazz);
				fieldData.addAll(scanner.fieldData);
			}
		}
		catch( IllegalAccessException e )
		{
			SectionUtils.throwRuntime(e);
		}
	}

	private PluginResourceHelper getHelper()
	{
		if( helper == null )
		{
			helper = ResourcesService.getResourceHelper(clazz);
		}
		return helper;
	}

	public void setup(Section section)
	{
		try
		{
			for( ResourceData data : fieldData )
			{
				PluginResourceHelper tempHelper;
				if( data.clazz != null )
				{
					tempHelper = handler.getForClass(data.clazz).getHelper();
				}
				else
				{
					tempHelper = getHelper();
				}
				data.field.set(section, tempHelper);
			}
		}
		catch( IllegalAccessException e )
		{
			SectionUtils.throwRuntime(e);
		}
	}
}
