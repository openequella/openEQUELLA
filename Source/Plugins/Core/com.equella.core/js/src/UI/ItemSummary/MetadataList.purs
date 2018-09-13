module OEQ.UI.ItemSummary.MetadataList where 


import Prelude

import MaterialUI.ListItem (listItem)
import MaterialUI.Properties (className, classes_, color, mkProp)
import MaterialUI.Properties as MUI
import MaterialUI.Styles (withStyles)
import MaterialUI.Typography (textSecondary, typography)
import React (ReactElement, statelessComponent, unsafeCreateLeafElement)
import React.DOM (text)
import Unsafe.Coerce (unsafeCoerce)

data MetaValue = HTML String | React ReactElement | Text String
type MetaEntry = {title::String, value :: MetaValue}

metaEntry :: MetaEntry -> ReactElement
metaEntry = unsafeCreateLeafElement $ withStyles styles $ statelessComponent render
 where 
 valueProps = [MUI.component "span", color textSecondary]
 render {title,value,classes} = listItem [classes_ {default: classes.attachmentMeta}] [ 
    typography [MUI.component "span", className classes.metaTitle] [text title],
    case value of 
      HTML h -> typography (valueProps <> [mkProp "dangerouslySetInnerHTML" $ unsafeCoerce {__html:h} :: String]) []
      Text t -> typography valueProps [ text t ]
      React r -> typography valueProps [ r ]
 ] 
 styles t = {
    attachmentMeta: {
      padding: 0
    }, 
    metaTitle: {
      paddingRight: t.spacing.unit
    }
 }
