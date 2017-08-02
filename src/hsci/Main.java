package hsci;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import indexesPredictionUtils.Util;

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
			Map<String, ArrayList<Object>> allStockData2 = 
					ParseFile.parseFile("D:\\stock data\\Indexes Prediction\\HSCI\\2016H1 - 2017H1 turnover data (600 stocks).csv");
			
			//double[] shiftArr = {0,0.05,0.1,0.15,0.2,-0.05,-0.1,-0.15,-0.2,1};
			double[] shiftArr = {0,0.2,1};
			String CUT_OFF_DATE = "31/12/2016";
			String CUT_OFF_DATE_DATE_FORMAT = "dd/MM/yyyy";
			
			
			for(double shift: shiftArr) {
				TurnoverScreening ts = new TurnoverScreening(CUT_OFF_DATE, CUT_OFF_DATE_DATE_FORMAT, shift);
				
				ts.turnoverScreening(ParseFile.parseStockList("D:\\stock data\\Indexes Prediction\\HSCI\\stock list - 600 stocks.csv"),allStockData2);
				ts.outputScreeningResults("D:\\stock data\\Indexes Prediction\\HSCI\\output - shift=" + String.valueOf(shift) + ".csv");
			}
			
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
