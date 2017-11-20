<#include "/com.tle.web.freemarker@/macro/sections.ftl">

<@css "imageprocessing.css"/>

<#assign TEMP_body>
<div class="area <#if m.stackTrace??>error<#else>processing</#if>">
	<br>
	<p>${b.key(m.messageKey)}</p>
	<#if m.stackTrace??>
		<p><pre>${m.stackTrace}</pre></p>
	</#if>
	<br>
	<#if m.confirmStartProcess>
		<@render section=s.startButton>${b.key('liv.confirm')}</@render>
	<#else>
		<div style="text-align:center;">
			<img src=${p.url("images/pageloader.gif")} alt="loading">
		</div>
	</#if>
</div>
</#assign>
