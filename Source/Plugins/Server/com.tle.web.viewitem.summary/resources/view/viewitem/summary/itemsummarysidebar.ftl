<#include "/com.tle.web.freemarker@/macro/sections.ftl">

<#list m.sections as s>
	<@render s />
	<hr>
</#list>