function TreeNav()
{
	this.disabled = false;
}

TreeNav.prototype.rootElement;
TreeNav.prototype.rootNodes;
TreeNav.prototype.disabled;
TreeNav.prototype.names;
TreeNav.prototype.lastSelectedUrl;
TreeNav.prototype.nav;
TreeNav.prototype.split;
TreeNav.prototype.g_firstlink;
TreeNav.prototype.g_nextlink;
TreeNav.prototype.g_prevlink;
TreeNav.prototype.g_lastlink;
TreeNav.prototype.closedToggler;
TreeNav.prototype.openToggler;
TreeNav.prototype.hiddenToggler;
TreeNav.prototype.nodecreated;
TreeNav.prototype.selectionChanged;


TreeNav.prototype.initialise = function() 
{
	var tree = this;
	
	this.lastSelectedUrl = null
	
	this.nav = true;
	this.split = false;
	this.g_firstlink = null;
	this.g_nextlink = null;
	this.g_prevlink = null;
	this.g_lastlink = null;
	
	this.closedToggler = "images/folderclosed.gif";
	this.openToggler = "images/folderopen.gif";
	this.hiddenToggler = "images/hiddentoggle.gif";
	
	//events
	this.nodecreated = this.nodeCreatedCallback;
	this.selectionChanged = this.selectionChangedCallback;
	
	this.rootNodes = [];
}


TreeNav.prototype.nodeCreatedCallback = function(node)
{
	if (!node.details.url) 
	{
		node.link.removeAttr("href");
		node.label.filter("a").hide();
		node.link.unbind("click");
		node.nodeline.removeClass("selectable");
		node.nodeline.toggleClass("file folder");
	} 
	else 
	{
		node.label.filter("span").hide();
	}
}


TreeNav.prototype.selectionChangedCallback = function(old)
{
	if (this.selected) 
	{
		this.lastSelectedUrl = this.selected.details.url;
		$(".content-base").attr("src", this.lastSelectedUrl);	
		this.updateNavigation();
	}
	return false;
}


TreeNav.prototype.select = function(node)
{
	if (node && this.selected != node) 
	{
		TreeLib.clickNode(this, node);
		TreeLib.ensureVisible(this, node);
	}
	this.updateNavigation();
	return false;
}


TreeNav.prototype.setupTopNav = function(nextlink, prevlink, firstlink, lastlink) 
{
	var tree = this;
	
	this.g_firstlink = firstlink;
	this.g_nextlink = nextlink;
	this.g_prevlink = prevlink;
	this.g_lastlink = lastlink;
	
	nextlink.bind("click", function (event) 
	{
		return tree.select(tree.findNext(tree.selected, false, true));
	});
	
	prevlink.bind("click", function (event) 
	{
		return tree.select(tree.findPrev(tree.selected, false));
	});
	
	firstlink.bind("click", function (event) 
	{
		return tree.select(tree.findNext(null, false, true));
	});
	
	lastlink.bind("click", function (event) 
	{
		return tree.select(tree.findPrev(null, false));
	});
	
	this.updateNavigation();
}


TreeNav.prototype.findNext = function(current, checkthis, intochild) 
{
	if (current == null) 
	{
		current = this.rootNodes[0];
		checkthis = true;
		intochild = true;
	}
	if (checkthis && current.details.url) 
	{
		return current;
	}
	if (intochild && current.children.length > 0) 
	{
		return this.findNext(current.children[0], true, true);
	}
	
	var ind = current.childIndex + 1;
	var parentList = TreeLib.getParentList(this, current);
	
	if (ind >= parentList.length) 
	{
		if (!current.parent) 
		{
			return null;
		}
		return this.findNext(current.parent, false, false);
	}
	return this.findNext(parentList[ind], true, true);
}


TreeNav.prototype.findPrev = function(current, checkthis) 
{
	if (current == null) 
	{
		current = this.rootNodes[this.rootNodes.length - 1];
		while (current.children.length > 0) 
		{
			current = current.children[current.children.length - 1];
		}
		checkthis = true;
	}
	
	if (checkthis && current.details.url) 
	{
		return current;
	}
	
	var ind = current.childIndex - 1;
	var parentList = TreeLib.getParentList(this, current);
	if (ind < 0) 
	{
		if (!current.parent) 
		{
			return null;
		}
		return this.findPrev(current.parent, true);
	}
	
	current = parentList[ind];
	while (current.children.length > 0) 
	{
		current = current.children[current.children.length - 1];
	}
	return this.findPrev(current, true);
}


TreeNav.prototype.updateNavigation = function() 
{
	if (!this.g_nextlink || !this.g_prevlink) 
	{
		return;
	}
	
	if (this.findNext(this.selected, false, true)) 
	{
		this.g_nextlink.removeAttr("disabled");
		this.g_lastlink.removeAttr("disabled");
	} 
	else 
	{
		this.g_nextlink.attr("disabled", "disabled");
		this.g_lastlink.attr("disabled", "disabled");
	}
	
	if (this.findPrev(this.selected, false)) 
	{
		this.g_prevlink.removeAttr("disabled");
		this.g_firstlink.removeAttr("disabled");
	} 
	else 
	{
		this.g_prevlink.attr("disabled", "disabled");
		this.g_firstlink.attr("disabled", "disabled");
	}
}

function getDocHeight() {
	return Math.max(
		$(document).height(),
		$(window).height(),
		/* For Opera: */
		document.documentElement.clientHeight
	);
}

function getWinHeight() {
	return Math.min(
		$(document).height(),
		$(window).height(),
		/* For Opera: */
		document.documentElement.clientHeight
	);
}

function resizeContent(hideBar) {
	var newWidth = $('#pv-content').innerWidth();

	if( !hideBar ) {
		newWidth -= $('#pv-divider').outerWidth();	
	}

	var left = $('#pv-content-left');
	if( left.is(':visible') ) {
		newWidth -= left.outerWidth();	
	}

	$('#pv-content-right').width(newWidth);
	//$('#content1 iframe').width(newWidth);
}

function resizeCols(resize, hideBar) {
	var h = resize ? getWinHeight() : getDocHeight();
	var msie = $.browser.msie ? 1 : 0;
	var barSize = hideBar ? 0 : $('.navbar').height();
	
	$('#pv-content-left').height(h - barSize - msie);
	$('#pv-content-right').height(h - barSize - msie);
	$('#pv-content-right-inner').height(h - barSize - msie - 3);
	$('#content1 iframe').height(h - barSize - msie);

	var $navButtons = $('#pv-content-left .btn-group');
	// 10 is 5 padding top and bottom
	var navButtonsSize = ($navButtons.length ? $navButtons.height() + 10 : 0);
	var logoSize = 90;
	$('#treeWrapper').height($('#pv-content-left').height() - navButtonsSize - logoSize);
}

function positionDivider(resize, hideBar) {
	var h = resize ? getWinHeight() : getDocHeight();
	var msie = $.browser.msie ? 1 : 0;
	var barSize = hideBar ? 0 : 33;
	
	$('#pv-divider').css('height', h - barSize - 2 - msie);
	$('#pv-divider-inner').css('height', h - barSize - 4 - msie);
}

function initTreeNav(treedef, nextId, prevId, firstId, lastId) 
{	
	var tree = new TreeNav();
	
	tree.rootElement = $("#root");
	tree.names = {open:"open", selected:"sel"};
	
	tree.initialise();
	
	TreeLib.addNodes(tree, null, treedef);
	
	tree.updateNavigation();
	tree.setupTopNav($("#"+nextId), $("#"+prevId), $("#"+firstId), $("#"+lastId));
	tree.select(tree.findNext(null, false, false));
	
	return tree;
}
