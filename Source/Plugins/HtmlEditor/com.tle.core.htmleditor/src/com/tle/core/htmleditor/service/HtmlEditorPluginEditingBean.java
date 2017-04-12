package com.tle.core.htmleditor.service;

import com.tle.core.services.entity.EntityEditingBean;

/**
 * @author Aaron
 */
public class HtmlEditorPluginEditingBean extends EntityEditingBean
{
	private static final long serialVersionUID = 1L;

	private String pluginId;
	private String author;
	private String type;
	private String buttons;
	private String config;
	private String extra;
	private String clientJs;
	private String serverJs;

	public String getPluginId()
	{
		return pluginId;
	}

	public void setPluginId(String pluginId)
	{
		this.pluginId = pluginId;
	}

	public String getAuthor()
	{
		return author;
	}

	public void setAuthor(String author)
	{
		this.author = author;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getButtons()
	{
		return buttons;
	}

	public void setButtons(String buttons)
	{
		this.buttons = buttons;
	}

	public String getConfig()
	{
		return config;
	}

	public void setConfig(String config)
	{
		this.config = config;
	}

	public String getExtra()
	{
		return extra;
	}

	public void setExtra(String extra)
	{
		this.extra = extra;
	}

	public String getClientJs()
	{
		return clientJs;
	}

	public void setClientJs(String clientJs)
	{
		this.clientJs = clientJs;
	}

	public String getServerJs()
	{
		return serverJs;
	}

	public void setServerJs(String serverJs)
	{
		this.serverJs = serverJs;
	}
}
