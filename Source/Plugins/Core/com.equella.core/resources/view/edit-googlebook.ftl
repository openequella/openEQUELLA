<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl" />
<#include m.commonIncludePath />

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