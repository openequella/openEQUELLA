package com.tle.core.services.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.springframework.beans.factory.annotation.Required;

import com.dytech.common.GeneralConstants;
import com.dytech.edge.exceptions.QuotaExceededException;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.tle.beans.Institution;
import com.tle.beans.filesystem.FileHandle;
import com.tle.beans.item.Item;
import com.tle.beans.system.QuotaSettings;
import com.tle.beans.system.QuotaSettings.UserQuota;
import com.tle.common.Check;
import com.tle.core.filesystem.InstitutionFile;
import com.tle.core.filesystem.StagingFile;
import com.tle.core.guice.Bind;
import com.tle.core.institution.InstitutionService;
import com.tle.core.quota.QuotaPolicy;
import com.tle.core.security.impl.AclExpressionEvaluator;
import com.tle.core.services.FileSystemService;
import com.tle.core.services.QuotaService;
import com.tle.core.services.config.ConfigurationService;
import com.tle.core.user.CurrentUser;
import com.tle.core.user.UserState;

/**
 * @author Charles O'Farrell
 */
@Bind(QuotaService.class)
@Singleton
public class QuotaServiceImpl implements QuotaService
{
	@Inject
	private FileSystemService fileSystemService;
	@Inject
	private ConfigurationService configService;
	@Inject
	private QuotaPolicy policy;
	@Inject
	private InstitutionService institutionService;

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
			long limit = policy.getLimit(quotas);
			long currentTotalFileSize = policy.calculateUserFileSize(state.getUserBean());

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

	// This is the only once currently
	@Required
	public void setPolicy(QuotaPolicy policy)
	{
		this.policy = policy;
	}

	@Override
	public long getInstitutionalConsumption(Institution inst)
	{
		Long cachedConsumption = cachedInstitutionConsumption.getIfPresent(inst.getUniqueId());
		if( cachedConsumption != null )
		{
			return cachedConsumption;
		}
		cachedInstitutionConsumption.put(inst.getUniqueId(), getFileSize(new InstitutionFile(inst)));

		return cachedInstitutionConsumption.getIfPresent(inst.getUniqueId());
	}

	@Override
	public void refreshCache(Institution inst)
	{
		cachedInstitutionConsumption.put(inst.getUniqueId(), getFileSize(new InstitutionFile(inst)));
	}

}
