<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.standard@/dialog.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>

<@script "webdav.js"/>

<#if c.webdav>
	<@render section=s.openWebdav class="ctrlbuttonNW">${b.gkey('wizard.controls.file.openfolder')}</@render>
	<@render section=s.refreshButton class="ctrlbuttonNW">${b.gkey('wizard.controls.file.refresh')}</@render>
</#if>

<@render s.filesTable />
