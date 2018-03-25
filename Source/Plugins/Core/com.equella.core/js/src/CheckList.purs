module CheckList where

import Prelude

import Dispatcher.React (ReactProps(..), createComponent)
import MaterialUI.Checkbox (CheckboxProps, checkbox)
import MaterialUI.List (disablePadding, list)
import MaterialUI.ListItem (disableGutters, listItem)
import MaterialUI.ListItemText (ListItemTextProps, listItemText)
import MaterialUI.Properties (IProp, className, classes_)
import MaterialUI.Styles (withStyles)

import React (ReactElement, createFactory)



checkList :: {entries :: Array {checkProps::Array (IProp CheckboxProps), textProps::Array (IProp ListItemTextProps)}} -> ReactElement
checkList = createFactory $ withStyles styles $ createComponent {} render unit
  where

  styles theme = {
    reallyDense: {
      padding: 0
    },
    smallerCheckbox: {
      height: theme.spacing.unit * 4
    },
    listText: {
      display: "flex",
      justifyContent: "space-between"
    }
  }

  render _ (ReactProps {entries,classes}) = list [disablePadding true] $ entry <$> entries
    where entry {checkProps, textProps} = listItem [classes_ {default:classes.reallyDense}, disableGutters true] [
        checkbox $ [classes_ {default:classes.smallerCheckbox} ] <> checkProps,
        listItemText $ [className classes.listText] <> textProps
    ]
