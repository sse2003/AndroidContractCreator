package org.sse.contracts.core;

import android.support.annotation.NonNull;
import trikita.log.Log;

import org.sse.contracts.Constants;
import org.sse.contracts.Utils;
import org.sse.contracts.core.exceptions.ExpressionException;
import org.sse.contracts.core.exceptions.NestedContractTypeException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HtmlParser
{
    private static final String TAG = "HtmlParser";
    private final String source;
    private final ContractType type;
    private Set<Expression> expressions = null;

    public HtmlParser(String source, ContractType type)
    {
        this(source, type, true);
    }

    private HtmlParser(String source, ContractType type, boolean skippingCRLF)
    {
        if (skippingCRLF)
        {
            source = source.replaceAll("\r\n", " ").replaceAll("\n", " ");
        }

        this.source = Utils.replace_cRu_to_cEn(source);
        this.type = type;
    }

    public Set<Expression> getExpressions() throws ExpressionException, NestedContractTypeException
    {
        if (expressions != null) return expressions;

        expressions = makeExpressions(source);
        return expressions;
    }

    private Set<Expression> makeExpressions(String src) throws NestedContractTypeException, ExpressionException
    {
        Set expressions = new LinkedHashSet<>();

        if (type != null)
        {
            src = Utils.removeAllContractTypeClassesFromHtmlExcept(src, type);
        }

        Map<Integer, HtmlString> groups = getGroups(src);

        Matcher matcher = Constants.PATTERN_EXPRESSION.matcher(src);

        while (matcher.find())
        {
            int pos = matcher.start();
            HtmlString group = checkGroup(groups, pos);
            String expression = matcher.group(Constants.GROUP_EXPRESSION_ALL);
            expressions.add(new Expression(expression, group));
        }

        return expressions;
    }

    public Expression getExpressionByHash(int hash) throws ExpressionException, NestedContractTypeException
    {
        for (Expression exp : getExpressions())
            if (exp.hashCode() == hash) return exp;

        return null;
    }

    private HtmlString checkGroup(Map<Integer, HtmlString> groups, int pos)
    {
        if (groups == null) return null;

        Iterator<Integer> it = groups.keySet().iterator();

        HtmlString last = null;

        while (it.hasNext())
        {
            Integer p = it.next();
            if (pos > p) last = groups.get(p);
            else return last;
        }

        return last;
    }

    @NonNull
    private Map<Integer, HtmlString> getGroups(String source)
    {
        Matcher matcher = Constants.PATTERN_GROUP.matcher(source);
        Map<Integer, HtmlString> groups = new LinkedHashMap<>();

        while (matcher.find())
        {
            int groupPos = matcher.start();
            HtmlString groupName = new HtmlString(matcher.group(Constants.GROUP_GROUP_TEXT));
            groups.put(groupPos, groupName);
        }
        return groups;
    }

    public String buildTemplate(boolean previewMode) throws ExpressionException, NestedContractTypeException
    {
        return build(false, previewMode, null);
    }

    public String buildNormal(boolean previewMode) throws ExpressionException, NestedContractTypeException
    {
        return build(true, previewMode, null);
    }

    private String build(boolean normal, boolean previewMode, Expression previewExpression) throws ExpressionException, NestedContractTypeException
    {
        String result = "";

        String src = source;
        if (type != null)
        {
            src = Utils.removeAllContractTypeClassesFromHtmlExcept(src, type);
        }

        Map<Integer, HtmlString> groups = getGroups(src);

        String[] results = src.split(Constants.PATTERN_GROUP.pattern());

        if (groups.size() + 1 != results.length)
            throw new ExpressionException("Не совпадают списки групп: " + groups.size() + " != " + results.length);

        Iterator<HtmlString> gIt = groups.values().iterator();
        HtmlString group = null;
        for (String block : results)
        {
            String tmp = block;

            for (Expression localExp : makeExpressions(block))
            {
                if (!localExp.isSetPriorityGroup())
                    localExp.setGroup(group);

                Expression globalExp = getExpressionByHash(localExp.hashCode());

                String replacement = normal ? globalExp.makeResponse() : globalExp.makeTemplate();
                if (previewMode)
                {
                    if (previewExpression == null || previewExpression.equals(localExp))
                        replacement = Utils.makePreviewColorString(replacement, localExp.hashCode());
                }

                tmp = replaceExpressions(tmp, localExp, replacement);
            }
            result += tmp;
            group = gIt.hasNext() ? gIt.next() : null;
        }

        return result;
    }

    @NonNull
    private String replaceExpressions(String source, Expression exp, String replacement)
    {
        String regex = Pattern.quote(exp.getExpression());
        String replacing = Matcher.quoteReplacement(replacement);

        source = source.replaceAll(regex, replacing);
        return source;
    }

    public List<ContractType> getCompatibleContractTypes()
    {
        List<ContractType> result = new ArrayList<>(4);

        for (ContractType type : ContractType.values())
        {
            if (source.contains("class=\"" + type.getStyleClassName())) result.add(type);
        }

        return result;
    }

    public String getPreview(Expression expression) throws NestedContractTypeException, ExpressionException
    {
        String all = build(true, true, expression);

        return makePreview(all, expression);
    }

    private String makePreview(String all, Expression baseExpression)
    {
        final String start[] = {"<br", "</h1", "</p", "<td"};
        final String end[] = {"<br", "<h1", "<p", "</td"};
        final String closeTag = ">";

        int pos = all.indexOf("id=\"" + baseExpression.hashCode() + "\"");
        if (pos == -1) return null;

        int startPos = 0;
        String startString = all.substring(0, pos);
        for (String s : start)
        {
            int find = startString.lastIndexOf(s);
            if (find != -1)
            {
                find = startString.indexOf(closeTag, find) + closeTag.length();
                startPos = Math.max(find, startPos);
            }
        }

        String endString = all.substring(pos);
        int endPos = Integer.MAX_VALUE;
        for (String s : end)
        {
            int find = endString.indexOf(s);
            if (find != -1)
            {
                endPos = Math.min(find, endPos);
            }
        }

        if (startPos == 0 || endPos == Integer.MAX_VALUE) return null;

        return prepareStringForPreview(all.substring(startPos, pos + endPos));
    }

    private String prepareStringForPreview(String str)
    {
        str = Utils.removeEndSpaces(Utils.removeStartSpaces(str));
        str = Utils.replaceStyleUnderlineSpanToTagU(str);
        str = Utils.removeSpanTags(str);

        return str;
    }
}
