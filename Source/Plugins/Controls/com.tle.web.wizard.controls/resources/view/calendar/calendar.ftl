<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.standard@/calendar.ftl"/>
<#include "/com.tle.web.sections.standard@/link.ftl"/>
<#include "/com.tle.web.sections.standard@/ajax.ftl"/>

<@div id="${id}_calendar-control" class="calendar-control" >
	<#if c.range>
		<label>${b.gkey('wizard.controls.calendar.range.from')}</label>
		<div class="input calendar">
			<@calendar section=s.date1 notAfter=s.date2/>
		</div>
		<label>${b.gkey('wizard.controls.calendar.range.to')}</label>
		<div class="input calendar">
			<@calendar section=s.date2 />
		</div>
	<#else>
		<label>${b.gkey('wizard.controls.calendar.single.text')}</label>
		<div class="input calendar">
			<@render section=s.date1/>
		</div>
	</#if>
	<@div id="${id}_clear" class="calendar-clear" >
		<#if m.showClearLink>
			<@link section=s.clearLink class="clear-filter">${b.gkey("wizard.controls.calendar.clear")}</@link>
		</#if>
	</@div>
</@div>
