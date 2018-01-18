<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.standard@/dialog.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>

<@script "filemanager.js"/>

<@dialog section=s.dialog class="filemanagerdialog" />
<@button section=s.dialog.opener><@bundlekey "launch" /></@button>

<#if c.webdav>
	<@render section=s.openWebdav class="ctrlbuttonNW">${b.gkey('wizard.controls.file.openfolder')}</@render>
	<@render section=s.refreshButton class="ctrlbuttonNW">${b.gkey('wizard.controls.file.refresh')}</@render>
</#if>
			
<@render s.filesTable />
