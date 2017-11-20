<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.equella@/macro/settings.ftl" />
<#include m.commonIncludePath />

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