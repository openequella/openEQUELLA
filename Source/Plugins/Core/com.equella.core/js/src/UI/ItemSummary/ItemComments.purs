module OEQ.UI.ItemSummary.ItemComments where 

import Prelude hiding (div)

import Control.Monad.Except (ExceptT(..), runExceptT, throwError)
import Control.Monad.Trans.Class (lift)
import Control.MonadZero (guard)
import Data.Argonaut (Json, decodeJson, jsonEmptyObject, (:=), (~>))
import Data.Argonaut.Encode ((~>?))
import Data.Array (catMaybes, findMap)
import Data.Array as Array
import Data.Int (fromString)
import Data.Maybe (Maybe(..), fromMaybe)
import Data.Traversable (traverse)
import Data.Tuple (Tuple(..))
import Dispatcher (affAction)
import Dispatcher.React (getProps, getState, modifyState, renderer)
import Effect.Uncurried (EffectFn1, mkEffectFn1)
import Global.Unsafe (unsafeEncodeURIComponent)
import MaterialUI.Styles (withStyles)
import Network.HTTP.Affjax as Ajax
import OEQ.API.Item (itemApiPath, uuidHeader)
import OEQ.API.Requests (errorOr, getJson, postJsonExpect)
import OEQ.Data.Error (ErrorResponse, mkUniqueError)
import OEQ.Data.Item (ItemComment, decodeComment)
import OEQ.UI.Common (checkChange, textChange, valueChange)
import OEQ.UI.Common as Utils
import OEQ.Utils.QueryString (queryString)
import React (ReactElement, component, unsafeCreateLeafElement)
import React.DOM (div, div', em', text)
import React.DOM.Props as RP
import ReactMUI.Button (button)
import ReactMUI.Enums (primary, raised)
import ReactMUI.FormControl (formControl)
import ReactMUI.FormControlLabel (formControlLabel)
import ReactMUI.FormGroup (formGroup_)
import ReactMUI.Icon (icon_)
import ReactMUI.IconButton (iconButton)
import ReactMUI.InputLabel (inputLabel_)
import ReactMUI.List (list)
import ReactMUI.ListItem (listItem)
import ReactMUI.ListItemSecondaryAction (listItemSecondaryAction_)
import ReactMUI.ListItemText (listItemText)
import ReactMUI.MenuItem (menuItem)
import ReactMUI.Switch (switch)
import ReactMUI.TextField (textField)

data Command = CommentText String 
  | AnonymousFlag Boolean 
  | SetRating String
  | MakeComment 
  | DeleteComment String 
  | LoadComments

type ItemCommentProps = {
  uuid :: String, 
  version :: Int, 
  onError :: EffectFn1 ErrorResponse Unit, 
  canAdd :: Boolean, 
  canDelete :: Boolean,
  anonymousOnly :: Boolean,
  allowAnonymous :: Boolean
}

type State = {
  commentText :: String, 
  anonymous :: Boolean,
  rating :: Maybe Int,
  comments :: Array ItemComment
}

encodeComment :: {comment::String, anonymous::Boolean, rating::Maybe Int} -> Json
encodeComment {comment, anonymous, rating} = 
  "comment" := comment ~> 
  "anonymous" := anonymous ~> 
  ((:=) "rating" <$> rating) ~>? 
  jsonEmptyObject

itemComments :: ItemCommentProps -> ReactElement
itemComments = unsafeCreateLeafElement $ withStyles styles $ component "ItemComments" $ \this -> do
  let 
    d = eval >>> affAction this

    renderComment canDelete c = listItem {disableGutters: true} $ catMaybes [
            -- listItemIcon_ [ userIcon ],
            Just $ listItemText {primary: c.comment, secondary: c.date <> " - " <> show c.rating } [], 
            guard canDelete $> listItemSecondaryAction_ [ 
                                  iconButton {onClick: d $ DeleteComment c.uuid}
                                  [ icon_ [ text "delete" ] ] 
                                ]
          ]

    ratingItem v = let sv = show v in menuItem {value: show v } [ text $ show v ]

    render {props:{classes,canAdd,canDelete,anonymousOnly,allowAnonymous}, state:{rating,commentText,comments,anonymous}} = let 
      renderAdd = [
        textField {
          onChange: mkEffectFn1 $ valueChange (d <<< CommentText),
          value: commentText, 
          label: "Enter comment", 
          placeholder: "Enter comment", 
          rowsMax: 3, 
          multiline: true, 
          fullWidth: true} [], 
        div [RP.className classes.commentButtons] $ catMaybes [ 
          Just $ textField {
                  className: classes.ratingField,
                  label: "Rating", select:true, 
                  value: fromMaybe "" (show <$> rating),
                  onChange: mkEffectFn1 $ Utils.valueChange (d <<< SetRating) } $ 
                    [ menuItem {value: ""} [ em' [ text "No rating" ] ] ] 
                      <> (ratingItem <$> Array.range 1 5),
          (guard $ not anonymousOnly && allowAnonymous) $> formGroup_ [
              formControlLabel {
                label: "Anonymous",
                control: switch { checked: anonymous, onChange: checkChange $ d <<< AnonymousFlag} []
              } []
            ],
          Just $ button {onClick: d MakeComment, variant: raised, color: primary} [text "Comment"]
        ]
      ]
     in div' $ guard canAdd *> renderAdd <> 
      [ list { disablePadding: true } $ renderComment canDelete <$> comments ]
    
    eval = case _ of 
      SetRating r -> 
        modifyState _ {rating = fromString r}
      LoadComments -> do 
        {uuid,version,onError} <- getProps
        (lift $ getJson (itemApiPath uuid version <> "/comment") (decodeJson >=> traverse decodeComment)) >>= 
          errorOr (\c -> modifyState _ {comments = c})
      CommentText t -> modifyState _ {commentText = t}
      AnonymousFlag b -> modifyState _ {anonymous = b}
      MakeComment -> do 
        {commentText,comments,anonymous:anon,rating} <- getState
        {uuid,version,anonymousOnly,allowAnonymous} <- getProps
        res <- lift $ runExceptT $ do
          let anonymous = anonymousOnly || (allowAnonymous && anon)
              commentJson = encodeComment {comment:commentText, anonymous,rating}
          {headers} <- ExceptT $ postJsonExpect 201 (itemApiPath uuid version <> "/comment")  commentJson
          case findMap uuidHeader headers of 
              Nothing -> throwError $ mkUniqueError 500 "No UUID header for comment" Nothing
              Just commentUuid -> do 
                  ExceptT $ getJson (itemApiPath uuid version <> "/comment/" <> unsafeEncodeURIComponent commentUuid) (decodeComment)
        errorOr (\c -> modifyState \s -> s {commentText = "", comments = Array.cons c s.comments}) res
        
      DeleteComment commentUuid -> do 
        {uuid,version} <- getProps
        void $ lift $ Ajax.delete_ (itemApiPath uuid version <> "/comment?" <> queryString [Tuple "commentuuid" commentUuid])
        modifyState \s -> s {comments = Array.filter (_.uuid >>> notEq commentUuid) s.comments}
  pure {
    render:renderer render this, 
    state: { commentText:"", comments:[], anonymous:false, rating: Nothing} :: State, 
    componentDidMount: d LoadComments
  }
  where
  styles t = {
    commentButtons: {
      marginTop: t.spacing.unit,
      display: "flex", 
      justifyContent: "flex-end"
    }, 
    ratingField: {
      width: "5em", 
      marginRight: t.spacing.unit
    }
  }