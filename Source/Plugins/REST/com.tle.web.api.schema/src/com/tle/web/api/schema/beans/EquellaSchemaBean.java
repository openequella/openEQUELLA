/**
 * 
 */
package com.tle.web.api.schema.beans;

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlRootElement;

import com.tle.beans.entity.schema.Citation;
import com.tle.web.api.schema.interfaces.beans.SchemaBean;

/**
 * An EQUELLA-compatible variety of SchemaBean.
 * 
 * @author larry
 */
@XmlRootElement
public class EquellaSchemaBean extends SchemaBean
{
	private List<Citation> citations;

	private Map<String, String> exportTransformsMap;

	private Map<String, String> importTransformsMap;

	private String ownerUuid;

	private String serializedDefinition;

	public List<Citation> getCitations()
	{
		return citations;
	}

	public void setCitations(List<Citation> citations)
	{
		this.citations = citations;
	}

	public Map<String, String> getExportTransformsMap()
	{
		return exportTransformsMap;
	}

	public void setExportTransformsMap(Map<String, String> exportTransformsMap)
	{
		this.exportTransformsMap = exportTransformsMap;
	}

	public Map<String, String> getImportTransformsMap()
	{
		return importTransformsMap;
	}

	public void setImportTransformsMap(Map<String, String> importTransformsMap)
	{
		this.importTransformsMap = importTransformsMap;
	}

	public String getOwnerUuid()
	{
		return ownerUuid;
	}

	public void setOwnerUuid(String ownerUuid)
	{
		this.ownerUuid = ownerUuid;
	}

	public String getSerializedDefinition()
	{
		return serializedDefinition;
	}

	public void setSerializedDefinition(String serializedDefinition)
	{
		this.serializedDefinition = serializedDefinition;
	}
}
