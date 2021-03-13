package org.sse.contracts.core;


public enum ContractType
{
    ФИЗ_ФИЗ("typeFF", "Физ-Физ"),
    ЮР_ЮР("typeUU", "Юр-Юр"),
    ФИЗ_ЮР("typeFU", "Физ-Юр"),
    ЮР_ФИЗ("typeUF", "Юр-Физ");

    private String styleClassName;
    private String description;

    ContractType(String styleClassName, String description)
    {
        this.styleClassName = styleClassName;
        this.description = description;
    }

    public String getStyleClassName()
    {
        return styleClassName;
    }

    public String getDescription()
    {
        return description;
    }
}
