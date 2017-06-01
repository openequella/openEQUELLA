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

package com.tle.web.htmleditor.tinymce;

import java.util.List;
import java.util.Map;

import com.tle.web.htmleditor.HtmlEditorModel;

/**
 * @author aholland
 */
public class TinyMceModel extends HtmlEditorModel
{
	private String baseUrl;
	private String actionUrl;
	private List<TinyMceAddOn> addOns;
	private Map<String, String> properties;
	private String languagesList;
	private String lang;
	private String directionality;

	public List<TinyMceAddOn> getAddOns()
	{
		return addOns;
	}

	public void setAddOns(List<TinyMceAddOn> addOns)
	{
		this.addOns = addOns;
	}

	public Map<String, String> getProperties()
	{
		return properties;
	}

	public void setProperties(Map<String, String> properties)
	{
		this.properties = properties;
	}

	public String getBaseUrl()
	{
		return baseUrl;
	}

	public void setBaseUrl(String baseUrl)
	{
		this.baseUrl = baseUrl;
	}

	public String getActionUrl()
	{
		return actionUrl;
	}

	public void setActionUrl(String actionUrl)
	{
		this.actionUrl = actionUrl;
	}

	public String getLanguagesList()
	{
		return languagesList;
	}

	public void setLanguagesList(String languagesList)
	{
		this.languagesList = languagesList;
	}

	public String getLang()
	{
		return lang;
	}

	public void setLang(String lang)
	{
		this.lang = lang;
	}

	public String getDirectionality()
	{
		return directionality;
	}

	public void setDirectionality(String directionality)
	{
		this.directionality = directionality;
	}
}
