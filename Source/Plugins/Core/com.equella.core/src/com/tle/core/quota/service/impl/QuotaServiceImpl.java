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

package com.tle.core.quota.service.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.springframework.transaction.annotation.Transactional;

import com.dytech.common.GeneralConstants;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.Institution;
import com.tle.beans.item.Item;
import com.tle.common.Check;
import com.tle.common.NameValue;
import com.tle.common.PathUtils;
import com.tle.common.filesystem.handle.AllInstitutionsFile;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.filesystem.handle.StagingFile;
import com.tle.common.quota.exception.QuotaExceededException;
import com.tle.common.quota.settings.QuotaSettings;
import com.tle.common.quota.settings.QuotaSettings.UserQuota;
import com.tle.common.usermanagement.user.CurrentUser;
import com.tle.common.usermanagement.user.UserState;
import com.tle.common.usermanagement.user.valuebean.UserBean;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.quota.dao.QuotaDao;
import com.tle.core.quota.service.QuotaService;
import com.tle.core.security.impl.AclExpressionEvaluator;
import com.tle.core.services.FileSystemService;
import com.tle.core.settings.service.ConfigurationService;

/**
 * @author Charles O'Farrell
 */
@NonNullByDefault
@Bind(QuotaService.class)
@Singleton
public class QuotaServiceImpl implements QuotaService
{
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private ConfigurationService configService;
	@Inject
	private InstitutionService institutionService;
	@Inject
	private QuotaDao quotaDao;

	private final Cache<Long, Long> cachedInstitutionConsumption = CacheBuilder.newBuilder()
		.expireAfterWrite(24, TimeUnit.HOURS).softValues().build();

	public QuotaServiceImpl()
	{
		super();
	}

	@Override
	public long checkQuotaAndReturnNewItemSize(Item item, StagingFile stagingFile1) throws QuotaExceededException
	{
		QuotaSettings quotaSettings = configService.getProperties(new QuotaSettings());
		AclExpressionEvaluator evaluator = new AclExpressionEvaluator();
		UserState state = CurrentUser.getUserState();
		List<UserQuota> quotas = new ArrayList<UserQuota>();
		for( UserQuota q : quotaSettings.getQuotas() )
		{
			String expression = q.getExpression();
			if( !Check.isEmpty(expression) && evaluator.evaluate(expression, state, false) )
			{
				quotas.add(q);
			}
		}

		long newFileSize = getFileSize(stagingFile1);
		long oldFileSize = item.getTotalFileSize();

		// If there's a user quota, impose its logic first
		if( !quotas.isEmpty() )
		{
			long limit = getHighestLimit(quotas);
			long currentTotalFileSize = calculateUserFileSize(state.getUserBean());

			long expectedTotalSize = currentTotalFileSize + newFileSize - oldFileSize;
			if( expectedTotalSize > limit )
			{
				throw new QuotaExceededException(currentTotalFileSize, limit);
			}
		}

		// no need to complain if there are no quotas, or quotas not exceeded
		return newFileSize;
	}

	@Override
	public Collection<Institution> getInstitutionsWithFilestoreLimits()
	{
		Collection<Institution> availableInsts = institutionService.enumerateAvailable();
		List<Institution> limitedInsts = new ArrayList<Institution>();

		for( Institution inst : availableInsts )
		{
			if( inst.getQuota() > 0 )
			{
				limitedInsts.add(inst);
			}
		}
		return limitedInsts;
	}

	@Override
	public long getFileSize(FileHandle file)
	{
		try
		{
			return fileSystemService.recursivefileLength(file, null);
		}
		catch( IOException e )
		{
			// Should never happen
			throw new RuntimeException(e);
		}
	}

	@Override
	public boolean isInstitutionOverLimit(Institution inst)
	{
		long inUse = getInstitutionalConsumption(inst);
		double policyLimitBytes = inst.getQuota() * GeneralConstants.GIGABYTE;

		return (inUse > policyLimitBytes);
	}

	@Override
	public long getInstitutionalConsumption(Institution inst)
	{
		Long cachedConsumption = cachedInstitutionConsumption.getIfPresent(inst.getUniqueId());
		if( cachedConsumption != null )
		{
			return cachedConsumption;
		}
		cachedInstitutionConsumption.put(inst.getUniqueId(), calculateUsage(inst));

		return cachedInstitutionConsumption.getIfPresent(inst.getUniqueId());
	}

	@Override
	public void refreshCache(Institution inst)
	{
		cachedInstitutionConsumption.put(inst.getUniqueId(), calculateUsage(inst));
	}

	private long calculateUsage(Institution inst)
	{
		long totes = 0;
		for( NameValue filestore : fileSystemService.listFilestores() )
		{
			final String filestoreId = filestore.getValue();
			totes += getFileSize(new FakeInstitutionFile(inst, filestoreId.equals("default") ? null : filestoreId));
		}
		return totes;
	}

	private long getHighestLimit(List<UserQuota> quotas)
	{
		long l = -1;
		for( UserQuota q : quotas )
		{
			l = Math.max(l, q.getSize());
		}
		if( l == -1 )
		{
			l = Long.MAX_VALUE;
		}
		return l;
	}

	@Transactional
	private long calculateUserFileSize(UserBean userBean)
	{
		return quotaDao.calculateUserFileSize(userBean);
	}

	public static class FakeInstitutionFile extends AllInstitutionsFile
	{
		private static final long serialVersionUID = 1L;

		private final Institution institution;
		@Nullable
		private final String filestoreId;

		public FakeInstitutionFile(Institution institution, String filestoreId)
		{
			this.institution = institution;
			this.filestoreId = filestoreId;
		}

		@Override
		protected String createAbsolutePath()
		{
			return PathUtils.filePath(super.createAbsolutePath(), institution.getFilestoreId());
		}

		@Nullable
		@Override
		public String getFilestoreId()
		{
			return filestoreId;
		}
	}
}
