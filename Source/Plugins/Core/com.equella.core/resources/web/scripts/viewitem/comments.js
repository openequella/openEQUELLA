var COMMENTS_LIST_DIV_ID = 'comments-list';
var COMMENTS_ADD_DIV_ID = 'comments-add';
var COMMENTS_LIST_HEAD_ID = 'comments-list-head';
var COMMENT_CLASS = 'comment';

function commentEffect(oldDiv, newContents, onSuccess)
{
	//disable add comment button...
	$('.addComment').attr('disabled', 'disabled');
	$('body').addClass("waitcursor");

	if (newContents)
	{
		updateIncludes(newContents, function()
		{
			var commentListData = newContents.html[COMMENTS_LIST_DIV_ID];
			if (commentListData)
			{
				var $commentsListDiv = $("#" + COMMENTS_LIST_DIV_ID);
				var $updatedCommentsListDiv = $('<div>' + commentListData.html + '</div>');

				//removed comments
				$.each($commentsListDiv.children('.' + COMMENT_CLASS), function(index, existingComment)
						{
							var $existingComment = $(existingComment);
							var id = $existingComment.attr('id');
							var $updatedComment = $updatedCommentsListDiv.children('#' + id);

							if ($updatedComment.length == 0)
							{
								$existingComment.slideUp(300, function(){$existingComment.remove();} );
							}
						}
				);

				//comment count header
				var $commentsListHead = $commentsListDiv.children('#' + COMMENTS_LIST_HEAD_ID);
				var $updatedCommentsListHead = $updatedCommentsListDiv.children('#' + COMMENTS_LIST_HEAD_ID);
				if ($updatedCommentsListHead.length == 0)
				{
					$commentsListHead.remove();
				}
				else if ($commentsListHead.length == 0 && $updatedCommentsListHead != 0)
				{
					$commentsListDiv.prepend($updatedCommentsListHead);
					$commentsListHead = $commentsListDiv.children('#' + COMMENTS_LIST_HEAD_ID);
				}
				else
				{
					$commentsListHead.html($updatedCommentsListHead.html());
				}

				//added comments
				$.each($updatedCommentsListDiv.children('.' + COMMENT_CLASS), function(index, updatedComment)
						{
							var $updatedComment = $(updatedComment);
							var id = $updatedComment.attr('id');
							var $existingComment = $commentsListDiv.children('#' + id);

							if ($existingComment.length == 0)
							{
								$updatedComment.hide();
								$commentsListHead.after($updatedComment);
								$updatedComment.slideDown();
							}
						}
				);

				$.globalEval(commentListData.script);
			}

			//add comment section
			var commentAddData = newContents.html[COMMENTS_ADD_DIV_ID];
			if (commentAddData)
			{
				var $commentAddDiv = $('#' + COMMENTS_ADD_DIV_ID);
				$commentAddDiv.html(commentAddData.html);
				$.globalEval(commentAddData.script);
			}

			$('body').removeClass("waitcursor");

			// Preserve the disabled state from the empty check in setupChangeEvents
			var $a = $('.addComment');
			var dis = $a.data('disabled');
			if(dis == 'undefined' || dis == '' || dis == '0')
			{
				a.removeAttr('disabled');
			}
			else
			{
				$a.attr('disabled', dis);
			}
			$a.data('disabled', null);
		});
	}
}

function setupChangeEvents($text, $rating, $addButton)
{
	var $c = $text;
	var $r = $rating;
	var $a = $addButton;

	var commentEmpty = function()
	{
		return $.trim($c.val()) == '';
	};

	var ratingEmpy = function()
	{
		var val = $r.val();
		return typeof(val) == 'undefined' || val == '' || val == '0';
	};

	var isEmptyCommentsAndRating = function(){
		if ( commentEmpty() )
		{
			if (ratingEmpy())
			{
				return true;
			}
		}
		return false;
	};

	var checkEmpty = function()
	{
		// Mr Lebowski is "disabled", yes.
		var empty = isEmptyCommentsAndRating();
		var data = empty ? 'disabled' : '';
		if(empty)
		{
			$a.attr('disabled', data);
			$a.data('disabled', data);
		}
		else
		{
			$a.removeAttr('disabled');
			$a.data('disabled', data);
		}
	}

	$c.on('keyup.comments', checkEmpty);
	$r.on('change.comments', checkEmpty);

	checkEmpty();
}