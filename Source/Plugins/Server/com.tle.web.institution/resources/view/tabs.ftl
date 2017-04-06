<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.equella@/macro/receipt.ftl">

<@css "institutions.css" />

<#assign TEMP_body>
	<@receipt m.receipt />
	<div id="institution-admin">
		<#if m.error??>
			<div>
				<span class="mandatory">${m.error?html}</span>
			</div>
		</#if>
		<@render m.selectedTab/>
	</div>
</#assign>
