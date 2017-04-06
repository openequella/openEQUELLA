package com.tle.core.customlinks.dao;

import java.util.List;

import com.tle.common.customlinks.entity.CustomLink;
import com.tle.core.dao.AbstractEntityDao;

public interface CustomLinkDao extends AbstractEntityDao<CustomLink>
{
	List<CustomLink> listLinksForUser();
}
