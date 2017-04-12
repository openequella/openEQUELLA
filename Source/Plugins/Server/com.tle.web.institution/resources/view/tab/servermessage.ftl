<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.standard@/textfield.ftl">
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>

<h2>${b.gkey("institutions.server.message.name")}</h2>
<@settingContainer false>
	<@setting label=b.gkey("institutions.server.message.message") section=s.message />
	<@setting label=b.gkey("institutions.server.message.enabled") section=s.enabled />
</@settingContainer>
<div class="button-strip">
	<@button section=s.save showAs="save" />
</div>