package org.sse.contracts.core.classes;

import android.text.InputType;

public class Class_число extends AbstractClass {

    @Override
    public String getTemplateLength() {
        return "5";
    }

    @Override
    public String getRequest() {
        return "Число";
    };

    @Override
    public Integer getInputType() {
        return InputType.TYPE_CLASS_NUMBER;
    }
}
