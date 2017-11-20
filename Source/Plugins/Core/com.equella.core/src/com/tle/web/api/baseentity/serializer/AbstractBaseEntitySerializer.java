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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.dytech.edge.common.LockedException;
import com.tle.common.beans.exception.InvalidDataException;
import com.google.common.collect.Lists;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.BaseEntity;
import com.tle.beans.entity.LanguageBundle;
import com.tle.beans.entity.LanguageString;
import com.tle.common.beans.exception.ValidationError;
import com.tle.common.interfaces.I18NString;
import com.tle.common.interfaces.I18NStrings;
import com.tle.common.interfaces.SimpleI18NString;
import com.tle.common.interfaces.equella.BundleString;
import com.tle.common.interfaces.equella.SimpleI18NStrings;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.common.security.TargetList;
import com.tle.common.security.TargetListEntry;
import com.tle.core.entity.service.AbstractEntityService;
import com.tle.core.security.TLEAclManager;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.api.interfaces.beans.BaseEntityBean;
import com.tle.web.api.interfaces.beans.UserBean;
import com.tle.web.api.interfaces.beans.security.BaseEntitySecurityBean;
import com.tle.web.api.interfaces.beans.security.TargetListEntryBean;

/**
 * @author Aaron
 */
@NonNullByDefault
public abstract class AbstractBaseEntitySerializer<BE extends BaseEntity, BEB extends BaseEntityBean, ED extends BaseEntityEditor<BE, BEB>>
	implements
		BaseEntitySerializer<BE, BEB>
{
	@Inject
	protected TLEAclManager aclManager;

	protected abstract AbstractEntityService<?, BE> getEntityService();

	protected abstract ED createNewEditor(BE entity, @Nullable String stagingUuid, boolean importing);

	protected abstract ED createExistingEditor(BE entity, @Nullable String stagingUuid, @Nullable String lockId,
		boolean importing);

	protected abstract BE createEntity();

	protected abstract BEB createBean();

	protected abstract Node getNonVirtualNode();

	@Override
	public BEB serialize(BE entity, @Nullable Object data, boolean heavy)
	{
		final BEB bean = createBean();
		copyBaseEntityFields(entity, bean, heavy);
		copyCustomLightweightFields(entity, bean, data);
		if( heavy )
		{
			copyCustomFields(entity, bean, data);
		}
		return bean;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	@Nullable
	@Override
	public BE deserializeEdit(@Nullable String uuid, BEB bean, @Nullable String stagingUuid, @Nullable String lockId,
		boolean keepLocked, boolean importing) throws LockedException, AccessDeniedException
	{
		validateBean(bean, false);
		final BE entity = getEntity(uuid, false);
		if( entity == null )
		{
			return null;
		}
		return doEdit(entity, bean, stagingUuid, lockId, keepLocked, true, importing);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	protected BE doEdit(BE entity, BEB bean, @Nullable String stagingUuid, @Nullable String lockId, boolean keepLocked,
		boolean editing, boolean importing)
	{
		ED entityEditor = getEntityEditor(entity, stagingUuid, lockId, keepLocked, editing, importing);
		entityEditor.doEdits(bean);
		entityEditor.finishEditing();
		return entity;
	}

	private ED getEntityEditor(BE entity, @Nullable String stagingUuid, @Nullable String lockId, boolean keepLocked,
		boolean editing, boolean importing)
	{
		if( editing )
		{
			final ED editor = createExistingEditor(entity, stagingUuid, lockId, importing);
			editor.setKeepLocked(keepLocked);
			return editor;
		}
		return createNewEditor(entity, stagingUuid, importing);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	@Override
	public BE deserializeNew(BEB bean, @Nullable String stagingUuid, boolean importing)
		throws AccessDeniedException, InvalidDataException
	{
		validateBean(bean, true);
		final BE entity = createEntity();
		return doEdit(entity, bean, stagingUuid, null, false, false, importing);
	}

	@Nullable
	protected BE getEntity(@Nullable String uuid, boolean create)
	{
		//final String uuid = bean.getUuid();
		final BE entity;
		if( !create && uuid != null )
		{
			entity = getEntityService().getForRestEdit(uuid);
		}
		else if( create )
		{
			entity = createEntity();
		}
		else
		{
			entity = null;
		}
		return entity;
	}

	protected void validateBean(BEB bean, boolean create)
	{
		final List<ValidationError> errors = new ArrayList<>();
		validateBaseEntityFields(bean, create, errors);
		validateCustom(bean, create, errors);
		if( errors.size() != 0 )
		{
			throw new InvalidDataException(errors);
		}
	}

	protected void validateCustom(BEB bean, boolean create, List<ValidationError> errors)
	{
		// No-op
	}

	protected void copyCustomFields(BE entity, BEB bean, @Nullable Object data)
	{
		// No-op
	}

	protected void copyCustomLightweightFields(BE entity, BEB bean, @Nullable Object data)
	{
		// No-op
	}

	protected void validateBaseEntityFields(BEB bean, boolean create, List<ValidationError> errors)
	{
		// Nothing to do?
	}

	protected void copyBaseEntityFields(BaseEntity source, BaseEntityBean target, boolean heavy)
	{
		target.setUuid(source.getUuid());

		target.setName(getI18NString(source.getName(), source.getUuid()));
		target.setNameStrings(getI18NStrings(source.getName(), true));
		if( heavy )
		{
			target.setDescription(getI18NString(source.getDescription(), null));
			target.setDescriptionStrings(getI18NStrings(source.getDescription(), false));

			target.setOwner(new UserBean(source.getOwner()));
			target.setCreatedDate(source.getDateCreated());
			target.setModifiedDate(source.getDateModified());

			List<TargetListEntryBean> targetListEntryBean = getEntityACLs(source);
			BaseEntitySecurityBean security = new BaseEntitySecurityBean();
			security.setRules(targetListEntryBean);
			target.setSecurity(security);

		}
		// attributes?
	}

	protected List<TargetListEntryBean> getEntityACLs(BaseEntity source)
	{
		TargetList targetForEntity = aclManager.getTargetList(getNonVirtualNode(), source);
		return convertToTargetListEntryBean(targetForEntity);
	}

	protected List<TargetListEntryBean> convertToTargetListEntryBean(TargetList targetForEntity)
	{
		List<TargetListEntryBean> targetListEntryBean = Lists.newArrayList();
		for( TargetListEntry entry : targetForEntity.getEntries() )
		{
			TargetListEntryBean entryBean = new TargetListEntryBean();
			entryBean.setGranted(entry.isGranted());
			entryBean.setWho(entry.getWho());
			entryBean.setPrivilege(entry.getPrivilege());
			entryBean.setOverride(entry.isOverride());
			targetListEntryBean.add(entryBean);
		}

		return targetListEntryBean.isEmpty() ? Collections.emptyList() : targetListEntryBean;
	}

	@Nullable
	protected I18NString getI18NString(@Nullable LanguageBundle bundle, @Nullable String defaultValue)
	{
		if( bundle != null )
		{
			return new BundleString(bundle, defaultValue);
		}
		if( defaultValue != null )
		{
			return new SimpleI18NString(defaultValue);
		}
		return null;
	}

	@Nullable
	protected I18NStrings getI18NStrings(@Nullable LanguageBundle bundle, boolean emptyDefault)
	{
		if( bundle != null )
		{
			final Map<String, LanguageString> strings = bundle.getStrings();
			if( strings != null )
			{
				return new SimpleI18NStrings(strings);
			}
		}
		if( emptyDefault )
		{
			return new SimpleI18NStrings(new HashMap<String, LanguageString>());
		}
		// if (defaultValue != null)
		// {
		// final Map<String, LanguageString> strings = new HashMap<>();
		// strings.put(CurrentLocale.getLocale().getLanguage(), defaultValue);
		// return new SimpleI18NStrings(strings);
		// }
		return null;
	}
}
