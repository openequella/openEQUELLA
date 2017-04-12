package com.tle.core.lti.consumers.service;

import com.tle.common.lti.consumers.entity.LtiConsumer;
import com.tle.core.lti.consumers.service.session.LtiConsumerEditingBean;
import com.tle.core.services.entity.AbstractEntityService;

public interface LtiConsumerService extends AbstractEntityService<LtiConsumerEditingBean, LtiConsumer>
{
	@SuppressWarnings("nls")
	public static final String ENTITY_TYPE = "LTI_CONSUMER";

	LtiConsumer findByConsumerKey(String consumerKey);

}
