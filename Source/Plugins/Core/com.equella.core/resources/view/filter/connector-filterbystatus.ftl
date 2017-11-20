<#include "/com.tle.web.freemarker@/macro/sections.ftl">

<div class="filter">
	<h3>${b.key("connector.filter.bystatus.title")}</h3>
	<div class="input select">
		<@render section=s.activationStatus />
	</div>
</div>
<hr>