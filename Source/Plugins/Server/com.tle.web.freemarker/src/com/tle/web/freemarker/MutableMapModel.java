/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
