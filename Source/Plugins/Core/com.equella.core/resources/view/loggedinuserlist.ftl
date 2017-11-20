<#include "/com.tle.web.freemarker@/macro/sections.ftl">

<div class="area">
	<h2><@bundlekey value="title" /></h2>

	<h3>${m.sessionsLabel}</h3>
	<@render s.sessionsTable />
</div>
