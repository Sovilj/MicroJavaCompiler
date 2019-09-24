
package rs.ac.bg.etf.pp1;

import java_cup.runtime.Symbol;

%%

%{
	// ukljucivanje informacije o poziciji tokena
	private Symbol new_symbol(int type) {
		return new Symbol(type, yyline+1, yycolumn);
	}
	// ukljucivanje informacije o poziciji tokena
	private Symbol new_symbol(int type, Object value) {
		return new Symbol(type, yyline+1, yycolumn, value);
	}
%}

%cup
%line
%column

%xstate COMMENT

%eofval{
	return new_symbol(sym.EOF);
%eofval}

%%

" " 	{ }
"\b" 	{ }
"\t" 	{ }
"\r\n" 	{ }
"\f" 	{ }

"program"   { return new_symbol(sym.PROG, yytext());}
"break"   { return new_symbol(sym.BREAK, yytext());}
"class" 	{ return new_symbol(sym.CLASS, yytext()); }
"interface" 	{ return new_symbol(sym.INTERFACE, yytext()); }
"enum" 		{ return new_symbol(sym.ENUM, yytext()); }
"else"   { return new_symbol(sym.ELSE, yytext());}
"const"   { return new_symbol(sym.CONST, yytext());}
"if"   { return new_symbol(sym.IF, yytext());}
"new"   { return new_symbol(sym.NEW, yytext());}
"print"   { return new_symbol(sym.PRINT, yytext());}
"read"   { return new_symbol(sym.READ, yytext());}
"return"   { return new_symbol(sym.RET, yytext());}
"void"   { return new_symbol(sym.VOID, yytext());}
"for"   { return new_symbol(sym.FOR, yytext());}
"extends"   { return new_symbol(sym.EXTENDS, yytext());}
"continue"   { return new_symbol(sym.CONTINUE, yytext());}
"implements" { return new_symbol(sym.IMPLEMENTS, yytext());}
"true"      {return new_symbol(sym.BOOLCONST, yytext());}
"false"		{return new_symbol(sym.BOOLCONST, yytext());}

"+" 		{ return new_symbol(sym.PLUS, yytext()); }
"-" 		{ return new_symbol(sym.MINUS, yytext()); }
"*" 		{ return new_symbol(sym.MUL, yytext()); }
"/" 		{ return new_symbol(sym.DIV, yytext()); }
"%" 		{ return new_symbol(sym.PERCENT, yytext()); }
"==" 		{ return new_symbol(sym.EQUALEQUAL, yytext()); }
"!=" 		{ return new_symbol(sym.NOTEQUAL, yytext()); }
">" 		{ return new_symbol(sym.GRT, yytext()); }
">=" 		{ return new_symbol(sym.GRTEQUAL, yytext()); }
"<" 		{ return new_symbol(sym.LES, yytext()); }
"<=" 		{ return new_symbol(sym.LESEQUAL, yytext()); }
"&&" 		{ return new_symbol(sym.AND, yytext()); }
"||" 		{ return new_symbol(sym.OR, yytext()); }
"=" 		{ return new_symbol(sym.EQUAL, yytext()); }
"++" 		{ return new_symbol(sym.PLUSPLUS, yytext()); }
"--" 		{ return new_symbol(sym.MINUSMINUS, yytext()); }
";" 		{ return new_symbol(sym.SEMI, yytext()); }
","			{ return new_symbol(sym.COMMA, yytext()); }
"." 		{ return new_symbol(sym.DOT, yytext()); }
"(" 		{ return new_symbol(sym.LBRACE, yytext()); }
")" 		{ return new_symbol(sym.RBRACE, yytext()); }
"[" 		{ return new_symbol(sym.LSQUARE, yytext()); }
"]" 		{ return new_symbol(sym.RSQUARE, yytext()); }
"{" 		{ return new_symbol(sym.LPAREN, yytext()); }
"}" 		{ return new_symbol(sym.RPAREN, yytext()); }



"//" {yybegin(COMMENT);}
<COMMENT> . {yybegin(COMMENT);}
<COMMENT> "\r\n" { yybegin(YYINITIAL); }


[0-9]+  { return new_symbol(sym.NUMCONST, new Integer (yytext())); }
"'"[\040-\176]"'" {return new_symbol (sym.CHARCONST, new Character (yytext().charAt(1)));}
([a-z]|[A-Z])[a-z|A-Z|0-9|_]* 	{return new_symbol (sym.IDENT, yytext()); }


. { System.err.println("Leksicka greska ("+yytext()+") u liniji "+(yyline+1)); }










