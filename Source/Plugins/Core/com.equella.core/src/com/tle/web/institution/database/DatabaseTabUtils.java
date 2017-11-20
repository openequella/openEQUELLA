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

package com.tle.web.institution.database;

import javax.inject.Inject;

import com.google.inject.Singleton;
import com.tle.beans.DatabaseSchema;
import com.tle.common.Check;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.DataSourceService;
import com.tle.core.hibernate.ExtendedDialect;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.LabelTagProcessor;
import com.tle.web.sections.render.SectionRenderable;
import com.tle.web.sections.render.TagState;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.standard.renderers.SpanRenderer;

@SuppressWarnings("nls")
@Bind
@Singleton
public class DatabaseTabUtils
{
	@Inject
	private DataSourceService dataSourceService;

	public SectionRenderable getNameRenderer(DatabaseSchema ds)
	{
		TagState state = new TagState();
		Label dbName = getConnectionLabel(ds);
		Label contents = dbName;

		String desc = ds.getDescription();
		if( !Check.isEmpty(desc) )
		{
			state.addTagProcessor(new LabelTagProcessor("alt", dbName));
			state.addTagProcessor(new LabelTagProcessor("title", dbName));
			contents = new TextLabel(desc);
		}
		return new SpanRenderer(state, contents);
	}

	public Label getConnectionLabel(DatabaseSchema ds)
	{
		String username;
		String url;
		if( ds.isUseSystem() )
		{
			url = dataSourceService.getSystemUrl();
			username = dataSourceService.getSystemUsername();
		}
		else
		{
			username = ds.getUsername();
			url = ds.getUrl();
		}
		ExtendedDialect dialect = dataSourceService.getDialect();
		return new TextLabel(username + " @ " + dialect.getDisplayNameForUrl(url));
	}

	public Label getNameLabel(DatabaseSchema ds)
	{
		String desc = ds.getDescription();
		if( !Check.isEmpty(desc) )
		{
			return new TextLabel(desc);
		}
		return getConnectionLabel(ds);
	}
}
