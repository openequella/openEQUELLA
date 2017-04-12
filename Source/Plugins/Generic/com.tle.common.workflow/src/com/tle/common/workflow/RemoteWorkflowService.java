/*
 * Created on 18/04/2006
 */
package com.tle.common.workflow;

import com.tle.core.remoting.RemoteAbstractEntityService;

public interface RemoteWorkflowService extends RemoteAbstractEntityService<Workflow>
{
	String ENTITY_TYPE = "WORKFLOW"; //$NON-NLS-1$
}
