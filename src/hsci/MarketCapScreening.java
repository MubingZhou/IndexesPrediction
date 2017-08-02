package hsci;

import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import indexesPredictionUtils.Util;

public class MarketCapScreening {
	public static ArrayList<String> allStocks = new ArrayList<String> ();
	public static Map<String, ArrayList> allStockData = new HashMap();
	public static ArrayList<String> eligibleStocks = new ArrayList<String>();
	
	public static ArrayList<Double> allStockAvg12MonthMarketCap = new ArrayList<Double>();
	public static ArrayList<Double> allStockPassMarketCapScreen = new ArrayList<Double>();
	
	public static Double _95PctMarketCapLine;

	public String CUT_OFF_DATE = "31/12/2016";
	public String CUT_OFF_DATE_DATE_FORMAT = "dd/MM/yyyy";
	/**
	 * 
	 * @param stockCodeArr
	 * @param "stockData" in the form of Map<String, ArrayList>
	 * A typical Map looks like below:
	 * {"6881", ArrayList_data
	 *  "1", ArrayList_data...}
	 *  where "6881" is the stock code, ArrayList_data contains the data. 
	 *  A typical ArrayList_data contains two another ArrayList:
	 *    ArrayList_stockData & ArrayList_date
	 *  e.g.
	 *  ArrayList_stockData = {12,23,24....} ArrayList_stockData in the format of ArrayList<Double> (month-end market cap)
	 *  ArrayList_date = {2016-06-30, 2017-05-31, ...} ArrayList_date in the format of ArrayList<Calendar>
	 * @return
	 * @throws Exception
	 */
	public MarketCapScreening(ArrayList<String> stockCodeArr, Map<String, ArrayList> stockData) throws Exception{
		// ======= we assume stockData contains month-end data, if not needs to re-write
		allStocks = stockCodeArr;
		allStockData = stockData;
		
		SimpleDateFormat sdf =   new SimpleDateFormat(CUT_OFF_DATE_DATE_FORMAT);
		Calendar cutOffDate = Calendar.getInstance();
		cutOffDate = Util.getLastDayOfMonth(CUT_OFF_DATE, CUT_OFF_DATE_DATE_FORMAT);  // use end of month
		int cutOffDateMonth = cutOffDate.get(Calendar.MONTH);
		
		// =========== get avg month-end market cap for each stock ============
		for(int i = 0; i < stockCodeArr.size(); i++) {
			String stockCode = stockCodeArr.get(i);
			
			ArrayList stockDate_Data = stockData.get(stockCode);
			ArrayList<Double> stockMarketCap = (ArrayList<Double>) stockDate_Data.get(0);  // data for a specific stock
			ArrayList<Calendar> stockDate = (ArrayList<Calendar>) stockDate_Data.get(1); 
		
			double cumMarketCap = 0.0;
			for(int j = 0; j < Math.min(Util.PAST_N_MONTH, stockMarketCap.size()); j++) {
				cumMarketCap = cumMarketCap + stockMarketCap.get(j);
			}
			double avgMarketCap = cumMarketCap / Math.min(Util.PAST_N_MONTH, stockMarketCap.size());
			
			allStockAvg12MonthMarketCap.add(avgMarketCap);
		}
		
		// ======== calculate 95% line ==============
		ArrayList<Double> ordered_allStockAvg12MonthMarketCap = new ArrayList<Double>(allStockAvg12MonthMarketCap);
		
		// sorting by ascending: the 1st element is the smallest
		Collections.sort(ordered_allStockAvg12MonthMarketCap, new Comparator<Double>() {
			 public int compare(Double arg0, Double arg1) {
	                return arg0.compareTo(arg1);
	            }
		});
		
		_95PctMarketCapLine = ordered_allStockAvg12MonthMarketCap.get(
				(int) (ordered_allStockAvg12MonthMarketCap.size() * 0.95));
		
		// ========== test if passed ===========
		for(int i = 0; i < stockCodeArr.size(); i++) {
			String stockCode = stockCodeArr.get(i);
			Double thisStockAvgMarketCap = allStockAvg12MonthMarketCap.get(i);
			
			if(thisStockAvgMarketCap >= _95PctMarketCapLine) {
				eligibleStocks.add(stockCode);
				allStockPassMarketCapScreen.add(1.0);
			}else {
				allStockPassMarketCapScreen.add(0.0);
			}
				
		}
		
	}
	
	// to output the screening result
	public static void outputScreeningResults(String outputFilePath) throws Exception{
		FileWriter fw = new FileWriter(outputFilePath);
		String header = "";  // header
		ArrayList<String> toWrite = new ArrayList<String> ();
		
		for(int i = 0; i < allStocks.size(); i++) {
			String lineToWrite = "";
			
			String stockCode = allStocks.get(i);
			String avg12MonthMC = String.valueOf(allStockAvg12MonthMarketCap.get(i));
			String _95Line = String.valueOf(_95PctMarketCapLine);
			String isPassed = allStockPassMarketCapScreen.get(i) == 1.0?"Y":"";
			
			lineToWrite = stockCode + "," + avg12MonthMC + "," + _95Line  + "," + isPassed;
			header = "stock code,avg 12 month market cap,95% market cap line,is passed";
			
			// output each month's data
			ArrayList<Double> stockMarketCap = (ArrayList<Double>) allStockData.get(0);
			for(int j = 0; j < Util.PAST_N_MONTH; j++) {
				// out put 4 data for each month: total issued shares, adj freefloat factor, median turnover, velocity
				String stockMarketCapStr = String.valueOf(stockMarketCap.get(j));
		
				lineToWrite = "," + stockMarketCapStr;
				
				String jStr = String.valueOf(j);
				header = "," + jStr + "month - market cap";
			}// end of for(int j = ....
			
			toWrite.add(lineToWrite);
		}// end of for(int i = ....
		
		// write into file
		fw.write(header);
		for(String line : toWrite)
			fw.write(line);
		
		fw.close();
	}
}
