package indexesPredictionUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


public class Util {
	//public static final String DATE_FORMAT = "dd/MM/yyyy";
	//public static final String CUT_OFF_DATE = "31/12/2016";	
	public static final int PAST_N_MONTH = 12;
	public static final int PAST_N_MONTH2 = 6;
	
	/**
	 * get the last day of this month
	 * @param cal
	 * @return
	 * @throws Exception
	 */
	public static Calendar getLastDayOfMonth(Calendar cal) throws Exception{
		cal.add(Calendar.MONDAY, 1);  	// move to next month
		cal.set(Calendar.DATE, 1);		// set to the first day of next month
		cal.add(Calendar.DATE, -1);		// move forward by one day, get the last day of this month
		
		return cal;
	}
	
	public static  Calendar getLastDayOfMonth(String date, String dateFormat) throws Exception{
		SimpleDateFormat sdf =   new SimpleDateFormat(dateFormat);
		Calendar cal = Calendar.getInstance();
		cal.setTime(sdf.parse(date));
		
		return getLastDayOfMonth(cal);
	}
	
	/**
	 * get the first day of a month
	 * @param cal
	 * @return
	 * @throws Exception
	 */
	public static  Calendar getFirstDayOfMonth(Calendar cal) throws Exception{
		cal.set(Calendar.DATE, 1);		// set to the first day of next month
		
		return cal;
	}
	public static  Calendar getFirstDayOfMonth(String date, String dateFormat) throws Exception{
		SimpleDateFormat sdf =   new SimpleDateFormat(dateFormat);
		Calendar cal = Calendar.getInstance();
		cal.setTime(sdf.parse(date));
		
		return getFirstDayOfMonth(cal);
	}
	
	/**
	 * get the month difference between 2 date. If c1 is before c2, the diff would be negative
	 * e.g. if c1 = 2017/07/07, c2 = 2017/06/30, it will return 1 
	 * @param c1
	 * @param c2
	 * @return
	 * @throws Exception
	 */
	public static int getMonthDiff(Calendar c1, Calendar c2) throws Exception{
		int month1 = c1.get(Calendar.MONTH);
		int year1 = c1.get(Calendar.YEAR);
		
		int month2 = c2.get(Calendar.MONTH);
		int year2 = c2.get(Calendar.YEAR);
		
		return  (year1 * 12 + month1) - (year2*12 + month2);
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
	
	public static boolean isDate(String str, String dateFormat) {
		boolean isOK = true;
		
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat);
			Date date = sdf.parse(str);
		}catch(ParseException pe) {
			isOK = false;
		}
		catch(Exception e) {
			isOK = false;
		}
		
		return isOK;
	}
	
	/**
	 * to convert a date array in String to in the form of ArrayList<Calendar>
	 * @param dateArr
	 * @param dateFormat
	 * @return date arrray
	 * @throws Exception
	 */
	public static ArrayList<Calendar> dateStr2Date(List<String> dateArr, String dateFormat) throws Exception{
		ArrayList<Calendar> toReturn = new ArrayList<Calendar> ();
		
		for(int i = 0; i < dateArr.size(); i++) {
			String todayDateStr = dateArr.get(i);
			
			toReturn.add(dateStr2Date(todayDateStr, dateFormat));
		}
		
		return toReturn;
	}
	
	public static Calendar dateStr2Date(String dateStr, String dateFormat) throws Exception{
		SimpleDateFormat sdf =   new SimpleDateFormat(dateFormat);
		Date date = sdf.parse(dateStr);
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		
		return cal;
	}
	
	public static String date2Str(Calendar cal, String dateFormat) throws Exception{
		SimpleDateFormat sdf =   new SimpleDateFormat(dateFormat);
		
		return sdf.format(cal.getTime());
	}
	
	/**
	 * to write data into a file. Line by line.
	 * @param data
	 * @param filePath
	 * @return
	 */
	public static boolean writeArrayListToFile(ArrayList<String> data, String filePath){
		boolean isOK = true;
		
		try{
			FileWriter fw = new FileWriter(filePath);
			for(String dataLine : data){
				fw.write(dataLine);
				
				String subStr = dataLine.substring(dataLine.length()-2, dataLine.length());
				if(!subStr.equals("\n"))
					fw.write("\n");
			}
			fw.close();
		}catch(Exception e){
			e.printStackTrace();
			isOK = false;
		}
		
		return isOK;
	}
	
	public static ArrayList<String> readFileByLine(String filePath) throws Exception{
		ArrayList<String> data = new ArrayList<String> ();
		
		InputStream input = new FileInputStream(new File(filePath));
		InputStreamReader inputStreamReader = new InputStreamReader(input);
		BufferedReader bufReader = new BufferedReader(inputStreamReader);
		
		String line = "";
		while ((line = bufReader.readLine()) != null) {
			data.add(line);
		}
		
		return data;
	}
	
	public static BufferedReader readFile_returnBufferedReader(String filePath) throws Exception{
		ArrayList<String> data = new ArrayList<String> ();
		
		InputStream input = new FileInputStream(new File(filePath));
		InputStreamReader inputStreamReader = new InputStreamReader(input);
		BufferedReader bufReader = new BufferedReader(inputStreamReader);
		
		return bufReader;
	}
	
	public static ArrayList<Calendar> changeStrDateToArray(String[] dateStrArr, String dateFormat) throws Exception{
		return changeStrDateToArray(new ArrayList<String>(Arrays.asList(dateStrArr)), dateFormat);
		
	}
	
	public static ArrayList<Calendar> changeStrDateToArray(ArrayList<String> dateStrArr, String dateFormat) throws Exception{
		ArrayList<Calendar> dateArr = new ArrayList<Calendar>();
		
		for(int i = 0; i < dateStrArr.size(); i++){
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat) ;
			Calendar thisCal = Calendar.getInstance();
			Date thisDate = sdf.parse(dateStrArr.get(i));
			thisCal.setTime(thisDate);
			dateArr.add(thisCal);
		}
		
		return dateArr;
		
	}
	
	/**
	 * to change date string to ArrayList<Calendar>
	 * e.g. input could be {"2017-07-27", "2017-07-28", "2017-07-29",....}
	 * output is an ArrayList<Calendar>
	 * @param dateStrArr
	 * @param dateFormat
	 * @return
	 * @throws Exception
	 */
	public static ArrayList<Calendar> changeStrDateToArray(List<String> dateStrArr, String dateFormat) throws Exception{
		ArrayList<Calendar> dateArr = new ArrayList<Calendar>();
		
		for(int i = 0; i < dateStrArr.size(); i++){
			SimpleDateFormat sdf = new SimpleDateFormat(dateFormat) ;
			Calendar thisCal = Calendar.getInstance();
			Date thisDate = sdf.parse(dateStrArr.get(i));
			thisCal.setTime(thisDate);
			dateArr.add(thisCal);
		}
		
		return dateArr;
		
	}
	
	/**
	 * get day difference. If c1 is before c2, it returns a negative number
	 * it could be a decimal
	 * @param c1
	 * @param c2
	 * @return
	 * @throws Exception
	 */
	public static double getDayDiff(Calendar c1, Calendar c2) throws Exception{
		Date d1 = c1.getTime();
		Date d2 = c2.getTime();
		
		return (double) (d1.getTime() - d2.getTime()) / (double) (1000 * 3600 * 24);
	}
}
