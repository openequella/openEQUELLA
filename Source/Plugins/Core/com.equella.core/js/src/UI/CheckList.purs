module OEQ.UI.CheckList where 

import Prelude

import Data.TSCompat (Any)
import Data.TSCompat.Class (asTS)
import Dispatcher.React (propsRenderer)
import MaterialUI.List (list)
import MaterialUI.ListItem (listItem)
import MaterialUI.Styles (withStyles)
import React (ReactElement, component, unsafeCreateLeafElement)



checkList :: {entries :: Array {checkbox:: {classes::Any} -> ReactElement, text:: {className::String} -> ReactElement}} -> ReactElement
checkList = unsafeCreateLeafElement $ withStyles styles $ component "CheckList" $ \this -> do 
  let render {entries,classes} = list {disablePadding: true} $ entry <$> entries  
        where 
        entry e = 
          listItem {classes: {default:classes.reallyDense}, disableGutters: true} [
              e.checkbox {classes: asTS {root:classes.smallerCheckbox}},
              e.text {className: classes.listText}
          ]
  pure {render: propsRenderer render this}
  where 
  styles theme = {
    reallyDense: {
      padding: 0
    },
    smallerCheckbox: {
      padding: 0
    },
    listText: {
      display: "flex",
      justifyContent: "space-between"
    }
  }

