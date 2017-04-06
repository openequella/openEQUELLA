<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.standard@/ajax.ftl" />

<@div id="catalogue-filters">
	<#if m.sections?size &gt; 0>
		<@render section=s.filterBox class="filter">
			<@renderList m.sections/>
		</@render>
	</#if>
</@div>