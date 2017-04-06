package com.tle.core.item.serializer.impl;

import javax.inject.Singleton;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.NamedType;
import com.tle.core.guice.Bind;
import com.tle.core.jackson.MapperExtension;
import com.tle.web.api.item.interfaces.beans.FileBean;
import com.tle.web.api.item.interfaces.beans.FolderBean;
import com.tle.web.api.item.interfaces.beans.GenericFileBean;
import com.tle.web.api.item.interfaces.beans.RootFolderBean;

@Bind
@Singleton
public class FileMapperExtension implements MapperExtension
{
	@Override
	public void extendMapper(ObjectMapper mapper)
	{
		mapper.registerSubtypes(new NamedType(RootFolderBean.class, RootFolderBean.TYPE));
		mapper.registerSubtypes(new NamedType(FolderBean.class, FolderBean.TYPE));
		mapper.registerSubtypes(new NamedType(FileBean.class, FileBean.TYPE));
		mapper.registerSubtypes(new NamedType(GenericFileBean.class, GenericFileBean.TYPE));
	}
}
