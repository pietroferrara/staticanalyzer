package it.unive.dais.staticanalyzer.parser.java;

import java.io.IOException;
import java.io.InputStream;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;

import it.unive.dais.staticanalyzer.cfg.CFG;
import it.unive.dais.staticanalyzer.cfg.Parser;
import it.unive.dais.staticanalyzer.parser.java.generated.JavaLexer;
import it.unive.dais.staticanalyzer.parser.java.generated.JavaParser;

public class BodyParser implements Parser<CFG>{
	private InputStream input;
	public BodyParser(InputStream input) {
		this.input = input;
	}

	@Override
	public CFG parse() throws IOException {
        // create a CharStream that reads from standard input
		CharStream input = CharStreams.fromStream(this.input);

        // create a lexer that feeds off of input CharStream
		JavaLexer lexer = new JavaLexer(input);

        // create a buffer of tokens pulled from the lexer
        CommonTokenStream tokens = new CommonTokenStream(lexer);

        // create a parser that feeds off the tokens buffer
        JavaParser parser = new JavaParser(tokens);

        JavaParser.MethodBodyContext tree = parser.methodBody(); // begin parsing at method body rule
        
        BodyVisitor  listener = new BodyVisitor();
        CFG block = listener.visit(tree);

        return block;
	}
	
}
