<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>

<div class="filter">
	<h3>${b.key("filter.type.title")}</h3>
	<div class="input select">
		<@render s.typeList />
	</div>
</div>
<hr>