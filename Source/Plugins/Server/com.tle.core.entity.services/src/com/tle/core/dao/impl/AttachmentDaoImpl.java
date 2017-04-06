package com.tle.core.dao.impl;

import java.util.List;

import javax.inject.Singleton;

import com.google.common.collect.Lists;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.item.ItemStatus;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.CustomAttachment;
import com.tle.beans.item.attachments.FileAttachment;
import com.tle.common.Check;
import com.tle.core.dao.AttachmentDao;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericDaoImpl;
import com.tle.core.user.CurrentInstitution;

@SuppressWarnings("nls")
@Bind(AttachmentDao.class)
@Singleton
public class AttachmentDaoImpl extends GenericDaoImpl<Attachment, Long> implements AttachmentDao
{
	public AttachmentDaoImpl()
	{
		super(Attachment.class);
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<Attachment> findByMd5Sum(final String md5Sum, ItemDefinition collection,
		boolean ignoreDeletedRejectedSuspenedItems)
	{
		String hql = "SELECT a FROM Item i LEFT JOIN i.attachments a WHERE a.md5sum = :md5sum"
			+ " AND i.itemDefinition = :collection AND i.institution = :institution";
		if( ignoreDeletedRejectedSuspenedItems )
		{
			hql += " AND i.status NOT IN ('REJECTED', 'SUSPENDED', 'DELETED')";
		}

		List<Attachment> attachments = getHibernateTemplate().findByNamedParam(hql,
			new String[]{"md5sum", "collection", "institution"},
			new Object[]{md5Sum, collection, CurrentInstitution.get()});

		return attachments;
	}

	@Override
	@SuppressWarnings("unchecked")
	public List<FileAttachment> findFilesWithNoMD5Sum()
	{
		String hql = "FROM FileAttachment WHERE (md5sum IS NULL OR md5sum = '') AND item.institution = :institution";
		return getHibernateTemplate().findByNamedParam(hql, "institution", CurrentInstitution.get());

	}

	@Override
	@SuppressWarnings("unchecked")
	public List<CustomAttachment> findResourceAttachmentsByQuery(final String query, boolean liveOnly, String sortHql)
	{
		String q = query;
		String hql = "FROM CustomAttachment a WHERE a.item.institution = :institution AND a.value1 = :resourcetype";

		final List<String> params = Lists.newArrayList();
		params.add("institution");
		params.add("resourcetype");

		final List<Object> paramVals = Lists.newArrayList();
		paramVals.add(CurrentInstitution.get());
		paramVals.add("resource");

		if( !Check.isEmpty(q) )
		{
			q = q.trim().toLowerCase();
			if( !q.startsWith("%") )
			{
				q = "%" + q;
			}
			if( !q.endsWith("%") )
			{
				q = q + "%";
			}
			hql += " AND LOWER(a.description) LIKE :query";
			params.add("query");
			paramVals.add(q);
		}
		if( liveOnly )
		{
			hql += " AND item.status = :status";
			params.add("status");
			paramVals.add(ItemStatus.LIVE.name());
		}
		hql += " " + sortHql;

		final List<CustomAttachment> attachments = getHibernateTemplate().findByNamedParam(hql,
			params.toArray(new String[params.size()]), paramVals.toArray());
		// it's possible that value1 could be
		return attachments;
	}
}