<#include "/com.tle.web.freemarker@/macro/sections.ftl">

<div class="filter">
	<@render s.labelTag>
		<h3>${b.key("filter.reason")}</h3>
	</@render>
	<div class="input select">
		<@render section=s.reasonList />
	</div>
</div>
<hr>