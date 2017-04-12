/*
 * Async Treeview 0.1 - Lazy-loading extension for Treeview
 * 
 * http://bassistance.de/jquery-plugins/jquery-plugin-treeview/
 *
 * Copyright (c) 2007 Jörn Zaefferer
 *
 * Dual licensed under the MIT and GPL licenses:
 *   http://www.opensource.org/licenses/mit-license.php
 *   http://www.gnu.org/licenses/gpl.html
 *
 * Revision: $Id$
 *
 */

;(function($) {

	function createNode(parent) {
		var current = $("<li/>").data("treeid", this.id || "").html("<span>" + this.text + "</span>").appendTo(parent);
		if (this.classes) {
			current.children("span").addClass(this.classes);
		}
		if (this.expanded) {
			current.addClass("open");
		}
		if (this.hasChildren || this.children && this.children.length) {
			var branch = $("<ul/>").appendTo(current);
			if (this.hasChildren) {
				current.addClass("hasChildren");
				createNode.call({
					classes: "placeholder",
					text: "&nbsp;",
					children:[]
				}, branch);
			}
			if (this.children && this.children.length) {
				$.each(this.children, createNode, [branch])
			}
		}
	}
	
function load(settings, root, child, container) {
	$.getJSON(settings.url, {root: root}, function(response) {
		child.empty();
		$.each(response, createNode, [child]);
        $(container).treeview({add: child});
    });
}

var proxied = $.fn.treeview;
$.fn.treeview = function(settings) {
	if (!settings.url) {
		return proxied.apply(this, arguments);
	}
	var container = this;
	
	if (settings.initial)
	{
		$.each(settings.initial, createNode, [this]);
        $(container).treeview({add: this});
	}
	else
	{
		load(settings, "source", this, container);
	}
	
	var userToggle = settings.toggle;
	return proxied.call(this, $.extend({}, settings, {
		collapsed: true,
		toggle: function() {
			var $this = $(this);
			// FIX BY TLE: When the tree is using unique:true, this method will get called
			// for every sibling of a node being expanded, because it asks for them to be
			// contracted.  This in turn causes the children to be loaded for all of the
			// siblings, even though they're not specifically being expanded.  We've added
			// the extra clause that the node must have the "collapsable" class, indicating
			// that it has actually been expanded.
			if ($this.hasClass("hasChildren") && $this.hasClass("collapsable")) {
				var childList = $this.removeClass("hasChildren").find("ul");
				load(settings, $this.data("treeid"), childList, container);
			}
			if (userToggle) {
				userToggle.apply(this, arguments);
			}
		}
	}));
};

})(jQuery);