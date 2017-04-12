package com.tle.web.api.baseentity.serializer;

import com.dytech.edge.common.LockedException;
import com.dytech.edge.exceptions.InvalidDataException;
import com.tle.annotation.NonNullByDefault;
import com.tle.annotation.Nullable;
import com.tle.beans.entity.BaseEntity;
import com.tle.exceptions.AccessDeniedException;
import com.tle.web.api.interfaces.beans.BaseEntityBean;

/**
 * @author Aaron
 */
@NonNullByDefault
public interface BaseEntitySerializer<BE extends BaseEntity, BEB extends BaseEntityBean>
{
	BEB serialize(BE entity, @Nullable Object data, boolean heavy);

	@Nullable
	BE deserializeEdit(BEB bean, @Nullable String stagingUuid, @Nullable String lockId, boolean keepLocked)
		throws LockedException, AccessDeniedException, InvalidDataException;

	BE deserializeNew(BEB bean, @Nullable String stagingUuid) throws AccessDeniedException, InvalidDataException;
}
