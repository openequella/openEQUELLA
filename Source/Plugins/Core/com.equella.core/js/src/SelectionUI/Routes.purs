module OEQ.SelectionUI.Routes where 

import Prelude

import Control.Alt ((<|>))
import Data.Either (either)
import Data.Maybe (Maybe(..))
import Data.String (Pattern(..), drop, indexOf, take)
import Effect (Effect)
import Effect.Uncurried (EffectFn1, mkEffectFn1)
import Foreign.Object as Object
import OEQ.Data.LegacyContent (LegacyURI(..), legacyRoute, legacyURIToString)
import OEQ.MainUI.Routes (pushRoute')
import OEQ.UI.Common (ClickableHref)
import React.SyntheticEvent (SyntheticEvent_, preventDefault, stopPropagation)
import Routing (match)
import Routing.Match (Match, end, int, lit, str)

data SessionParams = Session String (Maybe String)
data SelectionRoute = Route SessionParams SelectionPage
data SelectionPage = Search | ViewItem String Int | LegacySelectionPage LegacyURI

paramsFromString :: String -> SessionParams
paramsFromString s = case indexOf (Pattern ":") s of 
  Just i -> Session (take i s) $ Just (drop (i+1) s)
  Nothing -> Session s Nothing

paramsToString :: SessionParams -> String 
paramsToString (Session s intid) = case intid of 
  Just i -> s <> ":" <> i 
  _ -> s

selectionRouteMatch :: Match SelectionRoute
selectionRouteMatch = Route <$> (lit "selection" *> (paramsFromString <$> str)) <*> selectionPageMatch

selectionPageMatch :: Match SelectionPage
selectionPageMatch = 
  Search <$ (lit "search") <|>
  ViewItem <$> (lit "items" *> str) <*> int <|> 
  LegacySelectionPage (LegacyURI "access/contribute.do" Object.empty) <$ end <|>
  LegacySelectionPage <$> legacyRoute

matchSelection :: String -> Maybe SelectionRoute 
matchSelection = match selectionRouteMatch >>> (either (const Nothing) Just)

selectionClicker :: SelectionRoute -> ClickableHref
selectionClicker r = 
    let href = selectionURI r
        onClick :: forall e. EffectFn1 (SyntheticEvent_ e) Unit
        onClick = mkEffectFn1 $ \e -> do 
          preventDefault e 
          stopPropagation e
          pushSelectionRoute r
    in { href, onClick }


selectionURI :: SelectionRoute -> String 
selectionURI (Route sess r) = "selection/" <> paramsToString sess <> "/" <> case r of 
  Search -> "search"
  ViewItem uuid ver -> "items/" <> uuid <> "/" <> show ver
  LegacySelectionPage leg -> legacyURIToString leg

pushSelectionRoute :: SelectionRoute -> Effect Unit
pushSelectionRoute = pushRoute' selectionURI