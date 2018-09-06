module ItemSummary.ViewItem where 

import Prelude hiding (div)

import AjaxRequests (ErrorResponse, getJson)
import Control.Monad.Trans.Class (lift)
import Control.MonadZero (guard)
import Data.Array (catMaybes)
import Data.Either (either)
import Data.Maybe (Maybe(..), maybe)
import Dispatcher (affAction)
import Dispatcher.React (getProps, modifyState, renderer)
import Effect (Effect)
import Effect.Aff (runAff_)
import Effect.Class (liftEffect)
import Effect.Uncurried (EffectFn1, runEffectFn1)
import ItemSummary (AttachmentNode(..), ItemSummary, ItemSummarySection(..), decodeItemSummary)
import ItemSummary.Api (itemApiPath)
import ItemSummary.ItemComments (itemComments)
import LegacyContent (updateStylesheets)
import MaterialUI.Button (button)
import MaterialUI.Color as Color
import MaterialUI.DialogTitle (disableTypography)
import MaterialUI.List (disablePadding, list)
import MaterialUI.ListItem (disableGutters, listItem)
import MaterialUI.ListItemIcon (listItemIcon_)
import MaterialUI.ListItemSecondaryAction (listItemSecondaryAction_)
import MaterialUI.ListItemText (listItemText, primary, secondary)
import MaterialUI.Properties (className, classes_, color, mkProp, onClick, variant)
import MaterialUI.Properties as MUI
import MaterialUI.Styles (withStyles)
import MaterialUI.TextStyle (body1, display1, subheading, title)
import MaterialUI.Typography (textSecondary, typography)
import React (ReactElement, component, unsafeCreateLeafElement)
import React.DOM (div, div', img, text)
import React.DOM.Props (dangerouslySetInnerHTML, src)
import React.DOM.Props as DP
import Search.ItemResult (ItemSelection, SelectionType(..))
import Unsafe.Coerce (unsafeCoerce)

data Command = LoadPage 

type State = {
  content :: Maybe ItemSummary
}

type ViewItemProps = {
  uuid :: String, 
  version :: Int, 
  titleUpdate :: EffectFn1 String Unit, 
  onError :: EffectFn1 ErrorResponse Unit, 
  onSelect :: Maybe (ItemSelection -> Effect Unit)
}

viewItem :: ViewItemProps -> ReactElement
viewItem = unsafeCreateLeafElement $ withStyles styles $ component "SelectionViewItem" $ \this -> do
  let 
    d = eval >>> affAction this
    render {state:s@{content:Just {sections,title:itemName} }, props:p@{classes, onSelect}} = div' $ renderSection <$> sections
      where
      titleText t = typography [
        variant title, 
        color Color.secondary, 
        className classes.sectionTitle] [text t]
      renderSection section = div [DP.className classes.section ] $ case section of 
        BasicDetails {description} -> maybe [] (\desc -> [
          typography [variant display1] [ text itemName ],
          titleText "Description",
          typography [variant subheading] [text desc] ]) description
        DisplayNodes dn -> 
          let node {value:t,title} = listItem [disableGutters true] [ 
                  listItemText [primary title, secondary t] 
                ]
          in [
            titleText dn.sectionTitle, 
            list [disablePadding true] $ node <$> dn.meta
          ]
        HtmlSummarySection hs -> (guard hs.showTitle $> titleText hs.sectionTitle) <> [
          div [ dangerouslySetInnerHTML {__html:hs.html}] []
        ]
        CommentsSummarySection {sectionTitle,canAdd,canDelete,anonymousOnly,allowAnonymous} -> 
          let {uuid,version,onError} = p
          in [
            titleText sectionTitle, 
            itemComments {uuid,version,onError,canAdd,canDelete,anonymousOnly,allowAnonymous}
          ]
        Attachments att -> 
          let 
          attachView (Attachment {title,thumbnailHref,details,uuid} _) = let 
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
              ], 
              listItemSecondaryAction_ $ catMaybes [
                (\os -> button [onClick $ \_ -> os {
                  item:{
                    uuid:p.uuid, 
                    version:p.version, 
                    name: itemName, 
                    description: Nothing
                  }, 
                  selected: AttachmentUUID uuid, 
                  name:title, 
                  description:title, 
                  thumbnail:thumbnailHref
                }] [ text "Select"]) <$> onSelect
              ]
            ] 
          in [
            titleText att.sectionTitle, 
            list [disablePadding true] $ attachView <$> att.attachments
          ]
    render _ = div' []
 
    eval = case _ of 
      LoadPage -> do
        {uuid,version,onError,titleUpdate} <- getProps
        resp <- lift $ getJson (itemApiPath uuid version <> "/summary") decodeItemSummary
        let updateContent is@{title} = do 
                liftEffect $ runEffectFn1 titleUpdate "Summary"
                modifyState _ {content = Just is}
        either (liftEffect <<< runEffectFn1 onError) (updateContent) resp

  pure { render: renderer render this, 
    state: {content:Nothing} :: State,
    componentDidMount: d LoadPage, 
    componentWillUnmount: runAff_ (const $ pure unit) $ updateStylesheets true []
  }
  where
  styles t = {
    section: {
      marginTop: t.spacing.unit * 2
    },
    attachmentMeta: {
      padding: 0
    },
    detailList: {
      -- display: "flex"
    },
    metaTitle: {
      paddingRight: t.spacing.unit
    }, 
    sectionTitle: {
      marginTop: t.spacing.unit,
      marginBottom: t.spacing.unit
    }
  }
