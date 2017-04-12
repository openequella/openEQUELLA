<#include "/com.tle.web.freemarker@/macro/sections.ftl">

<div class="area">
	<h2>Click a task to run it</h2>
	<#list m.links as link>
		<@render link /> <br>
	</#list>
</div>
