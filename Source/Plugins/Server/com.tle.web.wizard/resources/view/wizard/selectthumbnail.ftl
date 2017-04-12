<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.standard@/list.ftl" />
<#include "/com.tle.web.sections.standard@/radio.ftl"/>
<#include "/com.tle.web.sections.standard@/textfield.ftl"/>

<@css path="selectthumbnail.css" />

<@textfield section=s.selected hidden=true />

<div class="select_area">
	<ul class="select_options">
		<@boollist section=s.options; opt, state>
			<li>
				<@radio state/>
			</li>
		</@boollist>	
	</ul> 
	
	<div class="thumbcontainer">
		<#if m.attachmentThumbs??> 
			<ul class ="thumbs">
				<#list m.attachmentThumbs as attachmentThumb>
					<@render attachmentThumb />
				</#list>
			</ul>
		<#else>
			<div class="thumbs">
				<div class="help-text">${b.key('selectthumbnail.help.nothumb')}</div>
			</div>
		</#if>
	</div>
	
</div>