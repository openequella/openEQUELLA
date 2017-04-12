<#ftl strip_whitespace=true />
<#include "/com.tle.web.sections.standard@/ajax.ftl" />
<#include "/com.tle.web.sections.standard@/button.ftl"/>
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl" />

<@css path="editcatalogue.css" hasRtl=true />

<@setting label=b.key('entity.uuid') >
	<input type="text" readonly="readonly" value="${m.entityUuid?html}" />
</@setting>

<@setting label=b.key('editor.label.title') section=s.title error=m.errors["title"] mandatory=true />

<@setting label=b.key('editor.label.description') section=s.description />

<#if m.customEditor??><@render m.customEditor /></#if>

<#if s.testable>
	<@div id="testDiv">
		<@setting label=b.key('gateway.edit.label.test') error=m.errors["testgateway"] help=m.storeRedirectUrl >
			<@button section=s.testButton showAs="verify" />
			<#if m.testStatus??>
				<span class="status ${m.testStatus}">${b.key('gateway.edit.status.' + m.testStatus)}</span>
			</#if>
		</@setting>
	</@div>
</#if>
<@setting label=s.enabledLabel error=m.errors["enabled"] labelFor=s.enabled >
	<@render s.enabled />
</@setting>