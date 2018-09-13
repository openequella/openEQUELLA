module OEQ.UI.CheckList where 

import Prelude

import Dispatcher.React (propsRenderer)
import MaterialUI.Checkbox (CheckboxProps, checkbox)
import MaterialUI.List (disablePadding, list)
import MaterialUI.ListItem (disableGutters, listItem)
import MaterialUI.ListItemText (ListItemTextProps, listItemText)
import MaterialUI.Properties (IProp, className, classes_)
import MaterialUI.Styles (withStyles)
import React (ReactElement, component, unsafeCreateLeafElement)



checkList :: {entries :: Array {checkProps::Array (IProp CheckboxProps), textProps::Array (IProp ListItemTextProps)}} -> ReactElement
checkList = unsafeCreateLeafElement $ withStyles styles $ component "CheckList" $ \this -> do 
  let render {entries,classes} = list [disablePadding true] $ entry <$> entries  
        where 
        entry {checkProps, textProps} = 
          listItem [classes_ {default:classes.reallyDense}, disableGutters true] [
              checkbox $ [classes_ {root:classes.smallerCheckbox} ] <> checkProps,
              listItemText $ [className classes.listText] <> textProps
          ]
  pure {render: propsRenderer render this}
  where 
  styles theme = {
    reallyDense: {
      padding: 0
    },
    smallerCheckbox: {
      height: "auto",
      width: "auto"
    },
    listText: {
      display: "flex",
      justifyContent: "space-between"
    }
  }

