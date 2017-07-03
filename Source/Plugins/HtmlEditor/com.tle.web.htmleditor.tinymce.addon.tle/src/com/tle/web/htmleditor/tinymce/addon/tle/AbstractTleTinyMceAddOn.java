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

package com.tle.web.htmleditor.tinymce.addon.tle;

import javax.inject.Inject;

import com.tle.common.PathUtils;
import com.tle.core.institution.InstitutionService;
import com.tle.web.htmleditor.tinymce.TinyMceAddOn;
import com.tle.web.resources.PluginResourceHelper;
import com.tle.web.sections.SectionTree;

/**
 * @author aholland
 */
@SuppressWarnings("nls")
public abstract class AbstractTleTinyMceAddOn implements TinyMceAddOn
{
	@Inject
	private InstitutionService institutionService;

	@Override
	public String getBaseUrl()
	{
		return institutionService.institutionalise(getResourceHelper().url("scripts/" + getId()));
	}

	@Override
	public String getJsUrl()
	{
		return PathUtils.urlPath(getBaseUrl(), "editor_plugin_src.js");
	}

	protected abstract PluginResourceHelper getResourceHelper();

	@Override
	public boolean isEnabled()
	{
		return true;
	}

	@Override
	public void register(SectionTree tree, String parentId)
	{
		// don't register anything by default
	}
}
