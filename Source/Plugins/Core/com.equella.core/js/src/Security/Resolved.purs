module Security.Resolved where 

import Prelude

import Data.Either (Either(..))
import Data.Tuple (Tuple(..))
import Security.Expressions (class ExpressionDecode, ExpressionTerm, OpType)
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
