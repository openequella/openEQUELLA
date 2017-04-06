<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.standard@/textfield.ftl"/>
<#include "/com.tle.web.search@/macros/compactquery.ftl"/>

<div class="filter ${id}">
	<@render s.labelTag><h3>${b.key("filter.bykeyword.title")}</h3></@render>
	<div class="input">
		<@compactquery qf=s.queryField b=s.searchButton auto=true />
	</div>
</div>
<hr>