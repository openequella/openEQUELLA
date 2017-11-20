<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.standard@/textfield.ftl">
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>

<@css "institutions.css" />

<div class="area">
	<h2>${b.key("institutions.logon.heading")}</h2>
	<#if m.error??>
		<p class="warning">${m.error?html}</p>
	</#if>
	<p>
		<@render m.fieldLabel/>
		<@textfield password=true section=s.password size=25 autoSubmitButton=s.loginButton/>
		<@button section=s.loginButton size="medium" />
	</p>
</div>
