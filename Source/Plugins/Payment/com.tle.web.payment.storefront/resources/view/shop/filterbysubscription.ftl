<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.standard@/calendar.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>
<#include "/com.tle.web.sections.standard@/ajax.ftl"/>

<@div class="filter date" id=s.ajaxDiv>
	<h3>${s.title}</h3>

	<div class="input select range">
		<@render s.range />
	</div>
	<div class="input calendar startdate">
		<@calendar section=s.datePrimary notAfter=s.dateSecondary />
	</div>
	<@render section=m.between class="enddate">
		<div class="input">
			<label>${b.key("store.price.filter.bydate.and")}</label>
		</div>
		<div class="input calendar">
			<@calendar section=s.dateSecondary notBefore=s.datePrimary />
		</div>
	</@render>
	<@div id="clear-sub">
		<#if m.showClearLink>
			<@button section=s.clearButton class="clear-filter" showAs="delete">${b.gkey("com.tle.web.search.filter.clear")}</@button>
		</#if>
	</@div>
</@div>
<hr>
