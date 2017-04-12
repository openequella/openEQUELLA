/*
 * Created on Jun 1, 2005
 */
package com.tle.beans.entity;

import java.util.Collection;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Type;

import com.dytech.edge.wizard.beans.DefaultWizardPage;
import com.tle.beans.entity.itemdef.ItemDefinition;

/**
 * @author jmaginnis
 */
@Entity
@AccessType("field")
public class PowerSearch extends BaseEntity
{
	private static final long serialVersionUID = 1L;

	@ManyToOne(fetch = FetchType.LAZY)
	@Index(name = "powerSearchSchema")
	private Schema schema;
	@ManyToMany
	private Collection<ItemDefinition> itemdefs;

	@Type(type = "xstream_immutable")
	private DefaultWizardPage wizard;

	public PowerSearch()
	{
		super();
	}

	public PowerSearch(long id)
	{
		this();
		setId(id);
	}

	public Collection<ItemDefinition> getItemdefs()
	{
		return itemdefs;
	}

	public void setItemdefs(Collection<ItemDefinition> itemdefs)
	{
		this.itemdefs = itemdefs;
	}

	public Schema getSchema()
	{
		return schema;
	}

	public void setSchema(Schema schema)
	{
		this.schema = schema;
	}

	public void setWizard(DefaultWizardPage wizard)
	{
		this.wizard = wizard;
	}

	public DefaultWizardPage getWizard()
	{
		return wizard;
	}
}
