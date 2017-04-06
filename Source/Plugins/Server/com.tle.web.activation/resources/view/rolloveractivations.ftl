<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.standard@/calendar.ftl"/>
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>

<@css path="rollover.css" />

<@settingContainer mandatory=false>
	<h2><@bundlekey value="rolloveractivations.title" /></h2><br>
	
	<@setting label=b.key("rolloveractivations.rollovercourse") section=s.courses />
	<@setting label=b.key("rolloveractivations.deactivateexsiting") section=s.cancelExisting />
	<@setting label=b.key("rolloveractivations.rolloverdates") section=s.rolloverActivationDates />
	<@setting label=b.key("rolloveractivations.from") labelFor=s.fromDate >
		<@calendar section=s.fromDate notAfter=s.toDate />
	</@setting>
	<@setting label=b.key("rolloveractivations.until") labelFor=s.toDate>
		<@calendar section=s.toDate notBefore=s.fromDate />
	</@setting>
</@settingContainer>
