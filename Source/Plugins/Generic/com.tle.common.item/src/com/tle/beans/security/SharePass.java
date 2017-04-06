package com.tle.beans.security;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;

import com.tle.beans.Institution;
import com.tle.beans.item.ForeignItemKey;
import com.tle.beans.item.Item;
import com.tle.common.security.SecurityConstants;

@Entity
@AccessType("field")
public class SharePass implements Serializable, ForeignItemKey
{
	private static final long serialVersionUID = 1L;

	public enum SharePassPrivilege
	{
		VIEW(SecurityConstants.VIEW_ITEM);

		private final String priv;

		private SharePassPrivilege(String priv)
		{
			this.priv = priv;
		}

		public String getPriv()
		{
			return priv;
		}
	}

	@Id
	@Column(length = 40)
	private String id;

	@JoinColumn(nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	@Index(name = "sharePassInstitution")
	private Institution institution;

	@JoinColumn(nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	@Index(name = "sharePassItem")
	private Item item;

	@Column(length = 150, nullable = false)
	private String emailAddress;

	private boolean activated;
	private Date started;
	private Date expiry;

	@Column(length = 20, nullable = false)
	private String privilege;
	private transient SharePassPrivilege privilegeEnum;

	@Column(length = 30)
	private String password;

	@Column(nullable = false)
	private String creator;

	public SharePass()
	{
		super();
	}

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public String getCreator()
	{
		return creator;
	}

	public void setCreator(String creator)
	{
		this.creator = creator;
	}

	public String getEmailAddress()
	{
		return emailAddress;
	}

	public void setEmailAddress(String emailAddress)
	{
		this.emailAddress = emailAddress;
	}

	public Date getExpiry()
	{
		return expiry;
	}

	public void setExpiry(Date expiry)
	{
		this.expiry = expiry;
	}

	public Institution getInstitution()
	{
		return institution;
	}

	public void setInstitution(Institution institution)
	{
		this.institution = institution;
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

	public SharePassPrivilege getPrivilege()
	{
		if( privilegeEnum == null && privilege != null )
		{
			privilegeEnum = SharePassPrivilege.valueOf(privilege);
		}
		return privilegeEnum;
	}

	public void setPrivilege(SharePassPrivilege privilege)
	{
		this.privilegeEnum = privilege;
		this.privilege = privilege != null ? privilege.name() : null;
	}

	public boolean isActivated()
	{
		return activated;
	}

	public void setActivated(boolean activated)
	{
		this.activated = activated;
	}

	public Date getStarted()
	{
		return started;
	}

	public void setStarted(Date started)
	{
		this.started = started;
	}

	public String getPassword()
	{
		return password;
	}

	public void setPassword(String password)
	{
		this.password = password;
	}
}
