<#include "/com.tle.web.freemarker@/macro/sections.ftl">

<div class="filter">
	<h3>${b.key("manage.filter.archive.title")}</h3>
	<div class="input checkbox">
		<@render section=s.checkState />
		<@render section=s.includeArchived />
	</div>
</div>
<hr>