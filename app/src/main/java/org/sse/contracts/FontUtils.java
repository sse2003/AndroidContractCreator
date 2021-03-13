package org.sse.contracts;

import android.support.annotation.NonNull;


public class FontUtils
{
    public static final int linePercentSizeToCharCount(int percent, @NonNull String defaultSymbol)
    {
        return percent * getSymbolCountInOneLine(defaultSymbol) / 100;
    }

    public static final int getSymbolCountInOneLine(@NonNull String defaultSymbol)
    {

        switch (defaultSymbol)
        {
            case "_":
                return 83;
            default:
                return 166;
        }
    }

    public static final int getSymbolWidthCorrection(int size, @NonNull String defaultSymbol)
    {
        float f = size;
        float k;

        switch (defaultSymbol)
        {
            case "_":
                k = 1.5f;
                break;
            default:
                k = 3.0f;
                break;
        }

        return (int) (f * k);
    }

}
