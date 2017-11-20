<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.standard@/ajax.ftl"/>
<#import "/com.tle.web.sections.standard@/list.ftl" as l />

<#include "/com.tle.web.sections.standard@/textfield.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>

<@css path="selectuser.css" hasRtl=true />

<div class="selectrole">

	<#if m.topRenderable??>
		<@render m.topRenderable />
	</#if>

	<h3>${s.title}</h3>
	<#if s.prompt??>
		<p>${s.prompt}</p>
	</#if>

	<div class="input text">
		<div class="control">
			<@textfield section=s.query class="query focus" autoSubmitButton=s.search/>
			<@button section=s.search showAs="search" size="medium" />
		</div>
	</div>

	<@div id="results">
		<div class="resultlist">
			<#if m.invalidMessageKey??>
				<h4>${b.key('utils.selectroledialog.validation.invalid')}</h4>
			<#else>
				<#if m.hasNoResults>
					<h4>${b.key('utils.selectroledialog.noresults')}</h4>
				<#else>
					<div class="modal-search-results">
						<ul class="modal-search-result">
							<@l.boollist section=s.roleList; opt, state>
								<li>
									<@render state/>
									<div class="displayname">${opt.rolename}</div>
								</li>
							</@l.boollist>
						</ul>
					</div>
				</#if>
			</#if>
		</div>
	</@div>
</div>
