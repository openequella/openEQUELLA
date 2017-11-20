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

package com.tle.core.item.service.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.dytech.edge.exceptions.DRMException;
import com.dytech.edge.wizard.beans.DRMPage;
import com.dytech.edge.wizard.beans.DRMPage.Container;
import com.dytech.edge.wizard.beans.DRMPage.Contributor;
import com.dytech.edge.wizard.beans.DRMPage.Network;
import com.dytech.edge.wizard.beans.DRMPage.UsersAndGroups;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.tle.beans.item.DrmAcceptance;
import com.tle.beans.item.DrmSettings;
import com.tle.beans.item.Item;
import com.tle.beans.item.ItemKey;
import com.tle.common.Check;
import com.tle.common.Pair;
import com.tle.common.Triple;
import com.tle.common.beans.exception.NotFoundException;
import com.tle.common.settings.standard.AutoLogin;
import com.tle.core.events.UserDeletedEvent;
import com.tle.core.events.UserEditEvent;
import com.tle.core.events.UserIdChangedEvent;
import com.tle.core.events.listeners.UserChangeListener;
import com.tle.core.guice.Bind;
import com.tle.core.item.dao.DrmAcceptanceDao;
import com.tle.core.item.event.ItemDeletedEvent;
import com.tle.core.item.event.listener.ItemDeletedListener;
import com.tle.core.item.service.DrmService;
import com.tle.core.security.impl.AclExpressionEvaluator;
import com.tle.core.services.user.UserSessionService;
import com.tle.core.settings.service.ConfigurationService;
import com.tle.common.usermanagement.user.CurrentUser;

/**
 * @author Nicholas Read
 */
@Singleton
@Bind(DrmService.class)
@SuppressWarnings("nls")
public class DrmServiceImpl implements DrmService, ItemDeletedListener, UserChangeListener
{
	private static final String KEY_PREVIEW_ALLOWED = "$DRM$PREVIEW$";

	@Inject
	private UserSessionService sessionService;

	private final Cache<String, Boolean> needsRights = CacheBuilder.newBuilder().softValues()
		.expireAfterAccess(30, TimeUnit.SECONDS).build();

	@Inject
	private DrmAcceptanceDao dao;
	@Inject
	private ConfigurationService configService;

	@Override
	public DrmAcceptance getAgreement(String userID, Item item)
	{
		Criterion c1 = Restrictions.eq("item", item);
		Criterion c2 = Restrictions.eq("user", userID);
		DrmAcceptance acceptance = dao.findByCriteria(c1, c2);
		if( acceptance == null )
		{
			throw new NotFoundException(
				"User " + userID + " has not accepted an agreement for item: " + item.toString());
		}
		return acceptance;
	}

	@Override
	public Pair<Long, List<DrmAcceptance>> enumerateAgreements(Item item, int limit, int offset, boolean sortByName,
		Date startDate, Date endDate)
	{
		Criterion[] criterion = prepareCriterion(item, startDate, endDate);

		long total = dao.countByCriteria(criterion);

		Order order = Order.desc(sortByName ? "user" : "date"); //$NON-NLS-2$
		List<DrmAcceptance> acceptances = dao.findAllByCriteria(order, offset, limit, criterion);

		return new Pair<Long, List<DrmAcceptance>>(total, acceptances);
	}

	@Override
	public List<DrmAcceptance> enumerateAgreements(Item item)
	{
		return dao.findAllByCriteria(Order.desc("date"), -1, prepareCriterion(item, null, null));
	}

	private long countAgreements(Item item)
	{
		return dao.countByCriteria(prepareCriterion(item, null, null));
	}

	private Criterion[] prepareCriterion(Item item, Date startDate, Date endDate)
	{
		List<Criterion> critList = new ArrayList<Criterion>();
		critList.add(Restrictions.eq("item", item));
		if( startDate != null && endDate != null )
		{
			critList.add(Restrictions.between("date", startDate, endDate));
		}
		return critList.toArray(new Criterion[critList.size()]);
	}

	@Override
	public boolean requiresAcceptanceCheck(ItemKey key, boolean isSummaryPage, boolean viewedInComposition)
	{
		String strkey = getCacheKey(key, isSummaryPage, viewedInComposition);
		Boolean needs = needsRights.getIfPresent(strkey);
		return needs == null || needs.booleanValue();
	}

	private String getCacheKey(ItemKey key, boolean isSummaryPage, boolean viewedInComposition)
	{
		return key.toString() + ':' + isSummaryPage + ':' + viewedInComposition + ':' + CurrentUser.getUserID();
	}

	@Override
	public DrmSettings requiresAcceptance(Item item, boolean isSummaryPage, boolean viewedInComposition)
	{
		DrmSettings rights = realAcceptanceCheck(item, isSummaryPage, viewedInComposition);
		if( rights == null )
		{
			String cacheKey = getCacheKey(item.getItemId(), isSummaryPage, viewedInComposition);
			needsRights.put(cacheKey, Boolean.FALSE);
		}
		return rights;
	}

	private DrmSettings realAcceptanceCheck(Item item, boolean isSummaryPage, boolean viewedInComposition)
	{
		if( CurrentUser.getUserState().isSystem() )
		{
			return null;
		}

		DrmSettings rights = item.getDrmSettings();
		if( rights == null )
		{
			return null;
		}

		if( isSummaryPage && rights.isAllowSummary() )
		{
			return null;
		}

		if( viewedInComposition && !rights.isStudentsMustAcceptIfInCompilation() )
		{
			return null;
		}

		final String currentUser = CurrentUser.getUserID();
		final boolean isOwner = currentUser.equals(item.getOwner());
		if( isOwner && !rights.isOwnerMustAccept() )
		{
			return null;
		}

		final String requireAcceptanceFrom = rights.getRequireAcceptanceFrom();
		if( !Check.isEmpty(requireAcceptanceFrom) )
		{
			AclExpressionEvaluator eval = new AclExpressionEvaluator();
			if( !eval.evaluate(requireAcceptanceFrom, CurrentUser.getUserState(), isOwner) )
			{
				return null;
			}
		}

		try
		{
			getAgreement(currentUser, item);
			return null;
		}
		catch( NotFoundException ex )
		{
			if( rights.getMaximumUsageCount() > 0 && countAgreements(item) >= rights.getMaximumUsageCount() )
			{
				throw new DRMException("This item has been licenced the maximum number" + " of allowable times.");
			}
			return rights;
		}
	}

	@Override
	public boolean hasAcceptedOrRequiresNoAcceptance(Item item, boolean isSummaryPage, boolean viewedInComposition)
	{
		if( requiresAcceptanceCheck(item.getItemId(), isSummaryPage, viewedInComposition) )
		{
			try
			{
				isAuthorised(item, CurrentUser.getUserState().getIpAddress());
			}
			catch( DRMException ex )
			{
				return false;
			}

			DrmSettings rights = requiresAcceptance(item, isSummaryPage, viewedInComposition);
			if( rights != null )
			{
				return false;
			}
		}
		return true;
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void acceptLicense(Item item)
	{
		// Do not accept the licence if they are a guest.
		if( CurrentUser.isGuest() )
		{
			return;
		}

		// Do not accept the licence if disabled for auto-logged in users
		if( CurrentUser.wasAutoLoggedIn() && configService.getProperties(new AutoLogin()).isTransientDrmAcceptances() )
		{
			return;

		}

		String userID = CurrentUser.getUserID();
		try
		{
			getAgreement(userID, item);
		}
		catch( NotFoundException ex )
		{
			DrmAcceptance agreement = new DrmAcceptance();
			agreement.setItem(item);
			agreement.setUser(userID);
			agreement.setDate(new Date());

			dao.save(agreement);
		}
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void revokeAcceptance(Item item, String userID)
	{
		Criterion c1 = Restrictions.eq("item", item);
		Criterion c2 = Restrictions.eq("user", userID);
		delete(c1, c2);
	}

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void revokeAllItemAcceptances(Item item)
	{
		delete(Restrictions.eq("item", item));
	}

	@Override
	public void itemDeletedEvent(ItemDeletedEvent event)
	{
		delete(Restrictions.eq("item.id", event.getKey()));
	}

	private void delete(Criterion... criterion)
	{
		for( DrmAcceptance acceptance : dao.findAllByCriteria(criterion) )
		{
			dao.delete(acceptance);
		}
	}

	@Override
	public void isAuthorised(Item item, String ipaddress)
	{
		if( CurrentUser.getUserState().isSystem() )
		{
			return;
		}

		DrmSettings rights = item.getDrmSettings();

		// Check if there are any rights for this item
		if( rights == null )
		{
			return;
		}

		// Check if this user is the owner
		if( item.getOwner().equals(CurrentUser.getUserID()) )
		{
			return;
		}

		// Wow.  Such hard coded strings.

		if( !checkForUserOrGroup(rights) )
		{
			throw new DRMException("User is not a member of the individuals or groups" + " allowed to view this item.");
		}

		if( !checkForDateTime(rights) )
		{
			throw new DRMException("User is viewing this item outside of the dates that" + " it is restricted to.");
		}

		if( !checkForNetwork(rights, ipaddress) )
		{
			throw new DRMException("User is not coming from an IP address that this" + " item is restricted to.");
		}
	}

	private boolean checkForUserOrGroup(DrmSettings rights)
	{
		List<String> recipients = rights.getRestrictedToRecipients();
		// Check if there is a restriction first
		if( recipients == null )
		{
			return true;
		}

		for( String recipient : recipients )
		{
			char type = recipient.charAt(0);
			recipient = recipient.substring(1);

			switch( type )
			{
				case 'u':
					if( recipient.equals(CurrentUser.getUserID()) )
					{
						return true;
					}
					break;

				case 'g':
					if( CurrentUser.getUsersGroups().contains(recipient) )
					{
						return true;
					}
					break;

				default:
					throw new IllegalStateException(Character.toString(type));
			}
		}

		return false;
	}

	private boolean checkForDateTime(DrmSettings rights)
	{
		Date now = new Date();

		Pair<Date, Date> dateRange = rights.getRestrictedToDateRange();
		if( dateRange == null )
		{
			return true;
		}

		Date start = dateRange.getFirst();
		if( start == null )
		{
			return true;
		}
		Date end = dateRange.getSecond();

		// Add a day to the end date to make sure we can access the item that
		// day.
		Calendar c = Calendar.getInstance();
		c.setTime(end);
		c.add(Calendar.DATE, 1);
		end = c.getTime();

		return start.before(now) && now.before(end);
	}

	private boolean checkForNetwork(DrmSettings rights, String ipaddress)
	{
		String[] remote = ipaddress.split("\\.");

		if( remote.length != 4 )
		{
			// We can't check this, so we will ignore it
			return true;
		}

		List<Triple<String, String, String>> restrictedToIpRanges = rights.getRestrictedToIpRanges();
		if( restrictedToIpRanges == null )
		{
			return true;
		}

		for( Triple<String, String, String> range : rights.getRestrictedToIpRanges() )
		{
			String[] start = range.getSecond().split("\\.");
			String[] end = range.getThird().split("\\.");

			// Each split must have 4 parts
			if( start.length == 4 && end.length == 4 )
			{
				boolean match = true;
				for( int j = 0; j < 4 && match; j++ )
				{
					if( !(start[j].equals("*") || end[j].equals("*")) ) //$NON-NLS-2$
					{
						try
						{
							int startNum = Integer.parseInt(start[j]);
							int endNum = Integer.parseInt(end[j]);
							int remNum = Integer.parseInt(remote[j]);
							if( remNum < startNum || remNum > endNum )
							{
								match = false;
							}
						}
						catch( NumberFormatException nfe )
						{
							// nothing
						}
					}
				}

				if( match )
				{
					return true;
				}
			}
		}

		return false;
	}

	@Override
	public void mergeSettings(DrmSettings settings, DRMPage page)
	{
		settings.setPreviewAllowed(page.isAllowPreview());
		settings.setEnforceAttribution(page.isAttributionIsEnforced());
		settings.setAllowSummary(page.isAllowSummary());

		final Contributor contributor = page.getContributor();

		if( !contributor.isAttribution() )
		{
			settings.setAttributionOfOwnership(page.isAttribution());
		}

		if( !contributor.isTerms() )
		{
			settings.setTermsOfAgreement(page.getRemark());
		}

		final Container container = page.getContainer();
		if( !contributor.isDatetime() )
		{
			Date acceptStart = container.getAcceptStart();
			if( acceptStart != null )
			{
				settings.setRestrictedToDateRange(new Pair<Date, Date>(acceptStart, container.getAcceptEnd()));
			}
			else
			{
				settings.setRestrictedToDateRange(null);
			}
		}

		if( Check.isEmpty(contributor.getNetworks()) )
		{
			List<Triple<String, String, String>> ipranges = new ArrayList<Triple<String, String, String>>();
			Set<Network> networks = container.getNetworks();
			for( Network network : networks )
			{
				ipranges.add(new Triple<String, String, String>(network.getName(), network.getMin(), network.getMax()));
			}
			settings.setRestrictedToIpRanges(ipranges.isEmpty() ? null : ipranges);
		}

		if( Check.isEmpty(contributor.getUsers()) && Check.isEmpty(contributor.getGroups()) )
		{
			settings.setRestrictedToRecipients(convertRecipients(container));
		}

		if( !contributor.isCount() )
		{
			settings.setMaximumUsageCount(container.getCount());
		}

		settings.setOwnerMustAccept(page.isOwnerMustAccept());
		settings.setStudentsMustAcceptIfInCompilation(page.isShowLicenceInComposition());
		settings.setHideLicencesFromOwner(page.isHideLicencesFromOwner());
		settings.setRequireAcceptanceFrom(page.getRequireAcceptanceFrom());
	}

	private List<String> convertRecipients(UsersAndGroups usersGroups)
	{
		List<String> recipients = new ArrayList<String>();
		for( String user : usersGroups.getUsers() )
		{
			recipients.add('u' + user);
		}
		for( String group : usersGroups.getGroups() )
		{
			recipients.add('g' + group);
		}
		return recipients.size() > 0 ? recipients : null;
	}

	@Override
	public boolean havePreviewedThisSession(ItemKey itemId)
	{
		String key = KEY_PREVIEW_ALLOWED + itemId.toString();
		return (Boolean.TRUE.equals(sessionService.getAttribute(key)));
	}

	@Override
	public void addPreviewItem(ItemKey itemId)
	{
		String key = KEY_PREVIEW_ALLOWED + itemId.toString();
		if( sessionService.getAttribute(key) == null )
		{
			sessionService.setAttribute(key, true);
		}
	}

	@Override
	public boolean isReferredFromDifferentItem(HttpServletRequest request, ItemKey itemId)
	{
		String refer = request.getParameter("referrer");
		if( refer == null )
		{
			refer = request.getHeader("Referer");
		}

		if( refer == null )
		{
			return false;
		}

		// Make sure it is from an item
		if( refer.indexOf("/file/") < 0 && refer.indexOf("/integ/") < 0 && refer.indexOf("/preview/") < 0
			&& refer.indexOf("/item/") < 0 && refer.indexOf("/items/") < 0 )
		{
			return false;
		}

		// Make sure it is from a different item
		return refer.indexOf(itemId.toString()) < 0;
	}

	@Override
	public boolean isReferredFromSamePackage(HttpServletRequest request, ItemKey itemId)
	{
		String refer = request.getParameter("referrer");
		if( refer == null )
		{
			refer = request.getHeader("Referer");
		}

		if( refer != null )
		{
			// Make sure it is from an item
			if( refer.indexOf("/file/") >= 0 || refer.indexOf("/integ/") >= 0 || refer.indexOf("/preview/") >= 0
				|| refer.indexOf("/item/") >= 0 || refer.indexOf("/items/") >= 0 )
			{

				String urlPart = itemId.toString();
				if( refer.indexOf(urlPart + "/viewims.jsp") >= 0 || refer.indexOf(urlPart + "/treenav.jsp") >= 0
					|| refer.indexOf(urlPart + "/viewscorm.jsp") >= 0 )
				{
					return true;
				}
			}
		}

		return false;
	}

	@Override
	@Transactional
	public void userDeletedEvent(UserDeletedEvent event)
	{
		// Don't care, still need record
	}

	@Override
	public void userEditedEvent(UserEditEvent event)
	{
		// Don't care
	}

	@Override
	@Transactional
	public void userIdChangedEvent(UserIdChangedEvent event)
	{
		dao.userIdChanged(event.getFromUserId(), event.getToUserId());
	}
}
