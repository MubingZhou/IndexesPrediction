package hsci;

import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import indexesPredictionUtils.Util;

public class TurnoverScreening {
	
	public static ArrayList<String> allStocks = new ArrayList<String> ();
	public static Map<String, ArrayList> allStockData = new HashMap();
	public static ArrayList<String> eligibleStocks = new ArrayList<String>();
	
	//get the month-end total shares for each month 
	// e.g. allStockEachMonthTotalShares.get(1) could get data for stock "388.HK" (stock order determined by stockCodeArr when this class is constructed)
	// e.g. allStockEachMonthTotalShares.get(1).get(2) could get the month-end total shares for Apr for "388,HK" (if cutoff date = Jun 2017)
	public static ArrayList<ArrayList<Double>> allStockEachMonthTotalShares = new ArrayList<ArrayList<Double>>(); 
	// data structure same as allStockEachMonthTotalShares 
	public static ArrayList<ArrayList<Double>> allStockEachMonthAdjFreefloatFactor = new ArrayList<ArrayList<Double>>(); 
	public static ArrayList<ArrayList<Double>> allStockEachMonthMedianTurnover = new ArrayList<ArrayList<Double>>(); 
	public static ArrayList<ArrayList<Double>> allStockEachMonthTurnoverVelocity = new ArrayList<ArrayList<Double>>(); 
	
	// for all stocks, it contains number of month that can pass the turnover screening test for last 12 months
	// e.g if "allStockPast12MonthPassVelocityScreen.get(1) == 10", it says the for stock "388.HK", there are 10 months that will pass the turnover screening test
	public static ArrayList<Double> allStockPast12MonthPassTurnoverScreen = new ArrayList<Double>();
	public static ArrayList<Double> allStockPast6MonthPassTurnoverScreen = new ArrayList<Double>();
	public static ArrayList<Double> allStockPassTurnoverScreen = new ArrayList<Double>(); // 1 - pass, 0 - not pass
	
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
	 *  ArrayList_stockData = {12,23,24....} ArrayList_stockData in the format of ArrayList<Double>
	 *  ArrayList_date = {2016-06-01, 2017-05-04, ...} ArrayList_date in the format of ArrayList<Calendar>
	 * @return
	 * @throws Exception
	 */
	public TurnoverScreening(ArrayList<String> stockCodeArr, Map<String, ArrayList> stockData) throws Exception{
		allStocks = stockCodeArr;
		allStockData = stockData;
		
		SimpleDateFormat sdf =   new SimpleDateFormat(Util.DATE_FORMAT);
		Calendar cutOffDate = Calendar.getInstance();
		cutOffDate = Util.getLastDayOfMonth(Util.CUT_OFF_DATE, Util.DATE_FORMAT);  // use end of month
		int cutOffDateMonth = cutOffDate.get(Calendar.MONTH);
		
		// for each stock, test if it can pass the screening
		for(int i = 0; i < stockCodeArr.size(); i++) {
			String stockCode = stockCodeArr.get(i);
			
			ArrayList stockDate_Data = stockData.get(stockCode);
			ArrayList<Double> stockTurnover = (ArrayList<Double>) stockDate_Data.get(0); 
			ArrayList<Calendar> stockDate = (ArrayList<Calendar>) stockDate_Data.get(1); 
			
			// to get the data for a specific month
			ArrayList<Double> medianTurnover = new ArrayList<Double>();
			ArrayList<Calendar> medianTurnover_date = new ArrayList<Calendar>();
			
			/*
			 * store the dates of each month
			 * e.g. if the cut off date = 30/06/2017, the eachMonthDate.get(0) returns an array of dates which fall with Jun 2017
			 *      and eachMonthDate.get(1) returns an array of dates which fall with May 2017 (i.e. one month before, and so forth)
			 * e.g. 
			 * ArrayList<Integer> monthDate = eachMonthDate.get(0) ;
			 * monthDate.get(0) may be "01/06/2017"
			 * 
			 * same for eachMonthTurnonver
			 */
			ArrayList<ArrayList<Calendar>> eachMonthDate = new ArrayList<ArrayList<Calendar>>();
			ArrayList<ArrayList<Double>> eachMonthTurnonver = new ArrayList<ArrayList<Double>>();
			
			// to construct eachMonthDate/eachMonthTurnonver
			for(int j = 0; j < 12; j++) {
				eachMonthDate.add(new ArrayList<Calendar>());
				eachMonthTurnonver.add(new ArrayList<Double>());
			}
			
			// to classify each date in stockDate; classify each data in stockTurnover
			for(int j = 0; j < stockDate.size(); j++) {
				Calendar todayDate = stockDate.get(j);
				Double todayTurnover = stockTurnover.get(j);
				
				int thisMonth = todayDate.get(Calendar.MONTH);
				
				int monthDiff = cutOffDateMonth - thisMonth;
				
				if(monthDiff < 12) {  // within the interested period
					eachMonthDate.get(monthDiff).add(todayDate);
					eachMonthTurnonver.get(monthDiff).add(todayTurnover);
				}
				
			}
			
			// ============== calculate turnover velocity ==============
			// eachMonthTurnonverMedian is an ArrayList<Double> with 12 elements
			//eachMonthTurnonverMedian.get(0) is the median turnover of Jun 2017 (if the cutoff date is 30Jun 2017)
			ArrayList<Double> eachMonthTurnonverMedian = getMedian(eachMonthTurnonver);
			allStockEachMonthMedianTurnover.add(eachMonthTurnonverMedian);
			
			ArrayList<Double> eachMonthAdjFreefloatFactor = getEachMonthAdjFreefloatFactor(stockCode, cutOffDate, eachMonthTurnonverMedian.size(), 0.7);
			allStockEachMonthAdjFreefloatFactor.add(eachMonthAdjFreefloatFactor);
			
			ArrayList<Double> eachMonthTotalIssuedShares = getEachMonthTotalIssuedShares(stockCode, cutOffDate,eachMonthTurnonverMedian.size());
			allStockEachMonthTotalShares.add(eachMonthTotalIssuedShares);
			
			// to calculate
			ArrayList<Double> eachMonthTurnoverVelocity = new ArrayList<Double>();
			
			ArrayList<Double> isEachMonthPassTurnoverScreening = new ArrayList<Double>();
			int okOver12Month = 0;
			int okOver6Month = 0;
			
			for(int j = 0; j < eachMonthTurnonverMedian.size(); j++) {
				Double thisMonthTurnoverVelocity = eachMonthTurnonverMedian.get(j) / (eachMonthTotalIssuedShares.get(j) * eachMonthAdjFreefloatFactor.get(j));
				eachMonthTurnoverVelocity.add(thisMonthTurnoverVelocity);
				
				if(thisMonthTurnoverVelocity > 0.0005) {
					isEachMonthPassTurnoverScreening.add(1.0);
					
					okOver12Month ++;
					if(j < 6) {
						okOver6Month ++;
					}
				}else {
					isEachMonthPassTurnoverScreening.add(0.0);
				}
			}
			allStockEachMonthTurnoverVelocity.add(eachMonthTurnoverVelocity);
			allStockPast12MonthPassTurnoverScreen.add((double) okOver12Month);
			allStockPast6MonthPassTurnoverScreen.add((double) okOver6Month);
			
			//============== screening using turnover velocity ================
			if(okOver12Month >= 10 && okOver6Month >= 5) {
				eligibleStocks.add(stockCode);
				allStockPassTurnoverScreen.add(1.0);
			}else {
				allStockPassTurnoverScreen.add(0.0);
			}
		
		} // end of for
	}
	
	/**
	 * eachMonthTurnonver should have 12 elements and each element is an ArrayList of that month's turnover
	 * to return an 12-element ArrayList<Double> with element the median turnover of each month  
	 * @param eachMonthTurnonver
	 * @return
	 * @throws Exception
	 */
	public static ArrayList<Double> getMedian(ArrayList<ArrayList<Double>> eachMonthTurnonver) throws Exception{
		ArrayList<Double> medianTurnover = new ArrayList<Double>();
		
		for(int i = 0; i < eachMonthTurnonver.size(); i++) {
			ArrayList<Double> thisMonthTurnoverData = eachMonthTurnonver.get(i);
			
			Double thisMonthTurnoverMedian = getSingleArrayMedian(thisMonthTurnoverData);
			
			medianTurnover.add(thisMonthTurnoverMedian);
		}
		
		return medianTurnover;
	}
	
	/**
	 * to return the median value of an ArrayList<Double>
	 * @param singleMonthData
	 * @return
	 * @throws Exception
	 */
	public static Double getSingleArrayMedian(ArrayList<Double> singleMonthData) throws Exception{
		Double median = null;
		
		//Collections.sort(singleMonthData, new SortArrayListDouble());
		
		Collections.sort(singleMonthData, new Comparator<Double>() {
			 public int compare(Double arg0, Double arg1) {
	                return arg0.compareTo(arg1);
	            }
		});
		
		int len = singleMonthData.size() ;
		
		if(len % 2 == 0) { // even number
			median = (singleMonthData.get((int)(len/2)) + singleMonthData.get((int)(len/2) - 1) ) / 2;
		}else {
			median = singleMonthData.get((len-1) / 2);
		}
		
		return median;
	}

	// to be completed
	public static ArrayList<Double> getEachMonthAdjFreefloatFactor(String stockCode, Calendar cutoffDate, int NMonth, double specifiedAdjFreefloat){
		ArrayList<Double> eachMonthAdjFreefloat = new ArrayList<Double>();
		
		for(int i = 0; i < NMonth; i++) {
			eachMonthAdjFreefloat.add(specifiedAdjFreefloat);
		}
		
		return eachMonthAdjFreefloat;
	}
	
	// to be completed
	public static ArrayList<Double> getEachMonthTotalIssuedShares(String stockCode, Calendar cutoffDate, int NMonth){
		ArrayList<Double> eachMonthTotalIssuedShares= new ArrayList<Double>();
		
		for(int i = 0; i < NMonth; i++) {
			eachMonthTotalIssuedShares.add(10000.0);
		}
		
		return eachMonthTotalIssuedShares;
	}

	// to output the screening result
	public static void outputScreeningResults(String outputFilePath) throws Exception{
		FileWriter fw = new FileWriter(outputFilePath);
		String header = "";  // header
		ArrayList<String> toWrite = new ArrayList<String> ();
		
		for(int i = 0; i < allStocks.size(); i++) {
			String lineToWrite = "";
			
			String stockCode = allStocks.get(i);
			String numOKLast12Month = String.valueOf(allStockPast12MonthPassTurnoverScreen.get(i));
			String numOKLast6Month = String.valueOf(allStockPast6MonthPassTurnoverScreen.get(i));
			String isPassed = allStockPassTurnoverScreen.get(i) == 1.0?"Y":"";
			
			lineToWrite = stockCode + "," + numOKLast12Month + "," + numOKLast6Month  + "," + isPassed;
			header = "stock code,num OK last 12 months,num OK last 6 months,is passed";
			// output each month's data
			for(int j = 0; j < Util.PAST_N_MONTH; j++) {
				// out put 4 data for each month: total issued shares, adj freefloat factor, median turnover, velocity
				String totalIssuedShares = String.valueOf(allStockEachMonthTotalShares.get(i).get(j));
				String adjFreefloatFactor = String.valueOf(allStockEachMonthAdjFreefloatFactor.get(i).get(j));
				String medianTurnover = String.valueOf(allStockEachMonthMedianTurnover.get(i).get(j));
				String velocity = String.valueOf(allStockEachMonthTurnoverVelocity.get(i).get(j));
				
				lineToWrite = "," + totalIssuedShares + "," + adjFreefloatFactor + "," + medianTurnover + "," + velocity;
				
				String jStr = String.valueOf(j);
				header = "," + jStr + "month - total shares," + jStr + "month - freefloat factor," + jStr + "month - median turnover," + jStr + " month - velocity";
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
