module ItemSummary.ViewItem where 

import Prelude hiding (div)

import AjaxRequests (ErrorResponse)
import Control.Monad.Trans.Class (lift)
import Control.MonadZero (guard)
import Data.Array (catMaybes, concat)
import Data.Array as Array
import Data.Either (either)
import Data.Maybe (Maybe(..), fromMaybe, maybe)
import Dispatcher (affAction)
import Dispatcher.React (getProps, modifyState, renderer)
import Effect (Effect)
import Effect.Class (liftEffect)
import Effect.Uncurried (EffectFn1, runEffectFn1)
import ItemSummary (AttachmentNode(..), ItemSummary, ItemSummarySection(..))
import ItemSummary.Api (loadItemSummary)
import ItemSummary.CopyrightSummary (copyrightSummary)
import ItemSummary.ItemComments (itemComments)
import ItemSummary.MetadataList (metaEntry)
import ItemSummary.MetadataList as ME
import LegacyPage (Command(..))
import MaterialUI.Button (button)
import MaterialUI.Color as Color
import MaterialUI.DialogTitle (disableTypography)
import MaterialUI.List (disablePadding, list)
import MaterialUI.ListItem (disableGutters, listItem)
import MaterialUI.ListItemIcon (listItemIcon_)
import MaterialUI.ListItemSecondaryAction (listItemSecondaryAction_)
import MaterialUI.ListItemText (listItemText, primary, secondary)
import MaterialUI.Properties (className, color, onClick, variant)
import MaterialUI.Styles (withStyles)
import MaterialUI.TextStyle (body1, display1, subheading, title)
import MaterialUI.Typography (typography)
import React (ReactElement, component, statelessComponent, unsafeCreateLeafElement)
import React.DOM (div, div', img, text)
import React.DOM.Props (dangerouslySetInnerHTML, src)
import React.DOM.Props as DP
import Search.ItemResult (ItemSelection, SelectionType(..))

type ViewItemP = (
  uuid :: String, 
  version :: Int,
  onError :: EffectFn1 ErrorResponse Unit, 
  onSelect :: Maybe (ItemSelection -> Effect Unit)
)

type ViewItemProps = {
  | ViewItemP
}

data Command = LoadItemSummary

type ViewItemSummaryProps = {
  content :: ItemSummary
  | ViewItemP
}

viewItem :: ViewItemProps -> ReactElement
viewItem = unsafeCreateLeafElement $ component "ViewItem" \this -> do 
  let 
    d = eval >>> affAction this
    render {state:{content: Just content}, props:{uuid,version,onError,onSelect}} = 
      viewItemSummary {uuid,version,content, onError, onSelect}
    render _ = div' []
    eval = case _ of 
      LoadItemSummary -> do 
        {uuid,version,onError} <- getProps
        er <- lift $ loadItemSummary uuid version
        either (liftEffect <<< runEffectFn1 onError) (\c -> modifyState _ {content = Just c }) er
  pure {render: renderer render this, state:{content:Nothing}, componentDidMount: d LoadItemSummary}

viewItemSummary :: ViewItemSummaryProps -> ReactElement
viewItemSummary = unsafeCreateLeafElement $ withStyles styles $ statelessComponent render
  where
  render p@{classes, onSelect, content:{title:itemName,copyright,sections}} = 
    div' $ (Array.fromFoldable $ copyrightSummary <<< {copyright: _} <$> copyright)
      <> (renderSection <$> sections)
    where
    titleText t = typography [
      variant title, 
      color Color.secondary, 
      className classes.sectionTitle] [text t]
    renderSection section = div [DP.className classes.section ] $ case section of 
      BasicDetails {description} -> [ typography [variant display1] [ text itemName ] ] <> 
        maybe [] (\desc -> [ titleText "Description", typography [variant subheading] [text desc] ]) description
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
          toMeta {title:t,value} = {title:t, value : ME.HTML value}
          extraDeets = list [disablePadding true] $ toMeta >>> metaEntry <$> details 
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
  styles t = {
    section: {
      marginTop: t.spacing.unit * 2
    },
    detailList: {
      -- display: "flex"
    },
    sectionTitle: {
      marginTop: t.spacing.unit,
      marginBottom: t.spacing.unit
    }
  }
