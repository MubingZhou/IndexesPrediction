package hsci;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ParseFile {
	public static final String DATE_FORMAT = "dd/MM/yyyy";
	/*
	 * To read and parse the files from bloomberg
	 * Typically, a file looks like this
	 * 			date1	date2	date3	...
	 * ticker1	[data]	[..]	[..]
	 * ticker2
	 * ...
	 * 
	 * i.e. each row contains the time series data for a stock
	 * each col contains the cross-sectional data for a date
	 */
	
	/**
	 * to get a Map which stores data by stock. A typical Map looks like below:
	 * {"6881", ArrayList_data
	 *  "1", ArrayList_data...}
	 *  where "6881" is the stock code, ArrayList_data contains the data. 
	 *  A typical ArrayList_data contains two another ArrayList:
	 *    ArrayList_stockData & ArrayList_date
	 *  e.g.
	 *  ArrayList_stockData = {12,23,24....} ArrayList_stockData in the format of ArrayList<Double>
	 *  ArrayList_date = {2016-06-01, 2017-05-04, ...} ArrayList_date in the format of ArrayList<Date>
	 *  
	 * @param filePath
	 * @return
	 * @throws Exception
	 */
	public static Map<String, ArrayList> parseFile(String filePath)  throws Exception{
		Map<String, ArrayList> dataByStock = new HashMap();
		
		// read file
		InputStream input = new FileInputStream(new File(filePath));
		InputStreamReader inputStreamReader = new InputStreamReader(input);
		BufferedReader bufReader = new BufferedReader(inputStreamReader);
		
		String line = "";
		int counter = 0;
		while ((line = bufReader.readLine()) != null) {
			ArrayList<String> lineArr = new ArrayList<String>();
			lineArr.addAll(Arrays.asList(line.split(",")));
			
			// to store date
			ArrayList<Date> masterDate = new ArrayList<Date>();
			
			if(counter == 0){ //excluding header
				String stockCode = lineArr.get(0);
				
				ArrayList<Double> stockData = new ArrayList<Double>(); 
				ArrayList<Date> dateToRemove = new ArrayList<Date>();
				
				for(int i = 1; i < lineArr.size(); i++) {
					String data = lineArr.get(i);
					
					// test for every data in this row
					if(!isDouble(data)) { // if the data is not correct
						dateToRemove.add(masterDate.get(i));
					}else {
						stockData.add(Double.parseDouble(data));
					}
				}
				masterDate.removeAll(dateToRemove);
				
				ArrayList rowData = new ArrayList();
				rowData.add(stockData);
				rowData.addAll(masterDate);
				
				dataByStock.put(stockCode, rowData);
				
			}else { // header
				// stores all trading date, but for some stocks, it has no trading on this date
				masterDate = dateStr2Date(lineArr.subList(1, lineArr.size()-2), DATE_FORMAT);
			}
			counter++;
		}
		
		return dataByStock;
	}
	
	/**
	 * to convert a date array in String to in the form of ArrayList<Date>
	 * @param dateArr
	 * @param dateFormat
	 * @return date arrray
	 * @throws Exception
	 */
	public static ArrayList<Date> dateStr2Date(List<String> dateArr, String dateFormat) throws Exception{
		ArrayList<Date> toReturn = new ArrayList<Date> ();
		
		for(int i = 0; i < dateArr.size(); i++) {
			String todayDateStr = dateArr.get(i);
			
			SimpleDateFormat sdf =   new SimpleDateFormat(dateFormat);
			Date date = sdf.parse(todayDateStr);
			
			toReturn.add(date);
		}
		
		return toReturn;
	}
	
	/**
	 * check if a String is a double
	 * @param str
	 * @return
	 */
	public static boolean isDouble(String str) {
		boolean isOK = true;
		
		try {
			double d = Double.parseDouble(str);
		}catch(NumberFormatException nfe) {
			isOK = false;
		}
		catch(Exception e) {
			isOK = false;
		}
		
		return isOK;
	}
}
