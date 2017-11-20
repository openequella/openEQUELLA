<#include "/com.tle.web.freemarker@/macro/sections.ftl">
<#include "/com.tle.web.sections.equella@/component/button.ftl">
<#import "/com.tle.web.sections.standard@/ajax.ftl" as a>

<@css path="selection.css" hasRtl=true />
<@css path="selectionreview.css" plugin="com.tle.web.sections.equella"/>

<@a.div id="selection-summary">
	<@render s.box>
		<#assign numResources = m.session.selectedResources?size>
		<h4>${b.key('selectionsbox.count.' + (numResources == 1)?string('singular', 'plural'), [numResources])}</h4>
	
		<ul class="blue">
			<#if numResources &gt; 0>
				<li><@render s.viewSelectedLink /></li>
				<li><@render s.unselectAllLink /></li>
			</#if>
		</ul>
	
		<#if s.finishedInBox>
			<div style="text-align: center">
				<@button section=s.finishedButton showAs="select" size="medium" />
			</div>
		</#if>
	</@render>
	
	<#if !s.finishedInBox>
		<@render s.finishedButton />
	</#if>
	<#if s.followWithHr>
		<hr>
	</#if>
</@a.div>