package com.tle.web.api.baseentity.serializer;

import com.tle.annotation.NonNullByDefault;
import com.tle.beans.entity.BaseEntity;
import com.tle.web.api.interfaces.beans.BaseEntityBean;

/**
 * @author Aaron
 */
@NonNullByDefault
public interface BaseEntityEditor<BE extends BaseEntity, BEB extends BaseEntityBean>
{
	void doEdits(BEB bean);

	void finishEditing();

	void setKeepLocked(boolean keepLocked);
}
