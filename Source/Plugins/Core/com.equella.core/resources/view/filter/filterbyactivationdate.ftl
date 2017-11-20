<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.standard@/calendar.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>
<#include "/com.tle.web.sections.standard@/ajax.ftl"/>

<@div class="filter date" id="activation-date-range-filter">
	<h3>${b.key("filter.byactivation.title")}</h3>

	<div class="input">
		<label>${b.key("filter.byactivation.start")}</label>
	</div>
	<div class="calendar">
		<@calendar section=s.dateStart1 notAfter=s.dateStart2 />
	</div>
	<div class="input">
		<label>${b.key("filter.byactivation.and")}</label>
	</div>
	<div class="calendar">
		<@calendar section=s.dateStart2 />
	</div>
	<hr>
	
	<div class="input">
		<label>${b.key("filter.byactivation.end")}</label>
	</div>
	<div class="calendar">
		<@calendar section=s.dateEnd1 notAfter=s.dateEnd2 />
	</div>
	<div class="input">
		<label>${b.key("filter.byactivation.and")}</label>
	</div>
	<div class="calendar">
		<@calendar section=s.dateEnd2 />
	</div>
	<hr>
	
	<div class="input">
		<label>${b.key("filter.byactivation.activated")}</label>
	</div>
	<div class="calendar">
		<@calendar section=s.dateActivate1 notAfter=s.dateActivate2 />
	</div>
	<div class="input">
		<label>${b.key("filter.byactivation.and")}</label>
	</div>
	<div class="calendar">
		<@calendar section=s.dateActivate2 />
	</div>
	<hr>
	<@div id="activation-date-clear">
		<#if m.showClearLink>
			<@button section=s.clearButton class="clear-filter" showAs="delete">${b.key("filter.byactivation.clear")}</@button>
		</#if>
	</@div>
</@div>
