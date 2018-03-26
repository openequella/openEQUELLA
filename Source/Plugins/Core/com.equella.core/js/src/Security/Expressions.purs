module Security.Expressions where 


data ExpressionTerm = Everyone | LoggedInUsers | Owner | Guests | 
    User String | Group String | Role String | Ip String | Referrer String | ShareSecretToken String



newtype AccessEntry = AccessEntry {priv::String, granted::Boolean, override::Boolean, term::ExpressionTerm} 