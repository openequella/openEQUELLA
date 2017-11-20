<#include "/com.tle.web.freemarker@/macro/sections.ftl"/>

<@css path="filelist.css" plugin="com.tle.web.sections.equella" hasRtl=true/>

<@css path="treeviewer.css" hasRtl=true/>
<@globalcss "treenav.css" />
<@globalcss "customer.css" />
<@script "treenav.js" />

<@css path="packageviewer.css" hasRtl=true />

<#assign PART_READY>
		
	<#if m.ajaxUrl??>
		${head.addJs("scripts/scorm_api_13.js")}

		// The SCORM API object.
		var API_1484_11 = new APIObject2004('${m.ajaxUrl}');
		var API = API_1484_11;
	</#if>
	
	var hideNavBar = <#if m.hideNavControls>true<#else>false</#if>;
	$(window).resize(function() {
		resizeCols(true, hideNavBar);
		positionDivider(true, hideNavBar);
		resizeContent(hideNavBar);
	});
	
	$(document).ready(function() {
		resizeCols(false, hideNavBar);
		var divider = $('#pv-divider');
		if( divider.length != 0 ) {
			positionDivider(false, hideNavBar);
			divider.click(function() {
				$('#pv-content').toggleClass('collapsed');
				resizeContent(hideNavBar);
			});
		} else {
			$('#pv-content').addClass('collapsed');
		}
		resizeContent(hideNavBar);
	});
	
	
	$(function() {
		var root = initTreeNav(${m.definition}, '${s.next}', '${s.prev}', '${s.first}', '${s.last}');
		
		// Set up the split/join view button
		$('.splitview').toggle(
			function() {
				$(this).text('<@bundlekey "navbar.splitview"/>');
				$('#content2 iframe').removeClass('content-base').parent().hide();
			}, 
			function() {
				$(this).text('<@bundlekey "navbar.joinviews"/>');
				
				var iframe2 = $('#content2 iframe');
				if (root.lastSelectedUrl)
				{
					iframe2.attr('src', root.lastSelectedUrl);
				}
				iframe2.addClass('content-base').parent().show();
			}
		).click();
	});
</#assign>

<#assign TEMP_body>
	<div class="package-viewer">
		<#if !m.hideNavBar>
			<@render s.navBar />
		</#if>	
		<div id="pv-content">
			<#if !m.hideTree>
				<div id="pv-content-left">
					<#if !m.hideNavControls>
						<div class="btn-group">
							<@render s.first />
							<@render s.prev />
							<@render s.next />
							<@render s.last />
						</div>
					</#if>
					<div id="treeWrapper">
						<div class="tree file-list">
							<div id="sampleNode" style="display:none;" class="navNode">
								<div class="nodeLine even selectable file" cellpadding="0" cellspacing="0">
										<span class="textBox">
											<a href="javascript:void(0);" class="label link">Text</a>
											<span class="label">Text</span>
										</span>
								</div>
								<div class="navChildren hidden"></div>
							</div>
							<div id="root"></div>
						</div>
					</div>

				</div>
				<div id="pv-divider">
					<div id="pv-divider-inner"></div>
				</div>
			</#if>
			<div id="pv-content-right">
				<div id="pv-content-right-inner">
					<div id="content1">
						<iframe id="iframe1" class="content-base"></iframe>
					</div>
					<div id="content2" style="display:none;">
						<iframe id="iframe2"></iframe>
					</div>
				</div>
			</div>
		</div>
	</div>
</#assign>
