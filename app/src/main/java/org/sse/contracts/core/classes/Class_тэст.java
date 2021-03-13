package org.sse.contracts.core.classes;


public class Class_тэст extends AbstractClass {

    public static final String REQUEST = "Запрос для тэста";
    public static final int TEMPLATE_LEN = 10;

    @Override
    public String getRequest() {
        return REQUEST;
    }

    @Override
    public String getTemplate() {
        return "_________";
    }

    @Override
    public String getTemplateLength() {
        return "" + TEMPLATE_LEN;
    }
}
