package com.tle.core.powersearch;

import java.util.List;

import com.tle.beans.entity.BaseEntityLabel;
import com.tle.beans.entity.PowerSearch;
import com.tle.beans.entity.Schema;
import com.tle.core.remoting.RemotePowerSearchService;
import com.tle.core.services.entity.AbstractEntityService;
import com.tle.core.services.entity.EntityEditingBean;

/**
 * @author Nicholas Read
 */
public interface PowerSearchService
	extends
		AbstractEntityService<EntityEditingBean, PowerSearch>,
		RemotePowerSearchService
{
	List<BaseEntityLabel> listSearchable();

	List<BaseEntityLabel> listAllForSchema(long schemaId);

	List<BaseEntityLabel> listAllForSchema(Schema schema);
}
