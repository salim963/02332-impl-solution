grammar impl;

/* A small imperative language */

start   :  cs+=command* EOF ;

program : c=command                                # SingleCommand
	| '{' cs+=command* '}'                         # MultipleCommands
	;
	
command : x=ID '=' e=expr ';'	                   # Assignment
	| a=ID '[' i=expr ']' '=' e=expr ';'           # ArrayAssignment
	| 'output' e=expr ';'                           # Output
    | 'while' '('c=condition')' p=program           # WhileLoop
	| 'for' '(' x=ID '=' e1=expr '..' e2=expr ')' p=program # ForLoop
	| 'if' '(' c=condition ')' p=program            # If
	| 'Breakpoint' e=expr ';'                       #Breakpoint
	;
	
expr	: e1=expr o=('*' | '/') e2=expr # Multiplication
	| e1=expr o=('+' | '-') e2=expr # Addition
	| c=FLOAT     	      # Constant
	| '-' c=FLOAT         # NegativeConstant
	| x=ID                # Variable
	| a=ID '[' e=expr ']' # Array
	| '(' e=expr ')'      # Parenthesis
	;

condition : e1=expr '!=' e2=expr # Unequal
	  | e1=expr '==' e2=expr # Equal
	  | e1=expr '<' e2=expr # Smaller
	  | c1=condition '||' c2=condition # Disjunction
	  | c1=condition '&&' c2=condition # Conjunction
	  | '!' c=condition     # Negation
	  | '(' c=condition ')' # ParenthesisCondition
	  ;  

ID    : ALPHA (ALPHA|NUM)* ;
FLOAT : NUM+ ('.' NUM+)? ;

ALPHA : [a-zA-Z_ÆØÅæøå] ;
NUM   : [0-9] ;

WHITESPACE : [ \n\t\r]+ -> skip;
COMMENT    : '//'~[\n]*  -> skip;
COMMENT2   : '/*' (~[*] | '*'~[/]  )*   '*/'  -> skip;
