// This JavaScript file is dedicated to the 'Select Session' page in the new UI.
// It triggers the `clearSelectionForSingleSelectionMode` event first and
// then utilizes the `history.back` callback to navigate back to the 'Search' page
// while preserving all parameters.
function continueSelection() {
  const form = document.getElementById("eqpageForm");

  if(typeof EQ !== "undefined") {
    EQ.postAjax(form, '.clearSelectionForSingleSelectionMode', [], () => history.back());
  }
}
