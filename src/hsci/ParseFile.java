package hsci;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import indexesPredictionUtils.Util;

public class ParseFile {
	/**
	 * to get a Map which stores data by stock. A typical Map looks like below:
	 * {"6881", ArrayList_data
	 *  "1", ArrayList_data...}
	 *  where "6881" is the stock code, ArrayList_data contains the data. 
	 *  A typical ArrayList_data contains two another ArrayList:
	 *    ArrayList_stockData & ArrayList_date
	 *  e.g.
	 *  ArrayList_stockData = {12,23,24....} ArrayList_stockData in the format of ArrayList<Double>
	 *     if it is halted for trading, the turnover is 0 at that day
	 *  ArrayList_date = {2016-06-01, 2017-05-04, ...} ArrayList_date in the format of ArrayList<Calendar>
	 *  
	 * @param filePath
	 * @return
	 * @throws Exception
	 */
	public static Map<String, ArrayList<Object>> parseFile(String filePath)  throws Exception{
		/*
		 * Typically, a file looks like this
		 * 			date1	date2	date3	...
		 * ticker1	[data]	[..]	[..]
		 * ticker2
		 * ...
		 * 
		 * i.e. each row contains the time series data for a stock
		 * each col contains the cross-sectional data for a date
		 */
		Map<String, ArrayList<Object>> dataByStock = new HashMap();
		//========= before read stock file, get all trading date first =========
		//String tradingDateFilePath = "D:\\stock data\\Indexes Prediction\\HSCI\\all trading date.csv";
		//String allTradingDateStr = Util.readFileByLine(tradingDateFilePath).get(0);
		//String[]  allTradingDateStrArr = allTradingDateStr.split(",");
		ArrayList<Calendar> allTradingDate = new ArrayList<Calendar> ();
		
		// ============ get stock-specific trading date & turnover =============
		BufferedReader bufReader = Util.readFile_returnBufferedReader(filePath);
		
		String line1 = "";
		//String line2 = "";
		int counter = 0;
		while ((line1 = bufReader.readLine()) != null) {
			ArrayList<String> lineArr = new ArrayList<String>();
			lineArr.addAll(Arrays.asList(line1.split(",")));
			
			if(counter == 0) {// first line, the trading date
				lineArr.remove(0); // remove the first element
				allTradingDate = Util.changeStrDateToArray(lineArr, "dd/MM/yyyy");
			}else {
				String stockCode = lineArr.get(0);
				lineArr.remove(0);// remove first element
				
				ArrayList<Double> dataDouble = new ArrayList<Double>();
				for(String dataStr : lineArr) {
					// if can't parse the data, return 0
					dataDouble.add(Util.isDouble(dataStr)?Double.parseDouble(dataStr):0.0);
				}
				
				ArrayList<Object> dataArr = new ArrayList<Object>();
				dataArr.add(dataDouble);
				dataArr.add(allTradingDate);
				
				dataByStock.put(stockCode, dataArr);
			}
			counter++;
		}
		
		//outputStockDataMap(dataByStock);
		
		return dataByStock;
	}
	
	/**
	 * return data format the same as parseFile
	 *  
	 * @param filePath
	 * @return
	 * @throws Exception
	 */
	public static Map<String, ArrayList<Object>> parseFile_specific(String filePath)  throws Exception{
		/*
		 * Please note that this function is designed to parse a specific file, it is not universally applicable.
		 */
		Map<String, ArrayList<Object>> dataByStock = new HashMap();
		//========= before read stock file, get all trading date first =========
		String tradingDateFilePath = "D:\\stock data\\Indexes Prediction\\HSCI\\all trading date.csv";
		String allTradingDateStr = Util.readFileByLine(tradingDateFilePath).get(0);
		String[]  allTradingDateStrArr = allTradingDateStr.split(",");
		ArrayList<Calendar> allTradingDate = Util.changeStrDateToArray(allTradingDateStrArr, "yyyy/MM/dd");
		
		// ============ get stock-specific trading date & turnover =============
		BufferedReader bufReader = Util.readFile_returnBufferedReader(filePath);
		
		String line1 = "";
		String line2 = "";
		int counter = 0;
		while ((line1 = bufReader.readLine()) != null) {
			//read 2 lines at once
			ArrayList<String> line1Arr = new ArrayList<String>();
			line1Arr.addAll(Arrays.asList(line1.split(",")));
			String stockCode = line1Arr.get(0);
			line1Arr.remove(0);
			line1Arr.remove(0); // remove the 1st two elements
			
			line2 = bufReader.readLine();
			ArrayList<String> line2Arr = new ArrayList<String>();
			line2Arr.addAll(Arrays.asList(line2.split(",")));
			line2Arr.remove(0);
			line2Arr.remove(0); // remove the 1st two elements
			
			// get date & data
			//String dateFormat = "yyyy/MM/dd";
			String dateFormat = "dd/MM/yyyy";
			ArrayList<Calendar> thisStockTradingDate = new ArrayList<Calendar>();
			ArrayList<String> thisStockVolStr = new ArrayList<String>();
			
			// test which line is for date
			if(Util.isDate(line1Arr.get(0), dateFormat)) { // first line is the date line
				thisStockTradingDate = Util.changeStrDateToArray(line1Arr, dateFormat);
				thisStockVolStr = line2Arr;
			}else {
				System.out.println("counter = " + counter + " line2Arr.get(0) = " + line2Arr.get(0));
				thisStockTradingDate = Util.changeStrDateToArray(line2Arr, dateFormat);
				thisStockVolStr = line1Arr;
			}
			
			ArrayList<Double> thisStockVol = new ArrayList<Double>();
			for(String str : thisStockVolStr)
				thisStockVol.add(Double.parseDouble(str));
			
			// align thisStockTradingDate with allTradingDate
			int thisInd_thisStockTradingDate = 0; 
			Calendar thisStockFirstTradingDate = thisStockTradingDate.get(0);
			for(int i = allTradingDate.indexOf(thisStockFirstTradingDate); i < allTradingDate.size(); i++){
				Calendar eachTradingDate = allTradingDate.get(i);
				if(thisStockTradingDate.indexOf(eachTradingDate) == -1){ // if stock halt trading on this date 
					thisStockTradingDate.add(thisInd_thisStockTradingDate, eachTradingDate);
					thisStockVol.add(thisInd_thisStockTradingDate,0.0);
				}
				thisInd_thisStockTradingDate++;
			}// end of for
			
			// ============= add to dataByStock ============= 
			ArrayList<Object> thisStockData = new ArrayList<Object>();
			thisStockData.add(thisStockVol);
			thisStockData.add(thisStockTradingDate);
			dataByStock.put(stockCode, thisStockData);
		}
		
		return dataByStock;
	}
	
	/**
	 * for the result, same format as in parseFile(String filePath), except that the stock data is freefloat
	 * @param filePath
	 * @return
	 * @throws Exception
	 */
	public static Map<String, ArrayList<Object>> parseFreefloatAndTotalShares(String filePath)  throws Exception{
		Map<String, ArrayList<Object>> dataByStock = new HashMap();
		
		ArrayList<String> readData = Util.readFileByLine(filePath);
		
		ArrayList<Calendar> dateArr = new ArrayList<Calendar> ();
				
		int counter = 0;
		for(String line : readData) {
			String[] lineArr = line.split(",");
			ArrayList<String> lineArray = new ArrayList<String>();
			lineArray.addAll(Arrays.asList(lineArr));
			
			// get the 1st element and subsequent elements
			String stockCode = lineArray.get(0);
			ArrayList<String> data = new ArrayList<String>(lineArray.subList(1, lineArray.size()));
			
			if(counter == 0) { //header	
				dateArr = Util.changeStrDateToArray(data, "dd/MM/yyyy");
			}else {
				ArrayList<Double> dataDouble = new ArrayList<Double>()	;
				for(String freefloatStr:data) {
					dataDouble.add(Util.isDouble(freefloatStr)?Double.parseDouble(freefloatStr):-100.0);
				}
				ArrayList<Object> dataArr = new ArrayList<Object>();
				
				dataArr.add(dataDouble);
				dataArr.add(dateArr);
				
				dataByStock.put(stockCode, dataArr);
			}
			
			counter++;
		}
		
		return dataByStock;
	}
	
	/**
	 * to return the stock list
	 * @param filePath
	 * @return
	 * @throws Exception
	 */
	public static ArrayList<String> parseStockList(String filePath) throws Exception{
		ArrayList<String> readData = Util.readFileByLine(filePath);
		
		return new ArrayList<String>(Arrays.asList(readData.get(0).split(",")));
	}
	
	public static void outputStockDataMap(Map<String, ArrayList<Object>> dataByStock) throws Exception{
		/*
		 * From now on, we can use the below files as data source. 
		 */
		// ========== print dataByStock ==========
		String writefilePath = "D:\\stock data\\indexes prediction\\stock_connect_turnover_screen_new.csv";
		FileWriter fw = new FileWriter(writefilePath);
		
		Set<String> allStock = dataByStock.keySet();
		
		int counter1 = 0;
		for(String stockCode : allStock){
			
			ArrayList<Object> thisStockData = dataByStock.get(stockCode);
			
			ArrayList<Double> thisStockVol = (ArrayList<Double>) thisStockData.get(0);
			ArrayList<Calendar> thisStockDate = (ArrayList<Calendar>) thisStockData.get(1);
			
			String volLine = stockCode ;
			String dateLine = stockCode;
			
			for(int i = 0; i < thisStockVol.size(); i++){
				volLine = volLine + "," + String.valueOf(thisStockVol.get(i));
				dateLine = dateLine + "," + Util.date2Str(thisStockDate.get(i), "yyyy/MM/dd");
			}
			
			fw.write(dateLine);
			fw.write("\n");
			fw.write(volLine);
			
			counter1++;
			if(counter1 != allStock.size())
				fw.write("\n");
		}
		fw.close();
	}
	
}
