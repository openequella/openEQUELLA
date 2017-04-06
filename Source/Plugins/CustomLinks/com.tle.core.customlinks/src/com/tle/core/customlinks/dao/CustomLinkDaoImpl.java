package com.tle.core.customlinks.dao;

import java.util.List;

import javax.inject.Singleton;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tle.common.customlinks.entity.CustomLink;
import com.tle.core.dao.impl.AbstractEntityDaoImpl;
import com.tle.core.guice.Bind;
import com.tle.core.security.impl.SecureOnReturn;

@Bind(CustomLinkDao.class)
@Singleton
public class CustomLinkDaoImpl extends AbstractEntityDaoImpl<CustomLink> implements CustomLinkDao
{
	public CustomLinkDaoImpl()
	{
		super(CustomLink.class);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	@SecureOnReturn(priv = "VIEW_CUSTOM_LINK")
	public List<CustomLink> listLinksForUser()
	{
		return enumerateAll();
	}

}
