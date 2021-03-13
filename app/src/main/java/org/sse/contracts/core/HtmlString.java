package org.sse.contracts.core;

import org.sse.contracts.Constants;

public class HtmlString
{
    private String source;

    public HtmlString(String source)
    {
        this.source = source;
    }

    public String toString()
    {
        return source;
    }

    public String getTextOnly()
    {
        String result = source.replaceAll(Constants.SPACE_SYMBOL, " ");
        return result.replaceAll("\r|\r\n", " ");
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null) return false;
        if (getClass() != o.getClass() && !(o instanceof String)) return false;

        return source != null ? source.equals(o.toString()) : o == null;
    }

    @Override
    public int hashCode()
    {
        return source != null ? source.hashCode() : 0;
    }
}
