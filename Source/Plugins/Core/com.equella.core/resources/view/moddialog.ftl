<#include "/com.tle.web.freemarker@/macro/sections.ftl">

<@css path="moderate.css" hasRtl=true/>
<div id="moderate-allmods">
	<div class="moddialog-list">
		<h2>${b.key('moddialog.remaining')}</h2>
		<ul>
			<#list m.moderators as mod>
				<li><@render mod.moderator/></li>
			</#list>
		</ul>
	</div>
	<#if m.moderatorsAccepted?size gt 0>
		<div class="moddialog-list">
			<h2>${b.key('moddialog.accepted')}</h2>
			<ul>
				<#list m.moderatorsAccepted as mod>
					<li><@render mod.moderator/></li>
				</#list>
			</ul>
		</div>
	</#if>
</div>