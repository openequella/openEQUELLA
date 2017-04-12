package com.tle.web.api.item.equella.interfaces.beans;

import javax.xml.bind.annotation.XmlRootElement;

import com.tle.common.interfaces.BaseEntityReference;
import com.tle.web.api.interfaces.beans.AbstractExtendableBean;

/**
 * @author larry
 */
@XmlRootElement
public class ItemDefinitionScriptBean extends AbstractExtendableBean
{
	private BaseEntityReference collection;
	private String script;

	public BaseEntityReference getCollection()
	{
		return collection;
	}

	public void setCollection(BaseEntityReference collection)
	{
		this.collection = collection;
	}

	public String getScript()
	{
		return script;
	}

	public void setScript(String script)
	{
		this.script = script;
	}
}
