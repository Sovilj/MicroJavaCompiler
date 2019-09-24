package rs.ac.bg.etf.pp1;

import java.util.Iterator;
import java.util.LinkedList;

import org.apache.log4j.*;
import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.symboltable.*;
import rs.etf.pp1.symboltable.concepts.*;
import rs.etf.pp1.symboltable.structure.HashTableDataStructure;
import rs.etf.pp1.symboltable.structure.SymbolDataStructure;

public class SemanticAnalizer extends VisitorAdaptor {

	public static final Struct boolType = new Struct(Struct.Bool);

	Struct currType;
	Scope progScope;
	Obj currMethod = null;
	boolean returnFound = false;
	int currLevel = 0;

	int nested = -1;
	// String methodCalled;
	String[] methodCalledList = new String[100];
	// int actArgCount = 0;
	int[] actArgCount = new int[100];

	LinkedList<Struct> actParsList = new LinkedList<Struct>();

	String enumName;
	int enumPret = -1;
	LinkedList<Integer> enumElem = new LinkedList<Integer>();
	static SymbolDataStructure enumData = new HashTableDataStructure();

	int LocalVarDeclCount = 0;
	int GlobalVarDeclCount = 0;
	int ConstDeclCount = 0;
	int FuncCallCount = 0;
	int PrintCallCount = 0;
	int ArrayElemCount = 0;

	boolean errorDetected = false;

	int nVars;

	Logger log = Logger.getLogger(getClass());

	public void report_error(String message, SyntaxNode info) {
		errorDetected = true;
		StringBuilder msg = new StringBuilder(message);
		int line = (info == null) ? 0 : info.getLine();
		if (line != 0)
			msg.append(" na liniji ").append(line);
		log.error(msg.toString());
		System.err.println(msg.toString());
	}

	public void report_info(String message, SyntaxNode info) {
		StringBuilder msg = new StringBuilder(message);
		int line = (info == null) ? 0 : info.getLine();
		if (line != 0)
			msg.append(" na liniji ").append(line);
		log.info(msg.toString());
		System.out.println(msg.toString());
	}

	public boolean passed() {
		return !errorDetected;
	}

	public boolean isDeclared(String name) {
		if (name == null)
			return true;

		Obj temp = Tab.find(name);
		if (temp.getKind() == Obj.Type)
			return true; // int int;

		temp = Tab.currentScope.findSymbol(name);

		if (temp == Tab.noObj || temp == null)
			return false;

		return true;
	}

//=====================================================================

	public void visit(ProgName progName) {
		progName.obj = Tab.insert(Obj.Prog, progName.getProgName(), Tab.noType);
		Tab.openScope();
		Tab.insert(Obj.Type, "bool", boolType);
		progScope = Tab.currentScope;
	}

	public void visit(Program program) {
		nVars = Tab.currentScope.getnVars();
		Tab.chainLocalSymbols(program.getProgName().obj);
		Tab.closeScope();
	}

	public void visit(Type type) {
		Obj typeNode = Tab.find(type.getTypeName());
		currType = null;
		if (typeNode == Tab.noObj || (type.struct != null && type.struct.equals(Tab.noType))
				|| typeNode.getKind() != Obj.Type) {
			report_error("Greska - Nije pronadjen tip " + type.getTypeName() + " u tabeli simbola ", type);
			type.struct = Tab.noType;
		} else {
			if (Obj.Type == typeNode.getKind()) {
				type.struct = typeNode.getType();
			} else {
				report_error("Greska - Ime " + type.getTypeName() + " ne predstavlja tip ", type);
				type.struct = Tab.noType;
			}
		}
		currType = type.struct;
	}

	// ------------CONSTANTE---------------------

	public void visit(SingleConstantNum singleConstant) {
		ConstDeclCount++;

		Obj obj = new Obj(Obj.Con, "", Tab.intType);
		obj.setAdr(singleConstant.getS().intValue());
		singleConstant.obj = obj;

		if ((singleConstant != null) && (currType != null)) {

			if (currType.getKind() != singleConstant.obj.getType().getKind()) {
				report_error("Greska - Konstanta " + singleConstant.getName() + " nije odgovarajuceg tipa ",
						singleConstant);
			} else if (isDeclared(singleConstant.getName())) {
				report_error("Greska - Konstanta " + singleConstant.getName() + " je vec deklarisana ", singleConstant);

			} else {
				Obj temp = Tab.insert(Obj.Con, singleConstant.getName(), currType);
				temp.setAdr(singleConstant.obj.getAdr());
				report_info("Deklariasana konstanta num " + singleConstant.getName(), singleConstant);
			}
		}
	}

	public void visit(SingleConstantChar singleConstant) {
		ConstDeclCount++;

		Obj obj = new Obj(Obj.Con, "", Tab.charType);
		obj.setAdr(singleConstant.getS().charValue());
		singleConstant.obj = obj;

		if ((singleConstant != null) && (currType != null)) {

			if (currType.getKind() != singleConstant.obj.getType().getKind()) {
				report_error("Greska - Konstanta " + singleConstant.getName() + " nije odgovarajuceg tipa ",
						singleConstant);
			} else if (isDeclared(singleConstant.getName())) {
				report_error("Greska - Konstanta " + singleConstant.getName() + " je vec deklarisana ", singleConstant);

			} else {
				Obj temp = Tab.insert(Obj.Con, singleConstant.getName(), currType);
				temp.setAdr(singleConstant.obj.getAdr());
				report_info("Deklariasana konstanta char " + singleConstant.getName(), singleConstant);
			}
		}
	}

	public void visit(SingleConstantBool singleConstant) {
		ConstDeclCount++;

		Obj obj = new Obj(Obj.Con, "", new Struct(Struct.Bool, boolType));
		if (singleConstant.getS().equals("true"))
			obj.setAdr(1);
		else
			obj.setAdr(0);
		singleConstant.obj = obj;

		if ((singleConstant != null) && (currType != null)) {
			if (currType.getKind() != singleConstant.obj.getType().getKind()) {
				report_error("Greska - Konstanta " + singleConstant.getName() + " nije odgovarajuceg tipa ",
						singleConstant);
			} else if (isDeclared(singleConstant.getName())) {
				report_error("Greska - Konstanta " + singleConstant.getName() + " je vec deklarisana ", singleConstant);

			} else {
				Obj temp = Tab.insert(Obj.Con, singleConstant.getName(), currType);
				temp.setAdr(singleConstant.obj.getAdr());
				report_info("Deklariasana konstanta bool " + singleConstant.getName(), singleConstant);
			}
		}
	}

	// -----------------VARIABLE----------------------

	public void visit(Var variable) {

		if (Tab.currentScope == progScope) {
			GlobalVarDeclCount++;
		} else {
			LocalVarDeclCount++;
		}

		Obj obj = Tab.find(variable.getVarName());
		Struct sq = variable.getSquare().struct;

		if (currType != Tab.noType) {
			if (sq.getKind() == Struct.Array) {
				if (obj == Tab.noObj || currLevel != obj.getLevel()) {
					variable.obj = Tab.insert(Obj.Var, variable.getVarName(), new Struct(Struct.Array, currType));
					variable.obj.setLevel(currLevel);
					report_info("Deklarisana promenljiva " + variable.getVarName(), variable);
				} else {
					report_error("Greska - Promenjiva " + variable.getVarName() + " je vec deklarisana ", variable);

				}
			} else { // ako nije Array
				if (obj == Tab.noObj || currLevel != obj.getLevel()) {
					variable.obj = Tab.insert(Obj.Var, variable.getVarName(), currType);
					variable.obj.setLevel(currLevel);
					report_info("Deklarisana promenljiva " + variable.getVarName(), variable);
				} else {
					report_error("Greska - Promenjiva " + variable.getVarName() + " je vec deklarisana ", variable);

				}
			}

		} else {
			report_error("Greska - Tip ne postoji, Promenjiva " + variable.getVarName(), variable);
		}

	}

	public void visit(Sq sq) {
		sq.struct = new Struct(Struct.Array);
		sq.struct.setElementType(currType);
	}

	public void visit(NoSq NoSq) {
		NoSq.struct = currType;
	}
	// ---------------ENUM----------------------

	public void visit(EnumDecl enumDecl) { // na kraju enuma se zatvara scope i ulancavaju ti elementi
		Tab.chainLocalSymbols(enumDecl.getEnumName().obj);
		Tab.closeScope();
		currLevel--;
		enumElem = new LinkedList<Integer>();
		enumPret = -1;

	}

	public void visit(EnumName enumName) { // ovo se prvo nadje pa se tu otvara novi scope gde ce biti svi elementi

		Obj obj = Tab.find(enumName.getEnumName());
		if (obj != Tab.noObj) {
			return;
		} else {
			Struct enumSt = new Struct(Struct.Enum, enumData);
			enumName.obj = Tab.insert(Obj.Type, enumName.getEnumName(), enumSt);
			Tab.openScope();
			currLevel++;
			this.enumName = enumName.getEnumName();
			// report_info("enum name: "+ this.enumName, null);
		}
	}

	public void visit(SingleEnumNum singleEnum) {

		int vrednost = singleEnum.getS().intValue();
		Obj obj = new Obj(Obj.Con, "" + enumName + singleEnum.getName(), Tab.intType, vrednost, currLevel);

		for (int i = 0; i < enumElem.size(); i++) {
			if (enumElem.get(i).intValue() == vrednost) {
				report_error("Greska - U enumeraciji " + enumName + " ponavljanje vrednosti " + vrednost, singleEnum);
				break;
			}
		}
		enumElem.add(vrednost);
		enumPret = vrednost;
		// report_info(""+,null);
		if (enumData.searchKey(enumName + singleEnum.getName()) == null) {
			singleEnum.obj = obj;
			enumData.insertKey(singleEnum.obj);
			report_info("Deklariasana enum konstanta " + enumName + "." + singleEnum.getName() + " = " + vrednost,
					singleEnum);

		} else {
			report_error("Greska - U enumeraciji " + enumName + " ponavljanje imena " + singleEnum.getName(),
					singleEnum);

		}
	}

	public void visit(SingleEnumIdent singleEnum) { // ovo je element bez = NUMCONST treba da ima vrednost pret+1

		Obj obj = new Obj(Obj.Con, "" + enumName + singleEnum.getName(), Tab.intType, ++enumPret, currLevel);

		enumElem.add(enumPret);// dodaje na listu

		if (enumData.searchKey(enumName + singleEnum.getName()) == null) {

			singleEnum.obj = obj;
			enumData.insertKey(singleEnum.obj);
			report_info("Deklariasana enum konstanta " + enumName + "." + singleEnum.getName() + " = " + enumPret,
					singleEnum);

		} else {
			report_error("Greska - U enumeraciji " + enumName + " ponavljanje imena " + singleEnum.getName(),
					singleEnum);

		}

	}

	// -------------METODA----------------------------

	public void visit(MethodDeclaration methodDecl) {
		if (currMethod != null) {
			if (!returnFound && currMethod.getType() != Tab.noType) {
				report_error("Greska - Funkcija " + currMethod.getName() + " nema return iskaz ", methodDecl);
			}
			Tab.chainLocalSymbols(currMethod);
			Tab.closeScope();
			currLevel--;
			returnFound = false;
			currMethod = null;
		} else
			report_error("Greska - Metoda pogresno deklarisana ", methodDecl);
	}

	public void visit(MethodTypeName methodTypeName) {

		Obj obj = Tab.find(methodTypeName.getMethName());
		if (obj == Tab.noObj) {
			currMethod = Tab.insert(Obj.Meth, methodTypeName.getMethName(), currType);
			methodTypeName.obj = currMethod;
			Tab.openScope();
			currLevel++;
			report_info("Obradjuje se funkcija " + methodTypeName.getMethName(), methodTypeName);
		} else {
			report_error("Greska - Metoda " + methodTypeName.getMethName() + " vec postoji ", methodTypeName);
		}
	}

	public void visit(FormParamI formParam) {

		if (Tab.currentScope.findSymbol(formParam.getName()) == null && currMethod != null) {
			if (currMethod.getName().equals("main")) {
				report_error("Greska - Metoda MAIN ne sme da sadrzi formalne argumente ", formParam);
			} else {
				int numFormArgs = currMethod.getLevel();
				currMethod.setLevel(++numFormArgs);

				Obj fP = Tab.insert(Obj.Var, formParam.getName(), currType);
				fP.setFpPos(numFormArgs - 1);
				report_info(
						"Deklarisan je formalni parametar " + formParam.getName() + " u metodi " + currMethod.getName(),
						formParam);
			}
		} else {
			report_error("Greska - Metoda pogresno deklarisana " + formParam.getLine(), formParam);
		}
	}

	public void visit(FormParamIS formParam) {
		Struct tip = formParam.getType().struct;
		if (Tab.currentScope.findSymbol(formParam.getName()) == null) {
			if (currMethod.getName().equals("main")) {
				report_error("Greska - Metoda MAIN ne sme da sadrzi formalne argumente ", formParam);
			} else {
				int numFormArgs = currMethod.getLevel();
				currMethod.setLevel(++numFormArgs);

				Obj fP = Tab.insert(Obj.Var, formParam.getName(), new Struct(Struct.Array, currType));
				fP.setFpPos(numFormArgs - 1);

				report_info("Deklarisan je formalni parametar tipa niz, " + formParam.getName() + " u metodi "
						+ currMethod.getName(), formParam);
			}
		} else {
			report_error("Greska - Parametar pogresno deklarisan ", formParam);
		}
	}

	public void visit(MethodTypes methodType) {
		Obj obj = Tab.find(methodType.getType().getTypeName());
		if (obj != Tab.noObj) {
			if (obj.getKind() != Obj.Type) {
				report_error("Greska - Povratna vrednost metode " + methodType.getType().getTypeName()
						+ " ne predstavlja tip ", methodType);
				currType = Tab.noType;
			} else {
				currType = obj.getType();
			}
		} else
			report_error(
					"Greska - Povratna vrednost metode " + methodType.getType().getTypeName() + " ne predstavlja tip ",
					methodType);
	}

	public void visit(MethodVoid methodVoid) {
		currType = Tab.noType;
	}

	// ----------------DESIGNATOR STATEMENT---------------

	public void visit(DesigAssEx desigAssEx) {
		Designator desig = desigAssEx.getDesignator();
		Expr expr = desigAssEx.getExpr();

		if ((desig.obj == null || expr.struct == null) || (desig.obj.getKind() != Obj.Var
				&& desig.obj.getKind() != Obj.Elem && desig.obj.getKind() != Obj.Fld)) {
			report_error(
					"Greska - Nelegalan izraz, Designator mora biti promenjiva, element niza ili polje unutar objekta ",
					desigAssEx);
		} else {
			// report_info("ASSIGNMENT Tipovi "+ desig.obj.getType().getKind() , null);
			// report_info("ASSIGNMENT Tipovi " + expr.struct.getKind(), null);
			if (desig.obj.getType().getKind() == Struct.Int && expr.struct.getKind() == Struct.Enum) {
				report_info("Uspesno dodeljena vrednost promenjivoj " + desig.obj.getName(), desigAssEx);

			} else if (desig.obj.getType().getKind() == Struct.Array) {
				if (desig.obj.getType().getElemType().getKind() == expr.struct.getKind()
						|| (desig.obj.getType().getElemType().getKind() == Struct.Int
								&& expr.struct.getKind() == Struct.Enum)) {
					report_info("Uspesno dodeljena vrednost promenjivoj " + desig.obj.getName(), desigAssEx);

				} else if (expr.struct.getKind() == Struct.Array) {
					if (desig.obj.getType().getElemType().getKind() != expr.struct.getElemType().getKind()) {
						report_error("Greska - Kod dodele se tipovi ne slazu tip " + desig.obj.getType().getKind()
								+ " i tip " + expr.struct.getKind(), desigAssEx);

					} else
						report_info("Uspesno dodeljena vrednost promenjivoj " + desig.obj.getName(), desigAssEx);
				} else
					report_error("Greska - Kod dodele se tipovi ne slazu tip " + desig.obj.getType().getKind()
							+ " i tip " + expr.struct.getKind(), desigAssEx);

			} else if (expr.struct.getKind() == Struct.Array) {
				if (expr.struct.getElemType().getKind() != desig.obj.getType().getKind()) {
					report_error("Greska - Kod dodele se tipovi ne slazu tip " + desig.obj.getType().getKind()
							+ " i tip " + expr.struct.getKind(), desigAssEx);
				} else {
					report_info("Uspesno dodeljena vrednost promenjivoj " + desig.obj.getName(), desigAssEx);
				}
			} else if (desig.obj.getType().getKind() != Struct.Array
					&& desig.obj.getType().getKind() != expr.struct.getKind()) {
				report_error("Greska - Kod dodele se tipovi ne slazu tip " + desig.obj.getType().getKind() + " i tip "
						+ expr.struct.getKind(), desigAssEx);
			} else
				report_info("Uspesno dodeljena vrednost promenjivoj " + desig.obj.getName(), desigAssEx);
		}

	}

	public void visit(DesigActPars desigActPars) {
		Obj func = desigActPars.getDesignator().obj;
		if (Obj.Meth == func.getKind() &&  func.getLevel() > 0) {
				FuncCallCount++;
				report_info("Pronadjen poziv funkcije " + func.getName(), desigActPars);
			
		} else {
			report_error("Greska - Promenjiva " + func.getName() + " nije funkcija ", desigActPars);
		}
	}

	public void visit(DesigNoActPars desigNoActPars) {
		Obj func = desigNoActPars.getDesignator().obj;
		if (Obj.Meth == func.getKind()) {
			if (func.getLevel() != 0) {
				report_error("Greska - Funkcija " + func.getName() + " zahteva argumente ", desigNoActPars);
			}else {
				FuncCallCount++;
				report_info("Pronadjen poziv funkcije " + func.getName(), desigNoActPars);
			}
		} else {
			report_error("Greska - Promenjiva " + func.getName() + " nije funkcija ", desigNoActPars);
		}
	}

	public void visit(DesigPlusPlus desigPlusPlus) {
		Designator desig = desigPlusPlus.getDesignator();

		if (desig.obj != null && desig.obj.getKind() != Obj.Var && desig.obj.getKind() != Obj.Elem
				&& desig.obj.getKind() != Obj.Fld) {
			report_error("Greska - Designator mora biti promenjiva, element niza ili polje unutar objekta ",
					desigPlusPlus);

		} else if (desig.obj.getType().getKind() == Struct.Array) {
			if (desig.obj.getType().getElemType().getKind() != Struct.Int) {
				report_error("Greska - Designator mora biti int ", desigPlusPlus);
			} else
				report_info("Uspesno inkrementirana vrednost " + desig.obj.getName(), desigPlusPlus);

		} else if (desig.obj.getType().getKind() != Struct.Int) {
			report_error("Greska - Designator mora biti int ", desigPlusPlus);
		} else {
			report_info("Uspesno inkrementirana vrednost " + desig.obj.getName(), desigPlusPlus);
		}

	}

	public void visit(DesigMinusMinus desigMinusMinus) {
		Designator desig = desigMinusMinus.getDesignator();

		if (desig.obj != null && desig.obj.getKind() != Obj.Var && desig.obj.getKind() != Obj.Elem
				&& desig.obj.getKind() != Obj.Fld) {
			report_error("Greska - Designator mora biti promenjiva, element niza ili polje unutar objekta ",
					desigMinusMinus);

		} else if (desig.obj.getType().getKind() == Struct.Array) {
			if (desig.obj.getType().getElemType().getKind() != Struct.Int) {
				report_error("Greska - Designator mora biti int ", desigMinusMinus);
			} else
				report_info("Uspesno dekrementirana vrednost " + desig.obj.getName(), desigMinusMinus);

		} else if (desig.obj.getType().getKind() != Struct.Int) {
			report_error("Greska - Designator mora biti int ", desigMinusMinus);
		} else {
			report_info("Uspesno dekrementirana vrednost " + desig.obj.getName(), desigMinusMinus);
		}

	}

	// ----------------STATEMENT---------------------------------------

	public void visit(ReadStmt readStmt) {
		Designator desig = readStmt.getDesignator();
		// report_info(" funkcija read " + desig.obj.getKind(), readStmt);

		if (desig.obj != null && desig.obj.getKind() != Obj.Var && desig.obj.getKind() != Obj.Elem
				&& desig.obj.getKind() != Obj.Fld)

			report_error("Greska - Designator mora biti promenjiva, element niza ili polje unutar objekta! linija ",
					readStmt);

		else if (desig.obj.getType().getKind() == Struct.Array) {
			if (desig.obj.getType().getElemType().getKind() != Struct.Int
					&& desig.obj.getType().getElemType().getKind() != Struct.Char
					&& desig.obj.getType().getElemType().getKind() != Struct.Bool) {

				report_error("Greska - Designator mora biti promenjiva, element niza ili polje unutar objekta ",
						readStmt);
			} else
				report_info("Uspesno pozvana funkcija read ", readStmt);

		} else if (desig.obj.getType().getKind() != Struct.Int && desig.obj.getType().getKind() != Struct.Char
				&& desig.obj.getType().getKind() != Struct.Bool) {
			report_error("Greska - Designator kod read funkcije mora biti tipa int, char ili bool ", readStmt);

		} else {
			report_info("Uspesno pozvana funkcija read ", readStmt);
		}
	}

	public void visit(PrintStmt printStmt) {
		Expr expr = printStmt.getExpr();

		if (expr.struct.getKind() == Struct.Int || expr.struct.getKind() == Struct.Char
				|| expr.struct.getKind() == Struct.Bool || expr.struct.getKind() == Struct.Enum) {
			report_info("Uspesno pozvana funkcija print ", printStmt);
			PrintCallCount++;
		} else if (expr.struct.getKind() == Struct.Array) {
			if (expr.struct.getElemType().getKind() == Struct.Int || expr.struct.getElemType().getKind() == Struct.Char
					|| expr.struct.getElemType().getKind() == Struct.Bool) {
				report_info("Uspesno pozvana funkcija print ", printStmt);
				PrintCallCount++;
			} else {
				report_error("Greska - Kod Print funkcije expr mora biti tipa int, char ili bool tip = "
						+ expr.struct.getKind(), printStmt);
			}
		} else
			report_error("Greska - Kod Print funkcije expr mora biti tipa int, char ili bool " + expr.struct.getKind(),
					printStmt);

	}
	public void visit(PrintStmtNum printStmt) {
		Expr expr = printStmt.getExpr();

		if (expr.struct.getKind() == Struct.Int || expr.struct.getKind() == Struct.Char
				|| expr.struct.getKind() == Struct.Bool || expr.struct.getKind() == Struct.Enum) {
			report_info("Uspesno pozvana funkcija print ", printStmt);
			PrintCallCount++;
		} else if (expr.struct.getKind() == Struct.Array) {
			if (expr.struct.getElemType().getKind() == Struct.Int || expr.struct.getElemType().getKind() == Struct.Char
					|| expr.struct.getElemType().getKind() == Struct.Bool) {
				report_info("Uspesno pozvana funkcija print ", printStmt);
				PrintCallCount++;
			} else {
				report_error("Greska - Kod Print funkcije expr mora biti tipa int, char ili bool tip = "
						+ expr.struct.getKind(), printStmt);
			}
		} else
			report_error("Greska - Kod Print funkcije expr mora biti tipa int, char ili bool " + expr.struct.getKind(),
					printStmt);

	}

	public void visit(CondFactExprRelExpr condF) {
		// System.out.println("Cond fact" + condF.getExpr().struct.getKind() + " " +
		// condF.getExpr1().struct.getKind());
		if (condF.getExpr().struct.getKind() != condF.getExpr1().struct.getKind()) {
			if (condF.getExpr().struct.getKind() == Struct.Array) {
				if (condF.getExpr().struct.getElemType().getKind() != condF.getExpr1().struct.getKind()) {
					report_error("Greska - nisu isti tipovi kod conditiona", condF);
				}
			} else if (condF.getExpr1().struct.getKind() == Struct.Array) {
				if (condF.getExpr().struct.getKind() != condF.getExpr1().struct.getElemType().getKind()) {
					report_error("Greska - nisu isti tipovi kod conditiona", condF);
				}
			} else if ((condF.getExpr().struct.getKind() == Struct.Int
					&& condF.getExpr1().struct.getKind() == Struct.Enum)
					|| (condF.getExpr().struct.getKind() == Struct.Enum
							&& condF.getExpr1().struct.getKind() == Struct.Int)) {

			} else
				report_error("Greska - nisu isti tipovi kod conditiona", condF);
		} else if (condF.getExpr().struct.getKind() == Struct.Array
				&& condF.getExpr1().struct.getKind() == Struct.Array) {
			if (condF.getExpr().struct.getElemType().getKind() != condF.getExpr1().struct.getElemType().getKind()) {
				report_error("Greska - nisu isti tipovi kod conditiona", condF);
			}
		}
	}

	// BREAK CONTINUE nisu za nivo A
	/*
	 * public void visit(BreakStmt breakStmt) { if (((Statements)
	 * breakStmt.getParent()).getParent().getClass() == MethodDeclaration.class) {
	 * report_error("Greska! BREAK se ne nalazi u petlji! ", null); } else {
	 * report_info("BREAK", null); } }
	 */
	/*
	 * public void visit(ContinueStmt continueStmt) { if (((Statements)
	 * continueStmt.getParent()).getParent().getClass() == MethodDeclaration.class)
	 * { report_error("Greska! CONTINUE se ne nalazi u petlji!", null); } else {
	 * report_info("CONTINUE", null); } }
	 */

	public void visit(ActPars actPars) {
		actArgCount[nested]++;
		// report_info("-ADodat Stvarni argument na listu metode " +
		// methodCalledList[nested] + " linija "
		// + actPars.getLine() + "nested " + nested, null);

		Obj method = Tab.find(methodCalledList[nested]);
		int methodFormArgsCount = method.getLevel();
		int methodActArgsCount = actArgCount[nested];
		if (methodFormArgsCount != methodActArgsCount) {
			report_error("Greska - Razlicit broj formalnih i stvarnih argumenata " + methodCalledList[nested]
					+ " nested " + nested + " formArgCount = " + methodFormArgsCount + " actArgCount = "
					+ methodActArgsCount, actPars);
			return;
		}
		Obj formArgType = null;
		Iterator<Obj> iterator = method.getLocalSymbols().iterator();
		for (int i = 0; i < methodFormArgsCount; i++) {
			formArgType = iterator.next();
			if (formArgType.getFpPos() == methodFormArgsCount - methodActArgsCount) {
				break;
			}
		}
		// report_info("-A form " + formArgType.getType().getKind() + " act " +
		// actPars.getExpr().struct.getKind(), null);
		// report_info("-A form " + formArgType.getName() ,null);
		// report_info("-A form " + formArgType.getFpPos() ,null);

		// Obj formFound = Tab.find(formArgType.getName());

		if (formArgType.getType().getKind() == actPars.getExpr().struct.getKind()) {

			if (formArgType.getType().getKind() == Struct.Array && actPars.getExpr().struct.getKind() == Struct.Array) {
				// report_info("-E form " + formArgType.getType().getElemType().getKind()
				// ,null);
				// report_info("-E form " + actPars.getExpr().struct.getElemType().getKind()
				// ,null);
				if (formArgType.getType().getElemType().getKind() != actPars.getExpr().struct.getElemType().getKind()) {
					report_error("Greska - Tipovi podataka stvarnih i formalnih argumenta se ne poklapaju "
							+ " formalni = " + formArgType.getType().getElemType().getKind() + " stvarni = "
							+ actPars.getExpr().struct.getElemType().getKind(), actPars);

				}
			}
		} else if (actPars.getExpr().struct.getKind() == Struct.Array) {
			if (formArgType.getType().getKind() != actPars.getExpr().struct.getElemType().getKind()) {
				report_error("Greska - Tipovi podataka kod stvarnih i formalnih argumenta se ne poklapaju "
						+ " stvarni = " + actPars.getExpr().struct.getElemType().getKind() + " formalni = "
						+ formArgType.getType().getKind(), actPars);
			}

		} else if (formArgType.getType().getKind() == Struct.Int && actPars.getExpr().struct.getKind() == Struct.Enum) {

		} else
			report_error(
					"Greska - Tipovi podataka kod stvarnih i formalnih argumenta se ne poklapaju, stvarni = "
							+ actPars.getExpr().struct.getKind() + " formalni = " + formArgType.getType().getKind(),
					actPars);

		methodCalledList[nested] = null;
		actArgCount[nested] = 0;
		nested--;
		if (nested < 0)
			nested = 0;// ovo ne bi trebalo da se desi ali da ne bi iskakao null exeption

	}

	public void visit(Expresions expresions) {
		// Obj method = Tab.find(methodCalled);
		actArgCount[nested]++;
		// report_info("-EDodat Stvarni argument na listu metode " +
		// methodCalledList[nested] + " linija "
		// + expresions.getLine() + "nested " + nested, null);
		Obj method = Tab.find(methodCalledList[nested]);
		int methodFormArgsCount = method.getLevel();
		int methodActArgsCount = actArgCount[nested];

		if (methodFormArgsCount < methodActArgsCount) {
			report_error("Greska - Broj stvarnih argumenata je veci od broja formalnih " + methodCalledList[nested]
					+ " nested " + nested + " formArgCount =  " + methodFormArgsCount + " actArgCount "
					+ methodActArgsCount, expresions);
		}
		Obj formArgType = null;
		Iterator<Obj> iterator = method.getLocalSymbols().iterator();
		for (int i = 0; i < methodFormArgsCount; i++) {
			formArgType = iterator.next();
			// report_info("Pre if" + formArgType.getFpPos() ,null);
			if (formArgType.getFpPos() == methodActArgsCount) {

				break;
			}
		}
		// report_info("-E form " + formArgType.getType().getKind() + " act " +
		// expresions.getExpr().struct.getKind()+ "methodActArgsCount = "+
		// methodActArgsCount+" methodFormArgsCount "+methodFormArgsCount, null);
		// report_info("-E form " + formArgType.getName() ,null);
		// report_info("-E form " + formArgType.getFpPos() ,null);

		if (formArgType.getType().getKind() == expresions.getExpr().struct.getKind()) {

			if (formArgType.getType().getKind() == Struct.Array
					&& expresions.getExpr().struct.getKind() == Struct.Array) {
				// report_info("-E form " + formArgType.getType().getElemType().getKind()
				// ,null);
				// report_info("-E form " + actPars.getExpr().struct.getElemType().getKind()
				// ,null);
				if (formArgType.getType().getElemType().getKind() != expresions.getExpr().struct.getElemType()
						.getKind()) {
					report_error("Greska - Tipovi podataka stvarnih i formalnih argumenta se ne poklapaju "
							+ " formalni = " + formArgType.getType().getElemType().getKind() + " stvarni = "
							+ expresions.getExpr().struct.getElemType().getKind(), expresions);

				}
			}
		} else if (expresions.getExpr().struct.getKind() == Struct.Array) {
			if (formArgType.getType().getKind() != expresions.getExpr().struct.getElemType().getKind()) {
				report_error("Greska - Tipovi podataka kod stvarnih i formalnih argumenta se ne poklapaju "
						+ " stvarni = " + expresions.getExpr().struct.getElemType().getKind() + " formalni = "
						+ formArgType.getType().getKind(), expresions);
			}

		} else if (formArgType.getType().getKind() == Struct.Int
				&& expresions.getExpr().struct.getKind() == Struct.Enum) {

		} else
			report_error(
					"Greska - Tipovi podataka kod stvarnih i formalnih argumenta se ne poklapaju, stvarni = "
							+ expresions.getExpr().struct.getKind() + " formalni = " + formArgType.getType().getKind(),
					expresions);

	}

	// --------------------EXPRESSION---------------------------------

	public void visit(AddExpr addExpr) { // Expr = Expr:e Addop Term:t

		Struct expr = addExpr.getExpr().struct;
		Struct term = addExpr.getTerm().struct;

		// ako su expr i term tipa array
		if (expr == null || term == null) {
			report_error("Greska - Nelegalan izraz ", addExpr);
		} else if (term.getKind() == Struct.Array && expr.getKind() == Struct.Array) {
			if (term.getElemType().getKind() != Struct.Int || expr.getElemType().getKind() != Struct.Int) {
				report_error("Greska - Tipovi podataka kod sabiranja i oduzimanja moraju biti tipa INT ", addExpr);
				addExpr.struct = Tab.noType;
			} else {// ako su oba array i int na dalje su array int
				addExpr.struct = new Struct(Struct.Array, new Struct(Struct.Int));
			}

		} else if (term.getKind() == Struct.Array) {
			if ((term.getElemType().getKind() != Struct.Int && term.getElemType().getKind() != Struct.Enum)
					|| (expr.getKind() != Struct.Int && expr.getKind() != Struct.Enum)) {
				report_error("Greska - Tipovi podataka kod sabiranja i oduzimanja moraju biti tipa INT " + " expr = "
						+ expr.getKind() + " term = " + term.getKind(), addExpr);
				addExpr.struct = Tab.noType;
			} else
				addExpr.struct = Tab.intType;

		} else if (expr.getKind() == Struct.Array) {
			if ((expr.getElemType().getKind() != Struct.Int && expr.getElemType().getKind() != Struct.Enum)
					|| (term.getKind() != Struct.Int && term.getKind() != Struct.Enum)) {
				report_error("Greska - Tipovi podataka kod sabiranja i oduzimanja moraju biti tipa INT " + " expr = "
						+ expr.getKind() + " term = " + term.getKind(), addExpr);
				addExpr.struct = Tab.noType;
			} else
				addExpr.struct = Tab.intType;

		} else if ((expr.getKind() == Struct.Enum && term.getKind() == Struct.Int)
				|| (expr.getKind() == Struct.Int && term.getKind() == Struct.Enum)
				|| (expr.getKind() == Struct.Enum && term.getKind() == Struct.Enum)
				) {
			addExpr.struct = Tab.intType;

		} else if ((term != null && term.getKind() != Struct.Int) || (expr.getKind() != Struct.Int)) {
			report_error("Greska - Tipovi podataka kod sabiranja i oduzimanja moraju biti tipa INT " + " expr = "
					+ expr.getKind() + " term = " + term.getKind(), addExpr);
			addExpr.struct = Tab.noType;
		} else {
			addExpr.struct = Tab.intType;
		}
	}

	public void visit(TermExpr termExpr) { // Expr = Term:t
		Struct term = termExpr.getTerm().struct;
		if (term != null && term.getKind() == Struct.Array) {
			termExpr.struct = new Struct(Struct.Array, new Struct(term.getElemType().getKind()));
		} else if (term != null) {
			termExpr.struct = term;
		}

	}

	public void visit(MinusTerm minusTerm) { // Expr = Minus Term:t
		Struct term = minusTerm.getTerm().struct;
		if (term != null) {
			if (term.getKind() == Struct.Array && term.getElemType().getKind() == Struct.Int) {
				minusTerm.struct = new Struct(Struct.Array, new Struct(term.getElemType().getKind()));
			} else if (term.getKind() == Struct.Int) {
				minusTerm.struct = Tab.intType;
			} else {
				report_error("Greska - Tipovi podataka kod (minus Term) moraju biti tipa INT ", minusTerm);
			}

		}
	}

	public void visit(TermMul termMul) { // Term = Term:t Mulop Factor:f
		Struct term = termMul.getTerm().struct;
		Struct factor = termMul.getFactor().struct;

		if (term != null && factor != null) {
			if (term == Tab.intType && factor == Tab.intType) {
				termMul.struct = Tab.intType;
			} else if (term.getKind() == Struct.Array && term.getElemType().getKind() == Struct.Int
					&& factor == Tab.intType) {
				termMul.struct = Tab.intType;
			} else if (factor.getKind() == Struct.Array && factor.getElemType().getKind() == Struct.Int
					&& term == Tab.intType) {
				termMul.struct = Tab.intType;
			} else if (factor.getKind() == Struct.Array && factor.getElemType().getKind() == Struct.Int
					&& term.getKind() == Struct.Array && term.getElemType().getKind() == Struct.Int) {
				termMul.struct = Tab.intType;
			} else if (factor.getKind() == Struct.Int && term.getKind() == Struct.Enum) {
				termMul.struct = Tab.intType;
			} else if (factor.getKind() == Struct.Enum && term.getKind() == Struct.Int) {
				termMul.struct = Tab.intType;
			} else if (factor.getKind() == Struct.Enum && term.getKind() == Struct.Enum) {
				termMul.struct = Tab.intType;
			} else{
				report_error("Greska - Nekompatibilni tipovi u izrazu za mnozenje term =" + term.getKind()
						+ " factor = " + factor.getKind(), termMul);
				termMul.struct = Tab.noType;
			}
		}

	}

	public void visit(TermFactor termFactor) { // Term = Factor:f
		termFactor.struct = termFactor.getFactor().struct;
	}

	// -------------------------FACTOR---------------------------

	public void visit(FactorFunc factorFunc) {

		Obj func = factorFunc.getDesignator().obj;
		if (Obj.Meth == func.getKind()) {
			if (func.getType() == Tab.noType) {
				report_error("Greska - Funkcija je tipa void i ne sme biti u izrazu ", factorFunc);
			} else {
				FuncCallCount++;
				report_info("Pronadjen poziv funkcije " + func.getName(), factorFunc);
				factorFunc.struct = func.getType();
			}
		} else {
			report_error("Greska - Promenljiva " + func.getName() + " nije funkcija ", factorFunc);
			factorFunc.struct = Tab.noType;
		}

	}

	public void visit(FactorFuncBrace factorFuncBrace) {

		Obj func = factorFuncBrace.getDesignator().obj;
		if (Obj.Meth == func.getKind()) {
			if (Tab.find(func.getName()).getLevel() > 0) {
				report_error("Greska - Funkcija nema argumente funkcija = " + func.getName(), factorFuncBrace);
			} else if (func.getType() == Tab.noType) {
				report_error("Greska - Funkcija je tipa void i ne sme biti u izrazu ", factorFuncBrace);
			} else {
				FuncCallCount++;
				report_info("Pronadjen poziv funkcije " + func.getName(), factorFuncBrace);
				factorFuncBrace.struct = func.getType();
			}
		} else {
			report_error("Greska - Promenljiva " + func.getName() + " nije funkcija!", factorFuncBrace);
			factorFuncBrace.struct = Tab.noType;
		}

	}

	public void visit(FactorFuncDesig factorDesig) {
		factorDesig.struct = factorDesig.getDesignator().obj.getType();
	}

	public void visit(FactorBraceExpr factorBraceExpr) {
		factorBraceExpr.struct = factorBraceExpr.getExpr().struct;
	}

	public void visit(FactorNum factorNum) {
		factorNum.struct = Tab.intType;
	}

	public void visit(FactorChar factorChar) {
		factorChar.struct = Tab.charType;
	}

	public void visit(FactorBool factorBool) {
		factorBool.struct = boolType;
	}

	public void visit(FactorNewExpr factorNewExpr) {

		Struct type = factorNewExpr.getType().struct;
		Struct expr = factorNewExpr.getExpr().struct;

		if (type != null && expr != null) {

			if (expr.getKind() != Struct.Int && expr.getKind() != Struct.Enum) {
				if (expr.getKind() == Struct.Array) {
					if (expr.getElemType().getKind() == Struct.Int) {
						factorNewExpr.struct = new Struct(Struct.Array, currType);
					} else {
						report_error("Greska - Pgresan tip kod operatora NEW (Expr)", factorNewExpr);
						factorNewExpr.struct = Tab.noType;
					}
				} else {
					report_error("Greska - Pogresan tip kod operatora NEW (Expr)", factorNewExpr);
					factorNewExpr.struct = Tab.noType;
				}
			} else {
				factorNewExpr.struct = new Struct(Struct.Array, currType);
			}
		}

	}

	public void visit(FactorNew factorNew) {
		Struct type = factorNew.getType().struct;
		if (type != null) {
			if (type.getKind() != Struct.Class) {
				report_error("Greska - Pogresan tip kod operatora NEW ", factorNew);
				factorNew.struct = Tab.noType;
			} else {
				factorNew.struct = type;
			}
		}
	}

	public void visit(Designator designator) {
		Obj obj = Tab.find(designator.getName());
		if (designator.getDesignatorList() instanceof DesignatorArray) {
			// report_info("DESIGNATOR ARRAY
			// "+designator.getDesignatorList().struct.getKind(), null);
			String arrayN = designator.getName();
			DesignatorArray a = (DesignatorArray) (designator.getDesignatorList());
			Obj aObj = Tab.find(arrayN);
			if (a.struct.getKind() != Struct.Array) {
				report_error("Greska - Promenljiva " + arrayN + " nije array tipa ", designator);
			} else if (a.getExpr().struct.getKind() != Struct.Int && a.getExpr().struct.getKind() != Struct.Enum) {
				if (a.getExpr().struct.getKind() == Struct.Array) {
					if (a.getExpr().struct.getElemType().getKind() != Struct.Int
							&& a.getExpr().struct.getElemType().getKind() != Struct.Enum)
						report_error("Greska - Indeks kod pozivanja elementa niza mora biti int tipa ", designator);
				} else
					report_error("Greska - Indeks kod pozivanja elementa niza mora biti int tipa", designator);

			} else {
				designator.obj = new Obj(Obj.Elem, arrayN, aObj.getType().getElemType());
				// report_info("DESIGNATOR ARRAY array "+arrayN + " tip elem
				// "+aObj.getType().getElemType().getKind(),null);
			}

		} else if (designator.getDesignatorList() instanceof DesignatorEnum) {
			// report_info("DESIGNATOR ENUM
			// "+designator.getDesignatorList().struct.getKind(), null);
			String enumN = designator.getName();
			if (enumN.equals("this")) {
				report_error("Greska - Globalne funkcije ne smeju imati parametar this ", designator);
			} else {
				DesignatorEnum e = (DesignatorEnum) (designator.getDesignatorList());
				String elemN = e.getName();

				Obj eObj = Tab.find(enumN);
				// report_info("DESIGNATOR ENUM "+eObj.getType().getKind(), null);
				if (eObj != null && eObj.getType().getKind() != Struct.Enum) {
					report_error("Greska - Promenljiva " + enumN + " nije enum tipa ", designator);

				} else if (eObj.getType().getMembersTable().searchKey("" + enumN + elemN) != null) {
					designator.obj = new Obj(Obj.Con, "", Tab.intType);
					// report_info("DESIGNATOR ENUM pronadjen "+enumN+elemN , null);
				}
			}
		} else if (obj == Tab.noObj) {
			report_error("Greska - Promenljiva " + designator.getName() + " nije deklarisana ", designator);
		} else if (obj.getKind() == Obj.Meth) {
			nested++;
			methodCalledList[nested] = designator.getName();
			actArgCount[nested] = 0;

			// report_info("--Postavljeno methodCalled " + methodCalledList[nested] +
			// "nested = " + nested, null);
		}
		// System.out.println("No desig list "+obj.getKind()+" "+obj.getName());
		designator.obj = obj;
	}

	public void visit(DesignatorArray desigArr) {
		desigArr.struct = new Struct(Struct.Array);
		ArrayElemCount++;
	}

	public void visit(DesignatorEnum desigEnum) {

		desigEnum.struct = new Struct(Struct.Enum);
	}

	public void visit(RetExprStmt returnExpr) {
		returnFound = true;

		if (currMethod != null) {

			Struct currMethodType = currMethod.getType();
			// report_info("currmetod type = " + currMethod.getType().getKind(), null);
			// report_info("currmetod return = " + returnExpr.getExpr().struct.getKind(),
			// null);
			if (currMethodType.getKind() != returnExpr.getExpr().struct.getKind()) {
				if (returnExpr.getExpr().struct.getKind() == Struct.Array) {
					if (returnExpr.getExpr().struct.getElemType().getKind() != currMethodType.getKind()) {

						report_error("Greska - Tip izraza u return naredbi ne slaze se sa tipom povratne vrednosti funkcije "
								+ currMethod.getName() + " ret " + returnExpr.getExpr().struct.getKind() + " meth "
								+ currMethodType.getKind(), returnExpr);
					}

				} else
					report_error("Greska - Tip izraza u return naredbi ne slaze se sa tipom povratne vrednosti funkcije "
							+ currMethod.getName() + " ret " + returnExpr.getExpr().struct.getKind() + " meth "
							+ currMethodType.getKind(), returnExpr);

			}
		} else
			report_error("Greska - Metoda pogresno deklarisana", returnExpr);

	}

}
