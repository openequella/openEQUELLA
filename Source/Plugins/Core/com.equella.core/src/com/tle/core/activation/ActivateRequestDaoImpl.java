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

package com.tle.core.activation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.orm.hibernate3.HibernateCallback;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tle.beans.activation.ActivateRequest;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemIdKey;
import com.tle.beans.item.cal.request.CourseInfo;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.auditlog.AuditLogService;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericDaoImpl;

/**
 * @author Nicholas Read
 */
@Bind(ActivateRequestDao.class)
@Singleton
@SuppressWarnings({"unchecked", "nls"})
public class ActivateRequestDaoImpl extends GenericDaoImpl<ActivateRequest, Long> implements ActivateRequestDao
{
	private static final String AUDIT_CATEGORY_TYPE = "ACTIVATION";

	@Inject
	private AuditLogService auditLogService;

	public ActivateRequestDaoImpl()
	{
		super(ActivateRequest.class);
	}

	@Override
	@Transactional
	public List<ActivateRequest> getAllRequestsForDateRange(String type, Item item, Date start, Date end)
	{
		return getHibernateTemplate().findByNamedParam(
			"from ActivateRequest r"
				+ " where r.type = :type and r.item = :item and not (r.from > :until or r.until < :from)",
			new String[]{"type", "item", "from", "until"}, new Object[]{type, item, start, end});
	}

	@Override
	@Transactional
	public List<ActivateRequest> getAllRequests(String type, Item item)
	{
		return getHibernateTemplate().findByNamedParam("from ActivateRequest where type = :type and item = :item",
			new String[]{"type", "item"}, new Object[]{type, item});
	}

	@Override
	@Transactional
	public List<ActivateRequest> getAllRequests(Item item)
	{
		return new ArrayList<ActivateRequest>(getAllRequests(item.getId()));
	}

	@Override
	@Transactional
	public List<ActivateRequest> getAllRequestsForItems(String type, Collection<Item> items)
	{
		if( items.isEmpty() )
		{
			return Collections.emptyList();
		}
		return getHibernateTemplate().findByNamedParam("from ActivateRequest where type = :type and item in (:items)",
			new String[]{"type", "items"}, new Object[]{type, items});
	}

	@Override
	@Transactional
	public List<ActivateRequest> getAllActiveRequests(String type, Item item)
	{
		return getHibernateTemplate().findByNamedParam(
			"from ActivateRequest where type = :type and item = :item and status = :status",
			new String[]{"type", "item", "status"}, new Object[]{type, item, ActivateRequest.TYPE_ACTIVE});
	}

	@Override
	@Transactional
	public List<ActivateRequest> getAllActiveAndPendingRequests(String type, String attachmentUuid)
	{
		return getHibernateTemplate().findByNamedParam(
			"from ActivateRequest req where req.type = :type and req.attachment = :attachmentUuid and req.status != :status and req.item.institution = :institution",
			new String[]{"type", "attachmentUuid", "status", "institution"},
			new Object[]{type, attachmentUuid, ActivateRequest.TYPE_INACTIVE, CurrentInstitution.get()});
	}

	@Override
	@Transactional
	public List<ActivateRequest> getAllActiveRequestsForItems(String type, Collection<Item> items)
	{
		if( items.isEmpty() )
		{
			return Collections.emptyList();
		}

		return getHibernateTemplate().findByNamedParam(
			"from ActivateRequest where type = :type and item in (:items) and status = :status",
			new String[]{"type", "items", "status"}, new Object[]{type, items, ActivateRequest.TYPE_ACTIVE});
	}

	@Override
	@Transactional
	public List<ActivateRequest> getAllActiveOrPendingRequestsForItems(String type, Collection<Item> items)
	{
		if( items.isEmpty() )
		{
			return Collections.emptyList();
		}

		return getHibernateTemplate().findByNamedParam(
			"from ActivateRequest where type = :type and item in (:items) and status in (:statuses)",
			new String[]{"type", "items", "statuses"},
			new Object[]{type, items, new Integer[]{ActivateRequest.TYPE_ACTIVE, ActivateRequest.TYPE_PENDING}});
	}

	@Override
	@Transactional
	public List<ActivateRequest> getAllRequestsForCourse(CourseInfo course)
	{
		return getHibernateTemplate().findByNamedParam("from ActivateRequest where course = :course", "course", course);
	}

	@Override
	@Transactional
	public List<ActivateRequest> getAllRequestsByStatus(int status)
	{
		return getHibernateTemplate().findByNamedParam(
			"from ActivateRequest ar where ar.status = :status and " + " ar.item.institution = :institution",
			new String[]{"status", "institution"}, new Object[]{status, CurrentInstitution.get()});
	}

	public long countActive(ActivateRequest request)
	{
		return (Long) getHibernateTemplate().findByNamedParam(
			"select count(*) from ActivateRequest"
				+ " where item = :item and attachment = :attachment and status = :status",
			new String[]{"item", "attachment", "status"},
			new Object[]{request.getItem(), request.getAttachment(), ActivateRequest.TYPE_ACTIVE}).get(0);
	}

	@Override
	@Transactional
	public void removeRequests(String type, long itemId)
	{
		deleteAll(getAllRequests(type, itemId));
	}

	@Override
	@Transactional
	public ActivateRequest getLastActive(final String type, final Item item, final String attachment)
	{
		return (ActivateRequest) getHibernateTemplate().execute(new HibernateCallback()
		{
			@Override
			@Transactional
			public Object doInHibernate(Session session)
			{
				Query query = session.createQuery("from ActivateRequest req"
					+ " where req.type = :type and req.item = :item and req.attachment = :att and req.status = :status"
					+ " order by req.until desc");
				query.setFetchSize(1);
				query.setFirstResult(0);
				query.setString("type", type);
				query.setString("att", attachment);
				query.setEntity("item", item);
				query.setInteger("status", ActivateRequest.TYPE_ACTIVE);

				List<?> l = query.list();
				return l.size() == 0 ? null : l.get(0);
			}
		});
	}

	@Override
	@Transactional
	public Collection<ItemId> getAllActivatedItemsForInstitution()
	{
		return getHibernateTemplate().findByNamedParam(
			"select new com.tle.beans.item.ItemId(a.item.uuid, a.item.version) " + " from ActivateRequest a"
				+ " where a.item.institution = :institution group by a.item.uuid, a.item.version",
			new String[]{"institution"}, new Object[]{CurrentInstitution.get()});
	}

	@Override
	@Transactional
	public void deleteAllForItem(Item item)
	{
		deleteAll(getAllRequests(item.getId()));
	}

	private Collection<ActivateRequest> getAllRequests(long itemId)
	{
		return getHibernateTemplate().findByNamedParam("from ActivateRequest where item.id = :itemId",
			new String[]{"itemId"}, new Object[]{itemId});
	}

	private Collection<ActivateRequest> getAllRequests(String type, long itemId)
	{
		return getHibernateTemplate().findByNamedParam("from ActivateRequest where type = :type and item.id = :itemId",
			new String[]{"itemId", "type"}, new Object[]{itemId, type});
	}

	private void deleteAll(Collection<ActivateRequest> requests)
	{
		for( ActivateRequest activateRequest : requests )
		{
			delete(activateRequest);
		}
	}

	@Override
	@Transactional(propagation = Propagation.MANDATORY)
	public List<ActivateRequest> listAll()
	{
		return getHibernateTemplate().executeFind(new TLEHibernateCallback()
		{
			@Override
			public Object doInHibernate(Session session) throws HibernateException
			{
				Query query = session
					.createQuery("from " + getPersistentClass().getName() + " where item.institution = :institution");
				query.setEntity("institution", CurrentInstitution.get());
				query.setCacheable(true);
				query.setReadOnly(true);
				return query.list();
			}
		});
	}

	@Override
	@Transactional
	public ActivateRequest getByUuid(String requestUuid)
	{
		List<ActivateRequest> requests = getHibernateTemplate().findByNamedParam(
			"from ActivateRequest where uuid = :uuid and item.institution = :institution",
			new String[]{"uuid", "institution"}, new Object[]{requestUuid, CurrentInstitution.get()});
		return requests.isEmpty() ? null : requests.get(0);
	}

	@Override
	public List<ItemIdKey> getItemKeysForUserActivations(String userId)
	{
		return getHibernateTemplate().findByNamedParam(
			"SELECT new com.tle.beans.item.ItemIdKey(i.id, i.uuid, i.version) FROM ActivateRequest a "
				+ "JOIN a.item i WHERE a.user = :user and i.institution = :institution",
			new String[]{"user", "institution"}, new Object[]{userId, CurrentInstitution.get()});
	}

	@Override
	@Transactional
	public void delete(Item item)
	{
		deleteAllForItem(item);
	}

	@Override
	@Transactional
	public void delete(ActivateRequest entity)
	{
		auditLogService.logObjectDeleted(entity.getId(), AUDIT_CATEGORY_TYPE);
		super.delete(entity);
	}
}
