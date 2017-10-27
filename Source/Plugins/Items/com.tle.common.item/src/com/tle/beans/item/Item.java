/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.beans.item;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.Min;

import org.hibernate.annotations.AccessType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.Type;

import com.thoughtworks.xstream.annotations.XStreamOmitField;
import com.tle.beans.IdCloneable;
import com.tle.beans.Institution;
import com.tle.beans.ReferencedURL;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.ItemNavigationNode;
import com.tle.beans.item.attachments.NavigationSettings;
import com.tle.beans.security.SharePass;
import com.tle.common.Check;
import com.tle.common.Check.FieldEquality;
import com.tle.common.Pair;
import com.tle.common.util.UnmodifiableIterable;

/**
 * @author Nicholas Read
 */
@Entity
@AccessType("field")
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"institution_id", "uuid", "version"})})
public class Item implements Serializable, IdCloneable, FieldEquality<Item>, IItem<Attachment>
{
	private static final long serialVersionUID = 1;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private long id;

	@JoinColumn(nullable = false)
	@ManyToOne(fetch = FetchType.LAZY)
	@Index(name = "itemInstitutionIndex")
	@XStreamOmitField
	private Institution institution;
	@Index(name = "itemUuidIndex")
	@Column(length = 40)
	private String uuid;
	@Index(name = "itemVersionIndex")
	private int version;
	@Type(type = "blankable")
	@Column(length = 255, nullable = false)
	private String owner;

	@XStreamOmitField
	@SuppressWarnings("unused")
	private transient Set<String> usersNotified;

	@Column(nullable = false)
	private Date dateModified;
	@Column(nullable = false)
	private Date dateCreated;
	@Index(name = "itemDateForIndexIndex")
	@Column(nullable = false)
	private Date dateForIndex;
	private float rating = -1;
	private boolean moderating;

	// Solves issues surrounding Java 1.5/1.4 serialisation of enums
	@Column(length = 16)
	private String status;
	private transient ItemStatus statusEnum;

	// Extra security metadata targets to apply to this item
	@Type(type = "csv")
	@Column(length = 512)
	private List<String> metadataSecurityTargets;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@IndexColumn(name = "attindex", nullable = false)
	@Fetch(value = FetchMode.SUBSELECT)
	@JoinColumn(name = "item_id", nullable = false)
	private List<Attachment> attachments = new ArrayList<Attachment>();

	@Type(type = "xstream_immutable")
	private List<Pair<LanguageBundle, LanguageBundle>> searchDetails;

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(nullable = false)
	@Index(name = "itemItemDefinition")
	@Fetch(value = FetchMode.JOIN)
	private ItemDefinition itemDefinition;

	@ElementCollection(fetch = FetchType.LAZY)
	@Fetch(value = FetchMode.SUBSELECT)
	@Column(name = "element")
	private Set<String> collaborators = new HashSet<String>();

	@ManyToMany(fetch = FetchType.LAZY)
	@Fetch(value = FetchMode.SUBSELECT)
	private List<ReferencedURL> referencedUrls = new ArrayList<ReferencedURL>();

	@OneToOne(optional = true, fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@Index(name = "itemDrmSettings")
	private DrmSettings drmSettings;

	@ManyToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@Index(name = "itemModerationStatus")
	private ModerationStatus moderation;

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@IndexColumn(name = "histindex")
	private List<HistoryEvent> history = new ArrayList<HistoryEvent>();

	// Needs to here to be removed when an Item is deleted
	@OneToMany(fetch = FetchType.LAZY, mappedBy = "item", cascade = CascadeType.REMOVE)
	@Fetch(value = FetchMode.SUBSELECT)
	private List<Comment> comments;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "item", cascade = CascadeType.REMOVE)
	@Fetch(value = FetchMode.SUBSELECT)
	private List<SharePass> sharePasses;

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "item", cascade = CascadeType.REMOVE)
	@Fetch(value = FetchMode.SUBSELECT)
	private List<DrmAcceptance> acceptances = new ArrayList<DrmAcceptance>();

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
	@Fetch(value = FetchMode.SUBSELECT)
	@JoinColumn(name = "item_id", nullable = false)
	private List<ItemNavigationNode> treeNodes = new ArrayList<ItemNavigationNode>();

	@ElementCollection(fetch = FetchType.LAZY)
	@Fetch(value = FetchMode.SUBSELECT)
	@Column(name = "element")
	private Set<String> notifications = new HashSet<String>();

	@Transient
	@XStreamOmitField
	private boolean newItem;

	@Deprecated
	@Lob
	private String curricula;

	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@Index(name = "itemName")
	private LanguageBundle name;

	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@Index(name = "itemDescription")
	private LanguageBundle description;

	@JoinColumn(nullable = false)
	@Min(0)
	private long totalFileSize;

	@Embedded
	private NavigationSettings navigationSettings = new NavigationSettings();

	@Index(name = "itemItemXmlIndex")
	@OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(unique = true, nullable = false)
	@XStreamOmitField
	private ItemXml itemXml;

	@SuppressWarnings("nls")
	@Column(length = 512, nullable = false)
	private String thumb = "initial";

	public Item()
	{
		super();
	}

	@Override
	public long getId()
	{
		return id;
	}

	public String getIdString()
	{
		return getItemId().toString();
	}

	@Override
	public ItemId getItemId()
	{
		return new ItemId(uuid, version);
	}

	@Override
	public void setId(long key)
	{
		this.id = key;
	}

	@Override
	public String getUuid()
	{
		return uuid;
	}

	public void setUuid(String id)
	{
		this.uuid = id;
	}

	@Override
	public int getVersion()
	{
		return version;
	}

	public void setVersion(int version)
	{
		this.version = version;
	}

	public ItemDefinition getItemDefinition()
	{
		return itemDefinition;
	}

	public void setItemDefinition(ItemDefinition itemDefinition)
	{
		this.itemDefinition = itemDefinition;
	}

	public String getOwner()
	{
		return owner;
	}

	public void setOwner(String owner)
	{
		this.owner = owner;
	}

	public List<Pair<LanguageBundle, LanguageBundle>> getSearchDetails()
	{
		return searchDetails;
	}

	public void setSearchDetails(List<Pair<LanguageBundle, LanguageBundle>> searchDetails)
	{
		this.searchDetails = searchDetails;
	}

	@Override
	public Date getDateCreated()
	{
		return dateCreated;
	}

	public void setDateCreated(Date dateCreated)
	{
		this.dateCreated = dateCreated;
	}

	@Override
	public Date getDateModified()
	{
		return dateModified;
	}

	public void setDateModified(Date dateModified)
	{
		this.dateModified = dateModified;
	}

	public Date getDateForIndex()
	{
		return dateForIndex;
	}

	public void setDateForIndex(Date dateForIndex)
	{
		this.dateForIndex = dateForIndex;
	}

	@Override
	public float getRating()
	{
		return rating;
	}

	public void setRating(float rating)
	{
		this.rating = rating;
	}

	public ItemStatus getStatus()
	{
		if( statusEnum == null && status != null )
		{
			statusEnum = ItemStatus.valueOf(status);
		}
		return statusEnum;
	}

	public void setStatus(ItemStatus newStatus)
	{
		this.statusEnum = newStatus;
		this.status = newStatus != null ? newStatus.name() : null;
	}

	@Override
	public List<Attachment> getAttachments()
	{
		return attachments;
	}

	@Override
	public UnmodifiableIterable<Attachment> getAttachmentsUnmodifiable()
	{
		return new UnmodifiableIterable<Attachment>(attachments);
	}

	public void setAttachments(List<Attachment> attachments)
	{
		this.attachments = attachments;
	}

	@Override
	public String toString()
	{
		return Long.toString(id);
	}

	public List<HistoryEvent> getHistory()
	{
		if( history == null )
		{
			history = new ArrayList<HistoryEvent>();
		}
		return history;
	}

	public void setHistory(List<HistoryEvent> history)
	{
		this.history = history;
	}

	public List<ReferencedURL> getReferencedUrls()
	{
		return referencedUrls;
	}

	public void setReferencedUrls(List<ReferencedURL> referencedUrls)
	{
		this.referencedUrls = referencedUrls;
	}

	public ModerationStatus getModeration()
	{
		return moderation;
	}

	public void setModeration(ModerationStatus moderation)
	{
		this.moderation = moderation;
	}

	public boolean isNewItem()
	{
		return newItem;
	}

	public void setNewItem(boolean newItem)
	{
		this.newItem = newItem;
	}

	public Set<String> getCollaborators()
	{
		return collaborators;
	}

	public void setCollaborators(Set<String> collaborators)
	{
		this.collaborators = collaborators;
	}

	public DrmSettings getDrmSettings()
	{
		return drmSettings;
	}

	public void setDrmSettings(DrmSettings drmSettings)
	{
		this.drmSettings = drmSettings;
	}

	public boolean isModerating()
	{
		return moderating;
	}

	public void setModerating(boolean moderating)
	{
		this.moderating = moderating;
	}

	public Institution getInstitution()
	{
		return institution;
	}

	public void setInstitution(Institution institution)
	{
		this.institution = institution;
	}

	public List<String> getMetadataSecurityTargets()
	{
		return metadataSecurityTargets;
	}

	public void setMetadataSecurityTargets(List<String> extraTargets)
	{
		this.metadataSecurityTargets = extraTargets;
	}

	public List<DrmAcceptance> getAcceptances()
	{
		return acceptances;
	}

	public void setAcceptances(List<DrmAcceptance> acceptances)
	{
		this.acceptances = acceptances;
	}

	@Override
	public LanguageBundle getDescription()
	{
		return description;
	}

	public void setDescription(LanguageBundle description)
	{
		this.description = description;
	}

	@Override
	public LanguageBundle getName()
	{
		return name;
	}

	public void setName(LanguageBundle name)
	{
		this.name = name;
	}

	public long getTotalFileSize()
	{
		return totalFileSize;
	}

	public void setTotalFileSize(long totalFileSize)
	{
		this.totalFileSize = totalFileSize;
	}

	public List<Comment> getComments()
	{
		return comments;
	}

	public List<SharePass> getSharePasses()
	{
		return sharePasses;
	}

	@Override
	public List<ItemNavigationNode> getTreeNodes()
	{
		return treeNodes;
	}

	public void setTreeNodes(List<ItemNavigationNode> treeNodes)
	{
		this.treeNodes = treeNodes;
	}

	@Override
	public NavigationSettings getNavigationSettings()
	{
		if( navigationSettings == null )
		{
			navigationSettings = new NavigationSettings();
		}
		return navigationSettings;
	}

	public void setNavigationSettings(NavigationSettings navigationSettings)
	{
		this.navigationSettings = navigationSettings;
	}

	public ItemXml getItemXml()
	{
		return itemXml;
	}

	public void setItemXml(ItemXml itemXml)
	{
		this.itemXml = itemXml;
	}

	@Override
	public int hashCode()
	{
		return Check.getHashCode(id);
	}

	@Override
	public boolean equals(Object obj)
	{
		return Check.commonEquals(this, obj);
	}

	@Override
	public boolean checkFields(Item rhs)
	{
		return id == rhs.getId();
	}

	public Set<String> getNotifications()
	{
		return notifications;
	}

	public void setNotifications(Set<String> notifications)
	{
		this.notifications = notifications;
	}

	public void setComments(List<Comment> comments)
	{
		this.comments = comments;
	}

	public void setSharePasses(List<SharePass> sharePasses)
	{
		this.sharePasses = sharePasses;
	}

	public String getThumb()
	{
		return thumb;
	}

	public void setThumb(String thumb)
	{
		this.thumb = thumb;
	}
}
