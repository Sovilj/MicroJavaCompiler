package rs.ac.bg.etf.pp1;

import rs.ac.bg.etf.pp1.ast.FormParamI;
import rs.ac.bg.etf.pp1.ast.FormParamIS;
import rs.ac.bg.etf.pp1.ast.VarDecl;
import rs.ac.bg.etf.pp1.ast.VisitorAdaptor;

public class CounterVisitor extends VisitorAdaptor {

	protected int count;
	
	public int getCount() {
		return count;
	}
	public static class FormParamCounter extends CounterVisitor{
		
		public void visit (FormParamI formParam) {
			count++;
		}
		public void visit (FormParamIS formParam) {
			count++;
		}
	}
	public static class VarCounter extends CounterVisitor{
		
		public void visit (VarDecl varDecl) {
			count++;
		}

	}
}
