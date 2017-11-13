/*
 * Copyright 2017 Apereo
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.tle.web.institution.tab;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import com.dytech.edge.common.Version;
import com.google.common.collect.Maps;
import com.tle.beans.Institution;
import com.tle.common.FileSizeUtils;
import com.tle.common.filesystem.handle.FileHandle;
import com.tle.common.i18n.CurrentLocale;
import com.tle.core.filesystem.InstitutionFile;
import com.tle.core.healthcheck.HealthCheckService;
import com.tle.core.healthcheck.listeners.bean.ServiceStatus;
import com.tle.core.healthcheck.listeners.bean.ServiceStatus.Status;
import com.tle.core.institution.ClusterInfoService;
import com.tle.core.quota.service.QuotaService;
import com.tle.core.services.ApplicationVersion;
import com.tle.core.services.TaskService;
import com.tle.core.services.TaskStatus;
import com.tle.core.services.UrlService;
import com.tle.core.zookeeper.ZookeeperService;
import com.tle.web.freemarker.FreemarkerFactory;
import com.tle.web.freemarker.annotations.ViewFactory;
import com.tle.web.institution.AbstractInstitutionTab;
import com.tle.web.institution.InstitutionSection;
import com.tle.web.sections.SectionInfo;
import com.tle.web.sections.SectionResult;
import com.tle.web.sections.SectionTree;
import com.tle.web.sections.ajax.AjaxGenerator;
import com.tle.web.sections.ajax.AjaxGenerator.EffectType;
import com.tle.web.sections.ajax.handler.AjaxFactory;
import com.tle.web.sections.ajax.handler.UpdateDomFunction;
import com.tle.web.sections.annotations.EventFactory;
import com.tle.web.sections.annotations.EventHandlerMethod;
import com.tle.web.sections.equella.annotation.PlugKey;
import com.tle.web.sections.equella.annotation.PlugURL;
import com.tle.web.sections.equella.annotation.PluginResourceHandler;
import com.tle.web.sections.events.RenderEventContext;
import com.tle.web.sections.events.js.EventGenerator;
import com.tle.web.sections.events.js.JSHandler;
import com.tle.web.sections.jquery.libraries.JQueryCore;
import com.tle.web.sections.jquery.libraries.JQueryUICore;
import com.tle.web.sections.js.JSCallAndReference;
import com.tle.web.sections.js.JSCallable;
import com.tle.web.sections.js.generic.OverrideHandler;
import com.tle.web.sections.js.generic.function.ExternallyDefinedFunction;
import com.tle.web.sections.js.generic.function.IncludeFile;
import com.tle.web.sections.render.Label;
import com.tle.web.sections.render.TextLabel;
import com.tle.web.sections.result.util.IconLabel;
import com.tle.web.sections.result.util.IconLabel.Icon;
import com.tle.web.sections.result.util.KeyLabel;
import com.tle.web.sections.standard.AbstractTable.Sort;
import com.tle.web.sections.standard.Checkbox;
import com.tle.web.sections.standard.Table;
import com.tle.web.sections.standard.annotations.Component;
import com.tle.web.sections.standard.model.HtmlLinkState;
import com.tle.web.sections.standard.model.TableState;
import com.tle.web.sections.standard.model.TableState.TableCell;
import com.tle.web.sections.standard.renderers.LinkRenderer;

@SuppressWarnings("nls")
public class HealthTab extends AbstractInstitutionTab<HealthTab.ClusterModel>
{
	static
	{
		PluginResourceHandler.init(HealthTab.class);
	}

	@Inject
	@Named("versionserver.url")
	private String VERSION_SERVER_URL;
	@PlugURL("js/versioncheck.js")
	private static String URL_VERSIONCHECK_JS;

	private static Version version = ApplicationVersion.get();

	private static final IncludeFile INCLUDE_FILE = new IncludeFile(URL_VERSIONCHECK_JS, JQueryCore.PRERENDER);

	private static final JSCallable CHECK_VERSION = new ExternallyDefinedFunction("checkVersion", 4, INCLUDE_FILE);
	@PlugKey("institutions.clusternode.link.name")
	private static Label LINK_LABEL;
	@PlugKey("clusternodes.node.id")
	private static Label LABEL_ID;
	@PlugKey("clusternodes.node.ips")
	private static Label LABEL_IP_ADDRESSES;
	@PlugKey("clusternodes.debug.checkbox")
	private static Label LABEL_DEBUG;
	@PlugKey("clusternodes.services")
	private static Label SERVICE_STATUS;
	@PlugKey("clusternodes.thisnode")
	private static Label THIS_NODE_LABEL;
	@PlugKey("clusternodes.nonclustered")
	private static Label NON_CLUSTER;

	@PlugKey("services.status.waiting")
	private static Label SERVICE_STATUS_WAITING;
	@PlugKey("services.table.name")
	private static Label SERVICE_HEADER_NAME;
	@PlugKey("services.table.status")
	private static Label SERVICE_HEADER_STATUS;
	@PlugKey("services.table.moreinfo")
	private static Label SERVICE_HEADER_INFO;
	@PlugKey("services.table.moreinfo")
	private static Label MORE_INFO_LABEL;
	@PlugKey("services.name.")
	private static String SERVICE_NAME_KEY;
	@PlugURL("js/services.js")
	private static String SERVICES_JS_URL;
	@PlugKey("services.table.unknown")
	private static Label NULL_INFO;
	@PlugKey("services.status.unknown")
	private static Label SERVICE_STATUS_UNKNOWN;
	@PlugKey("clusternodes.tasks.task.label.title")
	private static Label LABEL_TASK_TITLE;
	@PlugKey("clusternodes.tasks.task.label.nodeid")
	private static Label LABEL_TASK_NODEID;
	@PlugKey("institutionusage.limit")
	private static Label INST_LIMIT;
	@PlugKey("institutionusage.usage")
	private static Label INST_USAGE;
	@PlugKey("institutionusage.name")
	private static Label INST_NAME;

	private static IncludeFile SERVICES_INCLUDE = new IncludeFile(SERVICES_JS_URL);
	private static JSCallAndReference REFRESH_FUNC = new ExternallyDefinedFunction("refresh", 2, SERVICES_INCLUDE,
		JQueryUICore.PRERENDER);

	@Inject
	private UrlService urlService;
	@Inject
	private ZookeeperService zooKeeperService;
	@Inject
	private HealthCheckService healthCheckService;
	@Inject
	private ClusterInfoService clusterInfoService;
	@Inject
	private TaskService taskService;
	@Inject
	private QuotaService quotaService;

	@AjaxFactory
	private AjaxGenerator ajax;
	@EventFactory
	private EventGenerator events;
	@ViewFactory
	private FreemarkerFactory viewFactory;

	@Component(name = "n")
	private Table clusterNodesTable;
	@Component(stateful = false)
	private Checkbox debugCheck;
	@Component
	private Table tasksTable;
	@Component
	private Table institutionUsageTable;

	private JSHandler refreshHandler;
	private JSHandler usageRefreshHandler;
	private UpdateDomFunction updateVersionCheck;

	@Override
	public SectionResult renderHtml(RenderEventContext context) throws Exception
	{
		final ClusterModel model = getModel(context);
		model.setCluster(zooKeeperService.isCluster());
		final TableState clusterNodesTableState = clusterNodesTable.getState(context);
		healthCheckService.startCheckRequest();
		Collection<Institution> instsitutions = quotaService.getInstitutionsWithFilestoreLimits();
		model.setDisplayQuotas(false);
		if( !instsitutions.isEmpty() )
		{
			model.setDisplayQuotas(true);
			institutionUsageTable.setColumnHeadings(context, INST_NAME, INST_LIMIT, INST_USAGE);

			Map<String, String> quotas = model.getQuotas();
			for( Institution inst : instsitutions )
			{
				FileHandle institutionBaseHandle = new InstitutionFile(inst);
				TableCell consumptionCell = null;
				if( !quotas.isEmpty() )
				{
					String currentConsumtion = quotas.get(inst.getName());
					consumptionCell = new TableCell(currentConsumtion);
				}
				else
				{
					consumptionCell = new TableCell();
					consumptionCell.addClass("usagewaiting");
				}
				TableCell iconCell = new TableCell();

				if( quotaService.isInstitutionOverLimit(inst) )
				{
					consumptionCell.addClass("overLimit");
				}
				institutionUsageTable.addRow(context, inst.getName(), inst.getQuota() + " GB", consumptionCell);
			}
		}
		if( zooKeeperService.isCluster() )
		{
			clusterNodesTable.setColumnHeadings(context, LABEL_ID, LABEL_IP_ADDRESSES, SERVICE_STATUS);
			Map<String, String> nodeAdresses = clusterInfoService.getIpAddresses();
			for( String nodeId : zooKeeperService.getAppServers() )
			{
				String ipList = nodeAdresses.get(nodeId);
				TableCell serviceCell = buildServiceCell(nodeId);
				TableCell idCell = new TableCell(new TextLabel(nodeId));
				if( nodeId.equals(zooKeeperService.getNodeId()) )
				{
					idCell.addContent(new IconLabel(Icon.INFO, null, false));
					idCell.setTitle(THIS_NODE_LABEL);
				}

				clusterNodesTableState.addRow(idCell, ipList, serviceCell);
				if( !serviceCell.getStyleClasses().contains(Status.WAITING.toString().toLowerCase()) )
				{
					TableCell servicesTable = new TableCell(buildServicesTable(nodeId));
					servicesTable.setColSpan(3);
					servicesTable.addClass("wait-hide table-cell");
					clusterNodesTableState.addRow(servicesTable);
				}
			}
		}
		else
		{
			// FIXME replace ip and zookeeper node id w/ something else
			clusterNodesTable.setColumnHeadings(context, LABEL_ID, SERVICE_STATUS);
			String thisId = zooKeeperService.getNodeId();
			clusterNodesTableState.addRow(thisId + " (" + NON_CLUSTER.getText() + ")", buildServiceCell(thisId));
			TableCell servicesTable = new TableCell(buildServicesTable(thisId));
			servicesTable.setColSpan(3);
			servicesTable.addClass("wait-hide table-cell");
			clusterNodesTableState.addRow(servicesTable);
		}

		final Map<String, TaskStatus> allStatuses = taskService.getAllStatuses();
		for( TaskStatus task : allStatuses.values() )
		{
			final String title = (task.getTitleKey() == null ? task.getInternalId()
				: CurrentLocale.get(task.getTitleKey()));
			tasksTable.addRow(context, title, task.getNodeIdRunning());
		}

		debugCheck.setChecked(context, zooKeeperService.isClusterDebugging());

		context.getBody().addReadyStatements(refreshHandler, usageRefreshHandler);
		context.getBody().addReadyStatements(CHECK_VERSION, version.getMmr(), version.getCommit(),
				version.getDisplay(), VERSION_SERVER_URL, urlService.getAdminUrl().toString(), updateVersionCheck);
		return viewFactory.createResult("tab/health.ftl", context);
	}

	@Override
	public void registered(String id, SectionTree tree)
	{
		super.registered(id, tree);

		refreshHandler = new OverrideHandler(REFRESH_FUNC, ajax.getAjaxUpdateDomFunction(getTree(), this, null,
			ajax.getEffectFunction(EffectType.REPLACE_IN_PLACE), "table-ajax"), SERVICE_STATUS_UNKNOWN);

		updateVersionCheck = ajax.getAjaxUpdateDomFunction(tree, this, events.getEventHandler("versionResponse"),
				"latestVersion");
		usageRefreshHandler = new OverrideHandler(ajax.getAjaxUpdateDomFunction(tree, this,
			events.getEventHandler("getUsages"), ajax.getEffectFunction(EffectType.REPLACE_IN_PLACE), "usagetable"));
		debugCheck.setLabel(LABEL_DEBUG);
		debugCheck.setClickHandler(new OverrideHandler(events.getNamedHandler("toggleDebug")));

		tasksTable.setColumnHeadings(LABEL_TASK_TITLE, LABEL_TASK_NODEID);

		institutionUsageTable.setColumnSorts(Sort.PRIMARY_ASC, Sort.SORTABLE_ASC, Sort.SORTABLE_ASC);
	}

	private TableCell buildServiceCell(String nodeId)
	{
		Status servicesOk = Status.GOOD;

		for( ServiceStatus status : healthCheckService.retrieveForSingleNode(nodeId) )
		{
			if( status.getServiceStatus().equals(Status.BAD) )
			{
				servicesOk = Status.BAD;
			}
			else if( status.getServiceStatus().equals(Status.WAITING) )
			{
				TableCell waitingCell = new TableCell(new IconLabel(Icon.WAIT, SERVICE_STATUS_WAITING, false));
				waitingCell.addClass(Status.WAITING.toString().toLowerCase());
				return waitingCell;
			}

		}
		HtmlLinkState accordianToggle = new HtmlLinkState();
		accordianToggle.addClass("droparrow wait-hide");
		TableCell statusCell = new TableCell(new LinkRenderer(accordianToggle));
		statusCell.addClass(servicesOk.toString().toLowerCase());
		return statusCell;
	}

	private TableState buildServicesTable(String nodeId)
	{
		List<ServiceStatus> statuses = healthCheckService.retrieveForSingleNode(nodeId);
		TableState table = new TableState();
		table.addHeaderRow(SERVICE_HEADER_NAME, SERVICE_HEADER_STATUS, SERVICE_HEADER_INFO);
		for( ServiceStatus status : statuses )
		{
			TableCell statusCell = new TableCell();
			statusCell.addClass(status.getServiceStatus().toString().toLowerCase());
			TableCell preformatted = new TableCell(
				status.getMoreInfo() == null ? NULL_INFO : status.getMoreInfo().trim());
			preformatted.addClass("pre");
			table.addRow(new KeyLabel(SERVICE_NAME_KEY + status.getServiceName()), statusCell, preformatted);

		}
		table.addClass("services-table");
		table.setColumnSorts(Sort.PRIMARY_ASC, Sort.NONE, Sort.NONE);

		return table;
	}

	@EventHandlerMethod
	public void versionResponse(SectionInfo info, boolean newer, String infoUrl)
	{
		ClusterModel model = getModel(info);
		if (newer)
		{
			model.setVersionInfoUrl(infoUrl);
		}
		else
		{
			model.setOnLatestVersion(true);
		}
	}

	@EventHandlerMethod
	public void toggleDebug(SectionInfo info)
	{
		zooKeeperService.setClusterDebugging(debugCheck.isChecked(info));
	}

	@EventHandlerMethod
	public void getUsages(SectionInfo info)
	{
		ClusterModel model = getModel(info);
		Map<String, String> quotas = model.getQuotas();
		Collection<Institution> instsitutions = quotaService.getInstitutionsWithFilestoreLimits();
		for( Institution inst : instsitutions )
		{
			FileHandle institutionBaseHandle = new InstitutionFile(inst);

			String currentConsumtion = FileSizeUtils
				.humanReadableGigabyte(quotaService.getInstitutionalConsumption(inst));
			quotas.put(inst.getName(), currentConsumtion);
		}
	}

	@Override
	public String getDefaultPropertyName()
	{
		return "clusternodes";
	}

	@Override
	public Class<ClusterModel> getModelClass()
	{
		return ClusterModel.class;
	}

	@Override
	protected boolean isTabVisible(SectionInfo info)
	{
		return true;
	}

	@Override
	public Label getName()
	{
		return LINK_LABEL;
	}

	public Table getClusterNodesTable()
	{
		return clusterNodesTable;
	}

	public synchronized Checkbox getDebugCheck()
	{
		return debugCheck;
	}

	public Table getTasksTable()
	{
		return tasksTable;
	}

	public Table getInstitutionUsageTable()
	{
		return institutionUsageTable;
	}

	public static class ClusterModel
	{
		private boolean isCluster;
		private String versionInfoUrl;
		private boolean onLatestVersion;

		private Map<String, String> quotas = Maps.newHashMap();
		private boolean displayQuotas;

		public Map<String, String> getQuotas()
		{
			return quotas;
		}

		public void addQuota(String inst, String quota)
		{
			this.quotas.put(inst, quota);
		}

		public boolean isCluster()
		{
			return isCluster;
		}

		public void setCluster(boolean isCluster)
		{
			this.isCluster = isCluster;
		}

		public boolean isDisplayQuotas()
		{
			return displayQuotas;
		}

		public void setDisplayQuotas(boolean displayQuotas)
		{
			this.displayQuotas = displayQuotas;
		}

		public String getVersionInfoUrl()
		{
			return versionInfoUrl;
		}

		public void setVersionInfoUrl(String versionInfoUrl)
		{
			this.versionInfoUrl = versionInfoUrl;
		}

		public boolean isOnLatestVersion()
		{
			return onLatestVersion;
		}

		public void setOnLatestVersion(boolean onLatestVersion)
		{
			this.onLatestVersion = onLatestVersion;
		}
	}
}
