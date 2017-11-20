var Attachments = (function()
{
	// private static methods
	var stopProp = function(e)
	{
		e.stopImmediatePropagation();
	}

	var getiOSVersion = function()
	{
		if (/iP(hone|od|ad)/.test(navigator.platform))
		{
			var v = (navigator.appVersion).match(/OS (\d+)_(\d+)_?(\d+)?/);
			return [ parseInt(v[1], 10), parseInt(v[2], 10), parseInt(v[3] || 0, 10) ];
		}
	}
	// Create static methods
	return {
		showAttachmentDetails : function($clicked)
		{
			var $detail = $clicked.find('.detail');

			if ($clicked.parents('.attachments-browse').hasClass('thumbs'))
			{
				var $droparrow = $clicked.find('.droparrow');
				$detail.css('top', $droparrow.css('top') + 19);
				if($droparrow.offset().left < $detail.width())
				{
					$detail.css('left', $droparrow.css('right'));
					$detail.css('border-radius', '3px');
				}else
				{
					$detail.css('right', $droparrow.css('right'));
				}
			}
			else
			{
				$detail.css('top', $clicked.outerHeight() - 2);
				$detail.css('right', -1);
			}
			$detail.slideDown(300);

			$(document).trigger('equella_showattachmentdetails');
		},

		setupAttachmentRows : function(itemId, attachmentsUlSelector)
		{
			var $ul = $(attachmentsUlSelector);
			var $lis = $ul.children('li');
			var activeElemId = null;

			var version = getiOSVersion();
			// running ios 7
			if (version && version[0] >= 7)
			{
				$lis.addClass("ios7");
			}

			var isActive = function($elem)
			{
				return activeElemId == $elem.attr('id');
			};
			var activate = function($elem)
			{
				$elem.removeClass('inactive');
				$elem.addClass('active');
				activeElemId = $elem.attr('id');
			};
			var deactivate = function($elems)
			{
				$elems.removeClass('active');
				$elems.addClass('inactive');
				$elems.find('.detail').hide();
				activeElemId = null;
			};
			var hideDetails = function(e)
			{
				var $targ = $(e.target);

				// if it's not an element of the UL or an element of the preview
				// box:
				var isAttachmentRows = $targ.parents('.attachments-browse').length > 0;
				var isPreviewBox = $targ.hasClass('.detail') || $targ.parents('.detail').length > 0;
				var isHideArrow = $targ.hasClass('droparrow') && $targ.parents('.attachmentrow').hasClass('active');

				if (isHideArrow || !(isAttachmentRows || isPreviewBox))
				{
					deactivate($lis);
				}
				//stopProp(e);
			};
			var hideOrShowDetails = function(e)
			{
				var $clicked = $(this);
				if (!isActive($clicked))
				{
					deactivate($lis);
					activate($clicked);
					Attachments.showAttachmentDetails($clicked);
				}
				else
				{
					deactivate($lis);
				}
				stopProp(e);
			};

			// Default attachment URL - must stop propagation or the row will
			// also receive a click event.
			$ul.on('click.attachments_viewer_prevent_trickle', 'a.defaultviewer', stopProp);

			if ('ontouchstart' in document.documentElement)
			{
				// Hide details on click anywhere
				$(document).on('touchstart', hideDetails);
				$ul.on('touchstart', 'li:has(a.droparrow)', hideOrShowDetails);
				$ul.on('touchstart', "input[name='lmse_as']", stopProp);
			}
			else
			{
				// Hide details on click anywhere
				$(document).on('click.attachments_hide_details', hideDetails)
				$ul.on('click.attachments_details', 'li:has(a.droparrow)', hideOrShowDetails);
				$ul.on('click.checkbox', "input[name='lmse_as']", stopProp);
			}
		},

		setupSelectButtons : function(selectCallback, itemId, itemExtensionType, attachmentsUlSelector)
		{
			var $ul = $(attachmentsUlSelector);

			$ul.on('a.defaultviewer', 'click.attachments', stopProp);

			$ul.on('click.attachments_select', 'button', function(e)
			{
				var attUuid = $(this).parents('.attachmentrow').find('[data-attachmentuuid]').attr(
						'data-attachmentuuid');

				// TODO: use a server event to interject on this callback?
				if (typeof CourseList != 'undefined')
				{
					CourseList.scrollToSelected();
					CourseList.transfer($(this));
				}
				selectCallback(attUuid, itemId, itemExtensionType);

				stopProp(e);
			});
		},

		setupSelectAllButton : function(selectAllCallback, uuids, itemId, itemExtensionType, attachmentsUlSelector)
		{
			if (typeof CourseList != 'undefined')
			{
				var $ul = $("#" + attachmentsUlSelector);
				CourseList.scrollToSelected();
				CourseList.transfer($ul);
			}
			selectAllCallback(uuids, itemId, itemExtensionType);
		},

		selectPackage : function($button, selectPackageCallback, itemId, controlId)
		{
			if (typeof CourseList != 'undefined')
			{
				CourseList.scrollToSelected();
				CourseList.transfer($button);
			}
			selectPackageCallback(itemId, null, controlId);
		},

		setupReorder : function(attachDivId, sortingOkCallback, warningString,saveLabel, cancelLabel, structured)
		{
			// TODO reorder shiz needs its own section rather than hacked into
			// attachments section
			var $attachDiv = $("#" + attachDivId);
			var $ul = $attachDiv.find('ul');

			$attachDiv.addClass('modal-attachments');
			
			var $overlay = $('<div id="overlay_' + attachDivId + '" class="overlay" tabindex="-1"></div>');
			$overlay.hide();
			$attachDiv.after($overlay);
			$overlay.fadeIn();

			// For each li, clone it and remove all click handlers
			$ul.find("li.attachmentrow").each(function()
			{
				var $li = $(this); 
				var $clone = $li.clone(true);
				$clone.find("a.defaultviewer, div.attachments-thumb a, div.thumbnail a").each(function()
				{
					var $cloneSubElement = $(this); 
					$cloneSubElement.attr("onclick", "");
					$cloneSubElement.off("click");
					$cloneSubElement.on("click", function(e)
					{
						e.preventDefault();
						stopProp(e);
					});
				});
				$clone.find("a.droparrow").remove();
				$clone.addClass("clone");
				
				$clone.off("mousedown.attachments_reorder");
				$clone.on("mousedown.attachments_reorder", function()
				{
					var $clicked = $(this);
					var toggleBack = $clicked.hasClass("inactive");
					var $currentActive = $clicked.parent().find("li.active");
					$currentActive.toggleClass("inactive");
					$currentActive.toggleClass("active");
					$currentActive.find("div.detail").hide();

					if (toggleBack)
					{
						$clicked.toggleClass("inactive");
						$clicked.toggleClass("active");
					}

				});
				$li.after($clone);
				$li.toggleClass("modal-hide");
			});
			
			function sortCallback(event, ui)
			{
				if ($.browser.mozilla && ui.helper !== undefined)
				{
					ui.helper.css('position','absolute').css('margin-top', $(window).scrollTop());
				}
			}
			function beforeStopCallback(event, ui)
			{
				if ($.browser.mozilla && ui.helper !== undefined)
				{
					ui.helper.css('margin-top', '');
				}
			}
			$ul.sortable({
				distance: 1,
				opacity: 0.7,
				sort: sortCallback,
				beforeStop: beforeStopCallback,
				stop : function()
				{
					hideFirstLast($ul)
				},
				placeholder: 'attachment-placeholder attachmentrow',
				tolerance: 'pointer',
				revert: 300,
				forceHelperSize: true,
				forcePlaceholderSize: true,
				scrollSensitivity: 80,
				scrollSpeed: 2
			});
			
			$ul.sortable('enable');
			$ul.find("li.clone").css('cursor', 'move');
			$ul.find("li.clone a").css('cursor', 'move');
			$ul.find("a.viewer").toggleClass("modal-hide");
			var $modalCancel = $("<a class='modal-control modal-cancel' tabindex='0'>" + cancelLabel + "</a>"); 
			var $modalSave = $("<a class='modal-control modal-save' tabindex='0'>" + saveLabel + "</a>");
			$attachDiv.after($modalCancel);
			$attachDiv.after($modalSave);

			setupAccessibilityControls($ul, structured);

			$modalSave.on('click.attachments_modal_save keydown.attachments_modal_save', function(e)
			{
				if (e.which != 9 && e.which != 16)
				{
					finishWithCallback($ul, $attachDiv, sortingOkCallback)
				}
			}).focus();
			$modalCancel.on('click.attachments_modal_cancel keydown.attachments_modal_cancel', function(e)
			{
				if (e.which != 9 && e.which != 16)
				{
					finishWithoutCallback($ul, $attachDiv);
				}
			});

			$overlay.on("mousedown.attachments_overlay", function(e)
			{
				var $focused = $(':focus');
				if (confirm(warningString))
				{
					finishWithCallback($ul, $attachDiv, sortingOkCallback);
				}
				e.preventDefault();
				stopProp(e);
				$focused.focus();
			});
			$attachDiv.focus();
			var $tabbables = $attachDiv.find(":tabbable").add($modalSave).add($modalCancel);
			$attachDiv.on("keydown", captureTab);
			$modalSave.on("keydown", captureTab);
			$modalCancel.on("keydown", captureTab);

			function captureTab(event)
			{
				var $firstTabbable = $tabbables.filter(":first");
				var $lastTabbable = $tabbables.filter(":last");
				
				if ( event.keyCode !== $.ui.keyCode.TAB ) 
				{
					return;
				}
				if ( ( event.target === $lastTabbable[0] || event.target === $attachDiv[0] ) 
						&& !event.shiftKey ) 
				{
					$firstTabbable.focus( 1 );
					event.preventDefault();
				} 
				else if ( ( event.target === $firstTabbable[0] || event.target === $attachDiv[0] ) 
						&& event.shiftKey ) 
				{
					$lastTabbable.focus( 1 );
					event.preventDefault();
				}
			}

			function finishWithoutCallback($ul, $attachDiv)
			{
				$ul.sortable("cancel");
				resetUi($ul, $attachDiv);
			}

			function finishWithCallback($ul, $attachDiv, callback)
			{
				var callbackArray = [];
				$ul.find('li.clone a.defaultviewer').each(function()
				{
					var attachUuid = this.getAttribute('data-attachmentuuid');
					callbackArray.push(attachUuid);
				});
				callback(null, callbackArray);
				$("li.clone").replaceWith(function()
				{
					var id = $(this).attr('id');
					return $("#" + id + ".modal-hide");
				});
				resetUi($ul, $attachDiv);
			}

			function resetUi($ul, $attachDiv)
			{
				$ul.find('.clone').remove();
				try
				{
					$ul.sortable('destroy');
				}
				catch (err)
				{
					//Ignore
				}
				$attachDiv.removeClass('modal-attachments');
				$ul.find(".modal-hide").removeClass("modal-hide")
				$("#overlay_" + attachDivId + ", .modal-control").fadeOut();
			}

			function setupAccessibilityControls($ul, structured)
			{
				// TODO limit focus to the ul
				// sets up up/down arrows and their event handlers

				var $moveUp;
				var $moveDown;
				if (structured)
				{
					$moveUp = $("<a class='reorder-button moveup reorder-structured' tabindex='0'><i class='icon-chevron-up'/></a>");
					$moveDown = $("<a class='reorder-button movedown reorder-structured' tabindex='0'><i class='icon-chevron-down'/></a>");
				}
				else
				{
					$moveUp = $("<a class='reorder-button moveup tiled-left reorder-tiled' tabindex='0'><i class='icon-chevron-down'/></a>");
					$moveDown = $("<a class='reorder-button movedown tiled-right reorder-tiled' tabindex='0'><i class='icon-chevron-up'/></a>");
				}

				$ul.find("li.attachmentrow.clone").append($moveDown);
				$moveDown = $ul.find("li.attachmentrow.clone .movedown");
				$moveDown.on('keydown.attachments_reorder_down click.attachments_reorder_down', function(e)
				{
					if (e.which != 9 && e.which != 16)
					{
						var $current = $(this).parent();
						$toReplace = $current.next();
						while (!$toReplace.hasClass("clone"))
						{
							$toReplace = $toReplace.next();
						}
						$toReplace.after($current);
						hideFirstLast($ul)
						$ul.find("a:first").focus();
						stopProp(e);
					}
				});

				$ul.find("li.attachmentrow.clone").append($moveUp);
				$moveUp = $ul.find("li.attachmentrow.clone .moveup");
				$moveUp.on('keydown.attachments_reorder_up click.attachments_reorder_up', function(e)
				{
					if (e.which != 9 && e.which != 16)
					{
						var $current = $(this).parent();
						$toReplace = $current.prev()
						while (!$toReplace.hasClass("clone"))
						{
							$toReplace = $toReplace.prev();
						}
						$toReplace.before($current);
						hideFirstLast($ul)
						$ul.find("a:first").focus();
						stopProp(e);
					}
				});

				hideFirstLast($ul);
				$ul.find("a:first").focus();
			}

			// hides first up arrow, last down arrow
			function hideFirstLast($ul)
			{
				$ul.find("li.clone .reorder-button.modal-hide").removeClass("modal-hide");
				$ul.find("li.clone .reorder-button.moveup").first().addClass("modal-hide");
				$ul.find("li.clone .reorder-button.movedown").last().addClass("modal-hide");
			}
		}

	}
})();
