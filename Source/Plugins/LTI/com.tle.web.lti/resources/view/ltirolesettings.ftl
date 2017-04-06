<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a/>

<div class="area">
	<h2>${b.key('settings.title')}</h2>
	
	<@settingContainer mandatory=false>
		<@setting label='' help=b.key('settings.help.createuser')>
			<div class="input checkbox"><@render s.createUserCheckbox /></div>
		</@setting>
	
		<@a.div id="instructorrole">
			<@setting label=b.key('settings.role.instructor.label') help=b.key('settings.role.instructor.help')>
				<#if m.instructorRole??>
					${m.instructorRole.displayName}
				</#if>
				<@button section=s.selectIns showAs="select_user" />
				<#if m.instructorRole??>
					<@button section=s.clearIns showAs="delete" />
				</#if>
			</@setting>
		</@a.div>
		<@a.div id="otherrole">
			<@setting label=b.key('settings.role.other.label') help=b.key('settings.role.other.help')>
				<#if m.otherRole??>
					${m.otherRole.displayName}
				</#if>
				<@button section=s.selectOther showAs="select_user" />
				<#if m.otherRole??>
					<@button section=s.clearOther showAs="delete" />
				</#if>				
			</@setting>		
		</@a.div>
	</@settingContainer>
	
	<div class="button-strip">
		<@button section=s.saveButton showAs="save" />
	</div>
</div>	