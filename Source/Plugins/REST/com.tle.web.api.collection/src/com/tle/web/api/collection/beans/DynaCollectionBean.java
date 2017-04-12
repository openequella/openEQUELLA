package com.tle.web.api.collection.beans;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import com.tle.web.api.interfaces.beans.BaseEntityBean;
import com.tle.web.api.item.equella.interfaces.beans.ItemDefinitionScriptBean;
import com.tle.web.api.item.equella.interfaces.beans.SchemaScriptBean;

/**
 * @author larry
 */
@XmlRootElement
public class DynaCollectionBean extends BaseEntityBean
{
	private String freetext;
	private List<String> usages;
	private List<ItemDefinitionScriptBean> collectionScripts;
	private List<SchemaScriptBean> schemaScripts;
	private String virtualisationValue;
	private String virtualisationPath;
	private String virtualisationId;
	private String compoundId;

	public String getFreetext()
	{
		return freetext;
	}

	public void setFreetext(String freetext)
	{
		this.freetext = freetext;
	}

	public List<String> getUsages()
	{
		return usages;
	}

	public void setUsages(List<String> usages)
	{
		this.usages = usages;
	}

	public List<ItemDefinitionScriptBean> getCollectionScripts()
	{
		return collectionScripts;
	}

	public void setCollectionScripts(List<ItemDefinitionScriptBean> collectionScripts)
	{
		this.collectionScripts = collectionScripts;
	}

	public List<SchemaScriptBean> getSchemaScripts()
	{
		return schemaScripts;
	}

	public void setSchemaScripts(List<SchemaScriptBean> schemaScripts)
	{
		this.schemaScripts = schemaScripts;
	}

	public String getVirtualisationValue()
	{
		return virtualisationValue;
	}

	public void setVirtualisationValue(String virtualisationValue)
	{
		this.virtualisationValue = virtualisationValue;
	}

	public String getVirtualisationPath()
	{
		return virtualisationPath;
	}

	public void setVirtualisationPath(String virtualisationPath)
	{
		this.virtualisationPath = virtualisationPath;
	}

	public String getVirtualisationId()
	{
		return virtualisationId;
	}

	public void setVirtualisationId(String virtualisationId)
	{
		this.virtualisationId = virtualisationId;
	}

	public String getCompoundId()
	{
		return compoundId;
	}

	public void setCompoundId(String compoundId)
	{
		this.compoundId = compoundId;
	}
}
