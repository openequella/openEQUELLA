<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>

<@setting section=s.serverUrl
		label=b.gkey('com.tle.web.connectors.editor.label.url', m.connectorLmsName) 
		error=m.errors["url"] 
		mandatory=true  
		rowStyle="urlRow" />
	
	<@setting 
		label=b.gkey('com.tle.web.connectors.editor.label.testurl', m.connectorLmsName) 
		error=m.errors["urltest"]
		help=b.gkey('com.tle.web.connectors.editor.help.testurl', m.connectorLmsName)
		mandatory=true
		rowStyle="testUrlRow">
		
		<@button section=s.testUrlButton showAs="verify" />
		
	  	<#if m.testedUrl??>
	  		<span class="status ${m.testUrlStatus}">
	  		${b.gkey('com.tle.web.connectors.editor.label.teststatus.' + m.testUrlStatus, m.connectorLmsName)}
	  		</span>
	  	</#if>
	</@setting>