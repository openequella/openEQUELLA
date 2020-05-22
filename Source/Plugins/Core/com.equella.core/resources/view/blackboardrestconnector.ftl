<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>
<#include "/com.tle.web.sections.standard@/textfield.ftl"/>
<#import "/com.tle.web.sections.standard@/ajax.ftl" as ajax/>

<@css "blackboardconnector.css" />

<@ajax.div id="blackboardrestsetup">

	<#include "/com.tle.web.connectors@/field/serverurl.ftl" />

	<#if m.testedUrl??>

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
