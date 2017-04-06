/*
 * Created on Jun 23, 2005
 */

package com.tle.beans.entity.schema;

import java.io.Serializable;

import javax.persistence.Embeddable;

@Embeddable
public class Citation implements Serializable
{
	private static final long serialVersionUID = 1;

	private String name;
	private String transformation;

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getTransformation()
	{
		return transformation;
	}

	public void setTransformation(String transformation)
	{
		this.transformation = transformation;
	}
}
