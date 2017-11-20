<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.equella@/component/button.ftl">

<@css "violation.css"/>

<div class="area error">
	<h2>${b.key("violation.copyright")}</h2>
	
	<#if m.exception.i18NMessage??>
		<p>${b.bundle(m.exception.i18NMessage, "")}</p>
	</#if>
	
	<@button id="cancelButton" section=s.cancelButton showAs="prev" size="medium" />
</div>