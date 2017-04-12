/*
 * Created on Oct 14, 2005
 */
package com.tle.beans.entity;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;

@Embeddable
public class SchemaTransform implements Serializable
{
	private static final long serialVersionUID = 1L;

	private String filename;
	private String type;

	public SchemaTransform()
	{
		super();
	}

	@Column(length = 80)
	public String getFilename()
	{
		return filename;
	}

	public void setFilename(String filename)
	{
		this.filename = filename;
	}

	@Column(length = 25)
	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}
}
