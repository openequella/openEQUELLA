module OEQ.Data.Security where 

import Prelude

import Control.Bind (bindFlipped)
import Data.Argonaut (class DecodeJson, class EncodeJson, decodeJson, getField, jsonEmptyObject, (.?), (:=), (~>))
import Data.Array (alterAt, cons, elemIndex, foldl, mapMaybe, reverse, uncons)
import Data.Array as A
import Data.Array.NonEmpty (drop, toArray)
import Data.Bifunctor (bimap, lmap)
import Data.Either (Either(..), either, fromRight)
import Data.Int (fromString)
import Data.Lens (Lens', lens)
import Data.List (List(Nil), fromFoldable, head, (:))
import Data.Maybe (Maybe(Just, Nothing), fromMaybe, isJust, maybe)
import Data.String (Pattern(Pattern), joinWith, split, trim)
import Data.String.Regex (Regex, match, regex)
import Data.String.Regex.Flags (noFlags)
import Data.Traversable (traverse)
import Data.Tuple (Tuple(..), fst)
import OEQ.Data.User (GroupDetails(..), RoleDetails(..), UserDetails(..))
import Global.Unsafe (unsafeDecodeURIComponent, unsafeEncodeURIComponent)
import Partial.Unsafe (unsafePartial)

data ExpressionTerm = Everyone 
  | LoggedInUsers 
  | Owner 
  | Guests 
  | User String 
  | Group String 
  | Role String 
  | Ip IpRange 
  | Referrer String 
  | SharedSecretToken String

derive instance eqET :: Eq ExpressionTerm

data OpType = AND | OR
derive instance eqOpType :: Eq OpType

newtype AccessEntry = AccessEntry {
    priv::String, 
    granted::Boolean, 
    override::Boolean, 
    expr::Expression
} 

newtype TargetList = TargetList {entries:: Array TargetListEntry}
newtype TargetListEntry = TargetListEntry {
    granted::Boolean, 
    override::Boolean, 
    privilege::String, 
    who::String
} 

derive instance eqTLE :: Eq TargetListEntry

data Expression = Term ExpressionTerm Boolean | Op OpType (Array Expression) Boolean

derive instance eqEx :: Eq Expression

data ResolvedTerm = Already ExpressionTerm | ResolvedUser UserDetails | ResolvedGroup GroupDetails | ResolvedRole RoleDetails

derive instance eqRT :: Eq ResolvedTerm 

data ResolvedExpression = RTerm ResolvedTerm Boolean | ROp OpType (Array ResolvedExpression) Boolean

derive instance eqRE :: Eq ResolvedExpression

data IpRange = IpRange Int Int Int Int Int
derive instance eqIP :: Eq IpRange

validMasks :: Array Int 
validMasks = [8, 16, 24, 32]

validRange :: IpRange -> Boolean 
validRange (IpRange i1 i2 i3 i4 im) = let byte b = b >= 0 && b < 256
    in byte i1 && byte i2 && byte i3 && byte i4 && (isJust $ elemIndex im validMasks)


_ip1 :: Lens' IpRange Int
_ip1 = lens (\(IpRange i1 _ _ _ _) -> i1) (\(IpRange _ i2 i3 i4 im) i1 -> IpRange i1 i2 i3 i4 im)

_ip2 :: Lens' IpRange Int
_ip2 = lens (\(IpRange _ i2 _ _ _) -> i2) (\(IpRange i1 _ i3 i4 im) i2 -> IpRange i1 i2 i3 i4 im)

_ip3 :: Lens' IpRange Int
_ip3 = lens (\(IpRange _ _ i3 _ _) -> i3) (\(IpRange i1 i2 _ i4 im) i3 -> IpRange i1 i2 i3 i4 im)

_ip4 :: Lens' IpRange Int
_ip4 = lens (\(IpRange _ _ _ i4 _) -> i4) (\(IpRange i1 i2 i3 _ im) i4 -> IpRange i1 i2 i3 i4 im)

_ipm :: Lens' IpRange Int
_ipm = lens (\(IpRange _ _ _ _ im) -> im) (\(IpRange i1 i2 i3 i4 _) im -> IpRange i1 i2 i3 i4 im)

class ExpressionDecode e t | e -> t where 
  decodeExpr :: e -> Tuple Boolean (Either t {op::OpType, exprs::Array e})
  fromTerm :: t -> Boolean -> e
  fromOp :: OpType -> Array e -> Boolean -> e

isnot :: forall e t. ExpressionDecode e t => e -> Boolean 
isnot = decodeExpr >>> fst

exprNotted :: Expression -> Expression
exprNotted (Term et n) = Term et (not n)
exprNotted (Op o exprs n) = Op o exprs (not n)

instance encTL :: EncodeJson TargetList where 
  encodeJson (TargetList {entries}) = "entries" := entries ~> jsonEmptyObject

instance encTLE :: EncodeJson TargetListEntry where 
  encodeJson (TargetListEntry {granted,override,privilege,who}) = 
    "granted" := granted ~> 
    "override" := override ~> 
    "privilege" := privilege ~> 
    "who" := who 
    ~> jsonEmptyObject

instance decTL :: DecodeJson TargetList where 
  decodeJson v = do 
    TargetList <<< {entries: _} <$> (decodeJson v >>= flip getField "entries")
      
instance decTLE :: DecodeJson TargetListEntry where 
  decodeJson v = do 
    o <- decodeJson v
    granted <- o .? "granted"
    override <- o .? "override"
    privilege <- o .? "privilege"
    who <- o .? "who"
    pure $ TargetListEntry {granted,override,privilege,who}

data ParsedTerm = BinOp OpType | NOT | StdTerm ExpressionTerm

paramRegex :: Regex
paramRegex = unsafePartial $ fromRight $ regex "^(\\w):(.*)$" noFlags

data ExpressionParseError = TermError TermParseError | NotEnoughOpArgs | TooManyExpressions
data TermParseError = UnknownParamType String String | Broken String

parseTerm :: String -> Either TermParseError ParsedTerm
parseTerm = let 
    t = Right <<< StdTerm 
 in case _ of 
    "AND" -> Right $ BinOp AND 
    "OR" -> Right $ BinOp OR
    "NOT" -> Right NOT 
    "*" -> t Everyone
    s | Just [_, Just a, Just v'] <- toArray <$> match paramRegex s -> let v = unsafeDecodeURIComponent v' in case a of 
        "U" -> t $ User v
        "G" -> t $ Group v
        "R" -> case v of 
            "TLE_LOGGED_IN_USER_ROLE" -> t LoggedInUsers
            "TLE_GUEST_USER_ROLE" -> t Guests
            _ -> t $ Role v
        "F" -> t $ Referrer v
        "T" -> t $ SharedSecretToken v
        "I" -> maybe (Left $ Broken v) (t <<< Ip) $ parseRange v
        u -> Left $ UnknownParamType u v
    "$OWNER" -> t Owner    
    a -> Left $ Broken a

parseWho :: String -> Either ExpressionParseError Expression
parseWho who = either Left (head >>> maybe (Left $ TooManyExpressions) Right) $ do 
  terms <- lmap TermError $ traverse parseTerm $ split (Pattern " ") $ trim who
  convertToInfix Nil $ fromFoldable terms
  where 
  convertToInfix (h : tailArgs) (NOT : tail) = convertToInfix (exprNotted h : tailArgs) tail
  convertToInfix (h1 : h2 : tailArgs) (BinOp op : tail) = convertToInfix (Op op [h1, h2] false : tailArgs) tail
  convertToInfix c (StdTerm t : tail) = convertToInfix (Term t false : c) tail
  convertToInfix c Nil = Right $ c
  convertToInfix _ _ = Left NotEnoughOpArgs

opText :: OpType -> String 
opText AND = "AND"
opText OR = "OR"

termToWho :: ExpressionTerm -> String 
termToWho = case _ of 
    Everyone -> "*"
    LoggedInUsers -> "R:TLE_LOGGED_IN_USER_ROLE"
    Owner -> "$OWNER"
    Guests -> "R:TLE_GUEST_USER_ROLE"
    User uid -> param "U" uid 
    Group gid -> param "G" gid
    Role rid -> param "R" rid 
    Ip range -> param "I" $ ipAsString range 
    Referrer ref -> param "F" ref
    SharedSecretToken tokenId -> param "T" tokenId
  where param a v = a <> ":" <> unsafeEncodeURIComponent v

rangeRegex :: Regex
rangeRegex = unsafePartial fromRight $ regex "(\\d+)\\.(\\d+)\\.(\\d+)\\.(\\d+)/(\\d+)" noFlags

parseRange :: String -> Maybe IpRange 
parseRange s = match rangeRegex s >>= toIpRange
  where 
  toIpRange a = case traverse (bindFlipped fromString) $ drop 1 a of 
    Just [i1, i2, i3, i4, i5] -> Just $ IpRange i1 i2 i3 i4 i5
    _ -> Nothing

ipAsString :: IpRange -> String 
ipAsString (IpRange ip1 ip2 ip3 ip4 ipm) = (joinWith "." $ show <$> [ip1, ip2, ip3, ip4]) <> "/" <> show ipm

textForTerm :: ExpressionTerm -> String
textForTerm = case _ of 
    Everyone -> "Everyone"
    LoggedInUsers -> "Logged in users"
    Owner -> "Owner"
    Guests -> "Guests"
    User uid -> "User with id " <> uid 
    Group gid -> "Group with id " <> gid
    Role rid -> "Role with id " <> rid 
    Ip range -> "IP Range - " <> ipAsString range
    Referrer ref -> "Referrer - " <> ref
    SharedSecretToken tokenId -> "Shared secret token " <> tokenId

instance exDecode :: ExpressionDecode Expression ExpressionTerm  where 
    decodeExpr (Term t n) = Tuple n (Left $ t)
    decodeExpr (Op op exprs n) = Tuple n (Right {op, exprs})
    fromTerm = Term
    fromOp = Op

textForExpression :: Expression -> String
textForExpression = expressionText textForTerm

expressionText :: forall e t. ExpressionDecode e t => (t -> String) -> e -> String
expressionText f e = let 
  textRecurse topLevel (Tuple n e) = 
    if n then "NOT " <> textRecurse false (Tuple false e) 
    else e # either f \{op,exprs} -> 
        let exprText = joinWith (" " <> opText op <> " ") $ (decodeExpr >>> textRecurse false <$> exprs) 
        in if topLevel then exprText else "(" <> exprText <> ")"
  in textRecurse true $ decodeExpr e

traverseExpr :: forall e t a. ExpressionDecode e t => (t -> Boolean -> a) -> ({op::OpType, exprs::Array a} -> Boolean -> a) -> e -> a
traverseExpr f fop = 
  let recurse e = let (Tuple n w) = decodeExpr e 
        in case w of 
            (Left t) -> f t n
            (Right {op, exprs:es}) -> let exprs = recurse <$> es in fop {op, exprs} n
  in recurse

collapseZero :: forall e t. ExpressionDecode e t => e -> Maybe e
collapseZero e = let (Tuple n w) = decodeExpr e in case w of 
  Right {exprs: []} -> Nothing
  Right {exprs: [o]} | (Tuple _ (Right _)) <- decodeExpr o -> collapseZero o
  Right {op,exprs} -> case mapMaybe collapseZero exprs of 
    [] -> Nothing
    o -> Just $ fromOp op o n
  _ -> Just e

collapseOps :: forall e t. ExpressionDecode e t => e -> Maybe e 
collapseOps e =
  let collapseOp :: OpType -> Boolean -> e -> Array e 
      collapseOp op n e = let (Tuple n2 w) = decodeExpr e in case w of 
          (Right {op:op2, exprs}) | op2 == op && n == n2 -> exprs
          _ -> [e]
      (Tuple n w) = decodeExpr e
  in case w of 
      Right {exprs: []} -> Nothing
      Right {exprs: [one]} | isnot one == n -> collapseOps one
      Right {op, exprs} ->  
          let currentLen = A.length exprs 
              newExprs = exprs >>= collapseOp op n
          in if currentLen /= A.length newExprs then collapseOps (fromOp op newExprs n) else Just $ e
      _ -> Just $ e

entryToTargetList :: AccessEntry -> TargetListEntry
entryToTargetList (AccessEntry {priv, granted, override, expr}) = TargetListEntry 
    {privilege:priv, granted, override, 
        who: joinWith " " $ A.reverse $ A.fromFoldable $ toWho expr}
  where 
  toWho :: Expression -> List String
  toWho t | isnot t = "NOT" : toWho (exprNotted t) 
  toWho (Term t _) = termToWho t : Nil
  toWho (Op op [h1] _) = toWho h1
  toWho (Op op [h1, h2] _) = opText op : (toWho h2 <> toWho h1)
  toWho (Op op exprs _) | Just {head,tail} <- uncons exprs = toWho (Op op [head, Op op tail false] false)
  toWho _ = Nil

instance tpeShow :: Show TermParseError where 
  show = case _ of 
    UnknownParamType p v -> "Unknown parameter type: " <> p <> " value: " <> v
    (Broken v) -> "Unparsable: " <> v

instance epeShow :: Show ExpressionParseError where 
  show = case _ of 
    TermError e -> show e
    NotEnoughOpArgs -> "Not enough op args"
    TooManyExpressions -> "Too many expressions"

instance rexDecode :: ExpressionDecode ResolvedExpression ResolvedTerm where 
  decodeExpr (RTerm rt n) = Tuple n (Left $ rt)
  decodeExpr (ROp op exprs n) = Tuple n (Right {op,exprs})
  fromTerm = RTerm 
  fromOp = ROp

resolvedToTerm :: ResolvedTerm -> ExpressionTerm 
resolvedToTerm = case _ of 
  Already e -> e 
  ResolvedUser (UserDetails {id}) -> User id
  ResolvedGroup (GroupDetails {id}) -> Group id
  ResolvedRole (RoleDetails {id}) -> Role id

findExprModify :: Int -> ResolvedExpression -> Either Int { get :: ResolvedExpression, modify :: Maybe ResolvedExpression -> Maybe ResolvedExpression }
findExprModify 0 e = Right $ {get:e, modify: identity}
findExprModify i (RTerm _ _) = Left $ i - 1
findExprModify i (ROp op exprs notted) = 
  let foldOp (Left {i,o}) e = let 
        update {get,modify} = { get, modify: \n -> 
          let newexprs = fromMaybe exprs $ alterAt o (\_ -> modify n) exprs
          in Just $ ROp op newexprs notted } 
        in bimap {i: _, o:o+1} update $ findExprModify i e
      foldOp r _ = r
  in bimap _.i identity $ foldl foldOp (Left $ {i: i - 1, o:0}) exprs 

findExprInsert :: Int -> ResolvedExpression -> Either Int (Array ResolvedExpression -> Array ResolvedExpression)
findExprInsert 0 e = Right $ \n -> n <> [e]
findExprInsert 1 t@(RTerm _ _)  = Right $ \n -> [t] <> n
findExprInsert 1 (ROp op exprs notted) = Right $ \n -> [ROp op (n <> exprs) notted]
findExprInsert i (RTerm _ _) = Left $ i - 1
findExprInsert i (ROp op exprs notted) = 
  let foldOp (Left {i,before}) e = bimap {i: _, before: cons e before } {insert:_, after:[], before} $ findExprInsert i e
      foldOp (Right {before,insert,after}) e = Right {before,insert,after: cons e after}
  in bimap _.i (\{before,insert,after} -> (\n -> [ROp op (reverse before <> insert n <> reverse after) notted])) 
    $ foldl foldOp (Left $ {i: i - 1, before:[]}) exprs 
