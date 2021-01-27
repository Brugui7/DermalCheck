package com.brugui.dermalcheck.utils;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class Constants {
    private static final String SUBSCRIBE_TO = "DermalCheck";
    public static final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd/MM/yyyy");
    public static final SimpleDateFormat D_M_Y_H_M = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    public static SimpleDateFormat YYYY_MM_DD_HH_MM_SS = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.UK);


}
