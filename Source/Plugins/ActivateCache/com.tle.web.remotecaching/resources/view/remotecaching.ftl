<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl" />
<#include "/com.tle.web.sections.equella@/component/button.ftl" />
<#import "/com.tle.web.sections.standard@/ajax.ftl" as ajax/>

<@css path="remotecaching.css" hasRtl=true/>

<div class="area">
	<h2>${b.key('remotecaching.title')}</h2>

	<div class="input checkbox allowoption">
		<@render section=s.allowUse />
	</div>

	<@ajax.div id="overallajaxdiv" class="overallPanel">
			<#if m.showControls>
				<@ajax.div id="includesexcludesajaxdiv" class="rightPanel">
					<div>
						<h3 class="rightheading includesheading">
							<@bundlekey value="includes.tablelabel" params=[m.selectedNodeName?html] />
						</h3>
						<div class="inexcludetables">
							<@render section=s.includesTable class="collectionincludes" />
						</div>
						
						<h3 class="rightheading excludesheading">
							<@bundlekey value="excludes.tablelabel" params=[m.selectedNodeName?html] />
						</h3>
						<div class="inexcludetables">
							<@render section=s.excludesTable class="collectionexcludes" />
						</div>
					</div>
				</@ajax.div>
			
				<@ajax.div id="leftpanelajaxdiv" class="leftPanel">
					<h3 class="leftheading">
						<@bundlekey "label.userstree" />
					</h3>
				
					<div class="treePanel">
						<@render section=s.usrGrpTree class="topic-tree treeview-gray" />
					</div>
	
					<@ajax.div id="buttonajaxdiv" class="adduserbuttons">
						<@render section=s.addUserButton />
						<@render section=s.addGroupButton />
						<@render section=s.removeUserGroupButton />
					</@ajax.div>
				</@ajax.div>
	
				<div style="clear:both"></div>
			</#if>
	</@ajax.div>

	<div class="button-strip">
		<@button section=s.saveButton showAs="save" />
	</div>
</div>
