<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>
<#include "/com.tle.web.sections.standard@/calendar.ftl"/>
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a/>

<h2>${b.key('activate.title')}</h2>
<div>
	<@settingContainer skinny=true>
		<@a.div id="errorajax">
		<#if m.error??>
			<div class="mandatory">
				${b.bundle(m.error)}
			</div>
		</#if>
		</@a.div>
			
		<@setting label=b.key('activate.name')>${_userformat("$LoggedInUser")}</@setting>
		
		<@setting label=b.key("activate.activate")>${m.attachmentList}</@setting>
		<@a.div id="courseajax">
			<#if m.showCourseSelector>
				<@setting label=b.key("activate.course") mandatory=true>
					<#if m.course??>
						<span>(${m.course.code}) ${b.bundle(m.course.name)}</span>
					</#if>
					<@button section=s.selectCourse />
				</@setting>
			<#else>
				<@setting label=b.key("activate.intended")>
					<span>(${m.course.code}) ${b.bundle(m.course.name)}</span>
				</@setting>
			</#if>
			
			<@setting label=b.key("activate.expected")><#if m.course??>${m.course.students}<#else>${b.key("activate.expected.none")}</#if></@setting>
		
		
		<@setting label=b.key("activate.makelivefrom") mandatory=true labelFor=s.untilDate ><@calendar section=s.fromDate notAfter=s.untilDate /></@setting>
		
		<@setting label=b.key("activate.makeliveuntil") mandatory=true labelFor=s.fromDate ><@calendar section=s.untilDate notBefore=s.fromDate /></@setting>
		
		<@setting label=b.key("activate.citation") section=s.citationList/>
		</@a.div>
		<div class="button-strip">
			<@button section=s.activateButton showAs="save">${m.addLabel}</@button>
			<@render section=s.cancelButton>${b.key("activate.cancel")}</@render>
		</div>
	</@settingContainer>
</div>
