<#include "/com.tle.web.freemarker@/macro/sections.ftl">

<#assign images=m.attributes["images"]>

<#if images?has_content>
	<div><#t/>
	<ul class="result_imagelist"><#t/>
		<#list images as thumb><#t/>
			<li><#rt/>
				<@render thumb.link><@render thumb.image/></@render><#t/>
			</li><#t/>
		</#list><#t/>
	</ul><#t/>
	</div><#t/>
<#else>
	<div>${b.key('noimages')}</div>
</#if>