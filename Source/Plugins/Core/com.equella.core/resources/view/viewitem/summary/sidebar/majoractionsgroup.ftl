<#include "/com.tle.web.freemarker@/macro/sections.ftl">

<#if m.sections?size gt 0 >
	<@renderList m.sections />
</#if>