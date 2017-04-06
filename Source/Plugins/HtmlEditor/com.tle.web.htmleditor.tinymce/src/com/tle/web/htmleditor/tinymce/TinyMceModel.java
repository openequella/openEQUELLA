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
