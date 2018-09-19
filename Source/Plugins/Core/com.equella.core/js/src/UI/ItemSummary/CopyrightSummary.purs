module OEQ.UI.ItemSummary.CopyrightSummary where 

import Prelude hiding (div)

import Control.Monad.Trans.Class (lift)
import Data.Array (catMaybes)
import Data.Lens (_Just, set)
import Data.Lens.Record (prop)
import Data.Maybe (Maybe(..), fromMaybe, isJust)
import Data.Symbol (SProxy(..))
import Dispatcher (affAction)
import Dispatcher.React (getState, modifyState, renderer)
import Effect (Effect)
import Effect.Class (liftEffect)
import Effect.Class.Console (log)
import Effect.Uncurried (EffectFn1, mkEffectFn1)
import Foreign.Object (empty)
import MaterialUI.Styles (withStyles)
import OEQ.API.Requests (errorOr, postJson, postJsonExpect)
import OEQ.Data.Activation (encodeActivateRequset)
import OEQ.Data.Error (ErrorResponse)
import OEQ.Data.Item (CopyrightSummary, HoldingSummary(..), ItemRef(..), CopyrightAttachment)
import OEQ.UI.Activation (ActivationData, activationParams)
import OEQ.UI.ItemSummary.MetadataList (MetaValue(..), metaEntry)
import React (ReactElement, component, unsafeCreateLeafElement)
import React.DOM (div', div, text)
import React.DOM.Dynamic (a, h1, h1')
import React.DOM.Props (href)
import React.DOM.Props as RP
import ReactMUI.Button (button)
import ReactMUI.Dialog (dialog)
import ReactMUI.DialogActions (dialogActions, dialogActions_)
import ReactMUI.DialogContent (dialogContent)
import ReactMUI.DialogTitle (dialogTitle, dialogTitle_)
import ReactMUI.Enums (primary)
import ReactMUI.List (list, list_)
import ReactMUI.ListItem (listItem, listItem_)
import ReactMUI.ListItemSecondaryAction (listItemSecondaryAction, listItemSecondaryAction_)
import ReactMUI.ListItemText (listItemText)

type CopyrightProps = {
  onError :: EffectFn1 ErrorResponse Unit,
  copyright :: CopyrightSummary
}

data Command = Activate CopyrightAttachment | ChangeData ActivationData | CancelDialog | FinishActivate

type State = {
  currentActivation :: Maybe {open::Boolean, attachment::CopyrightAttachment, activation :: ActivationData }
}

copyrightSummary :: CopyrightProps -> ReactElement
copyrightSummary = unsafeCreateLeafElement $ withStyles styles $ component "CopyrightSummary" $ \this -> do
  let
    _currentActivation = prop (SProxy :: SProxy "currentActivation")
    _activation = prop (SProxy :: SProxy "activation")
    _open = prop (SProxy :: SProxy "open")
    d = eval >>> affAction this
    render {props: {classes, onError, copyright: {holding}}, state:{currentActivation:ca}} = let 
      attachmentLink {title,href:h} = a [href $ fromMaybe "" h] [ text title ]
      bookSection {pageCount,attachment} = listItem_ [
        list_ $ map metaEntry [
          {title: "Pages:", value: Text $ show pageCount},
          {title: "Resource:", value: case attachment.href of 
            Nothing -> Text attachment.title
            Just h -> React $ a [href h] [ text attachment.title ]
          }
        ],
        listItemSecondaryAction_ [
          button {color: primary,  onClick: d $ Activate attachment} [ text "Activate" ]
        ]
        -- createActivation {onError, item: attachment.item, attachmentUuid: attachment.uuid}
      ]
      bookChapter {title, sections} = [ listItem {} [listItemText { primary: title} [] ] ] <> (bookSection <$> sections)
      renderHolding (BookSummary {totalPages,chapters}) = list {} $ bookChapter =<< chapters
      renderHolding (JournalSummary {}) = div' [ 

      ]
      in div' $ catMaybes [
          Just $ renderHolding holding,
          ca <#> (\a ->
            dialog {
              open: a.open, 
              onClose: d CancelDialog 
             } [
              dialogTitle_ [ text "Activate" ],
              dialogContent {className: classes.activateDialog} [
                activationParams {"data": a.activation, errors:empty, onChange: d <<< ChangeData, onError}
              ], 
              dialogActions_ [
                button {color: primary, onClick: d CancelDialog } [text "Cancel"],
                button {color: primary, onClick: d FinishActivate } [text "Activate"]
              ]
            ])
      ] 
    eval = case _ of 
      Activate a -> modifyState _ {currentActivation = Just {attachment:a, open:true,
        activation:{startDate:Nothing, course:Nothing, endDate:Nothing, citation:Nothing}}}
      ChangeData ad -> modifyState $ set (_currentActivation <<< _Just <<< _activation) ad
      CancelDialog -> modifyState $ set (_currentActivation <<< _Just <<< _open) false
      FinishActivate -> do 
        eval CancelDialog
        {currentActivation} <- getState
        case currentActivation of 
          Just { attachment:{item, uuid:attachmentUuid}, 
            activation:{startDate: Just from, 
                      endDate: Just until, 
                      citation, 
                      course: Just {uuid:courseUuid} 
                     } 
          } -> do
            errorOr (\_ -> pure unit) <=< lift $ postJsonExpect 201 "api/activation" $ 
              encodeActivateRequset {"type": "cal", 
                  item, attachmentUuid, 
                  from, 
                  until, 
                  courseUuid, 
                  citation
                }
          _ -> pure unit

  pure {render: renderer render this, state:{currentActivation:Nothing}::State}
  where 
  styles theme = {
    activateDialog: {
      height: "30em",
      width: "30em"
    }
  }