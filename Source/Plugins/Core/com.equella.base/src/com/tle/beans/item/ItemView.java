package com.tle.beans.item;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;

import javax.persistence.*;
import javax.validation.constraints.Min;
import java.io.Serializable;
import java.util.Date;

@Entity
@AccessType("field")
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"item_id"})})
//@org.hibernate.annotations.NamedQueries({
//		@org.hibernate.annotations.NamedQuery(name = "increment", cacheable = true, query = "UPDATE ItemView iv SET iv.views = iv.views + 1 WHERE iv.item = :item")})
public class ItemView implements Serializable, ForeignItemKey
{
	private static final long serialVersionUID = 1;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@OneToOne
	@Index(name = "itemViewItemIndex")
	@JoinColumn(unique = true, nullable = false)
	private Item item;

	@Column(nullable = false)
	@Index(name = "itemViewLastViewedIndex")
	private Date lastViewed;

	@Column(nullable = false)
	@Min(0)
	private int views;


	public long getId()
	{
		return id;
	}

	public void setId(long id)
	{
		this.id = id;
	}

	public Item getItem()
	{
		return item;
	}

	@Override
	public void setItem(Item item)
	{
		this.item = item;
	}

	public Date getLastViewed()
	{
		return lastViewed;
	}

	public void setLastViewed(Date lastViewed)
	{
		this.lastViewed = lastViewed;
	}

	public int getViews()
	{
		return views;
	}

	public void setViews(int views)
	{
		this.views = views;
	}
}
