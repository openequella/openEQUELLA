<#import "/com.tle.web.freemarker@/macro/sections.ftl" as sec />

<div class="area">
	<h2>${b.gkey("institutions.threaddump.title")}</h2>
	<@sec.render s.threadsTable />

	<h2>${b.gkey("institutions.threaddump.jvm")}</h2>
	<#list m.traces.keySet() as thread>
		<h4><a name="${thread.id}">${thread.name?html}</a></h4>
		<pre><#list m.traces.get(thread) as traceline> ${b.gkey('institutions.threaddump.atline')} ${traceline?html}
		</#list></pre>
	</#list>
</div>