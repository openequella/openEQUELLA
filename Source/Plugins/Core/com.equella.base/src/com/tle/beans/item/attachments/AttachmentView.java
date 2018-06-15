package com.tle.beans.item.attachments;

import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.tle.beans.item.attachments.Attachment;
import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Index;

import javax.persistence.*;
import javax.validation.constraints.Min;
import java.io.Serializable;
import java.util.Date;

@Entity
@AccessType("field")
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"attachment_id"})})
//@org.hibernate.annotations.NamedQueries({
//		@org.hibernate.annotations.NamedQuery(name = "increment", cacheable = true, query = "UPDATE AttachmentView iv SET iv.views = iv.views + 1 WHERE iv.attachment = :attachment")})
public class AttachmentView implements Serializable
{
	private static final long serialVersionUID = 1;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@OneToOne
	@Index(name = "attachmentViewAttachmentIndex")
	@JoinColumn(unique = true, nullable = false)
	private Attachment attachment;

	@Column(nullable = false)
	@Index(name = "attachmentViewLastViewedIndex")
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

	public Attachment getAttachment()
	{
		return attachment;
	}

	public void setAttachment(Attachment attachment)
	{
		this.attachment = attachment;
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
