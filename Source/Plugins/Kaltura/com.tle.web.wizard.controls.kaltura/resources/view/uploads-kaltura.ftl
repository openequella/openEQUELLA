<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.standard@/radio.ftl" />
<#include "/com.tle.web.sections.standard@/checklist.ftl"/>
<#import "/com.tle.web.sections.standard@/list.ftl" as l />

<@css path="kaltura.css" hasRtl=true />

<h3>${b.key("uploaded.heading")}</h3>
<p>${b.key("uploaded.description")}</p>

<div class="upload-scroller">
	<div class="upload-list">
		<#assign count = 0 />
		<@l.boollist section=s.selections; opt, state>
			<#assign media=opt.object>
			<div class="upload ${(count % 2 == 0)?string("odd","even")}">
				<@render state />

				<h4>${media.title}</h4>
				<#if opt.description??>
					<p>${media.description}</p>
				</#if>
				<#if opt.tags??>
					<p>${media.tags}</p>
				</#if>
				<#assign count = count +1 />
			</div>
		</@l.boollist>
	</div>
</div>
<@render s.selectAll/> | <@render s.selectNone />