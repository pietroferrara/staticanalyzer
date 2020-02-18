package it.unive.dais.staticanalyzer.cfg;

import java.io.IOException;

/**
 * The class representing a parser
 * @author Pietro Ferrara
 *
 * @param <T> the type of the parsed block
 */
public interface Parser<T extends ParsedBlock> {
	/**
	 * 
	 * @return the parsed block
	 * @throws IOException
	 */
	public T parse() throws IOException;

}
