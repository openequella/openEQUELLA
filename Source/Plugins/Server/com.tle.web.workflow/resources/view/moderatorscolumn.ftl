<#include "/com.tle.web.freemarker@/macro/sections.ftl" />

${m.moderatorsLabel}
<@renderAsHtmlList list=m.moderatorList class="moderators" />
<#if m.commentLink??>
	<br>
	<@render m.commentLink/>
</#if>