package com.tle.beans;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.AccessType;

import com.tle.beans.entity.Schema;
import com.tle.common.Check;
import com.tle.common.Check.FieldEquality;

/**
 * @author Nicholas Read
 */
@Embeddable
@AccessType("field")
public class SchemaScript implements EntityScript<Schema>, FieldEquality<SchemaScript>
{
	private static final long serialVersionUID = 1L;

	@JoinColumn(nullable = false)
	@ManyToOne
	private Schema entity;

	@Lob
	@Column(name = "script")
	private String script;

	public SchemaScript()
	{
		super();
	}

	public SchemaScript(Schema entity, String script)
	{
		setEntity(entity);
		setScript(script);
	}

	@Override
	public Schema getEntity()
	{
		return entity;
	}

	@Override
	public String getScript()
	{
		return script;
	}

	@Override
	public void setEntity(Schema entity)
	{
		this.entity = entity;
	}

	@Override
	public void setScript(String script)
	{
		this.script = script;
	}

	@Override
	public boolean equals(Object obj)
	{
		return Check.commonEquals(this, obj);
	}

	@Override
	public boolean checkFields(SchemaScript rhs)
	{
		return Check.bothNullOrDeepEqual(entity, rhs.entity) && Check.bothNullOrDeepEqual(script, rhs.script);
	}

	@Override
	public int hashCode()
	{
		return Check.getHashCode(entity, script);
	}
}
