<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>
<div id="footer" role="contentinfo">
	<div id="footer-wrap">
		<div id="footer-inner">
			<div id="footer-links">
				<@bundlekey "footer.thankyou"/> <#if m.displayVersion??><span title="${m.fullVersion}">${b.key('footer.version')} ${m.displayVersion}</span></#if>
				<br>

				<#if m.displayClusterInfo>
					<@bundlekey value="footer.clusterinfo" params=[m.clusterNode!''?html, m.clusterMembers?html] />
					<br>
				</#if>

				<@bundlekey "footer.link.home"/> <#if m.displayLinks><@bundlekey "footer.link.community"/> </#if>

				<#if m.withinInstitution>
					<@bundlekey "footer.link.credits"/>
				<#else>
					<@bundlekey "footer.link.instcredits"/>
				</#if>
			</div>

			<div id="socialmedia">
				<@bundlekey "footer.link.rss"/>
				<@bundlekey "footer.link.twitter" />
				<@bundlekey "footer.link.copyright"/>
			</div>
			
			<div id="wcag-compliance">
				<a href="http://www.w3.org/WAI/WCAG2AA-Conformance" title="Explanation of WCAG 2.0 Level Double-A Conformance"> 
					<img height="29" width="80" src="${p.url("images/wcag2AA.gif")}" alt="Level Double-A conformance, W3C WAI Web Content Accessibility Guidelines 2.0">
				</a>
			</div>
		</div>
	</div>
</div>
