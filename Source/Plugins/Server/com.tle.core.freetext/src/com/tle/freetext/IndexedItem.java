/*
 * Created on Aug 31, 2005
 */
package com.tle.freetext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Fieldable;

import com.dytech.devlib.PropBagEx;
import com.google.inject.assistedinject.Assisted;
import com.google.inject.assistedinject.AssistedInject;
import com.tle.beans.Institution;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemIdKey;
import com.tle.beans.item.ItemSelect;
import com.tle.beans.security.ACLEntryMapping;
import com.tle.common.security.SecurityConstants;
import com.tle.common.security.SecurityConstants.Recipient;
import com.tle.core.security.TLEAclManager;
import com.tle.freetext.index.ItemIndex;

public class IndexedItem
{
	@Inject
	private StandardIndexer standardIndexer;
	@Inject
	private TLEAclManager aclManager;

	private final ItemIdKey id;

	// Will be true after background indexer has loaded it
	private boolean prepared;

	// standard indexing objects
	private Item item;
	private PropBagEx itemxml;

	// cache for basic fields which should go in every index
	private List<Fieldable> basicFields;

	private Document itemdoc = new Document();
	private final Map<String, List<Document>> indexDocMap = new HashMap<String, List<Document>>();
	private final Map<String, List<Fieldable>> aclMap = new HashMap<String, List<Fieldable>>();
	private final ItemSelect itemSelect = new ItemSelect();
	private long expectedReturnTime = Long.MAX_VALUE;
	private boolean finishedFastIndexing;
	private boolean finishedAllIndexing;
	private boolean add;
	private boolean onIndexList;
	private boolean indexed;
	private boolean noLongerCurrent;
	private boolean newSearcherRequired;
	private Throwable error;
	private boolean deadlineAfterStart = true;
	private long timeAfterStart = TimeUnit.SECONDS.toMillis(10);

	private final Map<Object, Object> attributes = new HashMap<Object, Object>();
	private final Institution institution;

	@AssistedInject
	public IndexedItem(@Assisted ItemIdKey id, @Assisted Institution institution)
	{
		this.id = id;
		this.institution = institution;
	}

	public List<Document> getDocumentsForIndex(String index)
	{
		List<Document> docList = indexDocMap.get(index);
		if( docList == null )
		{
			docList = new ArrayList<Document>();
			indexDocMap.put(index, docList);
		}
		return docList;
	}

	public List<Fieldable> getBasicFields()
	{
		if( basicFields == null )
		{
			basicFields = standardIndexer.getBasicFields(this);
		}
		return basicFields;
	}

	public Document getItemdoc()
	{
		return itemdoc;
	}

	public Item getItem()
	{
		return item;
	}

	public Institution getInstitution()
	{
		return institution;
	}

	public FullIdKey getId()
	{
		return new FullIdKey(id.getKey(), institution.getUniqueId());
	}

	public void setItem(Item item)
	{
		this.item = item;
	}

	public void setItemXml(PropBagEx itemXml)
	{
		this.itemxml = itemXml;
	}

	public PropBagEx getItemxml()
	{
		return itemxml;
	}

	public boolean isAdd()
	{
		return add;
	}

	public void setAdd(boolean add)
	{
		this.add = add;
	}

	public long getExpectedReturnTime()
	{
		return expectedReturnTime;
	}

	public void setExpectedReturnTime(long expectedReturnTime)
	{
		this.expectedReturnTime = expectedReturnTime;
	}

	public boolean isOnIndexList()
	{
		return onIndexList;
	}

	public void setOnIndexList(boolean onIndexList)
	{
		this.onIndexList = onIndexList;
	}

	public long getTimeAfterStart()
	{
		return timeAfterStart;
	}

	public boolean isDeadlineAfterStart()
	{
		return deadlineAfterStart;
	}

	public void setDeadlineAfterStart(boolean deadlineAfterStart)
	{
		this.deadlineAfterStart = deadlineAfterStart;
	}

	public void setTimeAfterStart(long timeAfterStart)
	{
		this.timeAfterStart = timeAfterStart;
	}

	public boolean isNewSearcherRequired()
	{
		return newSearcherRequired;
	}

	public void setNewSearcherRequired(boolean b)
	{
		this.newSearcherRequired = b;
	}

	public synchronized void setError(Throwable t)
	{
		error = t;
		notifyAll();
	}

	// Sonar likes the get(...)s to be synchronised if the set(...) is
	public synchronized boolean isErrored()
	{
		return error != null;
	}

	public synchronized Throwable getError()
	{
		return error;
	}

	@Override
	public int hashCode()
	{
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj)
	{
		if( this == obj )
		{
			return true;
		}

		if( !(obj instanceof IndexedItem) )
		{
			return false;
		}

		return ((IndexedItem) obj).id.equals(id);
	}

	public TLEAclManager getAclManager()
	{
		return aclManager;
	}

	public List<Fieldable> getACLEntries(String privilege)
	{
		return aclMap.get(privilege);
	}

	public void prepareACLEntries(IndexedItem indexedItem, String privilege)
	{
		prepareACLEntries(indexedItem.getItem(), privilege, ItemIndex.convertStdPriv(privilege));
	}

	public List<Fieldable> queryACLEntries(Object domainObject, String privilege, String prefix)
	{
		List<Fieldable> fields = new ArrayList<Fieldable>();
		List<ACLEntryMapping> allEntriesForObject = aclManager.getAllEntriesForObject(domainObject, privilege);

		int i = 0;
		for( ACLEntryMapping entry : allEntriesForObject )
		{
			char grant = entry.getGrant();
			boolean all = entry.getExpression().equals(SecurityConstants.getRecipient(Recipient.EVERYONE));

			if( all && grant == SecurityConstants.REVOKE )
			{
				break;
			}

			fields.add(AbstractIndexingExtension.keyword(prefix + entry.getId(), String.format("%03d%c", i, grant))); //$NON-NLS-1$

			if( all )
			{
				break;
			}

			i++;
		}
		return fields;
	}

	public void prepareACLEntries(Object domainObject, String privilege, String prefix)
	{
		if( aclMap.containsKey(privilege) )
		{
			return;
		}
		aclMap.put(privilege, queryACLEntries(domainObject, privilege, prefix));
	}

	public ItemSelect getItemSelect()
	{
		return itemSelect;
	}

	public boolean isIndexed()
	{
		return indexed;
	}

	public synchronized void setIndexed(boolean indexed)
	{
		this.indexed = indexed;
		notifyAll();
	}

	@SuppressWarnings("unchecked")
	public <T> T getAttribute(Object key)
	{
		return (T) attributes.get(key);
	}

	public void setAttribute(Object key, Object val)
	{
		attributes.put(key, val);
	}

	public boolean isPrepared()
	{
		return prepared;
	}

	public void setPrepared(boolean prepared)
	{
		this.prepared = prepared;
	}

	public boolean isFinishedFastIndexing()
	{
		return finishedFastIndexing;
	}

	public void setFinishedFastIndexing(boolean finishedFastIndexing)
	{
		this.finishedFastIndexing = finishedFastIndexing;
	}

	public boolean isFinishedAllIndexing()
	{
		return finishedAllIndexing;
	}

	public void setFinishedAllIndexing(boolean finishedAllIndexing)
	{
		this.finishedAllIndexing = finishedAllIndexing;
	}

	public ItemIdKey getItemIdKey()
	{
		return id;
	}

	public boolean isNoLongerCurrent()
	{
		return noLongerCurrent;
	}

	public synchronized void setNoLongerCurrent(boolean noLongerCurrent)
	{
		this.noLongerCurrent = noLongerCurrent;
		notifyAll();
	}

}
