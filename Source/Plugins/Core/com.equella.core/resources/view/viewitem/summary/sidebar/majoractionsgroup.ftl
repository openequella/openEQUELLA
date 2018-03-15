<#include "/com.tle.web.freemarker@/macro/sections.ftl">

<#if m.sections?size gt 0 >
    <div class="majorActions">
	    <@renderList m.sections />
	</div>
</#if>