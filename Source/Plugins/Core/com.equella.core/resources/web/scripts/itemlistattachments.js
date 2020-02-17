var ItemListAttachments = {
  toggle: function($toggler, $attachments, updateFunc, uuid, version) {
    const opened = $attachments.hasClass("opened");
    if (opened) {
      $attachments.removeClass("opened");
      updateFunc(uuid, version, false);
    } else {
      $attachments.addClass("opened");
      updateFunc(uuid, version, true);
    }
    $attachments.attr("aria-hidden", opened);
  },

  endToggle: function() {
    $(document).trigger("equella_showattachments");
  }
};
