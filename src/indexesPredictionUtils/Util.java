package indexesPredictionUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Util {
	public static final String DATE_FORMAT = "dd/MM/yyyy";
	public static final String CUT_OFF_DATE = "30/06/2017";	
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
		SimpleDateFormat sdf =   new SimpleDateFormat(Util.DATE_FORMAT);
		Calendar cal = Calendar.getInstance();
		cal.setTime(sdf.parse(Util.CUT_OFF_DATE));
		
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
		SimpleDateFormat sdf =   new SimpleDateFormat(Util.DATE_FORMAT);
		Calendar cal = Calendar.getInstance();
		cal.setTime(sdf.parse(Util.CUT_OFF_DATE));
		
		return getFirstDayOfMonth(cal);
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
}
