<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<div class="area">
	<#list m.sections as section>
		<@render section=section />
		<br>
	</#list>
</div>
