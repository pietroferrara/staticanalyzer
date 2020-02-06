package it.unive.dais.staticanalyzer.cfg;

import java.io.IOException;

public interface Parser<T extends ParsedBlock> {
	public T parse() throws IOException;

}
