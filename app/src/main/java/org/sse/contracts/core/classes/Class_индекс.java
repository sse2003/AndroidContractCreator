package org.sse.contracts.core.classes;

import android.text.InputType;

public class Class_индекс extends AbstractClass {

    @Override
    public String getTemplateLength() {
        return "6";
    }

    @Override
    public String getRequest() {
        return "Индекс";
    };

    @Override
    public Integer getInputType() {
        return InputType.TYPE_CLASS_NUMBER;
    }
}
