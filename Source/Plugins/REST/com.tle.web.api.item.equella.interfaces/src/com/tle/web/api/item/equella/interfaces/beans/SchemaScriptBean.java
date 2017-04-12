package com.tle.web.api.item.equella.interfaces.beans;

import javax.xml.bind.annotation.XmlRootElement;

import com.tle.common.interfaces.BaseEntityReference;
import com.tle.web.api.interfaces.beans.AbstractExtendableBean;

/**
 * @author larry
 */
@XmlRootElement
public class SchemaScriptBean extends AbstractExtendableBean
{
	private BaseEntityReference schema;
	private String script;

	public BaseEntityReference getSchema()
	{
		return schema;
	}

	public void setSchema(BaseEntityReference schema)
	{
		this.schema = schema;
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
