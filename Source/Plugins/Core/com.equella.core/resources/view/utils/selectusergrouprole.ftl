<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.standard@/ajax.ftl"/>
<#include "/com.tle.web.sections.standard@/checklist.ftl"/>
<#include "/com.tle.web.sections.standard@/textfield.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>
<#import "/com.tle.web.sections.standard@/list.ftl" as l />

<@css path="selectuser.css" hasRtl=true />

<div class="selectuser">

	<#if m.topRenderable??>
		<@render m.topRenderable />
	</#if>

	<h3>${b.key('utils.selectuserdialog.usergrouprole.title')}</h3>

	<#if s.userGroupRoleSelector??>
		<@l.boollist section=s.userGroupRoleSelector; opt, state>
			<span class="sidebyside">
				<@render state />
			</span>
		</@>
	</#if>

	<#if s.prompt??>
		<p>${s.prompt}</p>
	</#if>

	<div class="input text">
		<div class="control">
			<@textfield section=s.query class="query" autoSubmitButton=s.search/>
			<@button section=s.search showAs="search">${b.key('utils.selectuserdialog.searchbutton')}</@button>
		</div>
	</div>

	<@div id="results">
		<div class="resultlist">
			<#if m.invalidMessageKey??>
				<h4>${b.key(m.invalidMessageKey)}</h4>
			<#else>
				<#if m.hasNoResults>
					<h4>${b.key('utils.selectuserdialog.noresults')}</h4>
				<#else>
					<div class="modal-search-results">
						
						<@checklist section=m.resultList list=true class="modal-search-result"/>
						<#--	
					
						<#if s.isShowGroups(_info)>
							<@checklist section=s.groupList list=true class="modal-search-result"/>
						<#elseif s.isShowRoles(_info)>
							<@checklist section=s.roleList list=true class="modal-search-result"/>
						<#else>
							<@checklist section=s.userList list=true class="modal-search-result"/>
						</#if>
						-->
						
					</div>
				</#if>
			</#if>
		</div>
	</@div>
</div>
