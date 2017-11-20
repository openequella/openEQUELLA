<#include "/com.tle.web.freemarker@/macro/sections.ftl">

<@css "bulkresults.css"/> 
<@css path="selectionreview.css" plugin="com.tle.web.sections.equella"/>

<div id="bulkdialog">
	<#if m.selectionRows?has_content>
		<#if m.forExecute>
			<label for="${s.operationList}">
				<h3>${b.key("opresults.choose")}</h3>
			</label>
			<@render section=s.operationList />
		</#if>
		<p>${m.opResultCountLabel}<span id="bulkresults_showing">${m.showingLabel}</span></p>
		<@render section=s.selectionsTable />
		<@render s.pager/>
	<#else>
		${b.key('opresults.noselections')}
	</#if>
</div>
