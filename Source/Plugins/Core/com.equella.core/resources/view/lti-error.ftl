<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<@css "lti-viewer.css" />

<#if m.error??>
	<div class="error">
		<span>${m.error}</span>
	</div>
</#if>
