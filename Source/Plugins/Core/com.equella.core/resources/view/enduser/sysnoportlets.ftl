<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />

<@css path="noportlets.css" hasRtl=true/>

<div class="area dashboard-page">
	<h2><@bundlekey "enduser.noportlets.title"/></h2>
	<h2>Hello ${m.username}</h2>
	<h2>resources contributed : ${m.itemcount}</h2>
	<h2>resources need to be moderated by user : ${m.taskCount}</h2>
	${b.key('sysuser.noportlets.spiel')}
	
</div>