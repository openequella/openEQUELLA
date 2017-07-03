package com.tle.core.workflow.standard.filter;

import java.util.Collection;

import com.tle.core.guice.BindFactory;

/**
 * @author Aaron
 *
 */
@BindFactory
public interface WorkflowStandardFilterFactory
{
	CheckModerationForStepsFilter checkForSteps(Collection<Long> nodeIds, boolean forceModify);
}
