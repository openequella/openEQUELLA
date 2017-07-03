package com.tle.core.usermanagement.standard.dao.impl;

import javax.inject.Singleton;

import com.tle.beans.security.SharePass;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.dao.GenericDaoImpl;
import com.tle.core.usermanagement.standard.dao.SharePassDao;

/**
 * @author Nicholas Read
 */
@Bind(SharePassDao.class)
@Singleton
public class SharePassDaoImpl extends GenericDaoImpl<SharePass, String> implements SharePassDao
{
	public SharePassDaoImpl()
	{
		super(SharePass.class);
	}
}