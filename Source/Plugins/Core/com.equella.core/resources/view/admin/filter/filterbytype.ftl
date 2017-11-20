<#include "/com.tle.web.freemarker@/macro/sections.ftl">

<div class="filter">
	<h3>${b.key("filter.bytype.title")}</h3>
	<div class="input select">
		<@render section=s.portletType />
	</div>
</div>
<hr>