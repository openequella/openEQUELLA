<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.standard@/ajax.ftl">

<@css "approvals.css" />

<div class="area">
	<h2>${b.key('approvals.title')}</h2>
	<p>${b.key('approvals.description')}</p>

	<p>${b.key('approvals.help.enabled')}</p>
	<div class="input checkbox"><@render s.enabled /></div>
		
	<h3>${b.key('approvals.table.title')}</h3>
	<p>${b.key('approvals.table.description')}</p>
	<@div id="approvalsTable">
		<@render section=s.approvalsTable />		
	</@div>
	
	<h3>${b.key('payments.table.title')}</h3>
	<p>${b.key('payments.table.description')}</p>
	<@div id="paymentsTable">
		<@render section=s.paymentsTable />		
	</@div>	
</div>
