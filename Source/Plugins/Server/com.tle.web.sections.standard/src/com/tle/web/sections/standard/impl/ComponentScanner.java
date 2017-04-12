package com.tle.web.sections.standard.impl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import com.tle.web.sections.Section;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.SectionUtils;
import com.tle.web.sections.events.BookmarkEvent;
import com.tle.web.sections.standard.AbstractHtmlComponent;
import com.tle.web.sections.standard.ComponentFactory;
import com.tle.web.sections.standard.annotations.Component;

public class ComponentScanner
{
	private List<ComponentData> components;

	public static class ComponentData
	{
		Field field;
		String id;
		Component annotation;
	}

	public ComponentScanner(Class<?> clazz, ComponentRegistrationHandler handler)
	{
		components = new ArrayList<ComponentData>();
		Field[] fields = clazz.getDeclaredFields();
		for( Field field : fields )
		{
			Component annotation = field.getAnnotation(Component.class);
			if( annotation != null )
			{
				field.setAccessible(true);
				ComponentData data = new ComponentData();
				data.field = field;
				data.annotation = annotation;
				String name = annotation.name();
				if( name.isEmpty() )
				{
					name = field.getName();
				}
				data.id = name;
				components.add(data);
			}
		}
		clazz = clazz.getSuperclass();
		if( clazz != null )
		{
			ComponentScanner scanner = handler.getForClass(clazz);
			components.addAll(scanner.components);
		}
	}

	@SuppressWarnings("unchecked")
	public void registerComponents(String parentId, SectionTree tree, Section section, ComponentFactory factory)
	{
		for( ComponentData data : components )
		{
			try
			{
				Component annotation = data.annotation;
				AbstractHtmlComponent component = (AbstractHtmlComponent) data.field.get(section);
				String id = data.id;
				if( component == null )
				{
					component = factory.createComponent(parentId, id, tree,
						(Class<AbstractHtmlComponent>) data.field.getType(), false);
					data.field.set(section, component);
				}
				else
				{
					factory.setupComponent(parentId, id, tree, component);
				}
				component.setStateful(annotation.stateful());
				component.setIgnoreForContext(new HashSet<String>(Arrays.asList(annotation.ignoreForContext())));
				HashSet<String> onlyForContexts = new HashSet<String>(Arrays.asList(annotation.onlyForContext()));

				HashSet<String> contexts = new HashSet<String>(Arrays.asList(annotation.contexts()));
				contexts.addAll(onlyForContexts);
				if( annotation.supported() )
				{
					contexts.add(BookmarkEvent.CONTEXT_SUPPORTED);
				}
				if( !annotation.parameter().isEmpty() )
				{
					component.setParameterId(annotation.parameter());
				}
				component.setOnlyForContext(onlyForContexts);
				component.setContexts(contexts);
				if( annotation.register() )
				{
					tree.registerInnerSection(component, parentId);
				}
			}
			catch( Exception e )
			{
				SectionUtils.throwRuntime(e);
			}
		}
	}

}
