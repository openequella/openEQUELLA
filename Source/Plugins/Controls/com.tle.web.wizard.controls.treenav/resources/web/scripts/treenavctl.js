function TreeNavControl(confirmMessage, nodeDisplayNameTextField, tabAddButton, popupTabNameTextField, popupSaveButton, popupCancelButton)
{
	this.disabled = false;
	this.nodeDisplayNameTextField = nodeDisplayNameTextField;
	this.tabAddButton = tabAddButton;
	this.popupTabNameTextField = popupTabNameTextField;
	this.popupSaveButton = popupSaveButton;
	this.popupCancelButton = popupCancelButton;
	this.setRemoveConfirmMessage(confirmMessage);
}

TreeNavControl.prototype.rootElement;
//The dropdown to select an attachment in a single tab situation
TreeNavControl.prototype.$singleAttachment;
//The dropdown to select a viewer in a single tab situation
TreeNavControl.prototype.$singleViewer;
TreeNavControl.prototype.optMultiple;
TreeNavControl.prototype.optSingle;
TreeNavControl.prototype.disabled;
TreeNavControl.prototype.isSingle;
TreeNavControl.prototype.leftPanel;
TreeNavControl.prototype.upDownButtonPanel;
TreeNavControl.prototype.itemSettings;

TreeNavControl.prototype.singleAttachmentSetterCallback;
TreeNavControl.prototype.multiAttachmentSetterCallback;
TreeNavControl.prototype.singleViewerSetterCallback;
TreeNavControl.prototype.multiViewerSetterCallback;

TreeNavControl.prototype.nodeDisplayNameTextField;
TreeNavControl.prototype.tabAddButton;
TreeNavControl.prototype.popupTabNameTextField;
TreeNavControl.prototype.popupSaveButton;
TreeNavControl.prototype.popupCancelButton;

var removeConfirmMessage;
TreeNavControl.prototype.setRemoveConfirmMessage = function(msg)
{
	removeConfirmMessage = msg;
};


TreeNavControl.prototype.updateNode = function(node)
{
	if (node)
	{
		node.details.modified = true;
		node.textvalue = this.nodeDisplayNameTextField.val();
		TreeLib.updateTreeDisplay(this, node);
	}
};

var g_tctl_leftPanel;
var g_tctl_upDownButtonPanel;
TreeNavControl.prototype.resizePanel = function()
{
	//sadly there is no 'this' when used to hook onto window.resize
	//if (this.leftPanel)
	if (g_tctl_leftPanel)
	{
		var panelHeight = document.documentElement.clientHeight * 0.6 - 5;
		if (panelHeight < 300)
		{
			panelHeight = 300;
		}
		//this.leftPanel.css({height:panelHeight});
		g_tctl_leftPanel.css("height", panelHeight+"px");

		//this.upDownButtonPanel
		if (g_tctl_upDownButtonPanel)
		{
			var udtop = panelHeight/2 - 25;
			//this.upDownButtonPanel.css({top:udtop});
			g_tctl_upDownButtonPanel.css("top", udtop+"px");
		}
	}
};

TreeNavControl.prototype.createNode = function(isSibling)
{
	var newid = this.nextNumber++;
	var pare;
	if (isSibling)
	{
		if (this.selected)
		{
			pare = this.selected.parent;
		}
	}
	else
	{
		pare = this.selected;
	}

	var node = TreeLib.addNode(this, pare,
				{ uuid:newid, name:"New Node", open:false, newnode:true, modified:true }
			);
	TreeLib.clickNode(this, node);

	return false;
};


TreeNavControl.prototype.initialise = function()
{
	var tree = this;

	g_tctl_leftPanel = this.leftPanel;
	g_tctl_upDownButtonPanel = this.upDownButtonPanel;

	window.onresize = this.resizePanel;
	this.resizePanel();

	this.nodeDisplayNameTextField.on("keypress", this,
			function (event)
			{
				var tree = event.data;
				if (tree.selected)
				{
					tree.updateNode(tree.selected);
				}
				return true;
			}
		);
	this.nodeDisplayNameTextField.on("keyup", this,
			function (event)
			{
				var tree = event.data;
				if (tree.selected)
				{
					tree.updateNode(tree.selected);
				}
				return true;
			}
		);

	this.closedToggler = "images/folderclosed.gif";
	this.openToggler = "images/folderopen.gif";
	this.hiddenToggler = "images/hiddentoggle.gif";

	//events
	this.deleted = this.nodeDeletedCallback;
	this.selectionChanged = this.selectionChangedCallback;
	this.nodecreated = this.nodeCreatedCallback;
	this.moved = this.nodeMovedCallback;

	this.rootNodes = [];
	TreeLib.initTree(this);

	//when the attachment dropdown changes (single tab)
	var $viewerList = this.$singleViewer;
	var setter = this.singleViewerSetterCallback;
	this.$singleAttachment.on('change.treeNavAttachments',
			function (event)
			{
				var value;
				var selectedNode = tree.selected;
				if (selectedNode && selectedNode.tabs && selectedNode.tabs.length > 0)
				{
					value = selectedNode.tabs[0].viewer;
				}
				else
				{
					value = $viewerList.val();
				}
				tree.singleViewerAjaxCall(
						function()
						{
							setter(value);
						}
				);
			}
	);

	this.selectionChanged(null);
};


TreeNavControl.prototype.nodeCreatedCallback = function(node)
{
	if (!this.disabled)
	{
		var tree = this;
		var nodeline = node.nodeline;
		nodeline.get(0).node = node;
		var before = $(".before", node);
		before.get(0).node = node;
		var after = $(".after", node);
		after.get(0).node = node;

		nodeline.draggable({opacity:0.7, helper:"clone", distance: 5});
		nodeline.droppable({accept:".droppable", hoverClass:"drophover", tolerance:"pointer",
			drop:function (ev, ui)
			{
				var node = ui.draggable[0].node;
				TreeLib.moveNode(tree, node, this.node, "on");
			}
		});
		before.droppable({accept:".droppable", hoverClass:"midhover", tolerance:"pointer",
			drop:function (ev, ui)
			{
				var node = ui.draggable[0].node;
				TreeLib.moveNode(tree, ui.draggable[0].node, this.node, "before");
			}
		});
		after.droppable({accept:".droppable", hoverClass:"midhover", tolerance:"pointer",
			drop:function (ev, ui)
			{
				var node = ui.draggable[0].node;
				TreeLib.moveNode(tree, ui.draggable[0].node, this.node, "after");
			}
		});
	}
};

TreeNavControl.prototype.nodeDeletedCallback = function(node)
{
	while (node.children.length > 0)
	{
		TreeLib.deleteNode(this, node.children[0]);
	}
	var parent = node.parent;
	var children = this.rootNodes;
	if (parent)
	{
		children = parent.children;
	}
	var del = false;
	for (var i = 0; i < children.length; i++)
	{
		var child = children[i];
		if (!del && child == node)
		{
			children.splice(i, 1);
			i--;
			del = true;
		}
		if (del)
		{
			child.details.modified = true;
		}
	}
};

TreeNavControl.prototype.nodeMovedCallback = function(node, origparent, dest, where)
{
	node.details.modified = true;
	var children = this.rootNodes;
	if (origparent)
	{
		children = origparent.children;
	}
	var del = false;
	for (var i = 0; i < children.length; i++) {
		var child = children[i];
		if (!del && child == node) {
			children.splice(i, 1);
			i--;
			del = true;
		}
		if (del) {
			child.details.modified = true;
		}
	}
	children = this.rootNodes;
	if (node.parent)
	{
		children = node.parent.children;
	}
	if (dest == null)
	{
		children.push(node);
	}
	else
	{
		del = false;
		for (var i = 0; i < children.length; i++)
		{
			var child = children[i];
			if (!del && child == dest)
			{
				if (where == "after")
				{
					children.splice(i + 1, 0, node);
				}
				else
				{
					children.splice(i, 0, node);
				}
				i++;
				del = true;
			}
			if (del)
			{
				child.details.modified = true;
			}
		}
		TreeLib.setupImages(this, dest);
	}
	TreeLib.setupImages(this, origparent);
	TreeLib.setupImages(this, node.parent);
	TreeLib.setupImages(this, node)

};

TreeNavControl.prototype.selectionChangedCallback = function(old)
{
	if (old)
	{
		this.updateNode(old);
	}

	var newone = this.selected;
	var isNewOne = newone ? true : false;

	this.nodeDisplayNameTextField.val(isNewOne ? newone.textvalue : "");
	this.nodeDisplayNameTextField.attr("disabled", !isNewOne || this.disabled);
	this.tabAddButton.attr("disabled", !isNewOne || this.disabled);
	this.nodeChanged(old, newone);

	// It is understood that 'newone' may be null here
	this.refreshTabs(newone);

	this.tabAddButton.unbind().on("click", this,
		function (event)
		{
			var tree = event.data;
			var tab = tree.createTab(tree.selected, 'Tab ' + (tree.selected.tabs.length+1));
			tree.startEditTab(tree.selected, tab, true);
			return false;
		}
	);
	return false;
};

TreeNavControl.prototype.addDataForNodes = function(nodes)
{
	for (var i = 0; i < nodes.length; i++)
	{
		var node = nodes[i];
		var id = node.attr("id");
		
		//add to array of edits
		TreeLib.addHidden(this, this.names.edit, id, false); 
		
		TreeLib.addHidden(this, "dis-" + id, node.textvalue, true); //display name of node
		TreeLib.addHidden(this, "ind-" + id, i, true); //index of this node
		
		if (node.details.newnode)
		{
			// set isNew
			TreeLib.addHidden(this, "new-" + id, node.details.newnode, true);
		}
		if (node.parent)
		{
			// set parent
			TreeLib.addHidden(this, "par-" + id, node.parent.attr("id"), true);
		}
		if (node.tabs)
		{
			//Clear existing data so this operation is repeatable
			TreeLib.removeHidden(this, "tab-" + id, null);
			for (var j = 0; j < node.tabs.length; j++)
			{
				TreeLib.addHidden(this, "tab-" + id, TreeLib.tabEncoder(this, node.tabs[j]), false);
			}
		}
		this.addDataForNodes(node.children);
	}
};

TreeNavControl.prototype.refreshTabs = function(currentNode)
{
	var tree = this;
	
	$("li", tree.tabs).unbind().remove();
	if (!currentNode || !currentNode.tabs)
	{
		return;
	}

	JQueryUtils.visible($("#radios"), !(currentNode.tabs.length > 1));
	$.each(currentNode.tabs,
		function (i, tab)
		{
			var uid = "tab" + i;
			var editLink = $("<a href=\"javascript:void(0)\" id=\"" + uid + "\" name=\"" + uid + "\">" + tab.name + "</a>")
					.on("click", tab,
						function (event)
						{
							tree.startEditTab(currentNode, event.data);
							return false;
						});
			
			if (!tree.disabled)
			{
				var deleteLink = $("<a href=\"javascript:void(0)\" id=\"" + uid + "delete\" name=\"" + uid + "delete\" class=\"unselect\"></a>")
					.on("click", tab,
						function (event)
						{
							if (confirm(removeConfirmMessage))
							{
								var ind = tree.findTabIndex(currentNode.tabs, event.data);
								currentNode.tabs.splice(ind, 1);
								tree.refreshTabs(currentNode);
							}
							return false;
						});
			}
			var style = (i % 2 == 0) ? "even" : "odd";

			var li = $("<li class=\"" + style + "\"></li>").prepend(editLink);
			if (!tree.disabled)
			{
				li = li.append(deleteLink);
			}
			li.appendTo(tree.tabs);
		}
	);
};

TreeNavControl.prototype.findTabIndex = function(tabs, tab)
{
	for (var i = 0; i < tabs.length; i++)
	{
		if (tabs[i] == tab)
		{
			return i;
		}
	}
	return -1;
};

TreeNavControl.prototype.createTab = function(currentNode, tabname)
{
	if (!tabname || tabname == '')
	{
		tabname = 'Tab 1';
	}
	var tab = {name:(tabname), root: this.rootElement, viewer:''};
	if (!currentNode.tabs)
	{
		currentNode.tabs = [];
	}
	currentNode.tabs.push(tab);
	this.refreshTabs(currentNode);

	return tab;
};

TreeNavControl.prototype.stopEditTab = function(node, tab)
{
	var popup = this.tabPopup;
	//tab.name = popup.name.val();
	var orig = tab.name;
	var newname = this.popupTabNameTextField.val();
	var resname = popup.attachment.children("option").filter(":selected").text()

	if(newname != '')
	{
		tab.name = newname;
	}
	else if(resname != '')
	{
		tab.name = resname;
	}
	else
	{
		tab.name = orig;
	}
	
	tab.attachment = popup.attachment.val();
	tab.viewer = popup.viewer.val();
	this.refreshTabs(node);
	this.tabDialogClose();
};

TreeNavControl.prototype.startEditTab = function(node, tab, isNew)
{
	var tree = this;
	var popup = this.tabPopup;
	//popup.name.val(tab.name);
	this.popupTabNameTextField.val(tab.name);
	popup.attachment.off('change');
	this.multiAttachmentSetterCallback(tab.attachment ? tab.attachment : '');

	var viewer = tab.viewer;
	var viewerSetter = this.multiViewerSetterCallback;
	popup.attachment.change(
			function ()
			{
				if (viewer == null)
				{
					viewer = popup.viewer.val();
				}
				popup.ajaxCall(popup.attachment.val(),
						function(results){
							popup.updateCallback(results);
							viewerSetter(viewer);
							viewer = null;
						}
				);
			}
	);
	popup.attachment.change();

	if (this.popupSaveButton)
	{
		this.popupSaveButton.unbind().click(
			function ()
			{
				tree.stopEditTab(node, tab);
			}
		);
	}
	if (this.popupCancelButton)
	{
		this.popupCancelButton.unbind().click(
			function ()
			{
				if (isNew)
				{
					tree.cancelNewTab(node, tab);
				}
				else
				{
					tree.cancelEditTab(node, tab);
				}
			}
		);
	}
	tree.tabDialogOpen();
};

TreeNavControl.prototype.cancelNewTab = function(node, tab)
{
	var ind = this.findTabIndex(node.tabs, tab);
	node.tabs.splice(ind, 1);

	this.refreshTabs(node);
	this.tabDialogClose();
};

TreeNavControl.prototype.cancelEditTab = function(node, tab)
{
	this.tabDialogClose();
};

TreeNavControl.prototype.moveNode = function(node, isUp)
{
	var dest;
	if (isUp)
	{
		dest = TreeLib.getPrevNode(this, node);
	}
	else
	{
		dest = TreeLib.getNextNode(this, node);
	}
	TreeLib.moveNode(this, node, dest, (isUp ? "before" : "after"));
};

TreeNavControl.prototype.saveSingleValues = function(node)
{
	if (node)
	{
		var tab = node.tabs[0];
		if (tab)
		{
			if (this.$singleAttachment)
			{
				tab.attachment = this.$singleAttachment.val();
			}
			if (this.$singleViewer)
			{
				tab.viewer = this.$singleViewer.val();
			}
		}
	}
};

TreeNavControl.prototype.nodeChanged = function(oldnode, newnode)
{
	//save single values if applicable
	if (oldnode && this.isSingle)
	{
		this.ensureTabs(oldnode);
		this.saveSingleValues(oldnode);
	}
	if (newnode)
	{
		if (this.itemSettings)
		{
			this.itemSettings.css({display:"block"});
		}
		this.ensureTabs(newnode);
		var forceMulti = newnode.tabs.length > 1;
		this.setupSettingsForNode(newnode, forceMulti);
	}
	else
	{
		if (this.itemSettings)
		{
			this.itemSettings.css({display:"none"});
		}
	}
};

TreeNavControl.prototype.ensureTabs = function(node)
{
	if (node && (!node.tabs || node.tabs.length == 0))
	{
		var tab = this.createTab(node, "");
		tab.viewer = "";
		tab.attachment = "";
	}
};

TreeNavControl.prototype.loadSingleValues = function(node)
{
	var tab = node.tabs[0];
	var tree = this;
	var $viewerList = this.$singleViewer;

	//temporarily unbind the singleAttachment changed function
	this.$singleAttachment.off('change.treeNavAttachments');
	this.singleAttachmentSetterCallback(tab.attachment);

	var setter = this.singleViewerSetterCallback;
	this.$singleAttachment.on('change.treeNavAttachments',
			function (event)
			{
				var value;
				var selectedNode = tree.selected;
				if (selectedNode && selectedNode.tabs && selectedNode.tabs.length > 0)
				{
					value = selectedNode.tabs[0].viewer;
				}
				else
				{
					value = $viewerList.val();
				}
				tree.singleViewerAjaxCall(
						function()
						{
							setter(value);
						}
				);
			}
	);

	this.singleViewerAjaxCall(
			function()
			{
				setter(tab.viewer);
			}
	);
};

TreeNavControl.prototype.confirmPrepop = function(msg)
{
	if( this.rootNodes.length > 0)
	{
		return confirm(msg);
	}
	return true;
};

TreeNavControl.prototype.ensureTabNames = function(node)
{
	if (node)
	{
		$.each(node.tabs,
			function (i, tab)
			{
				if (tab && tab.name == '')
				{
					if (this.$singleAttachment)
					{
						this.singleAttachmentSetterCallback(tab.attachment);
						tab.name = $("option:selected", this.$singleAttachment).text();
					}
				}
			}
		);
	}
};

TreeNavControl.prototype.setupSettingsForNode = function(node, multi)
{
	this.ensureTabs(node);
	if (multi)
	{
		JQueryUtils.check(this.optSingle, false);
		JQueryUtils.check(this.optMultiple, true);
		JQueryUtils.visible($("#singlesettings"), false);
		JQueryUtils.visible($("#multisettings"), true);

		this.ensureTabNames(node);
		this.refreshTabs(node);
		this.isSingle = false;
	}
	else
	{
		JQueryUtils.check(this.optMultiple, false);
		JQueryUtils.check(this.optSingle, true);
		JQueryUtils.visible($("#singlesettings"), true);
		JQueryUtils.visible($("#multisettings"), false);

		this.loadSingleValues(node);
		this.isSingle = true;
	}
};

TreeNavControl.prototype.changeRadio = function(multi)
{
	var node = this.selected;
	if (this.isSingle)
	{
		this.saveSingleValues(node);
	}
	this.setupSettingsForNode(node, multi);
};

TreeNavControl.prototype.submit = function()
{
	if (this.selected)
	{
	  this.updateNode(this.selected);
	  if (this.isSingle)
	  {
	  	this.saveSingleValues(this.selected);
	  }
	}

	//Clear existing data so this operation is repeatable
	TreeLib.removeHidden(this, this.names.edit, null); //array of edited node ids	
	//TreeLib.removeHidden(this, this.names., null);
	
	this.addDataForNodes(this.rootNodes);
};


function submitTree(tree)
{
	if (tree)
	{
		tree.submit();
	}
}

function createTreeNode(tree, sibling)
{
	if (tree && !tree.disabled)
	{
		return tree.createNode(sibling);
	}
}

function removeTreeNode(tree)
{
	if (tree && !tree.disabled && tree.selected)
	{
		TreeLib.deleteNode(tree, tree.selected);
	}
	return false;
}

function moveTreeNodeUp(tree)
{
	if (tree && !tree.disabled && tree.selected)
	{
		tree.moveNode(tree.selected, true);
	}
	return false;
}

function moveTreeNodeDown(tree)
{
	if (tree && !tree.disabled && tree.selected)
	{
		tree.moveNode(tree.selected, false);
	}
	return false;
}

function refreshTabs(tree, saveButton, cancelButton)
{
	if (tree)
	{
	}
}

function addTab(tree, saveButton, cancelButton)
{
	if (tree)
	{

	}
}
