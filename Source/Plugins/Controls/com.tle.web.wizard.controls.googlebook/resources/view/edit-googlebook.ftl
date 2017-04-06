<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl" />
<#include "/com.tle.web.wizard.controls.universal@/common-edit-handler.ftl" />

<@css path="googlebook.css" hasRtl=true />

<@detailArea >
	<div>
		<#if m.specificDetail["description"]?? >
			<p>${m.specificDetail["description"].second}</p>
		</#if>
	</div>
	<@editArea />
</@detailArea >

<@detailList />

<br clear="both">