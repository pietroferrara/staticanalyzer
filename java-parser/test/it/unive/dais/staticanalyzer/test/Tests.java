package it.unive.dais.staticanalyzer.test;

import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.apache.commons.cli.ParseException;
import org.junit.Assert;
import org.junit.Test;

import it.unive.dais.staticanalyzer.api.XmlUtility;

public class Tests {


	@Test
	public void simpleTest() throws FileNotFoundException, IOException, ParseException, JAXBException {
		Assert.assertTrue(Comparator.runAndCompare(XmlUtility.readAnalysisResult("C:\\Users\\PietroFerrara\\analyzer\\java-parser\\results\\results.xml")));
	}
}
