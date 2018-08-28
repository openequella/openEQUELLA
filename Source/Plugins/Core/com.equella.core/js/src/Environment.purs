module EQUELLA.Environment where

import Prelude

import Data.Either (Either)
import Data.Int (floor)
import Data.Lens (view)
import Data.Newtype (unwrap)
import Data.Time.Duration (Minutes(..), fromDuration)
import Effect (Effect)
import Effect.Aff (runAff_)
import Effect.Class.Console (log)
import Effect.Timer (setInterval)
import Network.HTTP.Affjax as Ajax
import Network.HTTP.Affjax.Response as Response
import Text.Parsing.Parser (ParseError, runParser)
import URI.AbsoluteURI (AbsoluteURI, HierPath, Host, Path, Port, Query, UserInfo, _hierPart, _path, parser)
import URI.HostPortPair (HostPortPair)
import URI.HostPortPair as HPP
import URI.Path (print)

foreign import baseUrl :: String

foreign import prepLangStrings :: forall r. {prefix::String, strings :: (Record r)} -> Record r

parseURI :: String -> Either ParseError (AbsoluteURI UserInfo (HostPortPair Host Port) Path HierPath Query)
parseURI = flip runParser $ parser {
  parseUserInfo: pure, 
  parseHosts: HPP.parser pure pure, 
  parsePath: pure, 
  parseHierPath: pure, 
  parseQuery: pure
}

basePath :: Either ParseError String
basePath = print <$> view (_hierPart <<< _path) <$> parseURI baseUrl

startHearbeat :: Effect Unit
startHearbeat = void $ setInterval (floor $ unwrap $ fromDuration $ Minutes 2.0) $ runAff_ (\_ -> pure unit) $ void $ do 
    {response} <- Ajax.get Response.string $ baseUrl <> "api/status/heartbeat"
    if response == "OK" then pure unit else log response