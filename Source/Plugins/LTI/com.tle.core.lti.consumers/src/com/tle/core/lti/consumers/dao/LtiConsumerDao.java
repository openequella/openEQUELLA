package com.tle.core.lti.consumers.dao;

import com.tle.common.lti.consumers.entity.LtiConsumer;
import com.tle.core.dao.AbstractEntityDao;

public interface LtiConsumerDao extends AbstractEntityDao<LtiConsumer>
{
	LtiConsumer findByConsumerKey(String consumerKey);
}
