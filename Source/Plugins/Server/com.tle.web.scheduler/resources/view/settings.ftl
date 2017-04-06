<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>

<@css path="schedulersettings.css" hasRtl=true />

<div class="area schedulersettings">
	<h2>${b.key('title')}</h2>
	<@settingContainer mandatory=false>
		<@setting label=b.key('label.daily') section=s.dailyTaskHour />
		<@setting label=b.key('label.weekly')>
			<@render s.weeklyTaskHour />
			<span class="dayprefix">${b.key('label.weekly.dayprefix')}</span>
			<@render s.weeklyTaskDay />
		</@setting>
	</@settingContainer>
	
	<div class="button-strip">
		<@button section=s.saveButton showAs="save" />
	</div>
</div>
