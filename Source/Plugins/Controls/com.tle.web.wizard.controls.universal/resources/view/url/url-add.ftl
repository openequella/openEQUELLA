<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl" />

<@css "universalresource.css" />
<label for="${s.url}">
	<h3>${b.key('handlers.url.add.heading')}</h3>
	<p>${b.key('handlers.url.add.description')}</p>
</label>

<@settingContainer mandatory=false>
	<@setting mandatory=true label=b.key('handlers.url.edit.url') labelFor=s.url error=m.errors["url"]>
		<@render section=s.url class="focus"/>
	</@setting>
</@settingContainer>
