module ItemSummary.ViewItem where 

import Prelude hiding (div)

import AjaxRequests (ErrorResponse, getJson)
import Control.Monad.Trans.Class (lift)
import Data.Either (either)
import Data.Maybe (Maybe(..), maybe)
import Debug.Trace (spy)
import Dispatcher (affAction)
import Dispatcher.React (getProps, modifyState, renderer)
import Effect.Aff (runAff_)
import Effect.Class (liftEffect)
import Effect.Uncurried (EffectFn1, runEffectFn1)
import ItemSummary (AttachmentNode(..), ItemSummary, ItemSummarySection(..), decodeItemSummary)
import LegacyContent (updateStylesheets)
import MaterialUI.DialogTitle (disableTypography)
import MaterialUI.List (disablePadding, list)
import MaterialUI.ListItem (disableGutters, listItem)
import MaterialUI.ListItemIcon (listItemIcon, listItemIcon_)
import MaterialUI.ListItemText (listItemText, primary, secondary)
import MaterialUI.Properties (className, classes_, color, mkProp, variant)
import MaterialUI.Properties as MUI
import MaterialUI.Styles (withStyles)
import MaterialUI.TextStyle (body1, subheading, title)
import MaterialUI.Typography (textSecondary, typography)
import React (ReactElement, component, unsafeCreateLeafElement)
import React.DOM (div, div', img, text)
import React.DOM.Props (dangerouslySetInnerHTML, src)
import React.DOM.Props as DP
import Unsafe.Coerce (unsafeCoerce)

data Command = LoadPage

type State = {
  content :: Maybe ItemSummary
}

itemPath :: String -> Int -> String
itemPath uuid version = "api/item/" <> uuid <> "/" <> show version <> "/summary"

viewItem :: {uuid :: String, version :: Int, titleUpdate :: EffectFn1 String Unit, onError :: EffectFn1 ErrorResponse Unit} -> ReactElement
viewItem = unsafeCreateLeafElement $ withStyles styles $ component "SelectionViewItem" $ \this -> do
  let 
    d = eval >>> affAction this
    render {state:s@{content:Just {sections} }, props:{classes}} = div' $ renderSection =<< sections
      where
      sectionTitle t = typography [variant title] [text t]
      renderSection = case _ of 
        BasicDetails {description} -> maybe [] (\desc -> [
          sectionTitle "Description",
          typography [variant subheading] [text desc] ]) description
        DisplayNodes dn -> 
          let node {value:t,title} = listItem [disableGutters true] [ 
                  listItemText [primary title, secondary t] 
                ]
          in [
            sectionTitle dn.sectionTitle, 
            list [] $ node <$> dn.meta
          ]
        HtmlSummarySection {html} -> [
          div [ dangerouslySetInnerHTML {__html:html}] []
        ]
        CommentsSummarySection {sectionTitle:st} -> [
          sectionTitle st,
          div' [ text "TODO" ]
        ]
        Attachments att -> 
          let 
          attachView (Attachment {title,thumbnailHref,details} _) = let 
            detailEntry meta = listItem [classes_ {default: classes.attachmentMeta}] [ 
              typography [MUI.component "span", className classes.metaTitle] [text meta.title],
              typography [MUI.component "span", color textSecondary, 
                mkProp "dangerouslySetInnerHTML" $ unsafeCoerce {__html:meta.value} :: String] []
            ]
            extraDeets = list [disablePadding true] $ detailEntry <$> details 
            in listItem [disableGutters true] [ 
              listItemIcon_ [img [src thumbnailHref]],
              listItemText [disableTypography true, 
                primary $ typography [variant body1] [text title], 
                secondary extraDeets 
              ]
            ] 
          in pure $ div [DP.className classes.section] [
            sectionTitle att.sectionTitle, 
            list [] $ attachView <$> att.attachments
          ]
    render _ = div' []
 
    eval = case _ of 
      LoadPage -> do 
        {uuid,version,onError,titleUpdate} <- getProps
        resp <- lift $ getJson decodeItemSummary (itemPath uuid version)
        let updateContent is@{title} = do 
                liftEffect $ runEffectFn1 titleUpdate title
                modifyState _ {content = Just is}
        either (liftEffect <<< runEffectFn1 onError) (updateContent) resp

  pure { render: renderer render this, 
    state: {content:Nothing } :: State,
    componentDidMount: d LoadPage, 
    componentWillUnmount: runAff_ (const $ pure unit) $ updateStylesheets true []
  }
  where
  styles t = {
    section: {
      marginTop: t.spacing.unit * 4
    },
    attachmentMeta: {
      padding: 0
    },
    detailList: {
      -- display: "flex"
    }, 
    metaTitle: {
      paddingRight: t.spacing.unit
    }
  }
