<#include "/com.tle.web.freemarker@/macro/controls.ftl" />
<#include "/com.tle.web.freemarker@/macro/sections.ftl" />
<#include "/com.tle.web.sections.standard@/image.ftl" />
<#include "/com.tle.web.sections.equella@/component/button.ftl" />

<@css path="treenavctl.css" hasRtl=true/>
<@globalcss "treenav.css" />

<@render section=s.tabDialog />

<#-- TODO remove when proper radio, label, button renderers used -->
<#include "/com.tle.web.freemarker@/macro/_visinput.ftl">
<#macro radio id group value=unspec checked=false onClick=unspec>
	<#if checked>
		<#local _radio_extra='checked' />
	<#else>
		<#local _radio_extra='' />
	</#if>

	<#if value==unspec>
		<#assign value="">
	</#if>
	<@visinput id=id name=group type="radio"
		value=value
		extra=_radio_extra
		onClick=onClick />
</#macro>

<#macro label for value><#t/>
<label for="${for}">${value?html}</label><#rt/>
</#macro>

<#assign PART_READY>
	function setupTree(tree, treedef, callbacks, controls)
	{
		tree.rootElement = $("#root");
		tree.tabs = $("#tabs");
		tree.tabPopup = {
			attachment: controls.tabatt,
			viewer: controls.tabviewer,
			updateCallback: callbacks.tabViewerCallback,
			ajaxCall: ${s.viewerListAjaxCall}
			};
		tree.names = {open:'${id}open', selected:'${id}sel', edit: '${id}edit', deleted:'${id}deleted'};
		tree.leftPanel = $('#leftPanel');
		tree.$singleAttachment = $(${s.attachmentList.JSElement});
		tree.$singleViewer = $(${s.viewerList.JSElement});

		tree.singleAttachmentSetterCallback = callbacks.singleAttachmentSetterCallback;
		tree.multiAttachmentSetterCallback = callbacks.multiAttachmentSetterCallback;
		tree.singleViewerSetterCallback = callbacks.singleViewerSetterCallback;
		tree.multiViewerSetterCallback = callbacks.multiViewerSetterCallback;

		tree.singleViewerAjaxCall = ${s.viewerList.defaultAjaxCall};
		tree.optMultiple = $('#${id}optmultiple');
		tree.optSingle = $('#${id}optsingle');
		tree.itemSettings = $('#itemsettings');
		tree.upDownButtonPanel = $('#${id}upDownButtonPanel');
		
		tree.tabDialogOpen = controls.tabDialogOpen;
		tree.tabDialogClose = controls.tabDialogClose;

		tree.initialise();

		TreeLib.addNodes(tree, null, treedef);
	}
</#assign>

<@hidden id="${id}.method" value="" />

<div id="sampleNode" style="display:none;" class="navNode">
	<div class="droppable before"></div>
	<div class="nodeLine droppable file" cellpadding="0" cellspacing="0">
		<div class="container">
			<span class="iconBox">
				<a href="javascript:void(0);" class="link"><img src="${p.url('images/file.png')}" class="icon" /></a>
			</span>
			<span class="textBox">
				<a href="javascript:void(0);" class="label link">Text</a>
			</span>
		</div>
	</div>

	<div class="droppable after hidden"></div>
	<div class="navChildren hidden"></div>
</div>

<div class="overallPanel">
	<div class="titlePanel">
		<span class="heading">${b.key('define')}</span>
	</div>

	<div id="leftPanel" class="leftPanel">
		<div id="upDownContainerPanel" class="upDownContainerPanel">
			<span id="${id}upDownButtonPanel" class="upDownButtonPanel">
				<@button section=s.moveUpButton icon="up" iconOnly=true />
				<@button section=s.moveDownButton icon="down" iconOnly=true />
			</span>
		</div>

		<div class="treePanel">
			<div id="root"></div>
		</div>
	</div>


	<div class="rightPanel">
		<div id="allsettings" class="allsettings toppadded">
			<div class="rightHeading">
				<span class="heading">${b.key('allsettings')}</span>
			</div>

			<div class="checkrow">
				<div class="input checkbox">
					<@render s.showSplit />
				</div>
			</div>

			<div class="checkrow">
				<div class="input checkbox">
					<@render s.showUnassignedAttachments />
				</div>
			</div>
		</div>

		<div id="itemsettings" style="display: none" class="itemsettings toppadded">
			<div class="rightHeading">
				<span class="heading">${b.key('itemsettings')} </span>
			</div>

			<div class="formrow">
				<span class="form-label">${b.key('displayname')}</span>
				<@render section=s.nodeDisplayName class="formcontrol" />
			</div>

			<div class="tabsettingsHeading rightHeading">
				<span class="heading">${b.key('tabs.title')}</span>
			</div>

			<div class="tabsettings toppadded">
				<div id="radios" class="toppadded input radio">
						<@radio id="${id}optsingle" group="${id}type" checked=true onClick="tctl.changeRadio(false);" />
						<@label for="${id}optsingle" value=b.key('singletab') />
						<@radio id="${id}optmultiple" group="${id}type" onClick="tctl.changeRadio(true);" />
						<@label for="${id}optmultiple" value=b.key('multitabs') />
				</div>

				<div id="singlesettings">
					<div class="formrow">
						<span>${b.key('tabs.attachment')}</span>
						<@render section=s.attachmentList class="formcontrol"/>
					</div>

					<div class="formrow">
						<span>${b.key('tabs.viewer')}</span>
						<@render section=s.viewerList class="formcontrol"/>
					</div>
				</div>

				<div id="multisettings" style="display: none">
					<div class="formrow">
						<div class="formcontrol">
							<ul id="tabs" class="tablist">
							</ul>
							<@button section=s.tabAddButton showAs="add" />
						</div>
					</div>
				</div>
			</div>
		</div>
	</div>

	<div class="addRemovePanel">
		<@button section=s.addChildNodeButton showAs="add" />
		<@button section=s.addSiblingNodeButton showAs="add" />
		<br>
		<@button section=s.removeNodeButton showAs="delete" />
		<@button section=s.prepopButton />
	</div>
</div>
<div style="clear: both;"></div>