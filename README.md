# VIPAR: A LALR (1) lexer & parser

Vipar is a LALR(1) lexer and parser for a context-free grammar generating a Python-like language, written in Java.

## EBNF Grammar
<details>
<summary> Upper case used for non-terminals, lower case for terminals. (WARNING: Its quite long...)</summary>
<br>
S -> PROGRAM
<br>
PROGRAM -> PREMAINDECLARATIONS MAINFUNC {FUNC} <br>
PREMAINDECLARATIONS -> DECLARATION ';' PREMAINDECLARATIONS | ϵ<br>
MAINFUNC -> func main '(' ')' '{' STMTLIST '}'<br>
STMTLIST -> STMT STMTLIST | ϵ<br>
STMT -> CONDITIONAL | LOOP | DECLARATION | ASSIGNMENT | FUNCCALL<br>
CONDITIONAL -> IF | SWITCH<br>
IF -> if '(' CONDITION ')' '{' STMTLIST '}' {ELSEIFS} [ELSE]<br>
ELSE -> else '{' STMTLIST '}'<br>
ELSEIFS -> elif '(' CONDITION ')' '{' STMTLIST '}' <br>
SWITCH -> switch '(' EXPR ')' '{' {CASE} '}'<br>
CASE -> case EXPR '{' STMTLIST '}'<br>
LOOP -> FORLOOP | WHILELOOP<br>
FORLOOP -> for variable in variable LOOPOP variable [STEP] '{' STMTLIST '}'<br>
LOOPOP -> ... | ..<<br>
STEP -> step variable<br>
WHILELOOP -> while '(' CONDITION ')' '{' STMTLIST '}'<br>
DECLARATION -> var VARDECLLIST<br>
VARDECLLIST -> variable : VARTYPE OPTASSIGNRHS {MOREDECLS}<br>
MOREDECLS -> , variable : VARTYPE [ASSIGNRHS]<br>
OPTASSIGNRHS -> = ASSIGNRHS<br>
ASSIGNMENT -> variable = ASSIGNRHS<br>
ASSIGNRHS -> EXPR<br>
ASSIGNRHS -> EXPR if CONDITION else EXPR<br>
FUNCCALL -> FUNCCALLRHS<br>
FUNCCALLRHS -> variable ( [ACTPARAMLIST] )<br>
ACTPARAMLIST -> VAROREXP {MOREPARAMS}<br>
MOREPARAMS -> , VAROREXP<br>
VAROREXP -> EXPR<br>
FUNC -> func variable ( [FUNCPARAMS] ) arrow RETTYPE { FUNCSTMT }<br>
FUNCSTMT -> STMTLIST [RETURN]<br>
OPTRETURN -> return EXPR ;<br>
FUNCPARAMS -> variable : VARTYPE {MOREFUNCPARAMS}<br>
MOREFUNCPARAMS -> , variable : VARTYPE<br>
RETTYPE -> VARTYPE | void<br>
CONDITION -> [!] SCONDITION [(&& | ||) SCONDITION]<br>
SCONDITION -> EXPR RELOP EXPR | ( CONDITION )<br>
RELOP -> < | < | <= | >= | == | !=<br>
EXPR -> SUBEXPR [(+ | - | * | / | ^ | % | & | |) SUBEXPR]<br>
SUBEXPR -> variable | ( EXPR ) | FUNCCALLRHS<br>
VARTYPE -> int | real | string<br>
</details>

Here's the corresponding [BNF Grammar](src/Grammar.txt) (Psst... Its even longer!)
and the [parse table](src/ParseTable.xlsx) (this is the biggest!)

## Usage
Write some code and change the file name in the ```Runner``` class (extension doesn't really matter, but its cool to have your "own extension" :D). We've provided some sample valid code in ```code0.vipar```, ```code1.vipar``` and ```code2.vipar```

## Visualization
A beautiful parse tree is created in the outputs folder. The green leaves are the terminals which are actually part of the code. The red leaves expand to the null string. Feel free to open the ```.html``` files we've included for our sample code!
