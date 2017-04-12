package com.tle.web.api.item.impl;

import javax.inject.Singleton;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.tle.core.guice.Bind;
import com.tle.core.jackson.MapperExtension;
import com.tle.web.api.item.equella.interfaces.beans.EquellaItemBean;

/**
 * @author Aaron
 */
@Bind
@Singleton
public class ItemMapperExtension implements MapperExtension
{
	@Override
	public void extendMapper(ObjectMapper mapper)
	{
		mapper.registerModule(new ItemMapperExtensionModule());
	}

	public static class ItemMapperExtensionModule extends SimpleModule
	{
		public ItemMapperExtensionModule()
		{
			super("ItemMapperExtensionModule");
			registerSubtypes(new NamedType(EquellaItemBean.class, "equellaItem"));
		}
	}
}
