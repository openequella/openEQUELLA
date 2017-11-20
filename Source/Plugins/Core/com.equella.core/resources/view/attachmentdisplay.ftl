<#include "/com.tle.web.freemarker@/macro/sections.ftl" >

<@css path="attachmentdisplay.css" />

<div class="attcontainer"> 
	<ul class="attachments-browse ${m.structured?string("structured", "thumbs")}">
		<#list m.attachmentRows as attachmentRow>
			<@render attachmentRow.row />
		</#list>
	</ul>
</div>