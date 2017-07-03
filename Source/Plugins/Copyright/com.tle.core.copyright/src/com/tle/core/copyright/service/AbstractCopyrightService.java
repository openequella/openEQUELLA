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

package com.tle.core.copyright.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.dytech.common.collections.CombinedCollection;
import com.dytech.devlib.PropBagEx;
import com.google.common.base.Throwables;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.tle.beans.activation.ActivateRequest;
import com.tle.beans.entity.Schema;
import com.tle.beans.entity.itemdef.ItemDefinition;
import com.tle.beans.entity.schema.Citation;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemId;
import com.tle.beans.item.ItemIdInst;
import com.tle.beans.item.ItemPack;
import com.tle.beans.item.attachments.Attachment;
import com.tle.beans.item.attachments.AttachmentType;
import com.tle.beans.item.attachments.Attachments;
import com.tle.beans.item.attachments.IAttachment;
import com.tle.beans.item.attachments.UnmodifiableAttachments;
import com.tle.common.Check;
import com.tle.common.i18n.LangUtils;
import com.tle.common.institution.CurrentInstitution;
import com.tle.core.activation.ActivateRequestDao;
import com.tle.core.activation.service.ActivationImplementation;
import com.tle.core.copyright.Holding;
import com.tle.core.copyright.Portion;
import com.tle.core.copyright.Section;
import com.tle.core.copyright.dao.CopyrightDao;
import com.tle.core.copyright.exception.CopyrightViolationException;
import com.tle.core.filesystem.EntityFile;
import com.tle.core.filesystem.SubEntityFile;
import com.tle.core.item.helper.ItemHelper;
import com.tle.core.item.service.ItemService;
import com.tle.core.services.user.UserSessionService;
import com.tle.core.xml.service.XmlService;
import com.tle.core.xslt.service.XsltService;

@SuppressWarnings("nls")
public abstract class AbstractCopyrightService<H extends Holding, P extends Portion, S extends Section>
	implements
		CopyrightService<H, P, S>,
		ActivationImplementation
{
	private static final String COPYRIGHTSTATUS = "copyright";

	private final CopyrightDao<H, P, S> dao;

	@Inject
	private ActivateRequestDao requestDao;
	@Inject
	private UserSessionService userSessionService;
	@Inject
	private ItemService itemService;
	@Inject
	private XsltService xsltService;
	@Inject
	private XmlService xmlService;
	@Inject
	private ItemHelper itemHelper;

	private LoadingCache<ItemIdInst, Boolean> copyrightCache;

	protected abstract String getEnabledAttribute();

	protected abstract String getAgreementFileAttribute();

	protected abstract String getHasAgreementAttribute();

	protected abstract String getInactiveErrorAttribute();

	public AbstractCopyrightService(CopyrightDao<H, P, S> dao)
	{
		this.dao = dao;

		copyrightCache = CacheBuilder.newBuilder().expireAfterAccess(1, TimeUnit.MINUTES).maximumSize(100).softValues()
			.build(new CacheLoader<ItemIdInst, Boolean>()
			{
				@Override
				public Boolean load(ItemIdInst itemId) throws Exception
				{
					Item item = itemService.getUnsecure(itemId);
					return isCopyrightedItem(item);
				}
			});
	}

	@Override
	public boolean isCopyrightedItem(Item item)
	{
		return Boolean.valueOf(item.getItemDefinition().getAttributes().get(getEnabledAttribute()));
	}

	@Override
	@Transactional
	public H getHoldingForItem(Item item)
	{
		return dao.getHoldingForItem(item);
	}

	@Override
	@Transactional
	public List<ActivateRequest> getCurrentOrPendingActivations(H holding)
	{
		List<Item> items = dao.getAllItemsForHolding(holding);
		if( items.isEmpty() )
		{
			return Collections.emptyList();
		}
		return requestDao.getAllActiveOrPendingRequestsForItems(getActivationType(), items);
	}

	@Override
	@Transactional
	public S getSectionForAttachment(Item item, String attachmentUuid)
	{
		return dao.getSectionForAttachment(item, attachmentUuid);
	}

	@Override
	public Item getCopyrightedItem(ItemId itemId)
	{
		if( itemId != null && copyrightCache.getUnchecked(new ItemIdInst(itemId, CurrentInstitution.get())) )
		{
			return itemService.getUnsecure(itemId);
		}
		return null;
	}

	@Override
	@Transactional
	public Attachment getSectionAttachmentForFilepath(Item item, String filepath)
	{
		return dao.getSectionAttachmentForFilepath(item, filepath);
	}

	@Override
	@Transactional
	public AgreementStatus getAgreementStatus(Item item, IAttachment attachment)
	{
		AgreementStatus status = new AgreementStatus();
		String attachUuid = attachment.getUuid();
		Section section = getSectionForAttachment(item, attachUuid);
		if( section == null || !COPYRIGHTSTATUS.equals(section.getCopyrightStatus()) )
		{
			return status;
		}
		if( requestDao.getLastActive(getActivationType(), item, attachment.getUuid()) == null )
		{
			status.setInactive(true);
		}
		Holding holding = section.getPortion().getHolding();
		if( holding == null )
		{
			// This is fine as an non-i18n. It's an error for the programmers
			// really.
			//throw new IllegalStateException("This portion has no holding " + item.getItemId().toString());
			return status;
		}

		final ItemDefinition holdingCollection = holding.getItem().getItemDefinition();
		if( isNeedsAgreement(holdingCollection, item, attachment) )
		{
			String agreeFile = holdingCollection.getAttributes().get(getAgreementFileAttribute());
			if( !Check.isEmpty(agreeFile) )
			{
				status.setAgreementFile(new SubEntityFile(new EntityFile(holdingCollection), agreeFile));
			}

			status.setNeedsAgreement(true);
		}

		return status;
	}

	@Override
	public void acceptAgreement(Item item, IAttachment attachment)
	{
		userSessionService.setAttribute(getKeyForAttachment(item, attachment), true);
	}

	@Override
	@Transactional
	public CopyrightViolationException createViolation(Item item)
	{
		H holding = getHoldingForItem(item);
		ItemDefinition itemdef = holding.getItem().getItemDefinition();
		Map<String, String> attributes = itemdef.getAttributes();
		String activateErrorXml = attributes.get(getInactiveErrorAttribute());
		return new CopyrightViolationException(LangUtils.getBundleFromXmlString(activateErrorXml));
	}

	protected boolean isNeedsAgreement(ItemDefinition holdingCollection, Item item, IAttachment attachment)
	{
		if( Boolean.parseBoolean(holdingCollection.getAttributes().get(getHasAgreementAttribute())) )
		{
			if( !Boolean.TRUE.equals(userSessionService.getAttribute(getKeyForAttachment(item, attachment))) )
			{
				return true;
			}
		}
		return false;
	}

	private String getKeyForAttachment(Item item, IAttachment attachment)
	{
		return getActivationType() + "agree:" + attachment.getUuid() + ':' + item.getId();
	}

	protected final void ensureStates(List<ActivateRequest> requests)
	{
		Date now = new Date();
		for( ActivateRequest request : requests )
		{
			Date from = request.getFrom();
			Date until = request.getUntil();
			int status = ActivateRequest.TYPE_PENDING;
			if( now.after(from) )
			{
				if( now.before(until) )
				{
					status = ActivateRequest.TYPE_ACTIVE;
				}
				else
				{
					status = ActivateRequest.TYPE_INACTIVE;
				}
			}
			request.setStatus(status);
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Map<Long, H> getHoldingsForItems(List<Item> items)
	{
		return dao.getHoldingsForItems(items);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public Map<Long, List<P>> getPortionsForItems(List<Item> items)
	{
		Map<Long, List<P>> portionMap = new HashMap<Long, List<P>>();
		List<P> portions = dao.getPortionsForItems(items);
		for( P portion : portions )
		{
			long itemId = portion.getItem().getId();
			List<P> portionList = portionMap.get(itemId);
			if( portionList == null )
			{
				portionList = new ArrayList<P>();
				portionMap.put(itemId, portionList);
			}
			portionList.add(portion);
		}
		return portionMap;
	}

	private String findAttachmentDescription(Item item, String attachmentUuid)
	{
		Attachments attachments = new UnmodifiableAttachments(item);
		CombinedCollection<IAttachment> linksAndFiles = new CombinedCollection<>(
			attachments.getList(AttachmentType.FILE), attachments.getList(AttachmentType.LINK));
		Map<String, IAttachment> uuid2Attachment = UnmodifiableAttachments.convertToMapUuid(linksAndFiles);

		Attachment attachment = (Attachment) uuid2Attachment.get(attachmentUuid);
		if( attachment != null )
		{
			return attachment.getDescription();
		}
		return null;
	}

	@Override
	public String getActivationDescription(ActivateRequest request)
	{
		Item item = request.getItem();
		String cit = request.getCitation();
		String description = findAttachmentDescription(item, request.getAttachment());
		Schema schema = item.getItemDefinition().getSchema();
		for( Citation c : schema.getCitations() )
		{
			if( c.getName().equals(cit) )
			{
				Holding holding = getHoldingForItem(item);
				String file = c.getTransformation();
				// Just in case...
				if( file == null )
				{
					break;
				}
				if( holding == null )
				{
					throw new IllegalStateException(
						"There is no Copyright holding for this item: " + item.getItemId().toString());
				}
				PropBagEx xml = getUnifiedXml(item);
				try
				{
					ActivateRequest cloned = (ActivateRequest) request.clone();
					cloned.setItem(null);
					cloned.setCourse(null);
					xml.appendChildren("request", new PropBagEx(xmlService.serialiseToXml(cloned))); //$NON-NLS-1$
					xml.append("holding", getUnifiedXml(holding.getItem())); //$NON-NLS-1$
				}
				catch( CloneNotSupportedException e )
				{
					throw Throwables.propagate(e);
				}

				return xsltService.transform(new EntityFile(schema), file, xml, true);
			}
		}
		return description;
	}

	private PropBagEx getUnifiedXml(Item item)
	{
		PropBagEx xml = itemService.getItemXmlPropBag(item);
		return itemHelper.convertToXml(new ItemPack(item, xml, ""), //$NON-NLS-1$
			new ItemHelper.ItemHelperSettings(true));
	}

	@Override
	@Transactional
	public List<ActivateRequest> getAllRequests(Item item)
	{
		final H holding = getHoldingForItem(item);
		if( holding != null )
		{
			if( holding.getItem().getItemId().equals(item.getItemId()) )
			{
				List<Item> items = dao.getAllItemsForHolding(holding);
				return requestDao.getAllRequestsForItems(getActivationType(), items);
			}
			return requestDao.getAllRequests(getActivationType(), item);
		}
		return new ArrayList<ActivateRequest>();
	}
}
