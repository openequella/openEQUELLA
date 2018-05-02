module Common.CommonStrings where 

import Data.Tuple (Tuple(..))
import EQUELLA.Environment (prepLangStrings)

commonRawStrings = Tuple "common" {
  action: {
    save: "Save",
    cancel: "Cancel",
    undo: "Undo",
    add: "Add"
  }, 
  users : "Users", 
  groups: "Groups", 
  roles: "Roles"
}

commonString = prepLangStrings commonRawStrings
commonAction = commonString.action