<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>
<#import "/com.tle.web.sections.standard@/ajax.ftl" as ajax/>

<@css "moodleconnector.css" />

<@setting label='' help=b.key('editor.help.installmodule')>
	<hr>
</@setting>

<@ajax.div id="moodlesetup">
	<#include "/com.tle.web.connectors@/field/serverurl.ftl" />

	<#if m.testedUrl??>
		<div class="settingRow configureMoodleRow">
			<div class="settingLabel">${b.key('editor.label.setupwebservice')}</div>
			
			<div class="settingField">
				<div class="settingHelp">
					${b.key('editor.help.enablewebservices')}
					<div id="tokenHelp" class="helpLinks">
						${b.key('editor.help.mustbeadmin')}
						<ul>
							<li>${b.key('editor.help.link.enablewebservices', m.testedUrl)}</li>
							<li>${b.key('editor.help.link.enablerest', m.testedUrl)}</li>
							<li>${b.key('editor.help.link.createtoken', m.testedUrl)}</li>
						</ul>
					</div>
				</div>
			</div>
		</div>
		
		<@setting 
			label=b.key('editor.label.webservicetoken')
			section=s.webServiceToken 
			error=m.errors["token"] 
			help='' 
			mandatory=true
			rowStyle="tokenRow" />
	
		<@ajax.div id="testdiv">
			<@setting 
				label=b.key('editor.label.testwebservice')  
				error=m.errors["moodlewebservice"] 
				help=b.key('editor.help.testservice')
				mandatory=true
				rowStyle="testMoodleRow">
				 
				<@button section=s.testServiceButton showAs="verify" />
				
			  	<#if m.testMoodleStatus??>
			  		<span class="status ${m.testMoodleStatus}">${b.key('editor.label.teststatus.' + m.testMoodleStatus)}</span>
			  	</#if>
			</@setting>
		</@ajax.div>
	</#if>
</@ajax.div>

<@setting label=''>
	<hr>
</@setting>