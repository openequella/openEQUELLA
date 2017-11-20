<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.standard@/textfield.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>
<#include "/com.tle.web.sections.standard@/radio.ftl"/>
<#include "/com.tle.web.sections.standard@/list.ftl" />
<#include "/com.tle.web.sections.equella@/macro/settings.ftl"/>
<#include "/com.tle.web.sections.standard@/ajax.ftl"/>

<@css path="selector.css" hasRtl=true />
<@script "bootstrap-tab.js" />
 
<div class="tabbable"> 
	<ul class="nav nav-tabs" id="myTab">
		<li class="active"><a href="#tab1" data-toggle="tab">${b.key('selector.tabname.tab1')}</a></li>
		<li><a href="#tab2" data-toggle="tab">${b.key('selector.tabname.tab2')}</a></li>
	</ul>
	<div class="tab-content">
		<div class="tab-pane active" id="tab1">
			<div class="input text">
				<div class="control">
					<@textfield section=s.query class="query focus" autoSubmitButton=s.search/>
					<@button section=s.search showAs="search" size="medium" />
				</div>
			</div>
			
		    <div class ="radio-button-row">
		    	<ul id="typelist">
		    	<li>${b.key('selector.type')}</li>
			    	<@boollist section=s.types; opt, state>
						<li><@radio state /></li>
					</@boollist>
				</ul>
			</div>
			
			<@div id ="groupfilter">
				<#if m.groupFilterVisible>
					<#if m.hasGroupSelected>
						<div class="groups">
							${b.key('selector.within')}  
							<#list s.groupFilterNames as groupName>
								${groupName}<#if groupName_has_next>,</#if>
							</#list>
						</div>
						<div class="links">
							<@render section=s.editGroupLink /> |
							<@render section=s.clearGroupLink />
						</div>
					<#else>
						<@render section=s.addGroupLink />
					</#if>	
				</#if>
			</@div>
			
			
			<@div id="search-result-list">
				<#if m.invalidMessageKey??>
					<h4>${b.key('selector.validation.invalid')}</h4>
				<#else>
					<#if m.hasNoResults>
						<#if m.typeOption =="USER">
							<h4>${b.key('selector.noresult.users')}</h4>
						<#elseif m.typeOption =="GROUP">
							<h4>${b.key('selector.noresult.groups')}</h4>
						<#elseif m.typeOption =="ROLE">
							<h4>${b.key('selector.noresult.roles')}</h4>
						</#if>
					</#if>	
					<ul class="search-result">
						<#if m.typeOption == "USER">
							<@boollist section=s.userList; opt, state>
								<li>
									<@render state/>
									<div class="displayname"><@render opt.link /></div>
									<div class="username">${opt.username}</div>
									<div class="add-user"><@render opt.add /></div>
								</li>
							</@boollist>
						</#if>
					
						<#if m.typeOption == "GROUP">
							<@boollist section=s.groupList; opt, state>
								<li>
									<@render state/>
									<div class="add-user"><@render opt.link /></div>
								</li>
							</@boollist>
						</#if>
					
						<#if m.typeOption == "ROLE">
							<@boollist section=s.roleList; opt, state>
								<li>
									<@render state/>
									<div class="add-user"><@render opt.link /></div>
								</li>
							</@boollist>
						</#if>
					</ul>	
				</#if>
			</@div>		
					
			<@div id ="select-area">
				<#if m.showSelectArea>
		 			<div class="selectlinks"><@render s.selectAllLink/> | <@render s.selectNoneLink /></div> <@button section=s.addSelected showAs="add" size="medium" />
				</#if>
			</@div>
		</div>
		
		<div class="tab-pane" id="tab2">
			 <@div id ="other-types">
		    	<ul class = "other-options">
			    	<@boollist section=s.otherTypes; opt, state>
						<li><@radio state /></li>
						<#if opt.value == "SHARE_SECRET">
							<@render section=s.tokenIdList />
						<#elseif opt.value == "IP_ADDRESS" >
							<@render section=s.ipAddress />
						<#elseif opt.value == "HTTP_REFERRER">
							<@render section=s.httpReferrer />
							<@boollist section=s.referrerOptions; opt, state>
								<li class="referrer-options"><@radio state/></li>
							</@boollist>
						</#if>
					</@boollist>
				</ul>
			</@div>	
			<div class ="select-area">
				<@button section=s.addOtherButton showAs="add" size="medium"/>
			</div>	
		</div>
	</div>
</div>


<@div id="right-cloumn"> 
<#assign level=0 />
<#macro selectedList list>
    <#assign level=level+1 />
    <#list list as l>
        <ul class="grouping level${level}">
        	<li class="select" id ="${l.id}">
            	<@render l.grouping />
            	<@render l.delete />
            </li>
            
            <#if l.expression??>
            	<#list l.expression as e>
            		 <li><div class ="expression-text">${e.selection}</div> <@render e.deleteSelection /></li>
            	</#list>
            </#if>
        </ul>
        <#if l.children??>
        	<@selectedList l.children />
        </#if>
    </#list>
    <#assign level=level-1 />
</#macro>

<@textfield section=s.selected hidden=true />
<#if m.selectionDisplayTree??>
		<#assign level=level+1 />
		<ul class="grouping level${level}">
			<li class="select" id=${m.selectionDisplayTree.id}>
            	<@render m.selectionDisplayTree.grouping />
            </li>
            
            <#if m.selectionDisplayTree.expression??>
            	<#list m.selectionDisplayTree.expression as e>
            		 <li><div class ="expression-text">${e.selection}</div> <@render e.deleteSelection /></li>
            	</#list>
            </#if>
        </ul>
    <#if m.selectionDisplayTree.children??>    
		<@selectedList m.selectionDisplayTree.children />
	</#if>
</#if>
</@div>

<div class="add-group">
	<@button section=s.addGroupingButton showAs="add" size="medium" />
</div> 
