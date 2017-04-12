<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.equella@/macro/settings.ftl" />
<#include "/com.tle.web.wizard.controls.universal@/common-edit-handler.ftl" />

<@css "resource-edit.css" />

<@detailArea>
	<div>
		<#if m.specificDetail["itemdesc"]?? >
			<p>${m.specificDetail["itemdesc"].second}</p>
		</#if>
	</div>
	<@editArea />
</@detailArea>

<@detailList />

<br clear="both">