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

package com.tle.web.sections.equella.freemarker;

import java.io.Writer;
import java.util.Map;

import javax.inject.Inject;

import com.tle.annotation.NonNullByDefault;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.core.i18n.BundleCache;
import com.tle.web.freemarker.FreemarkerSectionResult;
import com.tle.web.freemarker.PluginFreemarkerFactory;
import com.tle.web.sections.PathGenerator;
import com.tle.web.sections.SectionWriter;
import com.tle.web.sections.render.TextUtils;
import com.tle.web.sections.result.util.BundleWriter;
import com.tle.web.sections.result.util.HeaderUtils;

@SuppressWarnings("nls")
@NonNullByDefault
public class ExtendedFreemarkerFactory extends PluginFreemarkerFactory
{
	@Inject
	private BundleCache bundleCache;

	@Inject
	public void setConfiguration(ExtendedConfiguration configuration)
	{
		this.configuration = configuration;
	}

	@Override
	protected void addRootObjects(Map<String, Object> map, FreemarkerSectionResult result, Writer writer)
	{
		super.addRootObjects(map, result, writer);
		map.put("b", new BundleWriter(pluginId, bundleCache));
		map.put("t", TextUtils.INSTANCE);
		map.put("currentUser", CurrentUser.getUserState());
		if( writer instanceof SectionWriter )
		{
			SectionWriter sWriter = (SectionWriter) writer;
			map.put("head", new HeaderUtils(sWriter));
			PathGenerator pathGen = sWriter.getPathGenerator();
			map.put("baseHref", pathGen.getBaseHref(sWriter).toString());
		}
	}
}
