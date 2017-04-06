<#include "/com.tle.web.freemarker@/macro/sections/render.ftl">

<@render section=m.start />
<br>
${b.bundle(m.description, "")?html}								

<#if m.remoteRepos?? >
	<ul>
		<#list m.remoteRepos as repo >
			<li><@render section=repo.start /></li>
		</#list>
	</ul>
</#if>