<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>

<@css "attachments/attachments.css" />

<h3<#if m.showSelectAllButton> class="taller"</#if>>
	${m.sectionTitle}
	<@button section=s.selectAllAttachmentButton showAs="plus"/>
</h3>

<@render section=s.div class="attachments row-fluid">
	<#-- Add "thumbnails" class when bootstrap upgraded --> 
	<div class="attcontainer" tabindex="-1">
		<ul id="${id}_browse" class="attachments-browse ${s.showStructuredView?string("structured", "thumbs")}">
			<#list m.attachmentRows as attachmentRow>
				<@render attachmentRow.row />
			</#list>
		</ul>
	</div>
</@render>

<div id="${id}_extras" class="extras">
		<@render s.fullScreenLink />
		<@render s.fullScreenLinkNewWindow />
		<@button section=s.selectPackageButton  showAs="plus" />
		<#-- This is a bit dirty, cloud version has no reorder link -->
		<#if s.reorderAttachments??>
			<@render s.reorderAttachments />
		</#if>
</div>