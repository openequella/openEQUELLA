var CourseList = {
		
	scrollToSelected: function()
	{
		var $selectedFolder = $('.targetfolder:has(input[type=radio]:checked)');
		if ($selectedFolder.length)
		{
			var $tree = $('.foldertree');
			var $treeScroll = $('.folderscroll');
			var folderTopRel = $selectedFolder.offset().top - $tree.offset().top;
			var folderHeight = $selectedFolder.height();
			var folderBottom = folderTopRel + folderHeight + 20;
			var treeHeight = $tree.height();
			
			//only scroll if not fully visible
			if (folderTopRel < 0 || folderBottom > treeHeight)
			{
				var st = $tree.scrollTop() + folderTopRel;
				$tree.scrollTop(Math.min(st, $treeScroll.height() - treeHeight));
			}
		}
	},
	
	transfer: function($source)
	{
		var $selectedFolder = $('.targetfolder:has(input[type=radio]:checked)');
		if ($selectedFolder.length)
		{
			$source.effect("transfer", { to: $selectedFolder, className: "courselisttransfer"}, 800, null);
		}
	},
	
	resize: function()
	{
		var $rhs = $('.selection-courses');
		var headerHeight = $("#selection-header").height();
		var visHeight = Math.min($(window).height(), $('#selection-content').height());
		//match the 15px offset at the top
		var rhsHeight = visHeight - headerHeight - 15;
		$rhs.find('.foldertree').height(rhsHeight - $rhs.find('.courselisttop').height() - 55);
		CourseList.scrollToSelected();
	},

	/**
	 * @param ajaxCb AJAX event when content is dropped onto a folder
	 * @param ajaxClickCb AJAX event when a folder is clicked
	 * @param doItems items are selectable?
	 * @param doAttachments attachments are selectable?
	 */
	setupDraggables: function(ajaxCb, ajaxClickCb, doItems, doAttachments)
	{
		var ajaxDropCallback = ajaxCb;
		var clickFolderCallback = ajaxClickCb;
		
		var listClass = 'itemlist';
		var resultClass = 'itemresult';
		var itemResultClass = 'itemresult-title';
		var itemSummaryClass = 'item-title';
		//var attachmentClass = '[data-attachmentuuid]';
		
		var listSelector = '.' + listClass;
		var resultSelector = '.' + resultClass;
		var itemResultSelector = '.selectable.' + itemResultClass;
		var itemSummarySelector = "h2." + itemSummaryClass;
		var attachmentSelector = '.selectable .attachmentrow.with-select';
		
		var dropCallback = function(event, ui)
		{
			var $res = $(ui.draggable);
			var $data = $res.find('[data-itemuuid]');
			if ($data.length == 0)
			{
				$data = $res;
			}
			var uuid = $data.attr('data-itemuuid');
			var version = 0 + $data.attr('data-itemversion');
			var attachmentUuid = $data.attr('data-attachmentuuid');
			var type = (attachmentUuid ? 'a' : 'p');
			var extensionType = $data.attr('data-extensiontype');
						
			var $target = $(this);
			var folder = $target.attr('data-folderid');
			
			if (ajaxDropCallback)
			{
				ajaxDropCallback(
					JSON.stringify({
						"uuid": uuid, 
						"version": version, 
						"type": type, 
						"attachmentUuid": attachmentUuid, 
						"folderId": folder,
						"extensionType": extensionType}),
					folder);
			}
		}
		var makeHelper = function()
		{
			var $dragged = $(this);
			var $div = $dragged.clone();
			$div.find('button').remove();
			debug('dw' + $dragged.width());
			debug('dh' + $dragged.height());
			$div.width($dragged.width());
			$div.height($dragged.height());
			$div.zIndex(10000);
			return $div;
		};
		var draggableOpts = {
				cursor: 'move',
				distance: 10,
				helper: makeHelper,
				opacity: 0.8,
				revert: 'invalid',
				scroll: false
			};
		
		if (doItems)
		{
			var itemListWatcher = function()
			{
				$(listSelector + ' ' + itemResultSelector).draggable(draggableOpts);
			};
			$(itemListWatcher);
			$(document).bind('equella_searchresults.courselist', itemListWatcher);
			
			$(function()
			{
				$(itemSummarySelector).draggable(draggableOpts);
			});
		}
		
		if (doAttachments)
		{
			var attachmentWatcher = function()
			{		
					$(attachmentSelector).draggable(draggableOpts);
			};
			$(attachmentWatcher);
			$(document).bind('equella_showattachments.courselist', attachmentWatcher);
		}
		
		var droppableOpts = {
				accept: itemResultSelector + ', ' + attachmentSelector + ', ' + itemSummarySelector,
				activeClass: 'droptarget',
				hoverClass: 'hover',
				tolerance: 'pointer',
				drop: dropCallback
			}; 
		var courseListWatcher = function()
		{
			$('.targetfolder').droppable(droppableOpts).on('click.courselist', function()
					{
						var $t = $(this);
						$('.targetfolder').removeClass('selected');
						//Don't call click(), you get stuck in a nasty event loop
						$t.find('input[type=radio]').attr('checked', true);
						$t.addClass('selected');
						if (clickFolderCallback)
						{
							clickFolderCallback();
						}
					}		
			);
		};
		$(courseListWatcher);
		$(document).bind('equella_courselistupdate.courselist', courseListWatcher);
				
		//sizing code... yuck
		$(document).ready(function(){setTimeout(CourseList.resize,50)});
		$(window).resize(CourseList.resize);
		$(document).bind('equella_searchresults.courselist', CourseList.resize);
	},
	
	updateTargetFolder: function(ajax, folderId, eventArgs, ajaxIds)
	{
		var fid = folderId;
		if (!fid)
		{
			var $checked = $('.targetfolder:has(input[type=radio]:checked)');
			if (!$checked.length)
			{
				return;
			}
			fid = $checked.attr('id').substring('folder_'.length);
		}
		
		//TODO: shouldn't need to tell the server the selected folder
		var reloadData = {
				ajaxIds : ajaxIds,
				folderId : fid,
				event : eventArgs
			};
		
		ajax(function(result)
		{
			updateIncludes(result, function()
			{
				var $tree = $(".foldertree");
				if (!$tree.length)
				{
					return;
				}
				var folderId = result.folderId;
				var newTree = result.updates['courselistajax'];
				var $newTree = $(newTree.html);
				$tree.find('[data-folderid="' + folderId + '"]').replaceWith($newTree.find('[data-folderid="' + folderId + '"]'));
				$(document).trigger('equella_courselistupdate');
			});
		}, JSON.stringify(reloadData));
	}
}