/*
 * Created on 4/05/2006
 */
package com.tle.core.institution.convert;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.user.TLEGroup;
import com.tle.core.dao.user.TLEGroupDao;
import com.tle.core.guice.Bind;

@Bind
@Singleton
public class GroupConverter extends TreeNodeConverter<TLEGroup>
{
	@Inject
	private TLEGroupDao tleGroupDao;

	public GroupConverter()
	{
		super("groups", "groups.xml"); //$NON-NLS-1$ //$NON-NLS-2$
	}

	@Override
	public TLEGroupDao getDao()
	{
		return tleGroupDao;
	}

	@Override
	public ConverterId getConverterId()
	{
		return ConverterId.GROUPS;
	}

	@Override
	public Class<TLEGroup> getNodeClass()
	{
		return TLEGroup.class;
	}
}
