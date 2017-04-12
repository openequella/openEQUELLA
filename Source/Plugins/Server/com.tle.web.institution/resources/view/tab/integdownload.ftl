<#include "/com.tle.web.freemarker@/macro/sections.ftl">

<div class="area">
	<h2>${b.key("downloads.title")}</h2>
	<p>${b.key("downloads.spiel")}</p>
	<ul class="integdownloads">
		<#list m.sections as section>
			<@render section=section />
		</#list>
	</ul>
</div>