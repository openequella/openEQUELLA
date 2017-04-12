var JQueryUtilsDebug = false;

/*
 * Started putting things in here as a real JQuery plug-in rather than the
 * JQueryUtils object.
 */

(function($) {

	$.fn.disabled = function(b) {
		if (b == undefined)
			return this.attr('disabled') != undefined;

		this.each(function() {
			var elem = jQuery(this);
			if (b) {
				elem.removeAttr('disabled');
			} else {
				elem.attr('disabled', 'disabled');
			}
		});
		return this;
	};

})(jQuery);

/*
 * Deprecated JQueryUtils object. See above for JQuery plug-in implementations
 */

var JQueryUtils = function() {
	return {
		visible : function(elem, isVis) {
			if (elem) {
				elem.css( {
					display : (isVis ? "block" : "none")
				});
			} else if (JQueryUtilsDebug) {
				alert('JQueryUtils.visible: Elem is null!');
			}
		},

		check : function(elem, isChecked) {
			if (elem) {
				elem.attr("checked", (isChecked ? "checked" : ""));
			} else if (JQueryUtilsDebug) {
				alert('JQueryUtils.check: Elem is null!');
			}
		},

		value : function(elem, val) {
			if (elem) {
				elem.val(val);
			} else if (JQueryUtilsDebug) {
				alert('JQueryUtils.value: Elem is null!');
			}
		},

		onEvent : function(elem, evt, fn, data) {
			if (elem && evt) {
				elem.unbind().bind(evt, data, fn);
			} else if (JQueryUtilsDebug) {
				alert('JQueryUtils.onEvent: Elem or event is null!');
			}
		},

		unbindEvent : function(elem, evt) {
			if (elem && evt) {
				elem.unbind(evt);
			} else if (JQueryUtilsDebug) {
				alert('JQueryUtils.unbindEvent: Elem or event is null!');
			}
		},

		center : function(elem) {
			if (elem) {
				// work out x and y position of centre
		var xc = 0;
		var yc = 0;
		if (window.innerHeight) {
			xc = Math.floor(window.innerWidth / 2);
			yc = Math.floor(window.innerHeight / 2);
		} else if (document.documentElement.clientHeight) {
			xc = Math.floor(document.documentElement.clientWidth / 2);
			yc = Math.floor(document.documentElement.clientHeight / 2);
		}

		elem.css( {
			position : "absolute",
			marginTop : 0,
			marginLeft : 0,
			left : xc - (elem.width() / 2),
			top : yc - (elem.height() / 2)
		});
	}
}
	};
}();