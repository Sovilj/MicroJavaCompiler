package rs.ac.bg.etf.pp1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Reader;

import java_cup.runtime.Symbol;

import org.apache.log4j.Logger;
import org.apache.log4j.xml.DOMConfigurator;

import rs.ac.bg.etf.pp1.ast.Program;
import rs.ac.bg.etf.pp1.util.Log4JUtils;
import rs.etf.pp1.mj.runtime.Code;
import rs.etf.pp1.symboltable.*;
import rs.etf.pp1.symboltable.concepts.Scope;
import rs.etf.pp1.symboltable.visitors.DumpSymbolTableVisitor;
import rs.etf.pp1.symboltable.visitors.SymbolTableVisitor;

public class Compiler {

	static {
		DOMConfigurator.configure(Log4JUtils.instance().findLoggerConfigFile());
		Log4JUtils.instance().prepareLogFile(Logger.getRootLogger());
	}

	public static void main(String[] args) throws Exception {

		Logger log = Logger.getLogger(Compiler.class);

		Reader br = null;
		try {
			String fileIN, fileOUT;
			if (args.length == 2) {
				fileIN = args[0];
				fileOUT = args[1];
			} else {
				fileIN = "test/program.mj";
				fileOUT = "test/program.obj";
			}
			File sourceCode = new File(fileIN);

			PrintStream console = new PrintStream("test/izlaz.out");
			System.setOut(console);
			PrintStream consolerr = new PrintStream("test/izlaz.err");
			System.setErr(consolerr);

			log.info("Compiling source file: " + sourceCode.getAbsolutePath());

			br = new BufferedReader(new FileReader(sourceCode));
			Yylex lexer = new Yylex(br);

			MJParser p = new MJParser(lexer);
			Symbol s = p.parse(); // pocetak parsiranja

			Program prog = (Program) (s.value);

			if (!p.errorDetected) {
				log.info("-------------PARSIRANJE JE USPESNO ZAVRSENO------------");
				// ispis sintaksnog stabla
				// log.info(prog.toString(""));
			} else {
				log.error("-----------Parsiranje NIJE uspesno zavrseno-----------");
			}

			log.info("\n=======================SEMANTIKA=======================");

			// ispis prepoznatih programskih konstrukcija
			Tab.init();
			// ExtendedTab.init(); // da bi dodali bool i enum
			SemanticAnalizer v = new SemanticAnalizer();
			prog.traverseBottomUp(v);

			log.info("\n===================ZAVRSENA SEMANTIKA==================");

			log.info(" Deklarisanih Lokalnih promenljivih ima = " + v.LocalVarDeclCount);

			log.info(" Deklarisanih Globalnih promenljivih ima = " + v.GlobalVarDeclCount);

			log.info(" Deklarisanih konstanti ima = " + v.ConstDeclCount);

			log.info(" Poziva funkcija ima = " + v.FuncCallCount);

			log.info(" Pristupa nizovima = " + v.ArrayElemCount);

			log.info(" Poziva print funkcije = " + v.PrintCallCount);

			System.out.println("\n===================ZAVRSENA SEMANTIKA=================="
					+ "\n Deklarisanih Lokalnih promenljivih ima = " + v.LocalVarDeclCount
					+ "\n Deklarisanih Globalnih promenljivih ima = " + v.GlobalVarDeclCount
					+ "\n Deklarisanih konstanti ima = " + v.ConstDeclCount
					+ "\n Poziva funkcija ima = "+ v.FuncCallCount
					+ "\n Pristupa nizovima = " + v.ArrayElemCount
					+ "\n Poziva print funkcije = "+ v.PrintCallCount);

			// log.info("\n===============Tab.dump====================");

			tsdump();

			if (!p.errorDetected && v.passed()) {
				log.info("\n=============PARSIRANJE I SEMANTIKA USPESNO ZAVRSENO=======================");

				File objFile = new File(fileOUT);
				if (objFile.exists())
					objFile.delete();
				CodeGenerator codeGenerator = new CodeGenerator();
				prog.traverseBottomUp(codeGenerator);
				Code.dataSize = v.nVars;
				Code.mainPc = codeGenerator.getMainPc();
				Code.write(new FileOutputStream(objFile));

			} else {
				log.error("--------------Parsiranje i semantika NIJE uspesno zavrseno----------");
			}

		} finally {
			if (br != null)
				try {
					br.close();
				} catch (IOException e1) {
					log.error(e1.getMessage(), e1);
				}
		}

	}

	public static void tsdump() {

		System.out.println("=====================SYMBOL TABLE DUMP=========================");
		SymbolTableVisitor stv = new TableDump();
		for (Scope s = Tab.currentScope; s != null; s = s.getOuter()) {
			s.accept(stv);
		}
		System.out.println(stv.getOutput());
	}

}
