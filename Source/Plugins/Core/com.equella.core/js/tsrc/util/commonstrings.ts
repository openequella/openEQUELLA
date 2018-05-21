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
      delete: "Delete"
    }, 
    users : "Users", 
    groups: "Groups", 
    roles: "Roles"
});
