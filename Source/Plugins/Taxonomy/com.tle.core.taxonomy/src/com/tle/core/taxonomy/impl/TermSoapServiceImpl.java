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

package com.tle.core.taxonomy.impl;

import java.util.List;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.dytech.edge.common.LockedException;
import com.tle.common.taxonomy.Taxonomy;
import com.tle.core.entity.service.EntityLockingService;
import com.tle.core.guice.Bind;
import com.tle.core.taxonomy.TaxonomyService;
import com.tle.core.taxonomy.TermService;
import com.tle.core.taxonomy.TermSoapService;
import com.tle.common.usermanagement.user.CurrentUser;

/**
 * Ensure we have a taxonomy lock before doing any operations that modify the
 * term tree.
 */
@Singleton
@Bind(TermSoapService.class)
@SuppressWarnings("nls")
public class TermSoapServiceImpl implements TermSoapService
{
	@Inject
	private EntityLockingService lockingService;
	@Inject
	private TaxonomyService taxonomyService;
	@Inject
	private TermService termService;

	@Override
	public void deleteTerm(String taxonomyUuid, String termFullPath)
	{
		termService.deleteTerm(t(taxonomyUuid), termFullPath);
	}

	@Override
	public String getData(String taxonomyUuid, String termFullPath, String dataKey)
	{
		return termService.getData(t(taxonomyUuid), termFullPath, dataKey);
	}

	@Override
	public void insertTerm(String taxonomyUuid, String parentFullPath, String term, int index)
	{
		termService.insertTerm(t(taxonomyUuid), parentFullPath, term, index);
	}

	@Override
	public String[] listTerms(String taxonomyUuid, String parentFullPath)
	{
		List<String> rv = termService.listTerms(t(taxonomyUuid), parentFullPath);
		return rv.toArray(new String[rv.size()]);
	}

	@Override
	public void move(String taxonomyUuid, String termToMove, String parent, int index)
	{
		termService.move(t(taxonomyUuid), termToMove, parent, index);
	}

	@Override
	public void renameTermValue(String taxonomyUuid, String termToRename, String newValue)
	{
		termService.renameTermValue(t(taxonomyUuid), termToRename, newValue);
	}

	@Override
	public void setData(String taxonomyUuid, String termFullPath, String dataKey, String dataValue)
	{
		termService.setData(t(taxonomyUuid), termFullPath, dataKey, dataValue);
	}

	@Override
	public void lockTaxonomyForEditing(String taxonomyUuid)
	{
		try
		{
			lockingService.lockEntity(t(taxonomyUuid));
		}
		catch( LockedException ex )
		{
			if( CurrentUser.getUserID().equals(ex.getUserID()) )
			{
				throw new LockedException(
					"Taxonomy is locked in a different session.  Call unlockTaxonomy with a force parameter value of true.",
					ex.getUserID(), ex.getSessionID(), ex.getEntityId());
			}
			else
			{
				throw new LockedException("Taxonomy is locked by another user: " + ex.getUserID(), ex.getUserID(),
					ex.getSessionID(), ex.getEntityId());
			}
		}
	}

	@Override
	public void unlockTaxonomy(String taxonomyUuid, boolean force)
	{
		try
		{
			lockingService.unlockEntity(t(taxonomyUuid), force);
		}
		catch( LockedException ex )
		{
			if( CurrentUser.getUserID().equals(ex.getUserID()) )
			{
				throw new RuntimeException(
					"Taxonomy is locked in a different session.  Call unlockTaxonomy with a force parameter value of true.");
			}
			else
			{
				throw new RuntimeException("You do not own the lock on this taxonomy.  It is held by user ID " //$NON-NLS-1$
					+ ex.getUserID());
			}
		}
	}

	private Taxonomy t(String taxonomyUuid)
	{
		Taxonomy tax = taxonomyService.getByUuid(taxonomyUuid);
		if( tax == null )
		{
			throw new RuntimeException("The taxonomy with UUID of " + taxonomyUuid + " could not be found");
		}
		return tax;
	}
}
