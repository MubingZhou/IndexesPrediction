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
			Map<String, ArrayList<Object>> allStockData = 
					ParseFile.parseFile("D:\\stock data\\Indexes Prediction\\HSCI\\stock_connect_turnover_screen.csv");
			
			
			TurnoverScreening ts = new TurnoverScreening(ParseFile.parseStockList("D:\\stock data\\Indexes Prediction\\HSCI\\stock list.csv"),allStockData);
			
			ts.outputScreeningResults("D:\\stock data\\Indexes Prediction\\HSCI\\output.csv");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
