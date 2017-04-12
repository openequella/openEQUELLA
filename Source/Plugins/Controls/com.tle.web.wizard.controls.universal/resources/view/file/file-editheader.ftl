<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.wizard.controls.universal@/common-edit-handler.ftl" />

<#--<@editheader title=m.editTitle thumbnail=m.thumbnail>
	${m.fileInfo.mimeType}<br>
	<#if m.fileInfo.size gt 0>
		${m.fileInfo.humanReadableSize}
	</#if>
</@editheader>-->

<@detailList />