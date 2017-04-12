package com.tle.core.institution.impl;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.security.AccessEntry;
import com.tle.common.Check;
import com.tle.core.dao.AclDao;
import com.tle.core.guice.Bind;
import com.tle.core.institution.AclService;

@Bind(AclService.class)
@Singleton
public class AclServiceImpl implements AclService
{
	@Inject
	private AclDao aclDao;

	@Override
	public List<AccessEntry> listAll()
	{
		List<AccessEntry> allAcls = aclDao.listAll();
		return allAcls;
	}

	@Override
	public void saveAll(List<AccessEntry> acls)
	{
		if( !Check.isEmpty(acls) )
		{
			for( AccessEntry ae : acls )
			{
				aclDao.save(ae);
			}
			aclDao.flush();
			aclDao.clear();
		}
	}

}
