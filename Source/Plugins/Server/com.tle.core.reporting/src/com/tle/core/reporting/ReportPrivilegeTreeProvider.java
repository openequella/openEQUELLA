package com.tle.core.reporting;

import javax.inject.Inject;
import javax.inject.Singleton;

import com.tle.beans.entity.report.Report;
import com.tle.common.security.PrivilegeTree.Node;
import com.tle.core.guice.Bind;
import com.tle.core.security.AbstractEntityPrivilegeTreeProvider;
import com.tle.web.resources.ResourcesService;

@Bind
@Singleton
@SuppressWarnings("nls")
public class ReportPrivilegeTreeProvider extends AbstractEntityPrivilegeTreeProvider<Report>
{
	@Inject
	public ReportPrivilegeTreeProvider(ReportingService reportingService)
	{
		super(reportingService, Node.ALL_REPORTS, ResourcesService.getResourceHelper(ReportPrivilegeTreeProvider.class)
			.key("securitytree.allreports"), Node.REPORT, ResourcesService.getResourceHelper(
			ReportPrivilegeTreeProvider.class).key("securitytree.targetallreports"));
	}

	@Override
	protected Report createEntity()
	{
		return new Report();
	}
}
