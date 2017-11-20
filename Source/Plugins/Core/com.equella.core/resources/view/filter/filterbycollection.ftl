<#include "/com.tle.web.freemarker@/macro/sections.ftl">

<div class="filter filterbycollection">
	<@render s.labelTag><h3>${b.key("filter.bycollection.title")}</h3></@render>
	<div class="input select">
		<@render section=s.collectionList />
	</div>
</div>
<hr>