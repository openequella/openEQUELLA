/*
 * Created on Apr 13, 2005
 */
package com.tle.core.harvester.oai.data;

@SuppressWarnings("nls")
public class MetadataFormat
{
	private String metadataPrefix;
	private String schema;
	private String metadataNamespace;

	public MetadataFormat()
	{
		super();
	}

	public MetadataFormat(String prefix, String scheme, String namespace)
	{
		this.schema = scheme;
		this.metadataNamespace = namespace;
		this.metadataPrefix = prefix;
	}

	public String getMetadataNamespace()
	{
		return metadataNamespace;
	}

	public void setMetadataNamespace(String metadataNamespace)
	{
		this.metadataNamespace = metadataNamespace;
	}

	public String getMetadataPrefix()
	{
		return metadataPrefix;
	}

	public void setMetadataPrefix(String metadataPrefix)
	{
		this.metadataPrefix = metadataPrefix;
	}

	public String getSchema()
	{
		return schema;
	}

	public void setSchema(String schema)
	{
		this.schema = schema;
	}

	public static final MetadataFormat DUBLIN_CORE_FORMAT = new MetadataFormat("oai_dc",
		"http://www.openarchives.org/OAI/2.0/oai_dc.xsd", "http://www.openarchives.org/OAI/2.0/oai_dc/");
}
