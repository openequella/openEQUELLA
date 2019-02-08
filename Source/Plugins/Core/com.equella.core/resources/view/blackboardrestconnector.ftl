<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>
<#include "/com.tle.web.sections.standard@/textfield.ftl"/>
<#import "/com.tle.web.sections.standard@/ajax.ftl" as ajax/>

<@css "blackboardconnector.css" />

<@setting label='' help=b.key('bb.editor.help.installmodule')>
	<hr>
</@setting>

<@ajax.div id="blackboardsetup">

	<#include "/com.tle.web.connectors@/field/serverurl.ftl" />

	<#if m.testedUrl??>

			<@ajax.div id="testdiv">

				<@setting
					label=''
					error=m.errors["blackboardwebservice"]
					help=b.key('editor.help.testwebservice')
					rowStyle="testBlackboardRow">

						<@button section=s.testWebServiceButton showAs="verify" />
					  	<#if m.testWebServiceStatus??>
					  		<span class="status ${m.testWebServiceStatus}">${b.key('bb.editor.label.testwebservice.' + m.testWebServiceStatus)}</span>
					  	</#if>
				</@setting>
			</@ajax.div>

			<@setting	label=b.key('blackboardrest.editor.label.apikey')
      				error=m.errors["apikey"]
      				help=b.key('blackboardrest.editor.help.apikey')
      				labelFor = s.apiKey
      				>
      				<@textfield section=s.apiKey />
      </@setting>

      <@setting	label=b.key('blackboardrest.editor.label.apisecret')
      				error=m.errors["apisecret"]
      				help=b.key('blackboardrest.editor.help.apisecret')
      				labelFor = s.apiSecret
      				>
      				<@textfield section=s.apiSecret password=true />
      </@setting>

	</#if>

</@ajax.div>

<@setting label=''>
	<hr>
</@setting>
