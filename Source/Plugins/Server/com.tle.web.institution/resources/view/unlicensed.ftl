<#include "/com.tle.web.freemarker@/macro/sections.ftl">

<@css "institutions.css" />

<div class="area">
	<h2>${b.key('unlicensed.header')}</h2>
	<p>${b.key('unlicensed.description')}</p>
	<p>${b.key('unlicensed.reason', [m.reason])}</p>
</div>