module Security.Expressions where 

import Prelude

import Control.Bind (bindFlipped)
import Data.Argonaut (class DecodeJson, class EncodeJson, decodeJson, getField, jsonEmptyObject, (.?), (:=), (~>))
import Data.Array (uncons)
import Data.Array as A
import Data.Bifunctor (lmap)
import Data.Either (Either(..), either, fromRight)
import Data.List (List(..), fromFoldable, head, snoc, (:))
import Data.Maybe (Maybe(..), fromMaybe, maybe)
import Data.String (Pattern(Pattern), joinWith, split, trim)
import Data.String.Regex (Regex, match, regex)
import Data.String.Regex.Flags (noFlags)
import Data.Traversable (traverse)
import Data.Tuple (Tuple(..), fst)
import Global (decodeURIComponent, encodeURIComponent)
import Partial.Unsafe (unsafePartial)

data ExpressionTerm = Everyone | LoggedInUsers | Owner | Guests | 
    User String | Group String | Role String | Ip String | Referrer String | ShareSecretToken String

derive instance eqET :: Eq ExpressionTerm

data OpType = AND | OR
derive instance eqOpType :: Eq OpType

newtype AccessEntry = AccessEntry {priv::String, granted::Boolean, override::Boolean, expr::Expression} 

newtype TargetList = TargetList {entries:: Array TargetListEntry}
newtype TargetListEntry = TargetListEntry {granted::Boolean, override::Boolean, privilege::String, who::String} 

data Expression = Term ExpressionTerm Boolean | Op OpType (Array Expression) Boolean

derive instance eqEx :: Eq Expression

class ExpressionDecode e t | e -> t where 
  decodeExpr :: e -> Tuple Boolean (Either t {op::OpType, exprs::Array e})
  fromTerm :: t -> Boolean -> e
  fromOp :: OpType -> Array e -> Boolean -> e

isnot :: forall e t. ExpressionDecode e t => e -> Boolean 
isnot = decodeExpr >>> fst

notted :: Expression -> Expression
notted (Term et n) = Term et (not n)
notted (Op o exprs n) = Op o exprs (not n)

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
    s | Just [_, Just a, Just v'] <- match paramRegex s -> let v = decodeURIComponent v' in case a of 
        "U" -> t $ User v
        "G" -> t $ Group v
        "R" -> case v of 
            "TLE_LOGGED_IN_USER_ROLE" -> t LoggedInUsers
            "TLE_GUEST_USER_ROLE" -> t Guests
            _ -> t $ Role v
        "F" -> t $ Referrer v
        "T" -> t $ ShareSecretToken v
        "I" -> t $ Ip v
        u -> Left $ UnknownParamType u v
    "$OWNER" -> t Owner    
    a -> Left $ Broken a

parseWho :: String -> Either ExpressionParseError Expression
parseWho who = either Left (head >>> maybe (Left $ TooManyExpressions) Right) $ do 
  terms <- lmap TermError $ traverse parseTerm $ split (Pattern " ") $ trim who
  convertToInfix Nil $ fromFoldable terms
  where 
  convertToInfix (h : tailArgs) (NOT : tail) = convertToInfix (notted h : tailArgs) tail
  convertToInfix (h1 : h2 : tailArgs) (BinOp op : tail) = convertToInfix (Op op [h1, h2] false : tailArgs) tail
  convertToInfix c (StdTerm t : tail) = convertToInfix (Term t false : c) tail
  convertToInfix c Nil = Right $ c
  convertToInfix _ _ = Left NotEnoughOpArgs



parseEntry :: TargetListEntry -> Either ExpressionParseError AccessEntry
parseEntry (TargetListEntry {granted,override,privilege,who}) = 
    parseWho who # map (\expr -> AccessEntry {granted,override,priv:privilege,expr: fromMaybe expr $ collapseOps expr })

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
    Ip ip -> param "I" ip 
    Referrer ref -> param "F" ref
    ShareSecretToken tokenId -> param "T" tokenId
  where param a v = a <> ":" <> encodeURIComponent v

textForTerm :: ExpressionTerm -> String
textForTerm = case _ of 
    Everyone -> "Everyone"
    LoggedInUsers -> "Logged in users"
    Owner -> "Owner"
    Guests -> "Guests"
    User uid -> "User with id " <> uid 
    Group gid -> "Group with id " <> gid
    Role rid -> "Role with id " <> rid 
    Ip ip -> "IP Range - " <> ip 
    Referrer ref -> "Referrer - " <> ref
    ShareSecretToken tokenId -> "Shared secret token " <> tokenId

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
entryToTargetList (AccessEntry {priv, granted, override, expr}) = TargetListEntry {privilege:priv, granted, override, who: 
    joinWith " " $ A.reverse $ A.fromFoldable $ toWho expr}
  where 
  toWho :: Expression -> List String
  toWho t | isnot t = "NOT" : toWho (notted t) 
  toWho (Term t _) = termToWho t : Nil
  toWho (Op op [h1] _) = toWho h1
  toWho (Op op [h1, h2] _) = opText op : (toWho h2 <> toWho h1)
  toWho (Op op exprs _) | Just {head,tail} <- uncons exprs = toWho (Op op [head, Op op tail false] false)
  toWho _ = Nil