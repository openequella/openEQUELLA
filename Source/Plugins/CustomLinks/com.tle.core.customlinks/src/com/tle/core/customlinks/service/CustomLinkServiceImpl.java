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

package com.tle.core.customlinks.service;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Transactional;

import com.tle.common.EntityPack;
import com.tle.common.beans.exception.ValidationError;
import com.tle.common.customlinks.entity.CustomLink;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.TargetList;
import com.tle.common.security.TargetListEntry;
import com.tle.core.customlinks.dao.CustomLinkDao;
import com.tle.core.entity.EntityEditingSession;
import com.tle.core.entity.service.impl.AbstractEntityServiceImpl;
import com.tle.core.filesystem.EntityFile;
import com.tle.core.guice.Bind;
import com.tle.core.security.TLEAclManager;
import com.tle.core.security.impl.RequiresPrivilege;
import com.tle.core.security.impl.SecureEntity;

@SuppressWarnings("nls")
@SecureEntity("CUSTOM_LINK")
@Bind(CustomLinkService.class)
@Singleton
public class CustomLinkServiceImpl
	extends
		AbstractEntityServiceImpl<CustomLinkEditingBean, CustomLink, CustomLinkService>
	implements CustomLinkService
{
	private static final String ORDER = "order";
	private static final String EDIT_CUSTOM_LINK = "EDIT_CUSTOM_LINK";
	private static final String DELETE_CUSTOM_LINK = "DELETE_CUSTOM_LINK";

	@Inject
	private TLEAclManager aclService;

	private final CustomLinkDao customLinkDao;

	public static final Comparator<CustomLink> CUSTOM_LINK_COMPARATOR = new Comparator<CustomLink>()
	{
		@Override
		public int compare(CustomLink link1, CustomLink link2)
		{
			Integer order1 = link1.getOrder();
			Integer order2 = link2.getOrder();

			return order1.compareTo(order2);
		}
	};

	@Inject
	public CustomLinkServiceImpl(CustomLinkDao customLinkDao)
	{
		super(Node.CUSTOM_LINK, customLinkDao);
		this.customLinkDao = customLinkDao;
	}

	@Override
	protected boolean isUseEditingBean()
	{
		return true;
	}

	@Override
	protected CustomLinkEditingBean createEditingBean()
	{
		return new CustomLinkEditingBean();
	}

	@Override
	protected void doValidation(EntityEditingSession<CustomLinkEditingBean, CustomLink> session, CustomLink entity,
		List<ValidationError> errors)
	{
		// Nothing to validate?
	}

	@Override
	protected void doValidationBean(CustomLinkEditingBean bean, List<ValidationError> errors)
	{
		super.doValidationBean(bean, errors);
	}

	@Override
	@RequiresPrivilege(priv = EDIT_CUSTOM_LINK)
	@Transactional
	public List<CustomLink> enumerateInOrder()
	{
		List<CustomLink> links = enumerate();
		Collections.sort(links, CUSTOM_LINK_COMPARATOR);
		return links;
	}

	@Override
	@RequiresPrivilege(priv = EDIT_CUSTOM_LINK)
	@Transactional
	public void insertLink(CustomLink newLink)
	{
		int newOrder = newLink.getOrder();
		List<CustomLink> links = customLinkDao.findAllByCriteria(Restrictions.ge(ORDER, newOrder),
			getInstitutionCriterion());

		for( CustomLink aLink : links )
		{
			aLink.setOrder(aLink.getOrder() + 1);
			customLinkDao.update(aLink);
		}

		customLinkDao.save(newLink);
	}

	@Override
	@RequiresPrivilege(priv = "DELETE_CUSTOM_LINK")
	@Transactional
	public void deleteLink(CustomLink link)
	{
		int order = link.getOrder();
		customLinkDao.delete(link);
		List<CustomLink> links = customLinkDao.findAllByCriteria(Restrictions.ge(ORDER, order),
			getInstitutionCriterion());

		for( CustomLink aLink : links )
		{
			aLink.setOrder(aLink.getOrder() - 1);
			customLinkDao.update(aLink);
		}
	}

	@Override
	@RequiresPrivilege(priv = EDIT_CUSTOM_LINK)
	@Transactional
	public void moveLink(String linkUuid, int newOrder)
	{
		CustomLink link = customLinkDao.getByUuid(linkUuid);
		List<CustomLink> links = customLinkDao.findAllByCriteria(getInstitutionCriterion());
		Collections.sort(links, CUSTOM_LINK_COMPARATOR);

		links.remove(link);
		links.add(newOrder, link);

		int initOrder = 0;
		for( CustomLink aLink : links )
		{
			aLink.setOrder(initOrder);
			customLinkDao.update(aLink);
			initOrder++;
		}
	}

	@Override
	@Transactional
	public List<CustomLink> listLinksForUser()
	{
		List<CustomLink> links = customLinkDao.listLinksForUser();
		Collections.sort(links, CUSTOM_LINK_COMPARATOR);
		return links;
	}

	@Override
	@Transactional
	public boolean showSettingLink()
	{
		return !aclService.filterNonGrantedPrivileges(Arrays.asList(EDIT_CUSTOM_LINK)).isEmpty();
	}

	@Override
	public final EntityPack<CustomLink> startEditInternal(CustomLink entity)
	{
		EntityFile from = new EntityFile(entity);
		StagingFile staging = stagingService.createStagingArea();
		if( fileSystemService.fileExists(from) )
		{
			fileSystemService.copy(from, staging);
		}

		ensureNonSystem(entity);
		EntityPack<CustomLink> result = new EntityPack<CustomLink>();
		result.setEntity(entity);
		result.setStagingID(staging.getUuid());
		fillTargetLists(result);
		return result;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <SESSION extends EntityEditingSession<CustomLinkEditingBean, CustomLink>> SESSION createSession(
		String sessionId, EntityPack<CustomLink> pack, CustomLinkEditingBean bean)
	{
		return (SESSION) new CustomLinkEditingSessionImpl(sessionId, pack, bean);
	}

	@Override
	protected void populateEditingBean(CustomLinkEditingBean bean, CustomLink entity)
	{
		super.populateEditingBean(bean, entity);

		CustomLinkEditingBean clBean = bean;

		final TargetList targets = aclManager.getTargetList(Node.CUSTOM_LINK, entity);
		String expression = null;
		for( TargetListEntry target : targets.getEntries() )
		{
			if( target.getPrivilege().equals("VIEW_CUSTOM_LINK") )
			{
				expression = target.getWho();
				break;
			}
		}
		clBean.setTargetExpression(expression);
		clBean.setFileName(entity.getAttribute("fileName"));
		clBean.setUrl(entity.getUrl());
		clBean.setOrder(entity.getOrder());
	}

	@Override
	protected void populateEntity(CustomLinkEditingBean clBean, CustomLink entity)
	{
		super.populateEntity(clBean, entity);

		entity.setAttribute("fileName", clBean.getFileName());
		entity.setUrl(clBean.getUrl());
		entity.setOrder(clBean.getOrder());
	}

	@Override
	protected void onStartNewSession(EntityEditingSession<CustomLinkEditingBean, CustomLink> session,
		EntityFile entFile)
	{
		super.onStartNewSession(session, entFile);

		CustomLink link = session.getEntity();
		link.setOrder((int) customLinkDao.countByCriteria(getInstitutionCriterion()));
		link.setAttribute("newWindow", "false");
	}

	@Override
	public boolean canDelete(CustomLink link)
	{
		Set<String> privs = new HashSet<String>();
		privs.add(DELETE_CUSTOM_LINK);
		return !aclManager.filterNonGrantedPrivileges(link, privs).isEmpty();
	}
}
