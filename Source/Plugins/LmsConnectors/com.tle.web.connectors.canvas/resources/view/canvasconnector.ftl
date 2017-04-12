<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>
<#include "/com.tle.web.sections.standard@/textfield.ftl"/>
<#import "/com.tle.web.sections.standard@/ajax.ftl" as ajax/>

<@ajax.div id="canvassetup">

	<#include "/com.tle.web.connectors@/field/serverurl.ftl" />

	<#if m.testedUrl??>
		
		<@setting label=b.key('editor.label.manualtoken') labelFor=s.manualTokenEntry mandatory=true help=b.key('editor.help.manualtoken') error=m.errors["tokenentry"] >
			<@textfield section=s.manualTokenEntry />
		</@setting>
		
		<@setting label=b.key('editor.label.testtoken') labelFor=s.testTokenButton mandatory=true error=m.errors["tokentest"]>
			<@button section=s.testTokenButton showAs="verify" />
		
			<#if m.testAccessTokenStatus??>
				<span class="status ${m.statusClass}"> ${m.testAccessTokenStatus} </span>
			</#if>
		</@setting>
		
	</#if>
</@ajax.div>

<@setting label=''>
	<hr>
</@setting>