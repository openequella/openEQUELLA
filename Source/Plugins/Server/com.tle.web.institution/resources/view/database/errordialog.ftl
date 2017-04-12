<#include "/com.tle.web.freemarker@/macro/sections.ftl">

<h1>${b.key('migrate.error.title')}</h1>

<#assign r = m.errorReport>

<p id="instructions">${b.key('migrate.error.instructions')}</p>
<#if !r.canRetry>
	<p>${b.key('migrate.error.mustresolve')}</p>
</#if>
<p id="task">
	<span class="errorheading">${b.key('migrate.error.task')}</span>
	<span>${r.name}</span>
</p>
<#if r.message?has_content>
	<p id="errormsg">
		<span class="errorheading">${b.key('migrate.error.message')}</span><br>
		<span>${r.message?html}</span>
	</p>
</#if>
<p id="stacktrace">
	<span class="errorheading">${b.key('migrate.error.stacktrace')}</span>
	<pre>${r.error?html}</pre>
</p>
<#if m.logAsString?has_content>
	<p id="log">
		<span class="errorheading">${b.key('migrate.error.log')}</span>
		<pre>${m.logAsString?html}</pre>
	</p>
</#if>
