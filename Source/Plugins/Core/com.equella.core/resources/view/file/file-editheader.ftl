<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include m.commonIncludePath />

<#--<@editheader title=m.editTitle thumbnail=m.thumbnail>
	${m.fileInfo.mimeType}<br>
	<#if m.fileInfo.size gt 0>
		${m.fileInfo.humanReadableSize}
	</#if>
</@editheader>-->

<@detailList />