<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.standard@/dropdown.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a/>
<#include "/com.tle.web.sections.equella@/component/button.ftl" />

<@css path="userscript.css" hasRtl=true />

<@setting label=b.key("editor.scripttype") >
	<div class="script-type">
		<@dropdown section=s.scriptTypeList />
	</div>
</@setting>
<@a.div id="script-field">
	<#if m.javascript>
		<@setting label=b.key("editor.modulename") error=m.errors['error'] mandatory=true >
			<div class="module-name">
				<@dropdown section=s.moduleNameField />
			</div>
		</@setting>
	</#if>
	
	<@setting label=b.key("editor.script") error=m.errors["errors.noscript"] mandatory=true >
		<#if m.javascript>
			<@render section=s.javascriptEditor />	
		<#else>
			<@render section=s.freemakerEditor />
		</#if>
	</@setting>
	<#if m.javascript>
		<@button section=s.checkSyntaxButton showAs="verify" />
		<@a.div id="syntax-div">
			<#if m.syntaxMessage??>
				<pre>${m.syntaxMessage}</pre>
			</#if>	
		</@a.div>
	</#if>
</@a.div>

	