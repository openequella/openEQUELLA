<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.equella@/component/button.ftl">
<@css path="copyright.css" hasRtl=true />

<div id="copyright-summary">
	<h3>${b.key("summary.title")}</h3>

	<#if m.holding.holdingLink??>
		<p id="copyright-holdinglink">${b.key('summary.in')} <@render m.holding.holdingLink/></p>
	</#if>
	<table id="copyright-table">
		<#list m.holding.portions as portion>
			<tr class="portion-row">
				<td class="copyright-portion" colspan="
					<#if m.holding.haveAdd && m.holding.haveActivate>
					6
					<#elseif m.holding.haveAdd || m.holding.haveActivate>
					5
					<#else>
					4
					</#if>">
					<#if m.holding.book>
						<#if portion.chapter??>
							<span class="chapterNumber">${b.key('summary.chapter')} ${portion.chapter}:</span> ${portion.title}
						<#else>
							<span class="chapterNumber">${portion.title}</span>
						</#if>
					<#else>
						${portion.title}
					</#if>
				</td>
			</tr>
			<#list portion.sections as section>
				<tr class="section-row"> 
				
					<td><div class="sectionCheckbox">
						<#if section.checkBox??><@render section.checkBox /></#if>
						</div>						
					</td>
					
					<td><div class="sectionAttachment">
						<#if section.icon??>
							<@render class="copyright-sectionlink" section=section.icon />
	          			</#if>
	          			<@render class="copyright-sectionlink" section=section.viewLink/>
	          			<#if section.portionLink??>
							<br><@render section=section.portionLink class="copyright-portionlink"/>
						</#if>
						</div>
					</td>
					
					<#if m.holding.showPages >
						<td><div class="sectionPages">
								${section.pages} ${b.key('summary.pages')}
								<#if section.percent?? && section.percent &gt; 0>
	                            	(${section.percent}%)
								</#if>
							</div>	
						</td>
					<#else>
						<td>&nbsp;</td>
					</#if>
					
					<td><div class="sectionStatus">
						${section.status}
						</div>
					</td>
					<#if m.holding.haveActivate>				
					<td class="sectionAction">
							<#if section.activateButton??>
								<@button section.activateButton/>
							</#if>	
						</td>
					</#if>
					<#if m.holding.haveAdd>				
						<td class="sectionAction">	
							<#if section.addButton??>												
								<@button section.addButton/>
							</#if>
						</td>
					</#if>					
				</tr>
			</#list>
		</#list>
	</table>
	<#if m.holding.holdingDisplay && m.holding.totalPages &gt; 0>
		<table id="copyright-totals">
			<tr>
				<td class="copyright-total">
					${b.key('summary.totalpages')}: 
				</td>
				<td class="copyright-totalvalue">
					${m.holding.totalPages}
				</td>
			</tr>
			<tr>
				<td class="copyright-total">
					${b.key('summary.pagesavailable')}: 
				</td>
				<td class="copyright-totalvalue">
					${m.holding.pagesAvailable}
				</td>
			</tr>
			<tr>
				<td class="copyright-total">
					${b.key("summary.alltotal")}:
				</td>
				<td class="copyright-totalvalue">
					${m.holding.totalPercent}%
				</td>
			</tr>
			<tr>
				<td class="copyright-total">
					${b.key("summary.activetotal")}:
				</td>
				<td class="copyright-totalvalue">
					${m.holding.totalActivePercent}%
				</td>
			</tr>
			<tr>
				<td class="copyright-total">
					${b.key("summary.inactivetotal")}:
				</td>
				<td class="copyright-totalvalue">
					${m.holding.totalInactivePercent}%
				</td>
			</tr>
		</table>
	</#if>
	<#if s.getActivateSelected().isDisplayed(_info)>
		<div class="button-strip">
			<@render s.activateSelected>${b.key("summary.activateall")}</@render>
		</div>
	</#if>		
	<#if m.showPortionLinks>
		<h4>${b.key('summary.others')}</h4>
		<ul>
			<#list m.holding.otherPortions as otherPortion>
				<li><@render otherPortion/></li>
			</#list>
		</ul>
	</#if>
</div>