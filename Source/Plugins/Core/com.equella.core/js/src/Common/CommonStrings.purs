module Common.CommonStrings where 

import EQUELLA.Environment (prepLangStrings)

commonRawStrings = {prefix:"common", 
  strings:{
    action: {
      save: "Save",
      cancel: "Cancel",
      undo: "Undo",
      add: "Add", 
      ok: "OK",
      discard: "Discard"
    }, 
    users : "Users", 
    groups: "Groups", 
    roles: "Roles"
  }
}

commonString = prepLangStrings commonRawStrings
commonAction = commonString.action