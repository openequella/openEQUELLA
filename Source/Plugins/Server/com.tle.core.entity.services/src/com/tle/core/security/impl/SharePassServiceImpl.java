package com.tle.core.security.impl;

import java.util.Date;
import java.util.UUID;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tle.beans.item.Item;
import com.tle.beans.security.SharePass;
import com.tle.beans.security.SharePass.SharePassPrivilege;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.SecurityConstants;
import com.tle.common.security.SecurityConstants.Recipient;
import com.tle.core.dao.SharePassDao;
import com.tle.core.guice.Bind;
import com.tle.core.security.SharePassService;
import com.tle.core.security.TLEAclManager;
import com.tle.core.services.user.UserService;
import com.tle.core.user.CurrentInstitution;
import com.tle.core.user.CurrentUser;

/**
 * @author Nicholas Read
 */
@Bind(SharePassService.class)
@Singleton
@SuppressWarnings("nls")
public class SharePassServiceImpl implements SharePassService
{
	@Inject
	private SharePassDao dao;
	@Inject
	private TLEAclManager aclManager;
	@Inject
	private UserService userService;

	@Override
	public boolean isEnabled()
	{
		return userService.isWrapperEnabled("com.tle.beans.usermanagement.standard.wrapper.SharePassSettings");
	}

	@Override
	@SecureOnCall(priv = "SHARE_ITEM")
	@Transactional(propagation = Propagation.REQUIRED)
	public String add(Item item, String email, Date expiry)
	{
		String id = UUID.randomUUID().toString();

		SharePass pass = new SharePass();
		pass.setId(id);
		pass.setCreator(CurrentUser.getUserID());
		pass.setInstitution(CurrentInstitution.get());
		pass.setEmailAddress(email);
		pass.setItem(item);
		pass.setExpiry(expiry);

		// The following are currently not configurable in the GUI, and
		// are here for future expansion without requiring DB schema changes.
		pass.setPrivilege(SharePassPrivilege.VIEW);
		pass.setStarted(new Date());

		dao.save(pass);

		return id;
	}

	@Override
	@Transactional
	public void removeExpiredPasses()
	{
		Criterion c1 = Restrictions.lt("expiry", new Date());
		for( SharePass pass : dao.findAllByCriteria(c1) )
		{
			dao.delete(pass);
		}
	}

	@Override
	@Transactional
	public String activatePasses(String token)
	{
		SharePass pass = dao.findById(token);
		if( pass == null || !isValidDateRange(pass) )
		{
			return null;
		}

		final String email = pass.getEmailAddress();

		// Activate all inactive share passes with the same email address
		Criterion c1 = Restrictions.eq("institution", CurrentInstitution.get());
		Criterion c2 = Restrictions.eq("emailAddress", email);
		Criterion c3 = Restrictions.eq("activated", false);

		for( SharePass other : dao.findAllByCriteria(c1, c2, c3) )
		{
			if( !other.isActivated() && isValidDateRange(other) )
			{
				String expression = SecurityConstants.getRecipient(Recipient.SHARE_PASS, email);
				aclManager.addAccessEntry(other.getItem(), Node.ITEM, true, false, other.getPrivilege().getPriv(),
					expression, other.getExpiry());

				other.setActivated(true);
				dao.save(other);
			}
		}

		return email;
	}

	private boolean isValidDateRange(SharePass pass)
	{
		Date now = new Date();
		return pass.getStarted().before(now) && pass.getExpiry().after(now);
	}
}
