<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<#include "/com.tle.web.sections.standard@/ajax.ftl"/>
<#include "/com.tle.web.sections.standard@/checklist.ftl"/>
<#include "/com.tle.web.sections.standard@/textfield.ftl"/>
<#include "/com.tle.web.sections.equella@/component/button.ftl"/>

<@css path="selectcourse.css" hasRtl=true/>


<div class="selectcourse">
	<h3>${b.key('selectcoursedialog.subtitle')}</h3>
	<#if s.promptKey??>
		<p>${b.gkey(s.promptKey)}</p>
	</#if>

	<div class="input text">
		<div class="control">
			<@textfield section=s.query class="query" autoSubmitButton=s.search/>
			<@button section=s.search showAs="search">${b.gkey('com.tle.web.sections.equella.utils.selectuserdialog.searchbutton')}</@button>
		</div>
	</div>
	<@div id="results">
		<div class="resultlist">
			<#if m.invalidQuery>
				<h4>${b.gkey('com.tle.web.sections.equella.utils.selectuserdialog.validation.invalid')}</h4>
			<#else>
				<#if m.hasNoResults>
					<h4>${b.key('selectcoursedialog.noresults')}</h4>
				<#else>
					<div class="modal-search-results">
						<@checklist section=s.courseList list=true class="modal-search-result"/>
					</div>
				</#if>
			</#if>
		</div>
	</@div>
</div>