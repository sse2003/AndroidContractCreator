package org.sse.contracts.core.classes;

import java.text.SimpleDateFormat;
import java.util.Calendar;


public class Class_фио extends AbstractClass {

    @Override
    public String getRequest() {
        return "ФИО";
    }
    @Override
    public String getTemplateLength() {
        return "40%";
    }

}
