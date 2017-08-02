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
	public ArrayList<String> eligibleStocks = new ArrayList<String>();
	
	public ArrayList<String> allStocks = new ArrayList<String> ();
	public Map<String, ArrayList<Object>> allStockData = new HashMap();
	public Map<String, ArrayList<Object>> allStockFreefloat = new HashMap();
	public Map<String, ArrayList<Object>> allStockTotalShares = new HashMap();
	
	//get the month-end total shares for each month 
	// e.g. allStockEachMonthTotalShares.get(1) could get data for stock "388.HK" (stock order determined by stockCodeArr when this class is constructed)
	// e.g. allStockEachMonthTotalShares.get(1).get(2) could get the month-end total shares for Apr for "388,HK" (if cutoff date = Jun 2017)
	public ArrayList<ArrayList<Double>> allStockEachMonthTotalShares = new ArrayList<ArrayList<Double>>(); 
	// data structure same as allStockEachMonthTotalShares 
	public ArrayList<ArrayList<Double>> allStockEachMonthAdjFreefloatFactor = new ArrayList<ArrayList<Double>>(); 
	public ArrayList<ArrayList<Double>> allStockEachMonthMedianTurnover = new ArrayList<ArrayList<Double>>(); 
	public ArrayList<ArrayList<Double>> allStockEachMonthTurnoverVelocity = new ArrayList<ArrayList<Double>>(); 
	
	// for all stocks, it contains number of month that can pass the turnover screening test for last 12 months
	// e.g if "allStockPast12MonthPassVelocityScreen.get(1) == 10", it says the for stock "388.HK", there are 10 months that will pass the turnover screening test
	public ArrayList<Double> allStockPast12MonthPassTurnoverScreen = new ArrayList<Double>();
	public ArrayList<Double> allStockPast6MonthPassTurnoverScreen = new ArrayList<Double>();
	public ArrayList<Double> allStockPassTurnoverScreen = new ArrayList<Double>(); // 1 - pass, 0 - not pass, -1 - not counted 
	public ArrayList<Double> allStockEligibleTurnoverScreen = new ArrayList<Double>(); // num of months eligible for counting
	
	public String CUT_OFF_DATE = "31/12/2016";
	public String CUT_OFF_DATE_DATE_FORMAT = "dd/MM/yyyy";
	
	public double freefloatShift = 0.0;
	
	public TurnoverScreening(String cutoffDat, String cutoffDateFormat, double freefloatShiftNew) {
		CUT_OFF_DATE = cutoffDat;
		CUT_OFF_DATE_DATE_FORMAT = cutoffDateFormat;
		freefloatShift = freefloatShiftNew;
	}
	
	public void setFreefloatShit(double freefloatShiftNew) {
		freefloatShift = freefloatShiftNew;
	}
	
	public void setCutoffDate(String date) {
		CUT_OFF_DATE = date;
	}
	
	public void setCutoffDateFormat(String fmt) {
		CUT_OFF_DATE_DATE_FORMAT = fmt;
	}
	
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
	public void turnoverScreening(ArrayList<String> stockCodeArr, Map<String, ArrayList<Object>> stockData) throws Exception{
		allStocks = stockCodeArr;
		allStockData = stockData;
		
		SimpleDateFormat sdf =   new SimpleDateFormat(CUT_OFF_DATE_DATE_FORMAT);
		Calendar cutOffDate = Calendar.getInstance();
		cutOffDate = Util.getLastDayOfMonth(CUT_OFF_DATE, CUT_OFF_DATE_DATE_FORMAT);  // use end of month
		int cutOffDateMonth = cutOffDate.get(Calendar.MONTH);
		
		// for each stock, test if it can pass the screening
		for(int i = 0; i < stockCodeArr.size(); i++) {
			String stockCode = stockCodeArr.get(i);
			System.out.println("=========== stock code = " + stockCode + " ===========");
			
			ArrayList stockDate_Data = stockData.get(stockCode);
			ArrayList<Double> stockTurnover = new ArrayList<Double>();
			ArrayList<Calendar> stockDate = new ArrayList<Calendar>();
			if(stockDate_Data != null) {
				stockTurnover = (ArrayList<Double>) stockDate_Data.get(0); 
				stockDate = (ArrayList<Calendar>) stockDate_Data.get(1); 
			}else {
				//System.out.println("=========== Null stock code = " + stockCode + " ===========");
				//continue;
			}
			
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
				
				int monthDiff = Util.getMonthDiff(cutOffDate, todayDate);
				
				if(monthDiff < 12 && monthDiff >= 0) {  // within the interested period
					eachMonthDate.get(monthDiff).add(todayDate);
					eachMonthTurnonver.get(monthDiff).add(todayTurnover);
				}
				
			}
			
			// ============== calculate turnover velocity ==============
			// eachMonthTurnonverMedian is an ArrayList<Double> with 12 elements
			//eachMonthTurnonverMedian.get(0) is the median turnover of Jun 2017 (if the cutoff date is 30Jun 2017)
			// if for some month, the data is null, it represents that no data for that month
			ArrayList<Double> eachMonthTurnonverMedian = getMedian(eachMonthTurnonver);
			allStockEachMonthMedianTurnover.add(eachMonthTurnonverMedian);
			
			ArrayList<Double> eachMonthAdjFreefloatFactor = getEachMonthAdjFreefloatFactor(stockCode, cutOffDate, freefloatShift);
			allStockEachMonthAdjFreefloatFactor.add(eachMonthAdjFreefloatFactor);
			
			ArrayList<Double> eachMonthTotalShares = getEachMonthTotalShares(stockCode, cutOffDate);
			allStockEachMonthTotalShares.add(eachMonthTotalShares);
			
			// to calculate
			ArrayList<Double> eachMonthTurnoverVelocity = new ArrayList<Double>();
			
			ArrayList<Double> isEachMonthPassTurnoverScreening = new ArrayList<Double>();
			int okOver12Month = 0;
			int okOver6Month = 0;
			int numOfNull = 0; // num of Null, if Null, this month is not counted for screening
			
			for(int j = 0; j < eachMonthTurnonverMedian.size(); j++) {
				Double thisMonthTurnoverVelocity = null;
				if(eachMonthTurnonverMedian.get(j) != null)
					thisMonthTurnoverVelocity = eachMonthTurnonverMedian.get(j) / (eachMonthTotalShares.get(j) * eachMonthAdjFreefloatFactor.get(j));
				else
					numOfNull++;
				eachMonthTurnoverVelocity.add(thisMonthTurnoverVelocity);
				
				if(thisMonthTurnoverVelocity == null){
					isEachMonthPassTurnoverScreening.add(-1.0); // this month is not counted for screening test
				}else{
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
				
			}
			allStockEachMonthTurnoverVelocity.add(eachMonthTurnoverVelocity);
			allStockPast12MonthPassTurnoverScreen.add((double) okOver12Month);
			allStockPast6MonthPassTurnoverScreen.add((double) okOver6Month);
			allStockEligibleTurnoverScreen.add((double) 12 - numOfNull);
			
			//============== screening using turnover velocity ================
			boolean isPassed = true;
			// first see if it has months not counted for screening
			if(numOfNull > 0){
				int numOfTradingMonth = 12 - numOfNull;
				// then see if the num of trading month >= 6 months
				if(numOfTradingMonth >= 6){
					// if num of trading month >= 6 months, to pass the screening, there should be at most 1 failed month
					if(numOfTradingMonth - okOver12Month > 1 ){
						isPassed = false;
					}
				}else if(numOfTradingMonth > 0){
					// if num of trading month < 6 months, all months should not be failed
					if(numOfTradingMonth > okOver12Month)
						isPassed = false;
				}else {
					isPassed = false;
				}
			}else{
				if(!(okOver12Month >= 10 && okOver6Month >= 5)) 
					isPassed = false;
			}
			
			if(isPassed){
				eligibleStocks.add(stockCode);
				allStockPassTurnoverScreen.add(1.0);
			}else{
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
	public ArrayList<Double> getMedian(ArrayList<ArrayList<Double>> eachMonthTurnonver) throws Exception{
		ArrayList<Double> medianTurnover = new ArrayList<Double>();
		
		for(int i = 0; i < eachMonthTurnonver.size(); i++) {
			ArrayList<Double> thisMonthTurnoverData = eachMonthTurnonver.get(i);
			
			Double thisMonthTurnoverMedian = null;
			
			if(thisMonthTurnoverData != null){ // if not listed this month
				// to see if the trading volume is not always 0 this month
				double cumTurn = 0.0;
				for(int j = 0; j < thisMonthTurnoverData.size(); j++){
					cumTurn = cumTurn + thisMonthTurnoverData.get(j);
				}
				if(cumTurn > 0)
					thisMonthTurnoverMedian = getSingleArrayMedian(thisMonthTurnoverData);
			}
			
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
	public Double getSingleArrayMedian(ArrayList<Double> singleMonthData) throws Exception{
		Double median = null;
		
		//Collections.sort(singleMonthData, new SortArrayListDouble());
		
		Collections.sort(singleMonthData, new Comparator<Double>() {
			 public int compare(Double arg0, Double arg1) {
	                return arg0.compareTo(arg1);
	            }
		});  // 
		
		int len = singleMonthData.size() ;
		
		if(len % 2 == 0) { // even number
			median = (singleMonthData.get((int)(len/2)) + singleMonthData.get((int)(len/2) - 1) ) / 2;
		}else {
			median = singleMonthData.get((len-1) / 2);
		}
		
		return median;
	}

	/**
	 * for a specific stock, to return a 12-element arraylist with each element be the freefloat pct for that month end
	 * month closer to the cutoff date should be at the front. e.g.
	 * @param stockCode
	 * @param cutoffDate
	 * @return
	 * @throws Exception
	 */
	public ArrayList<Double> getEachMonthAdjFreefloatFactor(String stockCode, Calendar cutoffDate, double shift) throws Exception{
		if(allStockFreefloat == null || allStockFreefloat.size() == 0) { // read the data
			String filePath = "D:\\stock data\\Indexes Prediction\\HSCI\\free float.csv";
			allStockFreefloat = ParseFile.parseFreefloatAndTotalShares(filePath);
			System.out.println("allStockFreefloat.size = " + allStockFreefloat.size());
		}
		
		ArrayList<Object> dataArr = allStockFreefloat.get(stockCode);
		
		ArrayList<Double> freefloatArr = (ArrayList<Double>) dataArr.get(0);
		ArrayList<Calendar> dateArr = (ArrayList<Calendar>) dataArr.get(1);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
		//System.out.println("--dateArr.get(1) = " + sdf.format(dateArr.get(1).getTime()));
		//Collections.sort(dateArr, Collections.reverseOrder()); // descending 
		
		ArrayList<Double> eachMonthAdjFreefloat = new ArrayList<Double>();
		for(int i = 0; i < 12; i++) { // construct eachMonthAdjFreefloat 
			eachMonthAdjFreefloat.add((double) -i-1);
		}
		
		for(int i = 0; i < dateArr.size(); i++) {
			Calendar thisMonthCal = dateArr.get(i);

			int diff = Util.getMonthDiff(cutoffDate, thisMonthCal);
			if(diff < 12 && diff >= 0) {
				double freefloatToSet = freefloatArr.get(i) + shift;
				if(freefloatToSet > 1)
					freefloatToSet = 1;
				if(freefloatToSet <= 0)
					freefloatToSet = 0.01;
				
				eachMonthAdjFreefloat.set(diff, freefloatToSet);
				//System.out.println("-- free float, data to add: " + sdf.format(thisMonthCal.getTime()));
			}
		}
		
		return eachMonthAdjFreefloat;
	}
	
	/**
	 * for a specific stock, to return a 12-element arraylist with each element be the total shares for that month end
	 * @param stockCode
	 * @param cutoffDate
	 * @return
	 * @throws Exception
	 */
	public ArrayList<Double> getEachMonthTotalShares(String stockCode, Calendar cutoffDate) throws Exception{
		if(allStockTotalShares == null || allStockTotalShares.size() == 0) { // read the data
			String filePath = "D:\\stock data\\Indexes Prediction\\HSCI\\total shares.csv";
			allStockTotalShares = ParseFile.parseFreefloatAndTotalShares(filePath);
		}
		
		ArrayList<Object> dataArr = allStockTotalShares.get(stockCode);
		ArrayList<Double> totalSharesArr = (ArrayList<Double>) dataArr.get(0);
		ArrayList<Calendar> dateArr = (ArrayList<Calendar>) dataArr.get(1);
		//Collections.sort(dateArr, Collections.reverseOrder()); // descending
		
		ArrayList<Double> eachMonthTotalShares = new ArrayList<Double>();
		for(int i = 0; i < 12; i++) { // construct eachMonthTotalShares 
			eachMonthTotalShares.add((double) -i);
		}
		
		for(int i = 0; i < dateArr.size(); i++) {
			Calendar thisMonthCal = dateArr.get(i);
			int diff = Util.getMonthDiff(cutoffDate, thisMonthCal);
			
			if(diff < 12 & diff >= 0) {
				eachMonthTotalShares.set(diff, totalSharesArr.get(i));
			}
		}
		
		return eachMonthTotalShares;
	}

	// to output the screening result
	public void outputScreeningResults(String outputFilePath) throws Exception{
		FileWriter fw = new FileWriter(outputFilePath);
		String header = "";  // header
		ArrayList<String> toWrite = new ArrayList<String> ();
		
		//header
		header = "stock code,num OK last 12 months,num OK last 6 months,num eligible month,is passed,free float shift";
		SimpleDateFormat sdf = new SimpleDateFormat("MMM yyyy"); 
		SimpleDateFormat sdf2 = new SimpleDateFormat(CUT_OFF_DATE_DATE_FORMAT); 
		for(int j = 0; j < 12; j++) {
			Calendar toOutputCal = Calendar.getInstance();
			toOutputCal.setTime(sdf2.parse(CUT_OFF_DATE));
			toOutputCal.add(Calendar.MONTH, -j);
			String jStr = sdf.format(toOutputCal.getTime());
			header = header + "," + jStr + " - total shares," + jStr + " - freefloat factor," + jStr + " - median turnover," + jStr + " - velocity";
		}
		toWrite.add(header);
		
		//content
		for(int i = 0; i < allStocks.size(); i++) {
			String lineToWrite = "";
			
			String stockCode = allStocks.get(i);
			String numOKLast12Month = String.valueOf(allStockPast12MonthPassTurnoverScreen.get(i));
			String numOKLast6Month = String.valueOf(allStockPast6MonthPassTurnoverScreen.get(i));
			String numEligibleMonth = String.valueOf(allStockEligibleTurnoverScreen.get(i));
			String isPassed = allStockPassTurnoverScreen.get(i) == 1.0?"1":"";
			
			lineToWrite = stockCode + "," + numOKLast12Month + "," + numOKLast6Month  + "," + numEligibleMonth + "," + isPassed + "," + String.valueOf(freefloatShift);
			
			// output each month's data
			for(int j = 0; j < 12; j++) {
				// out put 4 data for each month: total issued shares, adj freefloat factor, median turnover, velocity
				String totalIssuedShares = String.valueOf(allStockEachMonthTotalShares.get(i).get(j));
				String adjFreefloatFactor = String.valueOf(allStockEachMonthAdjFreefloatFactor.get(i).get(j));
				String medianTurnover = String.valueOf(allStockEachMonthMedianTurnover.get(i).get(j));
				String velocity = String.valueOf(allStockEachMonthTurnoverVelocity.get(i).get(j));
				
				lineToWrite = lineToWrite + "," + totalIssuedShares + "," + adjFreefloatFactor + "," + medianTurnover + "," + velocity;
			}// end of for(int j = ....
			
			toWrite.add(lineToWrite);
		}// end of for(int i = ....
		
		// write into file
		for(String line : toWrite)
			fw.write(line + "\n");
		
		fw.close();
	}
}
