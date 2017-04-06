<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.equella@/renderer/linklist.ftl"/>
<#include "/com.tle.web.sections.equella@/styles/altlinks.ftl">
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a />

<@css path="taskstatistics.css" hasRtl=true />

<#if m.showManageable >
<@a.div id="${id}taskstatsresults" class="taskstatsresults">
	<#if m.taskstats?size &gt; 0>
		<table class="alt-links">
			<thead>
				<tr><th>${b.key('portal.taskstats.task')}</th><th class="waiting">${b.key('portal.taskstats.waiting')}</th><th class="trend">${b.key('portal.taskstats.trend')}</th></tr>
			</thead>
			<tbody>
				<#list m.taskstats as stat >
					<@render section=stat.row class="${altclass(stat_index)} taskrow">
						<td class="task"><a href="javascript:void(0);">${stat.label}</a></td><td class="waiting">${stat.waiting}</td><td class="trend">${stat.trend}</td>
					</@render>
				</#list>
			</tbody>
		</table>
		<#if m.showCount >
			<div class="resourcecount">
				<p>
					<#if m.showItemsInWorkflow>
						<@render s.itemsInWorkflowLink />
					<#else>
						${b.key('portal.taskstats.itemcount')}
					</#if>
					${m.itemCount} 
				</p>
			</div>
		</#if>
	<#else>
		<div class="noresults">
			<p>${b.key("portal.taskstats.noresults")}</p>
		</div>
	</#if>
</@a.div>
<div class="controlcontainer">
	<div class="statscontrols">
		<div class="workflow">
			<@render s.workflowSelector />
		</div>
		<@a.div id="${id}trendselector" class="trend">
			<@linklist section=s.trendSelector />
		</@a.div>
	</div>
</div>
<#else>
	<div class="nomanageable">
		<p>${b.key("portal.taskstats.nomanageable")}</p>
	</div>
</#if>