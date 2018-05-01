module Security.Resolved where 

import Prelude

import Data.Array (alterAt, cons, foldl, index, reverse, tail, updateAt)
import Data.Bifunctor (bimap)
import Data.Either (Either(..))
import Data.Lens (Lens', ALens', lens, wander)
import Data.Lens.At (class At)
import Data.Lens.Index (class Index)
import Data.Maybe (Maybe(..), fromMaybe)
import Data.Tuple (Tuple(..))
import Security.Expressions (class ExpressionDecode, ExpressionTerm, OpType(..))
import Users.UserLookup (GroupDetails, RoleDetails, UserDetails)

data ResolvedTerm = Already ExpressionTerm | ResolvedUser UserDetails | ResolvedGroup GroupDetails | ResolvedRole RoleDetails

derive instance eqRT :: Eq ResolvedTerm 

data ResolvedExpression = Term ResolvedTerm Boolean | Op OpType (Array ResolvedExpression) Boolean

derive instance eqRE :: Eq ResolvedExpression

instance rexDecode :: ExpressionDecode ResolvedExpression ResolvedTerm where 
  decodeExpr (Term rt n) = Tuple n (Left $ rt)
  decodeExpr (Op op exprs n) = Tuple n (Right {op,exprs})
  fromTerm = Term 
  fromOp = Op

findExprModify :: Int -> ResolvedExpression -> Either Int { get :: ResolvedExpression, modify :: Maybe ResolvedExpression -> Maybe ResolvedExpression }
findExprModify 0 e = Right $ {get:e, modify: id}
findExprModify i (Term _ _) = Left $ i - 1
findExprModify i (Op op exprs notted) = 
  let foldOp (Left {i,o}) e = let 
        update {get,modify} = { get, modify: \n -> 
          let newexprs = fromMaybe exprs $ alterAt o (\_ -> modify n) exprs
          in Just $ Op op newexprs notted } 
        in bimap {i: _, o:o+1} update $ findExprModify i e
      foldOp r _ = r
  in bimap _.i id $ foldl foldOp (Left $ {i: i - 1, o:0}) exprs 

findExprInsert :: Int -> ResolvedExpression -> Either Int (Array ResolvedExpression -> Array ResolvedExpression)
findExprInsert 0 e = Right $ \n -> n <> [e]
findExprInsert 1 t@(Term _ _)  = Right $ \n -> [t] <> n
findExprInsert 1 (Op op exprs notted) = Right $ \n -> [Op op (n <> exprs) notted]
findExprInsert i (Term _ _) = Left $ i - 1
findExprInsert i (Op op exprs notted) = 
  let foldOp (Left {i,before}) e = bimap {i: _, before: cons e before } {insert:_, after:[], before} $ findExprInsert i e
      foldOp (Right {before,insert,after}) e = Right {before,insert,after: cons e after}
  in bimap _.i (\{before,insert,after} -> (\n -> [Op op (reverse before <> insert n <> reverse after) notted])) $ foldl foldOp (Left $ {i: i - 1, before:[]}) exprs 
