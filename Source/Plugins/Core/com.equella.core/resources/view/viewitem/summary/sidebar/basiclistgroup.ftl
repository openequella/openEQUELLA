<#include "/com.tle.web.freemarker@/macro/sections.ftl">

<#if m.sections?size gt 0 >
	<h3>${b.key(s.groupTitleKey)}</h3>
	<@renderAsHtmlList m.sections />
</#if>