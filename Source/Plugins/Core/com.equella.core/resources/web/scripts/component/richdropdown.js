(function($) {
  $.fn.setupRichDropDown = function(options) {
    return this.each(function() {
      var $select = $(this);

      var $richcont = $(
        '<div class="richdropdown-richcontainer richcontainer"/>'
      );
      var $richdd = $('<div class="richdropdown"></div>');
      var $containerDiv = $(
        '<div class="richdropdown-selectedcontainer selectedcontainer" tabIndex="0"></div>'
      );
      var $containerDivText = $(
        '<div class="richdropdown-selectedtext selectedtext"></div>'
      );

      $containerDiv.insertAfter($select);
      $containerDivText.prependTo($containerDiv);
      // if/else for Redmine #7163
      if ($("#dropdowncontainer").length != 0) {
        $("#dropdowncontainer").append($richcont.append($richdd));
      } else {
        $("#searchform").append($richcont.append($richdd));
      }

      $select.hide();

      // Build hidden div
      $select.children("optgroup").each(function() {
        // Create List
        var $optGroupList = $("<ul></ul>");
        $optGroupList.appendTo($richdd);

        // Get title
        var optionTitle = $(this).attr("label");
        if (optionTitle != "") {
          $titleH3 = $('<h3 tabindex="0"></h3>').text(optionTitle);
          var $optGroup = $("<li></li>").append($titleH3);
          $optGroup.appendTo($optGroupList);
        }

        $(this)
          .children()
          .each(function() {
            var option = $(this).html();
            var key = $(this).val();

            if ($(this).attr("selected")) {
              $containerDivText.text($(this).text());
            }
            var $optLink = $('<a href="javascript:void(0);"></a>').html(option);
            var $optGroup = $("<li></li>").append($optLink);
            $optGroupList.append($optGroup.data("key", key));
          });
      });

      // Cache links
      var $links = $richdd.find("ul li a");

      // Show menu on click or focus of tab selected, close on Esc
      $containerDiv
        .on("click.richdrop", function(event) {
          if ($containerDiv.hasClass("active")) {
            hideOptions();
          } else {
            showMenu(event);
          }
        })
        .on("keyup.richdrop", function(event) {
          //enter key
          if (event.which == 13 && $containerDiv.hasClass("active")) {
            hideOptions();
          } else if (event.which == 13) {
            showMenu(event);
          }
          if (event.which == 27) {
            hideOptions();
            $("#searchform-search").focus();
          }
        });

      function showMenu(event) {
        showOptions();
        event.stopImmediatePropagation();
      }

      // Add click event for li/a selection
      $richdd.find("ul").on("click.richdrop", "li a", function(event) {
        var $link = $(event.target);
        var text = $link.text();
        var val = $link.parent().data("key");

        // Update the select input
        $select.val(val);
        $select.change();

        // Update the selected text
        $containerDivText.text(text);

        // Hide the options
        hideOptions();
      });

      // Hide rich drop down on click elsewhere
      $(document).on("click.richdrop", function(event) {
        hideOptions();
      });

      $links.on("keyup.richdrop", function(event) {
        if (event.which == 27) {
          hideOptions();
          $("#searchform-search").focus();
        }
      });

      // TODO: This is too specific - uses the id of the search button and
      // queryfield,
      // or the id of the query textfield on shift+tab. Need to find
      // something to blur.
      // maybe use arrow keys to navigate and close on tab or selection
      $("#searchform-search, #q").on("focus.richdrop", function(event) {
        hideOptions();
      });

      // Re-position dropdown on window resize
      $(window).resize(function(event) {
        if ($richdd.hasClass("active")) {
          setPosition();
        }
      });

      // Show/Hide functions
      function showOptions() {
        setPosition();
        $richdd.slideDown({
          duration: 100,
          start: function() {
            $(this).css({
              display: "flex"
            });
          }
        });
        $richdd.addClass("active");
        $containerDiv.addClass("active");
      }

      function hideOptions() {
        $richdd.slideUp(100);
        $richdd.removeClass("active");
        $containerDiv.removeClass("active");
      }

      function setPosition() {
        $richdd.outerWidth($richcont.outerWidth());
        $richdd.css(
          "top",
          $containerDiv.position().top + $containerDiv.outerHeight() + "px"
        );
      }
    });
  };
})(jQuery);
