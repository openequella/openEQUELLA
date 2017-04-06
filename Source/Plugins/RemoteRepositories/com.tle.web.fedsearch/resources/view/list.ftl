<#include "/com.tle.web.freemarker@/macro/sections.ftl">

<#--<@css "repoitemlist.css"/>-->
<#-- a bit dodgy -->
<@css path="itemlist.css" plugin="com.tle.web.itemlist" hasRtl=true />

<#list m.items as item>
	<div class="itemresult-wrapper">
		<div class="itemresult-container">
			<div class="itemresult">
				<div class="itemresult-content">
					<h3 class="itemresult-title"><@render item.title/></h3>
					<p><#if item.description??>${item.description}</#if></p>
					
					<div class="itemresult-meta">
						<#list item.metadata as meta>
							<strong>${meta.label}:</strong>
							<@render meta.value/>
							<br>
						</#list>
					</div>
				</div>
			</div>
		</div>
		
		<div class="itemresult-rating">
			<div class="rating-bar">
			</div>
		</div>
	</div>
</#list>
