var select2courseExtension = {};

(function CourseSelectionList() {
  function displayText(course, defaultText) {
    if (course.id === "") {
      return defaultText;
    }
    if (course.text) {
      return course.text;
    }
    var text = (course.code ? course.code + " - " : "") + course.name;
    return text;
  }

  function htmlEncode(text) {
    return document
      .createElement("a")
      .appendChild(document.createTextNode(text)).parentNode.innerHTML;
  }

  function formatResult(params) {
    return function(course) {
      if (course.loading) {
        return params.searchingText;
      }

      var markup = '<div class="autocomplete-result">';
      markup +=
        '<div class="autocomplete-result-title">' +
        htmlEncode(displayText(course)) +
        "</div>";
      markup += "</div>";
      return markup;
    };
  }

  function formatSelection(params) {
    return function(course) {
      if (course) {
        return displayText(course, params.placeholderText);
      }
      return "";
    };
  }

  this.processParameters = function(queryParams, params, ajaxParams) {
    if (params.showArchived) {
      queryParams.archived = "true";
    }
    return queryParams;
  };

  this.processResultsExt = function(params, res, data) {
    for (var i = 0; i < res.results.length; i++) {
      var course = res.results[i];
      course.id = course.uuid;
      course.text = displayText(course);
    }
    return res;
  };

  this.options = function(params) {
    return {
      templateResult: formatResult(params),
      templateSelection: formatSelection(params)
    };
  };
}.apply(select2courseExtension));
