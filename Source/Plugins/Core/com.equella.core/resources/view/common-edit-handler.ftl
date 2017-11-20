<#include "/com.tle.web.sections.equella@/macro/settings.ftl" />

<#macro detailArea>
	<div class="area float-left attachment-content">
		<h3>${m.editTitle}</h3>
		<#nested />
	</div>
</#macro>

<#macro editArea>
	<hr>
	<div>
		<h3>${b.gkey(m.commonPrefix+"handlers.abstract.edit.attachment")}</h3>
		<@settingContainer mandatory=true>
			<@setting mandatory=true label=b.gkey(m.commonPrefix+"handlers.abstract.displayname.label") labelFor=s.displayName error=m.errors["displayName"]>
				 <@render section=s.displayName class="focus" />
			</@setting>				
			<#nested />
			<@viewerList />
			<@previewCheckBox />
			<@restrictCheckbox />
		</@settingContainer>
	</div>
</#macro>

<#macro viewerList >
	<#if m.showViewers >
		<@setting label=b.gkey(m.commonPrefix+'handlers.abstract.viewer.label') section=s.viewers />
	</#if>
</#macro>

<#macro previewCheckBox >
	<#if m.showPreview >
		<@setting label=b.gkey(m.commonPrefix+'handlers.abstract.preview') section=s.previewCheckBox />
	</#if>
</#macro>

<#macro restrictCheckbox >
	<#if m.showRestrict >
		<@setting label=b.gkey(m.commonPrefix+'handlers.abstract.restrict') section=s.restrictCheckbox />
	</#if>
</#macro>

<#macro detailList >
	<div class="float-right attachment-details" >
		<#if m.thumbnail??>
			<div class="thumbnail" ><@render m.thumbnail /></div>
		</#if>
		
		<@render section=s.detailTable class="detail alternate" />
		
		<#if m.viewlink??>
			<hr>
			<div class="viewlink"><@render m.viewlink /></div>
		</#if>
	</div>
</#macro>