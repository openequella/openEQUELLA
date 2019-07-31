module OEQ.Environment where

import Prelude

import Data.Either (Either)
import Data.Lens (view)
import Text.Parsing.Parser (ParseError, runParser)
import URI.AbsoluteURI (AbsoluteURI, HierPath, Host, Path, Port, Query, UserInfo, _hierPart, _path, parser)
import URI.HostPortPair (HostPortPair)
import URI.HostPortPair as HPP
import URI.Path (print)

foreign import baseUrl :: String

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
