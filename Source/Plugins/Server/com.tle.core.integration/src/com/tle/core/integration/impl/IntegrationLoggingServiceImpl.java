package com.tle.core.integration.impl;

import java.util.Collection;
import java.util.Date;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.tle.beans.audit.AuditLogTable;
import com.tle.beans.item.ItemId;
import com.tle.core.auditlog.AuditLogExtension;
import com.tle.core.auditlog.AuditLogExtensionDao;
import com.tle.core.guice.Bind;
import com.tle.core.integration.IntegrationLoggingDao;
import com.tle.core.integration.IntegrationLoggingService;
import com.tle.core.integration.IntegrationSelection;
import com.tle.core.integration.beans.AuditLogLms;
import com.tle.core.user.CurrentInstitution;
import com.tle.core.user.CurrentUser;

@Bind(IntegrationLoggingService.class)
@Singleton
public class IntegrationLoggingServiceImpl implements IntegrationLoggingService, AuditLogExtension
{
	@Inject
	private IntegrationLoggingDao integrationLoggingDao;

	@Override
	@Transactional(propagation = Propagation.REQUIRED)
	public void logSelections(Collection<IntegrationSelection> selections)
	{
		for( IntegrationSelection selection : selections )
		{
			AuditLogLms lmsAudit = new AuditLogLms();
			lmsAudit.setInstitution(CurrentInstitution.get());
			lmsAudit.setUserId(CurrentUser.getUserID());
			lmsAudit.setSessionId(CurrentUser.getSessionID());
			lmsAudit.setTimestamp(new Date());
			ItemId itemId = selection.getItemId();
			lmsAudit.setUuid(itemId.getUuid());
			lmsAudit.setVersion(itemId.getVersion());
			lmsAudit.setLatest(selection.isLatest());
			lmsAudit.setType(selection.getType());
			lmsAudit.setResource(selection.getResource());
			lmsAudit.setContentType(selection.getContentType());
			lmsAudit.setSelected(selection.getSelection());
			integrationLoggingDao.save(lmsAudit);
		}
	}

	@Override
	public AuditLogExtensionDao<? extends AuditLogTable> getDao()
	{
		return integrationLoggingDao;
	}

	@SuppressWarnings("nls")
	@Override
	public String getShortName()
	{
		return "integration";
	}
}
