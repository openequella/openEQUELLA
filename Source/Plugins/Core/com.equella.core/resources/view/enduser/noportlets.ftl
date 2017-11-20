<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />

<@css path="noportlets.css" hasRtl=true/>

<div class="area">
	<h2><@bundlekey "enduser.noportlets.title"/></h2>
	
	${b.key('enduser.noportlets.spiel')}
	
	<#if m.createPrivs>
		${b.key('enduser.noportlets.helptext', [p.url('images/noportal-example-1.png'), p.url('images/noportal-example-2.png')])}
	</#if>
</div>