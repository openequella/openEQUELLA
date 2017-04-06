package com.tle.core.url.dao;

import java.util.Collection;

import com.tle.beans.ReferencedURL;
import com.tle.core.hibernate.dao.GenericDao;

public interface URLCheckerDao extends GenericDao<ReferencedURL, Long>
{
	ReferencedURL retrieveOrCreate(String url, boolean httpUrl, boolean forImport);

	Collection<ReferencedURL> getRecheckingBatch(long startId, int batchSize);

	void updateWithTransaction(ReferencedURL rurl);
}
