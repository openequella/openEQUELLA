<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.standard@/ajax.ftl">

<@css path="itemlist.css" hasRtl=true />

<#if m.nullItemsRemoved?? && m.nullItemsRemoved==true>
	<div id="warningInfo">
		<strong>${b.gkey('command.purge.purgedfromresults')}</strong>
	</div>
</#if>
<#list m.items as item>
	<@div tag=item.tag class="itemresult-wrapper">
		<div class="itemresult-container${item.hilighted?string(" hilighted","")}${item.selected?string(" selected","")}">
			<div class="itemresult${item.hilighted?string(" hilighted","")}${item.selected?string(" selected","")}">
				<div class="itemresult-content">
					<#if item.icon??>
						<@render item.icon/>
					</#if>
					<#if item.assignedIcon??>
						<@render item.assignedIcon/>
					</#if>
					<#if item.thumbnail??>
						<div class="thumbnailinlist">
							<@render item.thumbnail />
						</div>
					</#if>
					
					<h3 class="itemresult-title <#if item.selectable?? && item.selectable>selectable</#if>"><@render section=item.title class="titlelink" /> <#if item.toggle??><@render item.toggle /></#if></h3>
	
					<#if item.description??><p>${item.description}</p></#if>
					
					<div class="itemresult-meta">
						<#list item.metadata as meta>
							<div class="itemresult-metaline">
								<strong>${meta.label}:</strong>
								<@render meta.value/>
							</div>
						</#list>
					</div>
				</div>
				<#if item.extras??>
					<@renderList item.extras />
				</#if>
			</div>
			<div class="itemresult-rating">
				<div class="rating-bar">
					<#if item.ratingBarLeft??> 
						<@renderList item.ratingBarLeft>
							<span class="separator">|</span>
						</@renderList>
					</#if>
				</div>
				<#if item.ratingBarRight??>
					<div class="float-right">
						<@renderList item.ratingBarRight />
					</div>
				</#if>
			</div>
		</div>
	</@div>
</#list>
