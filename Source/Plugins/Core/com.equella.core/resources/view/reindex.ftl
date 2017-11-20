<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/component/button.ftl" />
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a/>

<@css path="manualdatafix.css" hasRtl=true />

<div id="${id}">
	<h3>${b.key("reindex.heading")}</h3>
	<p>${b.key("reindex.description")}</p>
	<@a.div id="reindex">
		<@button section=s.execute showAs="dangerous" />
		<#if m.fired>
			<div class="fired">
				<label>${b.key("reindex.receipt")}</label>
			</div>
		<#else>
			<div class="spacer"></div>
		</#if>
	</@a.div>
</div>
