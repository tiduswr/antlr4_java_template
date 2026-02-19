grammar Expr;

prog: expr EOF;
expr: '(' NESTED_EXPR=expr ')'                          #Parenteses
    | <assoc=right> BASE=expr OP='^' EXPOENTE=expr      #Exponenciacao
    | O1=expr OP=('*'|'/') O2=expr                      #MulDiv
    | O1=expr OP=('+'|'-') O2=expr                      #SomaSub
    | NUMBER                                            #Numero
;


NUMBER: [0-9]+('.'[0-9]+)?;
WS: [ \t\r\n]+ -> skip;