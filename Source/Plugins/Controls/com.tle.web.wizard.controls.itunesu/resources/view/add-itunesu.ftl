<#ftl strip_whitespace=true />
<#import "/com.tle.web.sections.standard@/ajax.ftl" as ajax/>
<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>

<@ajax.div id="itunesTree">
	<div tabIndex="0" class="focus">
	<#if m.loading>
		${b.key("add.loading")}
	<#else>
		${b.key("add.choose")}
	</#if>	
	</div>
</@ajax.div>
<@render section=s.treeView class="itunestreeview" />
