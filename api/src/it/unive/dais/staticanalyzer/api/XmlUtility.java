package it.unive.dais.staticanalyzer.api;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 * Utilities to marshal and unmarshal xml files.
 * @author Pietro Ferrara
 *
 */
public class XmlUtility {

	
	private static <T> void marshal(Class<T> c, Writer writer, T data) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(c);
		Marshaller marshaller = jaxbContext.createMarshaller();
		marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
		marshaller.marshal(data, writer);
	}

	@SuppressWarnings("unchecked")
	private static <T> T unmarshal(Class<T> c, Reader reader) throws JAXBException {
		JAXBContext jaxbContext = JAXBContext.newInstance(c);
		Unmarshaller unmarshaller = jaxbContext.createUnmarshaller();
		return  (T) unmarshaller.unmarshal(reader);
	}
	
	/**
	 * Unmarshal and return an analysis options xml file
	 * @param file the path of the file containing the analysis options to be read
	 * @return the analysis options contained in the given file
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws JAXBException
	 */
	public static AnalysisOptions readAnalysisOptions(String file) throws FileNotFoundException, IOException, JAXBException {
		try(FileReader reader = new FileReader(file)) {
			return unmarshal(AnalysisOptions.class, reader);
		}
	}
	
	/**
	 * Marshal the analysis options to the given file
	 * @param file the path of the file that will contain the xml file containing the given analysis options
	 * @param options the analysis options to be marshaled
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws JAXBException
	 */
	public static void writeAnalysisOptions(String file, AnalysisOptions options) throws FileNotFoundException, IOException, JAXBException {
		try(FileWriter reader = new FileWriter(file)) {
			marshal(AnalysisOptions.class, reader, options);
		}
	}
	
	/**
	 * Unmarshal and return an analysis results xml file
	 * @param file the path of the file containing the analysis results to be read
	 * @return the analysis results contained in the given file
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws JAXBException
	 */
	public static AnalysisResult readAnalysisResult(String file) throws FileNotFoundException, IOException, JAXBException {
		try(FileReader reader = new FileReader(file)) {
			return unmarshal(AnalysisResult.class, reader);
		}
	}
	

	/**
	 * Marshal the analysis results to the given file
	 * @param file the path of the file that will contain the xml file containing the given analysis results
	 * @param options the analysis results to be marshaled
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws JAXBException
	 */
	public static void writeAnalysisResult(String file, AnalysisResult options) throws FileNotFoundException, IOException, JAXBException {
		try(FileWriter reader = new FileWriter(file)) {
			marshal(AnalysisResult.class, reader, options);
		}
	}
    
}
