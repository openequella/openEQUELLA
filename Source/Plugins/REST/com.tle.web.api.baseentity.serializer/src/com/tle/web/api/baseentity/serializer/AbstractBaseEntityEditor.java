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

package com.tle.web.api.baseentity.serializer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.inject.Inject;

import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import com.tle.common.beans.exception.InvalidDataException;
import com.google.common.base.Strings;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.LanguageBundle.DeleteHandler;
import com.tle.common.beans.exception.ValidationError;
import com.tle.common.i18n.CurrentLocale;
import com.tle.common.i18n.beans.LanguageBundleBean;
import com.tle.common.i18n.beans.LanguageStringBean;
import com.tle.common.institution.CurrentInstitution;
import com.tle.common.interfaces.I18NString;
import com.tle.common.interfaces.I18NStrings;
import com.tle.common.security.TargetList;
import com.tle.common.security.TargetListEntry;
import com.tle.core.auditlog.AuditLogService;
import com.tle.core.entity.service.AbstractEntityService;
import com.tle.core.events.ApplicationEvent;
import com.tle.core.events.services.EventService;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.web.api.interfaces.beans.BaseEntityBean;
import com.tle.web.api.interfaces.beans.UserBean;
import com.tle.web.api.interfaces.beans.security.BaseEntitySecurityBean;
import com.tle.web.api.interfaces.beans.security.TargetListEntryBean;

/**
 * @author Aaron
 */
@NonNullByDefault
public abstract class AbstractBaseEntityEditor<BE extends BaseEntity, BEB extends BaseEntityBean>
	implements
		BaseEntityEditor<BE, BEB>,
		DeleteHandler
{
	@Inject
	private EventService eventService;
	@Inject
	private AuditLogService auditLogService;

	protected final BE entity;
	protected final boolean editing;
	protected final boolean importing;
	@Nullable
	protected final String stagingUuid;
	@Nullable
	protected final String lockId;
	protected boolean keepLocked;
	@Nullable
	private TargetList targetList;
	@Nullable
	private Map<Object, TargetList> otherTargetLists;

	protected abstract AbstractEntityService<?, BE> getEntityService();

	protected AbstractBaseEntityEditor(BE entity, @Nullable String stagingUuid, @Nullable String lockId,
		boolean editing, boolean importing)
	{
		this.entity = entity;
		this.editing = editing;
		this.stagingUuid = stagingUuid;
		this.lockId = lockId;
		this.importing = importing;
	}

	@Override
	public void setKeepLocked(boolean keepLocked)
	{
		this.keepLocked = keepLocked;
	}

	protected void copyCustomFields(BEB bean)
	{
		// No-op
	}

	protected void afterFinishedEditing()
	{
		// No-op
	}

	protected void copyBaseEntityFields(BEB source)
	{
		entity.setInstitution(CurrentInstitution.get());

		final String sourceUuid = source.getUuid();
		if( !editing )
		{
			if( Strings.isNullOrEmpty(sourceUuid) )
			{
				entity.setUuid(UUID.randomUUID().toString());
			}
			else
			{
				entity.setUuid(sourceUuid);
			}
		}
		else if( sourceUuid != null && !sourceUuid.equals(entity.getUuid()) )
		{
			//FIXME: i18n
			throw new InvalidDataException(new ValidationError("uuid",
				CurrentLocale.get("com.tle.web.api.baseentity.serializer.validation.uuidcannotchange")));
		}

		entity.setName(getBundle(entity.getName(), source.getNameStrings(), source.getName()));
		entity.setDescription(
			getBundle(entity.getDescription(), source.getDescriptionStrings(), source.getDescription()));
		if( importing )
		{
			final UserBean owner = source.getOwner();
			if( owner != null )
			{
				entity.setOwner(owner.getId());
			}

			entity.setDateCreated(source.getCreatedDate());
			entity.setDateModified(source.getModifiedDate());
		}
		else
		{
			entity.setOwner(CurrentUser.getUserID());

			final Date now = new Date();
			if( editing )
			{
				entity.setDateModified(now);
			}
			else
			{
				entity.setDateCreated(now);
				entity.setDateModified(now);
			}
		}

		targetList = getTargetList(source);
		otherTargetLists = getOtherTargetLists(source);
	}

	protected LanguageBundle getBundle(@Nullable LanguageBundle sourceBundle, @Nullable I18NStrings strings,
		@Nullable I18NString single)
	{
		return getBundle(sourceBundle, getStrings(strings, single));
	}

	@Nullable
	protected LanguageBundle getBundle(@Nullable LanguageBundle sourceBundle, @Nullable Map<String, String> strings)
	{
		if( strings == null && sourceBundle == null )
		{
			return null;
		}

		final LanguageBundleBean bundleBean = new LanguageBundleBean();
		final Map<String, LanguageStringBean> beanStrings = new HashMap<>();
		bundleBean.setStrings(beanStrings);
		if( strings != null )
		{
			for( Entry<String, String> string : strings.entrySet() )
			{
				final LanguageStringBean langString = new LanguageStringBean();
				langString.setLocale(string.getKey());
				langString.setText(string.getValue());
				beanStrings.put(string.getKey(), langString);
			}
		}
		final LanguageBundle bundle = sourceBundle;
		if( bundle != null )
		{
			bundle.ensureStrings();
		}
		return LanguageBundle.edit(bundle, bundleBean, this);
	}

	@Nullable
	protected Map<String, String> getStrings(@Nullable I18NStrings strings, @Nullable I18NString single)
	{
		if( strings != null )
		{
			return strings.getStrings();
		}
		else if( single != null )
		{
			final String string = single.toString();
			return Collections.singletonMap(CurrentLocale.getLocale().getLanguage(), string);
		}

		return null;
	}

	@Nullable
	protected TargetList getTargetList(BEB bean)
	{
		final BaseEntitySecurityBean security = bean.getSecurity();
		if( security == null )
		{
			return null;
		}
		final TargetList tl = new TargetList();

		tl.setPartial(false);

		final List<TargetListEntry> tles = new ArrayList<>();
		tl.setEntries(tles);
		final List<TargetListEntryBean> rules = security.getRules();
		if( rules != null )
		{
			for( TargetListEntryBean rule : rules )
			{
				TargetListEntry tle = new TargetListEntry(rule.isGranted(), rule.isOverride(), rule.getPrivilege(),
					rule.getWho());
				tles.add(tle);
			}
		}
		return tl;
	}

	@Nullable
	protected Map<Object, TargetList> getOtherTargetLists(BEB bean)
	{
		return null;
	}

	@Override
	public void doEdits(BEB bean)
	{
		copyBaseEntityFields(bean);
		copyCustomFields(bean);
	}

	@Override
	public void finishEditing()
	{
		getEntityService().save(entity, targetList, otherTargetLists, stagingUuid, lockId, keepLocked);
		if( editing )
		{
			auditLogService.logEntityModified(entity.getId());
		}
		else
		{
			auditLogService.logEntityCreated(entity.getId());
		}
		afterFinishedEditing();
	}

	@Override
	public void deleteBundleObject(Object obj)
	{
		((DeleteHandler) getEntityService()).deleteBundleObject(obj);
	}

	protected void publishEvent(ApplicationEvent<?> event)
	{
		eventService.publishApplicationEvent(event);
	}

	protected void publishEventAfterCommit(final ApplicationEvent<?> event)
	{
		TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronizationAdapter()
		{
			@Override
			public void afterCommit()
			{
				eventService.publishApplicationEvent(event);
			}
		});
	}
}
