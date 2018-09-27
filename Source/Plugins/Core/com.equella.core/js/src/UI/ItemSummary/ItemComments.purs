module OEQ.UI.ItemSummary.ItemComments where 

import Prelude hiding (div)

import Common.CommonStrings (commonAction)
import Control.Monad.Except (ExceptT(..), runExceptT, throwError)
import Control.Monad.Trans.Class (lift)
import Control.MonadZero (guard)
import Data.Argonaut (Json, decodeJson, jsonEmptyObject, (:=), (~>))
import Data.Argonaut.Encode ((~>?))
import Data.Array (catMaybes, find, findMap)
import Data.Array as Array
import Data.Int (fromString)
import Data.Maybe (Maybe(..))
import Data.Newtype (unwrap)
import Data.Traversable (traverse)
import Data.Tuple (Tuple(..))
import Dispatcher (affAction)
import Dispatcher.React (getProps, getState, modifyState, renderer)
import Effect.Uncurried (EffectFn1)
import ExtUI.TimeAgo (timeAgo)
import Global.Unsafe (unsafeEncodeURIComponent)
import MaterialUI.Button (button)
import MaterialUI.Checkbox (checkbox)
import MaterialUI.Collapse (collapse)
import MaterialUI.Enums (primary, raised)
import MaterialUI.FormControlLabel (formControlLabel)
import MaterialUI.FormGroup (formGroup_)
import MaterialUI.Icon (icon_)
import MaterialUI.IconButton (iconButton)
import MaterialUI.List (list)
import MaterialUI.ListItem (listItem)
import MaterialUI.ListItemSecondaryAction (listItemSecondaryAction_)
import MaterialUI.ListItemText (listItemText')
import MaterialUI.Styles (withStyles)
import MaterialUI.TextField (textField'')
import Network.HTTP.Affjax as Ajax
import OEQ.API.Item (itemApiPath, uuidHeader)
import OEQ.API.Requests (errorOr, getJson, postJsonExpect)
import OEQ.API.User (lookupUsers)
import OEQ.Data.Error (ErrorResponse, mkUniqueError)
import OEQ.Data.Item (ItemComment, decodeComment)
import OEQ.Data.User (UserDetails(..), UserDetailsR, UserGroupRoles(..), userIds)
import OEQ.Environment (prepLangStrings)
import OEQ.UI.Common (checkChange, valueChange)
import OEQ.Utils.QueryString (queryString)
import React (ReactElement, component, unsafeCreateLeafElement)
import React.DOM (div, div', text)
import React.DOM.Dynamic (span')
import React.DOM.Props as RP

data Command = CommentText String 
  | AnonymousFlag Boolean 
  | SetRating String
  | MakeComment 
  | DeleteComment String 
  | LoadComments
  | ShowControls Boolean

type ItemCommentProps = {
  uuid :: String, 
  version :: Int, 
  onError :: EffectFn1 ErrorResponse Unit, 
  canAdd :: Boolean, 
  canDelete :: Boolean,
  anonymousOnly :: Boolean,
  allowAnonymous :: Boolean
}

type CommentWithUser = {userText :: Maybe UserDetailsR, comment::ItemComment}

type State = {
  commentText :: String, 
  anonymous :: Boolean,
  rating :: Maybe Int,
  comments :: Array CommentWithUser, 
  showControls :: Boolean
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
    string = prepLangStrings coreStrings

    renderComment canDelete {userText, comment:c} = let 
         textForUser = case userText, c.anonymous of 
           Just {username}, _ -> Just username
           _, true -> Just $ string.anonymous
           _, _ -> Nothing
      in listItem {disableGutters: true} $ catMaybes [
        -- listItemIcon_ [ userIcon ],
        Just $ listItemText' {
          primary: c.comment, 
          secondary: span' $ catMaybes [ 
            Just $ timeAgo {datetime:c.date}, 
            map (\t -> text $ " - " <> t) textForUser
          ]
        }, 
        guard canDelete $> listItemSecondaryAction_ [ 
                              iconButton {onClick: d $ DeleteComment c.uuid}
                              [ icon_ [ text "delete" ] ] 
                            ]
      ]

    -- ratingItem v = let sv = show v in menuItem {value: show v } [ text $ show v ]
          -- textField {
          --         className: classes.ratingField,
          --         label: "Rating", select:true, 
          --         value: fromMaybe "" (show <$> rating),
          --         onChange: mkEffectFn1 $ Utils.valueChange (d <<< SetRating) } $ 
          --           [ menuItem {value: ""} [ em' [ text "No rating" ] ] ] 
          --             <> (ratingItem <$> Array.range 1 5),

    render {props:{classes,canAdd,canDelete,anonymousOnly,allowAnonymous}, state:s@{rating,commentText,comments,anonymous}} = let 
      renderControls = catMaybes [ 
      ]
      renderAdd = [
        textField'' {
          onChange: valueChange (d <<< CommentText),
          onFocus: d $ ShowControls true,
          value: commentText,
          label: string.entermsg, 
          placeholder: string.entermsg,  
          rowsMax: 3, 
          multiline: true, 
          fullWidth: true} [], 
        collapse {"in":s.showControls} $ pure $ div [RP.className classes.commentControls] $ catMaybes [ 
            (guard $ not anonymousOnly && allowAnonymous) $> formGroup_ [
                  formControlLabel {
                    label: string.anonymous,
                    style: { alignSelf: "flex-end" },
                    control: checkbox { checked: anonymous, onChange: checkChange $ d <<< AnonymousFlag} []
                  } []
            ],
            Just $ div [ RP.className classes.commentButtons] [ 
              button {onClick: d $ ShowControls false, color: primary } [ text commonAction.cancel],
              button {onClick: d MakeComment, variant: raised, color: primary} [text string.commentmsg]
            ]
        ]
     ]
     in div' $ guard canAdd *> renderAdd <> 
      [ list { disablePadding: true } $ renderComment canDelete <$> comments ]
    
    fillUser :: Array UserDetails -> CommentWithUser -> CommentWithUser
    fillUser ud c = case c.comment.user of 
      Just userId | Just (UserDetails deets) <- find (unwrap >>> _.id >>> eq userId) ud -> 
        c {userText = Just deets}
      _ -> c

    eval = case _ of 
      ShowControls b -> modifyState _ {showControls=b, commentText=""}
      SetRating r -> 
        modifyState _ {rating = fromString r}
      LoadComments -> do 
        {uuid,version,onError} <- getProps
        (lift $ getJson (itemApiPath uuid version <> "/comment") (decodeJson >=> traverse decodeComment)) >>= 
          errorOr (\c -> do 
            let users = catMaybes $ _.user <$> c
            modifyState _ {comments = {comment: _, userText: Nothing} <$> c}
            UserGroupRoles ugr <- lift $ lookupUsers $ userIds users
            modifyState \s -> s {comments = map (fillUser ugr.users) s.comments}
            pure unit
          )
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
        errorOr (\c -> do 
          UserGroupRoles ugr <- lift $ lookupUsers $ userIds $ Array.fromFoldable c.user
          let newComment = fillUser ugr.users {comment:c, userText:Nothing}
          modifyState \s -> s {commentText = "", showControls=false, comments = Array.cons newComment s.comments}) res
        
      DeleteComment commentUuid -> do 
        {uuid,version} <- getProps
        void $ lift $ Ajax.delete_ (itemApiPath uuid version <> "/comment?" <> queryString [Tuple "commentuuid" commentUuid])
        modifyState \s -> s {comments = Array.filter (_.comment.uuid >>> notEq commentUuid) s.comments}
  pure {
    render:renderer render this, 
    state: { commentText:"", comments:[], anonymous:false, rating: Nothing, showControls:false} :: State, 
    componentDidMount: d LoadComments
  }
  where
  styles t = {
    commentButtons: {
      marginTop: t.spacing.unit,
      display: "flex", 
      justifyContent: "flex-end",
      flexGrow: 1
    },
    commentControls: {
      display: "flex"
    },
    ratingField: {
      width: "5em", 
      marginRight: t.spacing.unit
    }
  }

coreStrings = {
  prefix: "com.equella.core.comments", 
  strings: {
    anonymous: "Anonymous", 
    commentmsg: "Comment",
    entermsg: "Enter a comment"
  }
}