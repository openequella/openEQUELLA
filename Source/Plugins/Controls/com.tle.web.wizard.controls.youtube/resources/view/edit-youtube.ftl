<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl" />
<#include "/com.tle.web.wizard.controls.universal@/common-edit-handler.ftl" />
<#include "/com.tle.web.sections.standard@/textarea.ftl"/>

<@css path="youtube.css" hasRtl=true />

<@detailArea >
	<div>
		<#if m.specificDetail["embed"]?? >
			<div class="preview-container">${m.specificDetail["embed"].second}</div>
		</#if>
	</div>
	<@editArea />
	<@setting label=b.key("details.customparams") help=b.key("details.customparams.help") error=m.errors["customParams"] >
		<@textarea section=s.customParamsArea rows=3  />
	</@setting>
</@detailArea >

<@detailList />

<br clear="both">