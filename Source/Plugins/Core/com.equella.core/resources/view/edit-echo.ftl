<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl" />
<#include m.commonIncludePath />
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a />

<@css "echohandler.css" />

<@detailArea >

<div>
	<ul class="echolinks">
		<#list m.links as link>
			<li class="echolink ${(link_index % 2 == 0)?string("odd","even")}"><@render link /></li>
		</#list>
	</ul>
</div>
	<@editArea />
</@detailArea>

<@detailList />

<br clear="both">