package com.tle.web.api.baseentity.serializer;

import com.tle.beans.entity.BaseEntity;
import com.tle.web.api.interfaces.beans.BaseEntityBean;

/**
 * @author Aaron
 */
public abstract class AbstractEquellaBaseEntitySerializer<BE extends BaseEntity, BEB extends BaseEntityBean, ED extends BaseEntityEditor<BE, BEB>>
	extends
		AbstractBaseEntitySerializer<BE, BEB, ED>
{
	@Override
	protected void copyBaseEntityFields(BaseEntity source, BaseEntityBean target, boolean heavy)
	{
		super.copyBaseEntityFields(source, target, heavy);
		// Jolse says don't do it properly
		// target.set("systemType", source.isSystemType());
	}
}
