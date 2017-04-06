<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/component/button.ftl" />
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a/>

<div id="${id}">
	<h3>${b.key("fix.attachmenthash.title")}</h3>
	<p>${b.key("fix.attachmenthash.desc")}</p>
	<@a.div id="hash_status">
		<#if m.inProgress && m.taskStatus??>
			<div class="progressbar">${m.taskStatus.percentage}</div>
			<p>${m.taskLabel}</p>
		<#else>
			<@button section=s.execute showAs="dangerous" />
		</#if>
	</@a.div>
</div>
<hr>
