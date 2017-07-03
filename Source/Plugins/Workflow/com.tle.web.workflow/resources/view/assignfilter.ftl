<#include "/com.tle.web.freemarker@/macro/sections.ftl">

<div class="filter">
	<h3>${b.key("filter.assign")}</h3>
	<div class="input select">
		<@render section=s.assignList />
	</div>
	<div class="input checkbox">
		<@render section=s.mustCheckbox />
	</div>
</div>
<hr>