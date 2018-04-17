<#include "/com.tle.web.freemarker@/macro/sections.ftl">

<@css path="moderate.css" hasRtl=true hasNew=true/>
<div id="moderate-allmods">
	<@renderAsHtmlList list=m.allModerators class="moderators" />
</div>