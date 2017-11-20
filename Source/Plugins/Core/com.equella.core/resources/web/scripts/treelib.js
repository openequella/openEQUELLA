var TreeLib = function()
{
	return { 
		addNodes : function(tree, parent, nodes, level) 
		{	
			if (level==null)
			{
				level=1;
			}
			for (var i = 0; i < nodes.length; i++) {
				var obj = nodes[i];
				if (obj) {
					var child = TreeLib.addNode(tree, parent, obj, level);
					if (obj.children) 
					{
						TreeLib.addNodes(tree, child, obj.children, level+1);
					}
				}
			}
		},
		
		ensureToggler : function(tree, node, state) 
		{
			if (node.open != state) 
			{
				TreeLib.toggleNode(tree, node);
			}
		},
		
		ensureVisible : function(tree, node)
		{
			while(node.parent != null)
			{
				TreeLib.ensureToggler(tree, node.parent, true);
				node = node.parent;
			}
		},
		
		toggleNode : function(tree, node) 
		{
			var openname = tree.names.open;
			if (!node.open) {
				TreeLib.addHidden(tree, openname, node.attr("id"), false);
			} else {
				TreeLib.removeHidden(tree, openname, node.attr("id"));
			}
			node.open = !node.open;
			node.navchild.toggleClass("hidden");
			TreeLib.setupImages(tree, node);
			return false;
		},
		
		setupImages : function(tree, node) 
		{
			if (node != null)
			{
				var togImage = tree.closedToggler;
				if (node.children[0] == null) {
					togImage = tree.hiddenToggler;
				} else {
					if (node.open) {
						togImage = tree.openToggler;
					}
				}
				node.toggler.attr("src", togImage);
				if (node.details.icon) {
					node.icon.attr("src", node.details.icon);
				}
			}
		},
		
		updateTreeDisplay : function(tree, node) 
		{
			TreeLib.setupImages(tree, node);
			node.label.text(node.textvalue);
		},
		
		clickNode : function(tree, node) 
		{
			if (tree.clicked) {
				return tree.clicked(node);
			}
			return TreeLib.defaultClickNode(tree, node);
		},
		
		defaultClickNode : function(tree, node) 
		{
			var curselect = tree.selected;
			if (curselect) {
				curselect.nodeline.toggleClass("selected");
			}
			if (curselect != node) {
				node.nodeline.toggleClass("selected");
				tree.selected = node;
				TreeLib.editHidden(tree, tree.names.selected, node.attr("id"));
				TreeLib.tryChange(tree, curselect)
			} else {
				TreeLib.removeSelection(tree);
			}
			return false;
		},
		
		tryChange : function(tree, curselect)
		{
			if (curselect != tree.selected)
			{
				if (tree.selectionChanged)
				{
					tree.selectionChanged(curselect);
				}
			}
		},
		
		removeSelection : function(tree)
		{
			var curselect = tree.selected;
			tree.selected = null;
			TreeLib.tryChange(tree, curselect);
			TreeLib.editHidden(tree, tree.names.selected, "");
		},
		
		_getHidden : function(tree, name, val)
		{
			var $match = $('input:hidden[name=' + name + ']', tree.rootElement);
			if (val)
			{
				$match = $match.find('[value=' + val + ']');
				alert('Match count ' + $match.length + ' with name,val (' + name + ', ' + val + ')');
			}
			return $match;
		},
		
		removeHidden : function(tree, name, val) 
		{
			TreeLib._getHidden(tree, name, val).remove();
		},
		
		editHidden : function(tree, name, val) 
		{
			var $existing = TreeLib._getHidden(tree, name, null);
			if ($existing.length > 0)
			{
				return $existing.val(val);
			}
			//alert('No existing value for ' + name);
		},
		
		addHidden : function(tree, name, val, overwrite) 
		{
			if (overwrite)
			{
				var $existing = TreeLib._getHidden(tree, name, null);
				if ($existing.length > 0)
				{
					return $existing.val(val);
				}
			}
			return $('<input type="hidden">').attr('name', name).val(val).appendTo(tree.rootElement);
		},
		
		addNode : function(tree, parent, details, level)
		{
			var sample = $("#sampleNode");
			var sampleLine = $("#sampleNode .nodeLine");
			
			sampleLine.addClass("level"+level);
			var newone = sample.clone(true);
			sampleLine.removeClass("level"+level);
			
			sampleLine.toggleClass("odd even");
		
			newone.details = details;
			newone.parent = parent;
			
			var navParent;
			if (!parent) 
			{
				navParent = tree.rootElement;
			} 
			else 
			{
				//parent.details.nokids = false;
				navParent = parent.navchild;
			}
			
			newone.details.nokids = true;
			newone.tree = tree;
			newone.attr("id", details.uuid);
			
			newone.label = $(".label", newone);
			newone.toggler = $(".toggler", newone);
			
			newone.navchild = $(".navChildren", newone);
			newone.open = details.open;
			if (details.open) 
			{
				var openname = tree.names.open;
				newone.navchild.toggleClass("hidden");
				TreeLib.addHidden(tree, openname, details.uuid, false);
			}
			newone.tabs = details.tabs;
			
			var nodeLine = $(".nodeLine", newone);
			newone.nodeline = nodeLine;
			newone.icon = $(".icon", nodeLine);
			newone.text = $(".text", nodeLine);
			
			newone.link = nodeLine;
			
			newone.textvalue = details.name;
			
			TreeLib.updateTreeDisplay(tree, newone);
			navParent.append(newone);
		
			newone.children = [];
			var parentList = TreeLib.getParentList(tree, newone);
			newone.childIndex = parentList.length;
			parentList.push(newone);
		
			TreeLib.initialiseNode(tree, newone);
		
			if (tree.nodecreated) 
			{
				tree.nodecreated(newone);
			}
			
		    newone.show();
			return newone;
		},
		
		initialiseNode: function(tree, node)
		{
			var pare = node.parent;
			if (pare)
			{
				pare.details.nokids = false;
				TreeLib.setupImages(tree, pare);
			}
			
			node.toggler.bind("click", node, 
				function(event) 
				{
					return TreeLib.toggleNode(tree, event.data);
				});
					
			node.link.bind("click", node, 
				function(event) 
				{
					return TreeLib.clickNode(tree, event.data);
				});
				
			TreeLib.ensureVisible(tree, node);
		},	
			
		getPrevNode : function(tree, node)
		{
			var parentList = TreeLib.getParentList(tree, node);
			for (var i = 0; i < parentList.length; i++)
			{
				if (parentList[i] == node)
				{
					if (i > 0)
					{
						return parentList[i-1];
					}
					else
					{
						return null;
					}
				}
			}
			return null;
		},
		
		getNextNode : function(tree, node)
		{
			var parentList = TreeLib.getParentList(tree, node);
			for (var i = 0; i < parentList.length; i++)
			{
				if (parentList[i] == node)
				{
					if (i < parentList.length - 1)
					{
						return parentList[i+1];
					}
					else
					{
						return null;
					}
				}
			}
			return null;
		},
		
		initTree : function(tree) 
		{
			tree.nextNumber = 0;
		},
		
		getParentList : function(tree, node)
		{
			if (!node.parent) 
			{
				return tree.rootNodes;
			} 
			else 
			{
				return node.parent.children;
			}
		},
		
		deleteNode : function(tree, node)
		{
			if (tree.deleted)
			{
				tree.deleted(node);
			}
			var id = node.attr("id");
			TreeLib.addHidden(tree, tree.names.deleted, id, false);
			if (tree.selected == node)
			{
				TreeLib.removeSelection(tree);
			}
			node.remove();
		},
		
		moveNode : function(tree, src, dest, where)
		{
			if (src == dest || !dest)
			{
				return;
			}
			
			var pare = dest.parent;
			while (pare != null)
			{
				if (pare == src)
				{
					return;
				}
				pare = pare.parent;
			}
			
			pare = src.parent;
			if (where == 'on')
			{
				src.appendTo(dest.navchild);
				src.parent = dest;
				dest = null;
			}
			if (where == 'before')
			{
				dest.before(src);
				src.parent = dest.parent;
			}
			if (where == 'after')
			{
				dest.after(src);
				src.parent = dest.parent;
			}
			
			TreeLib.ensureVisible(tree, src);
			
			if (tree.moved)
			{
				tree.moved(src, pare, dest, where);
			}
		},
		
		tabEncoder : function(tree, tab) 
		{
			var val = escape(Utf8.encode(tab.name));
			val += '|';
			val += escape(Utf8.encode(tab.viewer));
			val += '|';
			var attach = tab.attachment;
			if (attach == null)
			{
				attach = '';
			}
			val += escape(Utf8.encode(attach));
			return val;
		}
	};
}();
