<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.standard@/ajax.ftl" />

<div class="input select">
	<label>${b.key('search.query.catalgoues.where')}</label><@render section=s.catalogueWhereList />
</div>