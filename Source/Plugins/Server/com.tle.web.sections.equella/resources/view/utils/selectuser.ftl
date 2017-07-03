<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.standard@/ajax.ftl"/>
<#import "/com.tle.web.sections.standard@/list.ftl" as l />

<#include "/com.tle.web.sections.standard@/textfield.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>

<@css path="selectuser.css" hasRtl=true />

<div class="selectuser">

	<#if m.topRenderable??>
		<@render m.topRenderable />
	</#if>

	<h3>${s.title}</h3>
	<#if s.subTitle??>
		${s.subTitle}
	</#if>
	<#if s.prompt??>
		<p>${s.prompt}</p>

		<#if s.groupFilterNames?? && s.groupFilterNames?size gt 0>
		<p><#t/>
			[<@render section=s.groupFilterTooltip class="filterstooltip">${b.key('utils.selectuserdialog.resultsfilteredlink')}</@render>]<#t/>
		</p><#t/>
		</#if><#t/>
	</#if>

	<div class="input text">
		<div class="control">
			<@textfield section=s.query class="query focus" autoSubmitButton=s.search/>
			<@button section=s.search showAs="search" size="medium" />
		</div>
	</div>
	<#-- Filter tooltip -->
	<#if s.groupFilterNames?? && s.groupFilterNames?size gt 0>
	<div class="displayfilters tooltip">
		<div class="control">
			<h4>${b.key('utils.selectuserdialog.resultsfiltereddesc')}</h4>
			<ul>
				<#list s.groupFilterNames as groupName><li>${groupName}</li></#list>
			</ul>
		</div>
	</div>
	</#if>

	<@div id="results">
		<div class="resultlist">
			<#if m.invalidMessageKey??>
				<h4>${b.key('utils.selectuserdialog.validation.invalid')}</h4>
			<#else>
				<#if m.hasNoResults>
					<h4>${b.key('utils.selectuserdialog.noresults')}</h4>
				<#else>
					<div class="modal-search-results">
						<ul class="modal-search-result">
							<@l.boollist section=s.userList; opt, state>
								<li>
									<@render state/>
									<#-- floated right.  damn I hate float -->
						
									<div class="username">${opt.username}</div>
									<div class="displayname"><@render opt.link /></div>
						
								</li>
							</@l.boollist>
						</ul>
					</div>
				</#if>
			</#if>
		</div>
	</@div>
</div>
