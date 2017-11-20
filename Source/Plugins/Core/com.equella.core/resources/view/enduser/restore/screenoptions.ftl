<#ftl strip_whitespace=true/>
<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>

<@css "portalhelp.css"/>

<h3><@bundlekey "enduser.restore"/></h3>
<div class="listContainer">
<ul class="portallist">
	<#list m.restorables as restorable>
		<li class="list-<#if restorable_index%2==0>left<#else>right</#if>">
			<@render restorable />
		</li>
	</#list>
</ul>
<div style="clear:both">&nbsp;</div>
	<div id="restoreAll">
		<@render section=s.restoreAllButton type="link"/>
	</div>
</div>
