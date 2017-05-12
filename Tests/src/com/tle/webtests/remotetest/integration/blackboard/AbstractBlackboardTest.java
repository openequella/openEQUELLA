package com.tle.webtests.remotetest.integration.blackboard;

import com.tle.webtests.framework.TestInstitution;
import com.tle.webtests.test.AbstractCleanupTest;

@TestInstitution("blackboard")
public class AbstractBlackboardTest extends AbstractCleanupTest
{
	protected static final String ADMIN_USERNAME = "administrator";
	protected static final String ADMIN_PASSWORD = "``````";
	protected static final String COURSE_NAME = "EQUELLA TEST COURSE";
	protected static final String OAUTH_CLIENT_ID = "4cc26aca-d58b-4ce9-bd79-3edbc6921423";
	protected static final String OAUTH_CLIENT_SECRET = "5f781a8c-f69d-401e-b292-0f4aedb0f698";

	@Override
	protected void customisePageContext()
	{
		context.setIntegUrl(testConfig.getIntegrationUrl("blackboard"));
	}
}
