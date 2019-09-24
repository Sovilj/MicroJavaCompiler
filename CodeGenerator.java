package rs.ac.bg.etf.pp1;

import rs.ac.bg.etf.pp1.CounterVisitor.FormParamCounter;
import rs.ac.bg.etf.pp1.CounterVisitor.VarCounter;
import rs.ac.bg.etf.pp1.ast.*;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.Tab;
import rs.etf.pp1.symboltable.concepts.Obj;
import rs.etf.pp1.symboltable.concepts.Struct;

public class CodeGenerator extends VisitorAdaptor {

	private int mainPc;

	public int getMainPc() {
		return mainPc;
	}

	public void visit(MethodTypeName methodName) {

		if ("main".equalsIgnoreCase(methodName.getMethName())) {
			mainPc = Code.pc;
		}
		methodName.obj.setAdr(Code.pc);
		// Collect arguments and local variables
		SyntaxNode methodNode = methodName.getParent();

		VarCounter varCnt = new VarCounter();
		methodNode.traverseTopDown(varCnt);

		FormParamCounter fpCnt = new FormParamCounter();
		methodNode.traverseTopDown(fpCnt);

		// Generate the entry
		Code.put(Code.enter);
		Code.put(fpCnt.getCount());
		Code.put( methodName.obj.getLocalSymbols().size() );
		// fpCnt.getCount() + methodName.obj.getLocalSymbols().size()fpCnt.getCount() +
	}

	public void visit(MethodDeclaration MethodDecl) {
		Code.put(Code.exit);
		Code.put(Code.return_);
	}

	public void visit(DesigAssEx desigAssExpr) {
		Designator desig = desigAssExpr.getDesignator();

		if (desig.getDesignatorList().getClass() == DesignatorArray.class) {
			// DesignatorArray da = (DesignatorArray)desig.getDesignatorList();
			// System.out.println("DesigAss "+desig.obj.getName());
			if (desig.obj.getType().getElemType().getKind() == Struct.Int
					|| desig.obj.getType().getElemType().getKind() == Struct.Bool) {
				Code.put(Code.astore);
			//	System.out.println("DesigAss "+desig.obj.getName());
			//	System.out.println("DesigAss expr "+desigAssExpr.getExpr().struct.getKind());
			}
			if (desig.obj.getType().getElemType().getKind() == Struct.Char) {
				Code.put(Code.bastore);
			}
		} else
			Code.store(desig.obj);

	}

	public void visit(DesigActPars desigActPars) {
		Obj funcCall = desigActPars.getDesignator().obj;
		int offset = funcCall.getAdr() - Code.pc;
		Code.put(Code.call);
		Code.put2(offset);
	//	System.out.println("Ofsset "+offset);
	//	System.out.println("Func "+funcCall.getName());
	}

	public void visit(DesigNoActPars desigNoActPars) {
		Obj funcCall = desigNoActPars.getDesignator().obj;
		int offset = funcCall.getAdr() - Code.pc;
		Code.put(Code.call);
		Code.put2(offset);
	}
	/*public void visit(RetExprStmt retExprStmt) {
		Code.put(Code.exit);
		Code.put(Code.return_);
	}
	public void visit(RetStmt retStmt) {
		Code.put(Code.exit);
		Code.put(Code.return_);
	}
	
	*/

	public void visit(DesigPlusPlus desigPlusPlus) {

		Designator desig = desigPlusPlus.getDesignator();
		if (desig.getDesignatorList() instanceof DesignatorArray) {
			if (desig.obj.getType().getElemType().getKind() == Struct.Int
					|| desig.obj.getType().getElemType().getKind() == Struct.Bool
					|| desig.obj.getType().getElemType().getKind() == Struct.Enum) {
				desig.traverseBottomUp(this);
				Code.put(Code.aload);
				Code.loadConst(1);
				Code.put(Code.add);
				Code.put(Code.astore);
			}
			if (desig.obj.getType().getElemType().getKind() == Struct.Char) {
				desig.traverseBottomUp(this);
				Code.put(Code.baload);
				Code.loadConst(1);
				Code.put(Code.add);
				Code.put(Code.bastore);
			}
		} else {
			Code.load(desig.obj);
			Code.loadConst(1);
			Code.put(Code.add);
			Code.store(desig.obj);
		}

	}

	public void visit(DesigMinusMinus desigMinusMinus) {
		Designator desig = desigMinusMinus.getDesignator();
		if (desig.getDesignatorList() instanceof DesignatorArray) {
			if (desig.obj.getType().getElemType().getKind() == Struct.Int
					|| desig.obj.getType().getElemType().getKind() == Struct.Bool
					|| desig.obj.getType().getElemType().getKind() == Struct.Enum) {
				desig.traverseBottomUp(this);
				Code.put(Code.aload);
				Code.loadConst(1);
				Code.put(Code.sub);
				Code.put(Code.astore);
			}
			if (desig.obj.getType().getElemType().getKind() == Struct.Char) {
				desig.traverseBottomUp(this);
				Code.put(Code.baload);
				Code.loadConst(1);
				Code.put(Code.sub);
				Code.put(Code.bastore);
			}
		} else {
			Code.load(desig.obj);
			Code.loadConst(1);
			Code.put(Code.sub);
			Code.store(desig.obj);
		}
	}

	public void visit(Designator designator) {
		if (designator.getDesignatorList() instanceof NoDesignatorList) {
		//	System.out.println("Designator "+designator.getDesignatorList());
			SyntaxNode parent = designator.getParent();
			
		//	System.out.println("Designator "+parent);
			if (parent.getClass() != FactorFunc.class && parent.getClass() != FactorFuncDesig.class
					&& parent.getClass() != FactorFuncBrace.class && parent.getClass() != DesigActPars.class
							&& parent.getClass() != DesigAssEx.class
					&& parent.getClass() != DesigNoActPars.class && parent.getClass() != DesigPlusPlus.class
					&& parent.getClass() != DesigMinusMinus.class && parent.getClass() != DesignatorEnum.class) {
				Code.load(designator.obj);
			//	System.out.println("LOAD not array or enum: "+designator.getName());
			}
		}if (designator.getDesignatorList() instanceof DesignatorArray) {
	
			//System.out.println("Designator array load " + designator.getName());
			Code.load(designator.obj);
			Code.put(Code.dup_x1);
			Code.put(Code.pop);
		}
		
	}

	public void visit(FactorFunc factorFunc) {
		Designator d = factorFunc.getDesignator();
		//System.out.println("Factor func "+d.getName());
		if (d.getDesignatorList() instanceof DesignatorArray) {
			if (d.obj.getType().getElemType().getKind() == Struct.Int
					|| d.obj.getType().getElemType().getKind() == Struct.Bool
					|| d.obj.getType().getElemType().getKind() == Struct.Enum) {
				Code.put(Code.aload);
			}
			if (d.obj.getType().getElemType().getKind() == Struct.Char) {
				Code.put(Code.baload);
			}

		} else if (d.getDesignatorList() instanceof DesignatorEnum) {
			if (d.obj.getType().getKind() == Struct.Enum) {
				int enumVal = 0;
				String enumName = d.getName();
				DesignatorEnum de = (DesignatorEnum) d.getDesignatorList();
				String elemN = de.getName();
				
				enumVal = d.obj.getType().getMembersTable().searchKey("" + enumName + elemN).getAdr();
				//System.out.println(""+enumName+elemN+ "= "+enumVal);
				
				Obj obj = new Obj(Obj.Con, "$", Tab.intType,enumVal,0);
				Code.load(obj);
			}

		} else {
			if (d.obj.getKind() == Obj.Meth) {
				int dest_adr = d.obj.getAdr() - Code.pc;
				Code.put(Code.call);
				Code.put2(dest_adr);
				//System.out.println("Ofsset "+dest_adr);
				//System.out.println("Func "+d.obj.getName());
			}
		}
			
	}

	public void visit(FactorFuncDesig factorFuncDesig) {
		Designator d = factorFuncDesig.getDesignator();
		//System.out.println("Factor funcDesig "+d.getName());
		if (d.getDesignatorList() instanceof DesignatorArray) {
			if (d.obj.getType().getElemType().getKind() == Struct.Int
					|| d.obj.getType().getElemType().getKind() == Struct.Bool
					|| d.obj.getType().getElemType().getKind() == Struct.Enum) {
				Code.put(Code.aload);
			}
			if (d.obj.getType().getElemType().getKind() == Struct.Char) {
				Code.put(Code.baload);
			}

		} else if (d.getDesignatorList() instanceof DesignatorEnum) {
			if (d.obj.getType().getKind() == Struct.Enum) {
				int enumVal = 0;
				
				String enumName = d.getName();
				DesignatorEnum de = (DesignatorEnum) d.getDesignatorList();
				String elemN = de.getName();
				
				enumVal = d.obj.getType().getMembersTable().searchKey("" + enumName + elemN).getAdr();
				// System.out.println(""+enumName+elemN+ "= "+enumVal);
				
				Obj obj = new Obj(Obj.Con, "$", Tab.intType,enumVal,0);
				Code.load(obj);
			}

		} else
			Code.load(d.obj);
	}

	public void visit(FactorFuncBrace factorFuncBrace) {
		
		Designator d = factorFuncBrace.getDesignator();
		//System.out.println("Factor funcbrace "+d.getName());
		if (d.getDesignatorList() instanceof DesignatorArray) {
			if (d.obj.getType().getElemType().getKind() == Struct.Int
					|| d.obj.getType().getElemType().getKind() == Struct.Bool
					|| d.obj.getType().getElemType().getKind() == Struct.Enum) {
				Code.put(Code.aload);
			}
			if (d.obj.getType().getElemType().getKind() == Struct.Char) {
				Code.put(Code.baload);
			}

		} else if (d.getDesignatorList() instanceof DesignatorEnum) {
			if (d.obj.getType().getKind() == Struct.Enum) {
				int enumVal = 0;
				String enumName = d.getName();
				DesignatorEnum de = (DesignatorEnum) d.getDesignatorList();
				String elemN = d.getName();
				Obj eObj = Tab.find(enumName);

				enumVal = eObj.getType().getMembersTable().searchKey("" + enumName + elemN).getAdr();
				Obj obj = new Obj(Obj.Con, "$", Tab.intType,enumVal,0);

				Code.load(obj);
			}

		} else
			Code.load(d.obj);
	}

	public void visit(PrintStmt printStmt) {
		if (printStmt.getExpr().struct == Tab.intType) {
			Code.loadConst(5);
			Code.put(Code.print);
		} else if (printStmt.getExpr().struct == Tab.charType) {
			Code.loadConst(1);
			Code.put(Code.bprint);
		} else if (printStmt.getExpr().struct.getKind() == SemanticAnalizer.boolType.getKind()) {
			Code.loadConst(5);
			Code.put(Code.print);
		}else if (printStmt.getExpr().struct.getKind() == Struct.Array) {
			
			if (printStmt.getExpr().struct.getElemType().getKind() == Struct.Int) {
				Code.loadConst(5);
				Code.put(Code.print);
			} else if (printStmt.getExpr().struct.getElemType().getKind() == Struct.Char) {
				Code.loadConst(1);
				Code.put(Code.bprint);
			} else if (printStmt.getExpr().struct.getElemType().getKind() == Struct.Bool) {
				Code.loadConst(5);
				Code.put(Code.print);
			}
		}else if (printStmt.getExpr().struct.getKind() == Struct.Enum) {
			Code.loadConst(5);
			Code.put(Code.print);
		}
		
	}
	public void visit(PrintStmtNum printStmt) {
		int width = printStmt.getN2();
		if (printStmt.getExpr().struct == Tab.intType) {
			Code.loadConst(width);
			Code.put(Code.print);
		} else if (printStmt.getExpr().struct == Tab.charType) {
			Code.loadConst(width);
			Code.put(Code.bprint);
		} else if (printStmt.getExpr().struct.getKind() == SemanticAnalizer.boolType.getKind()) {
			Code.loadConst(width);
			Code.put(Code.print);
		}else if (printStmt.getExpr().struct.getKind() == Struct.Array) {
			
			if (printStmt.getExpr().struct.getElemType().getKind() == Struct.Int) {
				Code.loadConst(width);
				Code.put(Code.print);
			} else if (printStmt.getExpr().struct.getElemType().getKind() == Struct.Char) {
				Code.loadConst(width);
				Code.put(Code.bprint);
			} else if (printStmt.getExpr().struct.getElemType().getKind() == Struct.Bool) {
				Code.loadConst(width);
				Code.put(Code.print);
			}
		}else if (printStmt.getExpr().struct.getKind() == Struct.Enum) {
			Code.loadConst(width);
			Code.put(Code.print);
		}
		
	}

	public void visit(ReadStmt readStmt) {
		Designator d = readStmt.getDesignator();

		if (d.getDesignatorList() instanceof DesignatorArray) {

			if (d.obj.getType().getElemType().getKind() == Struct.Int
					|| d.obj.getType().getElemType().getKind() == Struct.Bool) {
				Code.put(Code.read);
				Code.store(d.obj);
			} else if (d.obj.getType().getElemType().getKind() == Struct.Char) {
				Code.put(Code.bread);
				Code.put(Code.bastore);
			}

		} else {
			if (d.obj.getType().getKind() == Struct.Int || d.obj.getType().getElemType().getKind() == Struct.Bool) {
				Code.put(Code.read);
				Code.store(d.obj);
				Code.put(Code.pop);
			} else if (d.obj.getType().getElemType().getKind() == Struct.Char) {
				Code.put(Code.bread);
				Code.put(d.hashCode());
				Code.put(Code.pop);
			}
		}
	}

	public void visit(FactorNum factorNum) {
		Obj numConst = Tab.insert(Obj.Con, "$", Tab.intType);
		numConst.setLevel(0);
		numConst.setAdr(factorNum.getN1());
		Code.load(numConst);
	}

	public void visit(FactorChar factorChar) {
		Obj charConst = Tab.insert(Obj.Con, "$", Tab.charType);
		charConst.setLevel(0);
		charConst.setAdr(factorChar.getC1());
		Code.load(charConst);
	}

	public void visit(FactorBool factorBool) {
		Obj boolConst = Tab.insert(Obj.Con, "$", SemanticAnalizer.boolType);
		boolConst.setLevel(0);
		boolConst.setAdr(factorBool.getB1().equals("true") ? 1 : 0);
		Code.load(boolConst);
	}

	public void visit(FactorNewExpr factorNewExpr) {
		Code.put(Code.newarray);
		if (factorNewExpr.getType().struct == Tab.intType)
			Code.put(1);
		if (factorNewExpr.getType().struct == Tab.charType)
			Code.put(0);
		if (factorNewExpr.getType().struct.getKind() == Struct.Bool) {
			Code.put(1);
		}
	}

	/*public void visit(FactorNew factorNew) {
		Code.put(Code.newarray);
		if (factorNew.getType().struct.getKind() == Struct.Int)
			Code.put(1);
		if (factorNew.getType().struct.getKind() == Struct.Char)
			Code.put(0);
	
	}*/

	public void visit(AddExpr addExpr) {
		if (addExpr.getAddop() instanceof AddopP)
			Code.put(Code.add);
		else if (addExpr.getAddop() instanceof AddopM)
			Code.put(Code.sub);
	}

	public void visit(MinusTerm minusTerm) {
		Code.put(Code.neg);

	}

	public void visit(TermMul termMul) {
		if (termMul.getMulop() instanceof MulopM)
			Code.put(Code.mul);
		else if (termMul.getMulop() instanceof MulopD)
			Code.put(Code.div);
		else if (termMul.getMulop() instanceof MulopP)
			Code.put(Code.rem);
	}

}
