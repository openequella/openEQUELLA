<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>

<@css "editcommon.css" />

<@setting label=b.key('entity.uuid') >
	<input type="text" readonly="readonly" value="${m.entityUuid?html}" />
</@setting>

<@setting label=s.titleLabel section=s.title error=m.errors["title"] mandatory=true />

<@setting label=s.descriptionLabel section=s.description />

<#if m.customEditor??><@render m.customEditor /></#if>