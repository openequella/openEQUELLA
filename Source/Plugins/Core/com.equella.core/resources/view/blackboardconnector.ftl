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
		<@setting 
			label=b.key('editor.label.proxypass') 
			help=b.key('editor.help.proxypass') 
			rowStyle="passRow"
			labelFor = s.proxyToolPass>
			
			<@textfield section=s.proxyToolPass password=true />
		</@setting>
		<@ajax.div id="registerdiv">	
			<@setting 
				label='' 
				error=m.errors["registertool"] 
				help=b.key('editor.help.register', m.testedUrl) 
				rowStyle="testBlackboardRow">
				
				<@button section=s.registerButton showAs="verify" />
				
			  	<#if m.registerToolStatus??>
			  		<span class="status ${m.registerToolStatus}">${b.key('editor.label.register.' + m.registerToolStatus)}</span>
			  	</#if>
			</@setting>
			
			<@setting 
				label=b.key('editor.label.proxysecret') 
				error=m.errors["proxysecret"] 
				help=b.key('editor.help.proxysecret') 
				rowStyle="proxySecretRow"
				labelFor = s.proxyToolSecret
				>
				
				<@textfield section=s.proxyToolSecret password=true />
			</@setting>
		
			<@ajax.div id="testdiv">
				<@setting 
					label=b.key('bb.editor.label.testwebservice')
					help=b.key('editor.help.testwebservice.username')
					mandatory=true
					rowStyle="testBlackboardRow"
					error=m.errors["systemusername"]
					labelFor = s.testWebServiceUsername >
					
						<@render section=s.testWebServiceUsername />
				</@setting>
			
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
		</@ajax.div>
	</#if>
	
</@ajax.div>

<@setting label=''>
	<hr>
</@setting>