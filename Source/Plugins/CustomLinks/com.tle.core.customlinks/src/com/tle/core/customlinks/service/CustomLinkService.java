package com.tle.core.customlinks.service;

import java.util.List;

import com.tle.common.customlinks.entity.CustomLink;
import com.tle.core.services.entity.AbstractEntityService;

public interface CustomLinkService extends AbstractEntityService<CustomLinkEditingBean, CustomLink>
{
	List<CustomLink> enumerateInOrder();

	List<CustomLink> listLinksForUser();

	void insertLink(CustomLink link);

	void deleteLink(CustomLink link);

	boolean showSettingLink();

	void moveLink(String linkUuid, int newOrder);
}
