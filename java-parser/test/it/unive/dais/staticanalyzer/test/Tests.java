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
	public void Apron_test1() throws FileNotFoundException, IOException, ParseException, JAXBException {
		Assert.assertTrue(Comparator.runAndCompare(XmlUtility.readAnalysisResult("./test-res/xml-expected/apron/test1.xml")));
	}
	
	@Test
	public void Apron_test2() throws FileNotFoundException, IOException, ParseException, JAXBException {
		Assert.assertTrue(Comparator.runAndCompare(XmlUtility.readAnalysisResult("./test-res/xml-expected/apron/test2.xml")));
	}
	
	@Test
	public void Apron_test3() throws FileNotFoundException, IOException, ParseException, JAXBException {
		Assert.assertTrue(Comparator.runAndCompare(XmlUtility.readAnalysisResult("./test-res/xml-expected/apron/test3.xml")));
	}
	
	@Test
	public void Apron_test4() throws FileNotFoundException, IOException, ParseException, JAXBException {
		Assert.assertTrue(Comparator.runAndCompare(XmlUtility.readAnalysisResult("./test-res/xml-expected/apron/test4.xml")));
	}
	
	@Test
	public void Apron_test5() throws FileNotFoundException, IOException, ParseException, JAXBException {
		Assert.assertTrue(Comparator.runAndCompare(XmlUtility.readAnalysisResult("./test-res/xml-expected/apron/test5.xml")));
	}
	
	@Test
	public void Apron_test6() throws FileNotFoundException, IOException, ParseException, JAXBException {
		Assert.assertTrue(Comparator.runAndCompare(XmlUtility.readAnalysisResult("./test-res/xml-expected/apron/test6.xml")));
	}

	@Test
	public void Apron_test7() throws FileNotFoundException, IOException, ParseException, JAXBException {
		Assert.assertTrue(Comparator.runAndCompare(XmlUtility.readAnalysisResult("./test-res/xml-expected/apron/test7.xml")));
	}

	@Test
	public void Apron_test8() throws FileNotFoundException, IOException, ParseException, JAXBException {
		Assert.assertTrue(Comparator.runAndCompare(XmlUtility.readAnalysisResult("./test-res/xml-expected/apron/test8.xml")));
	}

	@Test
	public void Apron_test9() throws FileNotFoundException, IOException, ParseException, JAXBException {
		Assert.assertTrue(Comparator.runAndCompare(XmlUtility.readAnalysisResult("./test-res/xml-expected/apron/test9.xml")));
	}
}
