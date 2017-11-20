<#include "/com.tle.web.freemarker@/macro/sections.ftl">

<@css path="skinny-selection.css" />

<#assign d = m.decorations />

<#assign TEMP_body>
	<div id="wrapper">
		<div id="selection-header">
			<div id="selection-header-content" <#if m.hideDividers??> class="${m.hideDividers}"</#if>>
				<@render s.navBar />
			</div>
		</div>
	 	<div id="body">
			<div id="body-inner">
				<div id="selection-page">
					<div id="selection-content" class="content">
						<div id="selection-content-inner" ${d.contentBodyAttributes}>
							<@render m.parts["body"]/>	
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>
</#assign>
