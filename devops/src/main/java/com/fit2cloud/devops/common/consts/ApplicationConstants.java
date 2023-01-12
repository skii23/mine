package com.fit2cloud.devops.common.consts;

import java.util.ArrayList;
import java.util.List;

/**
 * @author wisonic
 */
public class ApplicationConstants {
    public final static List<String> WEEKDAY_LIST = new ArrayList<>();
    static {
        WEEKDAY_LIST.add("weekday.MONDAY");
        WEEKDAY_LIST.add("weekday.TUESDAY");
        WEEKDAY_LIST.add("weekday.WEDNESDAY");
        WEEKDAY_LIST.add("weekday.THURSDAY");
        WEEKDAY_LIST.add("weekday.FRIDAY");
        WEEKDAY_LIST.add("weekday.SATURDAY");
        WEEKDAY_LIST.add("weekday.SUNDAY");
    }
}
