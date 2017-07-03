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

package com.tle.core.system.impl;

import java.util.Collection;

import javax.inject.Singleton;

import org.springframework.transaction.annotation.Transactional;

import com.google.inject.Inject;
import com.tle.common.Check;
import com.tle.common.hash.Hash;
import com.tle.core.guice.Bind;
import com.tle.core.hibernate.SystemDatabase;
import com.tle.core.hibernate.event.SchemaListener;
import com.tle.core.security.impl.SecureOnCallSystem;
import com.tle.core.events.services.EventService;
import com.tle.core.system.SystemConfigService;
import com.tle.core.system.dao.SystemConfigDao;
import com.tle.core.system.events.ServerMessageChangeListener;
import com.tle.core.system.events.ServerMessageChangedEvent;
import com.tle.exceptions.BadCredentialsException;

@Singleton
@SystemDatabase
@SuppressWarnings("nls")
@Bind(SystemConfigService.class)
public class SystemConfigServiceImpl implements SystemConfigService, ServerMessageChangeListener, SchemaListener
{
	private static final String ADMIN_PASSWORD = "admin.password";
	private static final String ADMIN_EMAILS = "admin.emails";
	private static final String SMTP_SERVER = "smtpserver";
	private static final String SMTP_USER = "smtpuser";
	private static final String SMTP_PASSWORD = "smtppassword";
    private static final String NO_REPLY_SENDER = "noreplysender";
	private static final String LICENSE = "license";
	private static final String SERVER_MESSAGE = "servermessage";
	private static final String SERVER_MESSAGE_ENABLED = "servermessageenabled";
	private static final String SCHEDULED_TASKS = "scheduled.tasks";
	private static final String UNIQUE_INCREMENTING_ID = "unique.id";

	@Inject
	private SystemConfigDao systemConfigDao;
	@Inject
	private EventService eventService;

	private boolean serverMessageEnabled;
	private String serverMessage;
	private boolean serverMessageDirty = true;
	private boolean systemUp;

	@Override
	@Transactional
	public void checkAdminPassword(String password)
	{
		if( password == null )
		{
			throw new BadCredentialsException("No password given");
		}

		if( !Hash.checkPasswordMatch(_getAdminPassword(), password) )
		{
			throw new BadCredentialsException("Password is incorrect.");
		}
	}

	@Override
	@SecureOnCallSystem
	@Transactional
	public void setAdminPassword(String oldPass, String newPass)
	{
		checkAdminPassword(oldPass);
		_setAdminPassword(newPass);
	}

	@Override
	@Transactional
	public boolean adminPasswordNotSet()
	{
		return _getAdminPassword() == null;
	}

	@Override
	@Transactional
	@SecureOnCallSystem
	public void setInitialAdminPassword(String newPass)
	{
		if( adminPasswordNotSet() )
		{
			_setAdminPassword(newPass);
		}
		else
		{
			throw new BadCredentialsException("Admin password has already been set.");
		}
	}

	private void _setAdminPassword(String password)
	{
		if( Check.isEmpty(password) )
		{
			throw new BadCredentialsException("Password must not be left empty.");
		}

		systemConfigDao.updateConfig(ADMIN_PASSWORD, Hash.hashPassword(password));
	}

	private String _getAdminPassword()
	{
		return systemConfigDao.getConfig(ADMIN_PASSWORD);
	}

	@Override
	@Transactional
	@SecureOnCallSystem
	public void setLicense(String string)
	{
		systemConfigDao.updateConfig(LICENSE, string);
	}

	@Override
	@Transactional
	public String getLicense()
	{
		return systemConfigDao.getConfig(LICENSE);
	}

	@Override
	@Transactional
	public String getEmails()
	{
		return systemConfigDao.getConfig(ADMIN_EMAILS);

	}

	@Override
	@Transactional
	@SecureOnCallSystem
	public void setEmails(String emails)
	{
		systemConfigDao.updateConfig(ADMIN_EMAILS, emails);
	}

	@Override
	@Transactional
	public String getSmtpServer()
	{
		return systemConfigDao.getConfig(SMTP_SERVER);
	}

	@Override
	@Transactional
	@SecureOnCallSystem
	public void setSmtpServer(String smtp)
	{
		systemConfigDao.updateConfig(SMTP_SERVER, smtp);

	}

	@Override
	@Transactional
	public String getSmtpUser()
	{
		return systemConfigDao.getConfig(SMTP_USER);
	}

	@Override
	@Transactional
	@SecureOnCallSystem
	public void setSmtpUser(String smtpUser)
	{
		systemConfigDao.updateConfig(SMTP_USER, smtpUser);
	}

	@Override
	@Transactional
	public String getSmtpPassword()
	{
		return systemConfigDao.getConfig(SMTP_PASSWORD);
	}

	@Override
	@Transactional
	@SecureOnCallSystem
	public void setSmtpPassword(String smtpPassword)
	{
		systemConfigDao.updateConfig(SMTP_PASSWORD, smtpPassword);
	}


    @Override
    @Transactional
    public String getNoReplySender()
    {
        return systemConfigDao.getConfig(NO_REPLY_SENDER);
    }

    @Override
    @Transactional
    @SecureOnCallSystem
    public void setNoReplySender(String sender)
    {
        systemConfigDao.updateConfig(NO_REPLY_SENDER, sender);
    }


	private void updateServerMessageIfDirty()
	{
		if( serverMessageDirty )
		{
			serverMessageEnabled = Boolean.valueOf(systemConfigDao.getConfig(SERVER_MESSAGE_ENABLED));
			serverMessage = systemConfigDao.getConfig(SERVER_MESSAGE);
			serverMessageDirty = false;
		}
	}

	@Override
	@Transactional
	public boolean isServerMessageEnabled()
	{
		updateServerMessageIfDirty();
		return serverMessageEnabled;
	}

	@Override
	@Transactional
	public String getServerMessage()
	{
		updateServerMessageIfDirty();
		return serverMessage;
	}

	@Override
	@Transactional
	public void setServerMessage(String serverMessage, boolean serverMessageEnabled)
	{
		systemConfigDao.updateConfig(SERVER_MESSAGE, serverMessage);
		systemConfigDao.updateConfig(SERVER_MESSAGE_ENABLED, String.valueOf(serverMessageEnabled));

		this.serverMessage = serverMessage;
		this.serverMessageEnabled = serverMessageEnabled;

		eventService.publishApplicationEvent(new ServerMessageChangedEvent());
	}

	@Override
	public void serverMessageChangedEvent(ServerMessageChangedEvent event)
	{
		this.serverMessage = null;
		this.serverMessageEnabled = false;
		this.serverMessageDirty = true;
	}

	@Override
	@Transactional
	public String getScheduledTasksConfig()
	{
		return systemConfigDao.getConfig(SCHEDULED_TASKS);
	}

	@Override
	@Transactional
	public void setScheduleTasksConfig(String config)
	{
		systemConfigDao.updateConfig(SCHEDULED_TASKS, config);
	}

	@Override
	@Transactional
	public long createUniqueInstitutionId()
	{
		return systemConfigDao.getAndIncrement(UNIQUE_INCREMENTING_ID);
	}

	@Override
	@Transactional
	public void registerInstitutionIdInUse(long id)
	{
		systemConfigDao.increaseToAtLeast(UNIQUE_INCREMENTING_ID, id);
	}

	@Override
	public void systemSchemaUp()
	{
		systemUp = true;
	}

	@Override
	public void schemasAvailable(Collection<Long> schemas)
	{
		// who cares
	}

	@Override
	public void schemasUnavailable(Collection<Long> schemas)
	{
		// who cares
	}

	@Override
	public boolean isSystemSchemaUp()
	{
		return systemUp;
	}

}
