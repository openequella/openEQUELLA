<#ftl strip_whitespace=true />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />

<@css "gsiv.css" />

<@script "jquery.mousewheel.pack.js" />
<@script "GSIV.js" />
<@script "GSIV-thumbnail.js" />

<#assign PART_READY>
	var viewerBean = null;

	function initializeGraphic(zoomIn, zoomOut, rotateLeft, rotateRight, hideThumb, textHideThumb, textShowThumb) 
	{
		// opera triggers the onload twice
		if (viewerBean == null) {
			viewerBean = new GSIV('viewer', {
				tileBaseUri: '${m.tileBaseUri}',
				geometry: ${m.jsGeometry},
				blankTile: '${p.url("images/blank.gif")}',
				grabMouseCursor: '${p.url("images/grab.cur")}',
				grabbingMouseCursor: '${p.url("images/grabbing.cur")}'
			});
			viewerBean.fitToWindow();
			viewerBean.init();

			$('#'+zoomIn).click(function(){viewerBean.zoom(1)});
			$('#'+zoomOut).click(function(){viewerBean.zoom(-1)});
			$('#'+rotateLeft).click(function(){viewerBean.rotateLeft()});
			$('#'+rotateRight).click(function(){viewerBean.rotateRight()});

			setupGsivThumbnail(viewerBean, '#viewer .thumbnail', '#'+hideThumb, textHideThumb, textShowThumb);
		}
	}

	$(window).resize(function() {
		clearTimeout(viewerBean.resizeTimer);
		viewerBean.resizeTimer = setTimeout(function(){
			viewerBean.resize();
		}, 100);
	});
</#assign>

<#assign TEMP_body>
	<@render s.navBar />
	<div id="viewer">
		<div class="thumbnail"></div>
	</div>
</#assign>
