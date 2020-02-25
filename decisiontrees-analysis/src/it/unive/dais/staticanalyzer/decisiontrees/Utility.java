package it.unive.dais.staticanalyzer.decisiontrees;

import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

public class Utility {

	public static List<List<Double>> readCsv(String csvFile) throws CsvValidationException, IOException {
		List<List<Double>> records = new ArrayList<List<Double>>();
		try (CSVReader csvReader = new CSVReader(new FileReader(csvFile));) {
		    String[] values = null;
		    while ((values = csvReader.readNext()) != null) {
		    	records.add(
		    			Arrays.asList(values).stream().mapToDouble(Double::parseDouble).boxed().collect(Collectors.toList())
		    			);
		    }
		}
		return records;
	}
	
}
