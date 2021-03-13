package org.sse.contracts.core;

import org.sse.contracts.Utils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parameters
{
    private static final Pattern PATTERN_SUB_PARAMETERS = Pattern.compile("(?s)\\.(.+?)='(.*?)'");

    private Map<String, HtmlString> parameters = new LinkedHashMap<>();
    public static final String CHOIСES_SEPARATOR = "/";

    enum Params
    {
        t,  // Длинна пустого шаблона. Заполняется символом по умолчанию.
        m,  // Минимальная длинна текста. Остаток заполняется символом по умолчанию.
        s,  // Символ по умолчанию.
        c,  // En, Принадлежность к классу.
        с,  // Ru, Принадлежность к классу.
        h,  // Список выбора.
        g;  // Принадлежность выражения к группе. Имеет максимльный приоритет.

        public static boolean contains(String param)
        {
            try
            {
                return Params.valueOf(param) != null;
            } catch (IllegalArgumentException ex)
            {
                return false;
            }
        }
    }

    public Parameters()
    {

    }

    public Parameters(String str)
    {
        Matcher matcher = PATTERN_SUB_PARAMETERS.matcher(str);
        while (matcher.find())
            parameters.put(matcher.group(1), new HtmlString(matcher.group(2)));
    }

    public Map<String, HtmlString> getParameters()
    {
        return parameters;
    }

}
