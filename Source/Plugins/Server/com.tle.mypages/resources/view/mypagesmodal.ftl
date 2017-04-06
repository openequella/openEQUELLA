<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />

<@css path="mypageseditor.css" hasRtl=true />

<div class="mypagesmodal">
	<#list m.sections as r>
		<@render r />
	</#list>
</div>