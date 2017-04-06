<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />

<@css path="handler.css" hasRtl=true/>

<div class="area">
	<h2>${m.title}</h2>

	<@render m.handlerRenderer/>	
</div>