<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.search@/macros/compactquery.ftl"/>

<@css "search.css" />

<div class="quick-search">
	<@compactquery qf=s.query b=s.searchButton auto=true />
	<br clear="both">
</div>