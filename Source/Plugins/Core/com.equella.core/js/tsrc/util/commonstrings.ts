import { prepLangStrings } from "./langstrings";

export const commonString = prepLangStrings("common", {
  action: {
    save: "Save",
    cancel: "Cancel",
    undo: "Undo",
    add: "Add",
    ok: "OK",
    discard: "Discard",
    select: "Select",
    delete: "Delete",
    search: "Search",
    clear: "Clear",
    close: "Close",
    dismiss: "Dismiss",
    browse: "Browse...",
    apply: "Apply",
    resettodefault: "Reset to Default",
    revertchanges: "Revert Changes",
    register: "Register"
  },
  users: "Users",
  groups: "Groups",
  roles: "Roles"
});
