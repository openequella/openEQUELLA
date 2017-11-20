<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.standard@/link.ftl"/>

<div class="filter">
	<h3>${b.key("filter.bybadurl.title")}</h3>
	<div class="input checkbox">
		<@render section=s.filter/>
	</div>
</div>
<hr>