package com.tle.beans.item.attachments;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;

import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.tle.beans.IdCloneable;

@Entity
@AccessType("field")
public class ItemNavigationTab implements Serializable, Cloneable, IdCloneable
{
	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@ManyToOne
	@XStreamOmitField
	@Index(name = "itemNavTabNode")
	@JoinColumn(name = "node_id", insertable = false, updatable = false, nullable = false)
	private ItemNavigationNode node;

	@Lob
	private String name;

	@ManyToOne
	@Index(name = "itemNavTabAttachment")
	private Attachment attachment;

	@Column(length = 100)
	private String viewer;

	public ItemNavigationTab()
	{
		// nothing
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public Attachment getAttachment()
	{
		return attachment;
	}

	public void setAttachment(Attachment attachment)
	{
		this.attachment = attachment;
	}

	public String getViewer()
	{
		return viewer;
	}

	public void setViewer(String viewer)
	{
		this.viewer = viewer;
	}

	@Override
	public Object clone() throws CloneNotSupportedException
	{
		return super.clone();
	}

	@Override
	public long getId()
	{
		return id;
	}

	@Override
	public void setId(long id)
	{
		this.id = id;
	}

	public ItemNavigationNode getNode()
	{
		return node;
	}

	public void setNode(ItemNavigationNode node)
	{
		this.node = node;
	}
}
