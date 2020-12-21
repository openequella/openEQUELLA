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
  newSearchPageItemClass: 'SearchPage-Item',
  newSearchPageAttachmentClass: 'SearchPage-Attachment',
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
		var newSearchPageItemSelector = '.' + CourseList.newSearchPageItemClass;
		var newSearchPageAttachmentSelector = '.' + CourseList.newSearchPageAttachmentClass;

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
    if (doItems) {
      CourseList.prepareDraggableAndBind(listSelector + ' ' + itemResultSelector, true);
      // According to the existing code, there is no need to bind any handler to drag events happening in the ItemSummary page.
      CourseList.prepareDraggable(itemSummarySelector);
    }

    if (doAttachments) {
      CourseList.prepareDraggableAndBind(attachmentSelector, false)
    }

    var droppableSelectors =
      [itemResultSelector, attachmentSelector, itemSummarySelector,
        newSearchPageItemSelector, newSearchPageAttachmentSelector].join();
    CourseList.prepareDroppable(droppableSelectors, dropCallback, clickFolderCallback);

		//sizing code... yuck
		$(document).ready(function(){setTimeout(CourseList.resize,50)});
		$(window).resize(CourseList.resize);
		$(document).bind('equella_searchresults.courselist', CourseList.resize);
	},

  /**
   *  A function used to set up Draggables and attach the drag event handler to oEQ events.
   *
   *  @param selector A valid jQuery selector
   *  @param forItem True if set up for Items
   */
  prepareDraggableAndBind: function(selector, forItem) {
    var dragHandler = CourseList.prepareDraggable(selector);
    if(forItem) {
      $(document).bind('equella_searchresults.courselist', dragHandler);
    }
    else{
      $(document).bind('equella_showattachments.courselist', dragHandler);
    }
  },

  // Make a DOM element draggable and return the drag event handler.
  prepareDraggable: function(selector) {
    var makeDraggableHelper = function() {
      var $dragged = $(this);
      var $div = $dragged.clone();
      // In new UI some attachments are renderer as buttons due to their viewers so removing buttons will
      // result in attachment title missing in the dragged div. Hence, we need to keep them.
      $div.find('button').not('.MuiLink-button').remove();
      debug('dw' + $dragged.width());
      debug('dh' + $dragged.height());
      $div.width($dragged.width());
      $div.height($dragged.height());
      // Due to appendTo: "#body", font size is wrong in this dragged div so need to reset the font size.
      $div.css({'font-size': $dragged.css('font-size')})
      $div.zIndex(10000);
      return $div;
    };

    var draggableOpts = {
      cursor: 'move',
      distance: 10,
      helper: makeDraggableHelper,
      opacity: 0.8,
      revert: 'invalid',
      scroll: false,
      // Append to '#body' can ensure the dragged div is always visible in new UI
      // regardless of MUI Card's 'overflow:hidden'.
      appendTo: "#body",
      cancel: false // Buttons are not allowed to drag by default so set 'cancel' to false to drag buttons
    };

    var dragHandler = function()
    {
      $(selector).draggable(draggableOpts);
    };
    $(dragHandler);
    return dragHandler;
  },

  // Although there is no need to set up droppables in new UI, making this function independent
  // can improve readability of this file.
  prepareDroppable: function(acceptDraggables, dropCallback, clickFolderCallback) {
    var droppableOpts = {
      accept: acceptDraggables,
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
            clickFolderCallback(function(){});
          }
        }
      );
    };
    $(courseListWatcher);
    $(document).bind('equella_courselistupdate.courselist', courseListWatcher);
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
        CourseList.updateCourseList(result);
			});
		}, JSON.stringify(reloadData));
	},

  updateCourseList: function(result) {
    var $tree = $(".foldertree");
    if (!$tree.length)
    {
      return;
    }
    var folderId = result.folderId;
    var newTree = result.updates['courselistajax'];
    var $newTree = $(newTree.html);
    // Update the whole folder tree rather than a single node.
    $tree.html($newTree.find(".foldertree"))
    $(document).trigger('equella_courselistupdate');
  }
}
