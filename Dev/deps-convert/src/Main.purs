module Main where

import Prelude
import Control.Monad.Eff (Eff)
import Control.Monad.Eff.Console (CONSOLE, error, log)
import Control.Monad.Eff.Exception (EXCEPTION, throw)
import Data.Argonaut (class DecodeJson, decodeJson, (.?), jsonParser)
import Data.Argonaut.Decode.Combinators ((.??))
import Data.Array (concat, filter, fromFoldable, groupBy, mapMaybe, sortWith)
import Data.Either (Either(..), either)
import Data.Foldable (any, find, maximum, traverse_)
import Data.Function (on)
import Data.Maybe (Maybe(..), fromMaybe)
import Data.NonEmpty (NonEmpty(..))
import Data.StrMap (StrMap, lookup, unions)
import Data.String (Pattern(Pattern), joinWith, split, stripPrefix)
import Data.Traversable (traverse)
import Data.Tuple (Tuple(..), fst, snd)
import Node.Encoding (Encoding(..))
import Node.FS (FS)
import Node.FS.Sync (readTextFile)
import Node.Yargs.Applicative (class Arg, arg, runY, yarg)
import Node.Yargs.Setup (usage)

data Format = SBT | Gradle
instance formatArg :: Arg Format where
  arg n = fromStr <$> arg n
    where
      fromStr "gradle" = Gradle
      fromStr _ = SBT

newtype DepKey = DepKey {groupId :: String, artifactId :: String}

data BaseDep = BaseDep DepKey {
    version :: String
  , classifier :: Maybe String
  , excludes :: Array String
}

derive instance depKeyEq :: Eq DepKey
instance depKeyOrd :: Ord DepKey where
  compare (DepKey d1) (DepKey d2) = compare d1.groupId d2.groupId <> compare d1.artifactId d2.artifactId

instance showKey :: Show DepKey where
  show (DepKey {groupId,artifactId}) = groupId <> ":" <> artifactId

derive instance baseDepEq :: Eq BaseDep

type FullDep = {
    groupId:: String
  , artifactId:: String
  , version :: String
  , classifier :: Maybe String
  , jpfIncludes :: Array String
  , jpfExports :: Array String
  , excludes :: Array String
}

data Error = NoVersion String

instance showError :: Show Error where
  show (NoVersion err) = "No version for '" <> err <> "'"

newtype Dep = Dep FullDep
newtype DepsFile = DepsFile {exclusions::Array String, versions::StrMap String, dependencies::Array Dep}

instance depDecode :: DecodeJson Dep where
  decodeJson j = do
    o <- decodeJson j
    groupId <- o .? "groupId"
    artifactId <- o .? "artifactId"
    version <- o .? "version"
    jpfIncludes <- fromMaybe [] <$> o .?? "jpfIncludes"
    jpfExports <- fromMaybe [] <$> o .?? "jpfExports"
    excludes <- fromMaybe [] <$> o .?? "excludes"
    classifier <- o .?? "classifier"
    pure $ Dep {groupId,artifactId,version,jpfIncludes,jpfExports,excludes,classifier}

instance depsDecode :: DecodeJson DepsFile where
  decodeJson json = do
   o <- decodeJson json
   exclusions <- o .? "exclusions"
   versions <- o .? "versions"
   dependencies <- o .? "dependencies"
   pure $ DepsFile {exclusions,versions,dependencies}

keyOnly :: BaseDep -> DepKey
keyOnly (BaseDep k _) = k

versionOnly :: BaseDep -> String
versionOnly (BaseDep k {version}) = version

-- | Sort the dependencies and make sure we only use the highest version mentioned
-- | and warn about the others
mergeVersions :: Array BaseDep -> {warnings::Array String, merged::Array BaseDep}
mergeVersions deps =
  let grouped = groupBy (on eq keyOnly) $ sortWith keyOnly deps
      allSelections = pickDep <$> grouped
   in {warnings: mapMaybe snd allSelections, merged: fst <$> allSelections }
  where
    pickDep (NonEmpty d others) | not $ any (notEq d) others = Tuple d Nothing
    pickDep ne@(NonEmpty df@(BaseDep k _) _) = fromMaybe (Tuple df Nothing) do
      let versions = fromFoldable $ versionOnly <$> ne
      maxVersion <- maximum versions
      maxDep <- find (versionOnly >>> eq maxVersion) ne
      let warning = "Ignoring versions '" <> joinWith ", " (filter (notEq maxVersion) versions)
                    <> "' for dependency '" <> show k
      pure $ Tuple maxDep $ Just warning

toGradle :: StrMap String -> Array BaseDep -> Either Error String
toGradle versions deps = pure $ joinWith "\n" $ toGDep <$> deps
  where
  toGStr s = "'" <> s <> "'"
  resolveVersion s | (Just v) <- stripPrefix (Pattern "$") s = fromMaybe "" $ lookup v versions
  resolveVersion s = s
  classifierStr (Just c) = ":" <> c
  classifierStr _ = ""
  excludesStr [] = ""
  excludesStr allEx = " {\n " <> joinWith "\n " (exclude <$> allEx) <> "\n}"
    where exclude e = "exclude group: " <> toGStr e
  toGDep (BaseDep (DepKey k) d) = "compile(" <> toGStr (k.groupId <> ":"
                              <> k.artifactId <> ":"
                              <> resolveVersion d.version
                              <> classifierStr d.classifier) <> ")"
    <> excludesStr d.excludes

toSBT :: StrMap String -> Array BaseDep -> Either Error String
toSBT versions deps = pure $ "libraryDependencies ++= Seq(" <> (joinWith ",\n" $ toGDep <$> deps) <> "\n)"
  where
  toStr s = "\"" <> s <> "\""
  resolveVersion s | (Just v) <- stripPrefix (Pattern "$") s = toStr $ fromMaybe "" $ lookup v versions
  resolveVersion s = toStr s
  classifierStr (Just c) = " classifier " <> toStr c
  classifierStr _ = ""
  excludesStr [] = ""
  excludesStr allEx = " excludeAll(\n " <> joinWith ",\n" (exclude <$> allEx) <> "\n)"
    where
    exclude e = "ExclusionRule(" <> case split (Pattern ":") e of
        [o] -> "organization=" <> toStr o
        [o,m] -> "organization=" <> toStr o <> ", name=" <> toStr m
        _ -> toStr e
      <> ")"
  toGDep (BaseDep (DepKey k) d) = toStr k.groupId <> " % "
                              <> toStr k.artifactId <> " % "
                              <> resolveVersion d.version
                              <> classifierStr d.classifier <> ""
    <> excludesStr d.excludes


collectDeps :: Array DepsFile -> {versions:: StrMap String, deps:: Array BaseDep}
collectDeps files = {versions,deps}
  where
    versions = unions $ map (\(DepsFile {versions:v}) -> v) files
    deps = concat $ map (\(DepsFile {dependencies:d}) -> map doDep d) files
    doDep (Dep {groupId,artifactId,version,classifier,excludes}) = BaseDep (DepKey {groupId,artifactId}) {version,classifier,excludes}

outDeps :: forall e. Format -> Array String -> Eff (fs::FS, exception::EXCEPTION, console::CONSOLE|e) Unit
outDeps format files = do
    depFiles <- traverse outDep files
    let {versions,deps} = collectDeps depFiles
        {warnings, merged} = mergeVersions deps
    traverse_ error warnings
    either (show >>> throw) log $ (writeOut format) versions merged
  where
  writeOut SBT = toSBT
  writeOut Gradle = toGradle
  outDep :: String -> Eff (fs::FS, exception::EXCEPTION, console::CONSOLE|e) DepsFile
  outDep fn = do
    depStr <- readTextFile UTF8 fn
    either throw pure do
      decodeJson =<< jsonParser depStr


main :: forall e. Eff (fs::FS, console :: CONSOLE, exception::EXCEPTION | e) Unit
main = do
  runY (usage "Convert deps.txt") (outDeps <$> yarg "format" [] Nothing (Left SBT) true <*> arg "_" )
