var select2courseExtension = {};

(function(){
    function formatResult(params) {
        return function(course) {
            if (course.loading) {
                return params.searchingText;
            }

            var markup = '<div class="autocomplete-result">' +
                '<div class="autocomplete-result-title">' + course.code + ' - ' + course.name + '</div>';

            if (course.description) {
                markup += '<div class="autocomplete-result-description">' + course.description + '</div>';
            }
            markup += '</div>';

            return markup;
        };
    }

    function formatSelection(params){
        return function (course) {
            if (course){
                if (course.id === ''){
                    return params.placeholderText;
                }
                else if (course.id) {
                    return course.text;
                }
                return course.code + ' - ' + course.name;
            }
            return '';
        };
    }

    this.processResultsExt = function(res, data){
        for (var i = 0; i < res.results.length; i++){
            var course = res.results[i];
            course.id = course.uuid;
            course.text = course.code + ' - ' + course.name;
        }
        return res;
    }

    this.options = function(params){
        return {
            templateResult: formatResult(params),
            templateSelection: formatSelection(params)
        };
    };
}).apply(select2courseExtension);