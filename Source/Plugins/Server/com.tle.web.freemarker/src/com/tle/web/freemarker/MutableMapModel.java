package com.tle.web.freemarker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import freemarker.core.Environment;
import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.CollectionModel;
import freemarker.template.TemplateCollectionModel;
import freemarker.template.TemplateHashModelEx;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.utility.DeepUnwrap;

public class MutableMapModel implements TemplateHashModelEx, TemplateMethodModelEx
{
	private Map<String, TemplateModel> map = new HashMap<String, TemplateModel>();

	@Override
	public TemplateCollectionModel keys() throws TemplateModelException
	{
		return new CollectionModel(map.keySet(), (BeansWrapper) Environment.getCurrentEnvironment().getObjectWrapper());
	}

	@Override
	public int size() throws TemplateModelException
	{
		return map.size();
	}

	@Override
	public TemplateCollectionModel values() throws TemplateModelException
	{
		return new CollectionModel(map.values(), (BeansWrapper) Environment.getCurrentEnvironment().getObjectWrapper());
	}

	@Override
	public TemplateModel get(String s) throws TemplateModelException
	{
		return map.get(s);
	}

	@Override
	public boolean isEmpty() throws TemplateModelException
	{
		return map.isEmpty();
	}

	@Override
	public Object exec(List list) throws TemplateModelException
	{
		if( list.size() != 2 )
		{
			throw new RuntimeException("Needs a key and a value"); //$NON-NLS-1$
		}

		String key = (String) DeepUnwrap.unwrap((TemplateModel) list.get(0));
		map.put(key, (TemplateModel) list.get(1));
		return TemplateModel.NOTHING;
	}

}
