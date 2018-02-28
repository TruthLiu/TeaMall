package com.teamall.util;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

public class DateTimeUtil {

    //joda-time
    public static final String STANDARD_FORMAT="yyyy-MM-dd HH:mm:ss";

    //str->date
    //date->str

    //字符串转date数据类型
    public static Date strToDate(String dateTimeStr){
        DateTimeFormatter dateTimeFormatter= DateTimeFormat.forPattern(STANDARD_FORMAT);
        DateTime dateTime=dateTimeFormatter.parseDateTime(dateTimeStr);
        return dateTime.toDate();
    }


    public static  String dateToStr(Date date){
        if (date==null){
            return StringUtils.EMPTY;
        }
        DateTime dateTime=new DateTime(date);
        return dateTime.toString(STANDARD_FORMAT);
    }


//    public static void main(String[] args) {
//        System.out.println(DateToStr(new Date(),STANDARD_FORMAT));
//        System.out.println(strToDate("2018-1-1 18:18:18",STANDARD_FORMAT));
//    }


}
