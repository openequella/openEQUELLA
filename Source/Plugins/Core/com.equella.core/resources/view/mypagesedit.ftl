<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>

<@css "mypagesedit.css" />

<div class="mypagesedit">
	<#if m.warnLabel??>
		<p class="ctrlinvalidmessage">${m.warnLabel}</p>
	</#if>
	
	<@render m.myPages />
</div>