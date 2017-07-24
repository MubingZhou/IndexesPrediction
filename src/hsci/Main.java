package hsci;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class Main {
	/*
	 * https://www.hsi.com.hk/HSI-Net/HSI-Net
	 * 
	 * Three steps for a specific stock to be included into the HSCI:
	 * 1. pass Market Capitalising Screening
	 * 2. pass Turnover Screening
	 * 
	 * For now, we assume we already have a list of stocks that passed Market Capitalising Screening 
	 */
	public static void main(String[] args) {
		try {
			ArrayList<String> stocksForScreening = new ArrayList<String>();
			stocksForScreening.add("1");  // current in HSCI
			stocksForScreening.add("6881"); // current in HSCI
			stocksForScreening.add("28"); // current not in HSCI
			ArrayList<Integer> canPass = new ArrayList<Integer>(); // 1- pass, 0 - not pass
			
			// 
			String dateFormat = ParseFile.DATE_FORMAT;
			SimpleDateFormat sdf =   new SimpleDateFormat(dateFormat);
			Calendar cutOffDate = Calendar.getInstance();
			cutOffDate.setTime(sdf.parse("30/06/2017"));
			
			String filePath = "";
		
			Map<String, ArrayList> stockDataMap = ParseFile.parseFile(filePath);
			
			for(int i = 0; i < stocksForScreening.size(); i++) {
				String stockCode = stocksForScreening.get(i);
				
				ArrayList stockDataArr = stockDataMap.get(stockCode);
				ArrayList<Date> dateArr = (ArrayList<Date>) stockDataArr.get(1);
				ArrayList<Double> dataArr = (ArrayList<Double>) stockDataArr.get(0);
				
				// for the past 12 months, do the turnover screening
				for(int j = 0; j < 12; j ++) {
					
				}
				
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
