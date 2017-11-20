<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.standard@/ajax.ftl"/>
<#include "/com.tle.web.sections.standard@/radio.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>

<@css "courselist.css" />

<#assign level=0 />
<#macro folders folderList>
	<#if folderList?size gt 0> 
	<#assign level=level+1 />
	<ul class="folderlist">
	<#list folderList as f>
		
		<#if f.targetable> 
			<li id="folder_${f.id}" class="level${level} folder targetfolder<#if f.defaultFolder> defaultfolder</#if><#if f.selected> selected</#if>" data-folderid="${f.id}">
				<#-- float right -->
				<#if f.resourceCount gt 0>
					<div class="selectioncount">(${f.resourceCount?c})</div>
				</#if>
				
				<div class="foldername">
					<@radio section=f.select class="hidden" />
					<@render f.name />
				</div>
				
				<div class="prop"></div>
			</li>
			
		<#else>
			<li class="level${level} folder nontarget">
				<div class="foldername">
					<@render f.name />
				</div>
			
				<div class="prop"></div>
			</li>
		</#if>
		
		<#if f.folders?size gt 0>
		<li class="subfolders level${level}">
			<@folders f.folders />
		</li>
		</#if>
	</#list>
	</ul>
	<#assign level=level-1 />
	</#if>
</#macro>

<div class="selection-lmscontent">
	<div class="selection-courses" data-spy="affix" data-offset-top="0">
		<@div id="courselistajax" class="selection-courses-inner" >
			<div class="area">
				<div class="courselisttop">
					<#if m.noTargets>
						<div class="no-targets-warning">${b.key('courselist.warning.notargets')}</div>
					<#else>
						<div class="buttons">
							<@button section=s.saveButton showAs="save" size="medium" class="save" />
							<@button section=s.cancelButton showAs="cancel" size="medium" class="cancel" />
						</div>
						<@render section=s.viewSelections />
					</#if>
				</div>
				<hr class="foldertreeline">
				<#assign root=m.root />
				
				<#-- scrollable container -->
				<div class="foldertree">
					<div class="folderscroll">
						<#if root.targetable>
							<div id="folder_${root.id}" class="level0 folder rootfolder targetfolder<#if root.selected> selected</#if>" data-folderid="${root.id}">
								<#-- float right -->
								<#if root.resourceCount gt 0>
									<div class="selectioncount">(${root.resourceCount?c})</div>
								</#if>
							
								<div class="foldername">
									<@radio section=root.select class="hidden" />
									<@render root.name />
								</div>
								
								<div class="prop"></div>
							</div>
						
						<#else>
							<div class="level0 folder rootfolder nontarget">
								<div class="foldername">
									<@render root.name />
								</div>
								
								<div class="prop"></div>
							</div>
						</#if>
						
						<div class="folders">
							<@folders root.folders />
						</div> 
					</div>
				</div>
			</div>
		</@div>
	</div>
</div>
