/**
* @author Remy Sharp
* @url http://remysharp.com/2007/01/25/jquery-tutorial-text-box-hints/
*/

(function ($) {

$.fn.hint = function (blurClass) {
    if (!blurClass) blurClass = 'blur';

    return this.each(function () {
        // get jQuery instance of 'this'
        var $$ = $(this); 

        // get it once since it won't change
        var title = $$.attr('title'); 

        // only apply logic if the element has the attribute
        if (title) { 

            // Note this is a one liner

            // on blur, set value to title attr if text is blank
            $$.blur(function () {
                if ($$.val() == '') {
                    $$.val(title).addClass(blurClass);
                }
            })

            // on focus, set value to blank if current value matches title attr
            .focus(function () {
                if ($$.val() == title && $$.hasClass(blurClass)) {
                    $$.val('').removeClass(blurClass);
                }
            })

            // clear the pre-defined text when form is submitted
            .parents('form:first').submit(function () {
                if ($$.val() == title && $$.hasClass(blurClass)) {
                    $$.val('').removeClass(blurClass);
                }
            }).end()

            // now change all inputs to title
            .blur();

            // counteracts the effect of Firefox's autocomplete stripping the blur effect
            if ($.browser.mozilla && !$$.attr('autocomplete')) {
                setTimeout(function () {
                    if ($$.val() == title) $$.val('');
                    $$.blur();
                }, 10);
            }
        }
    });
};

})(jQuery);