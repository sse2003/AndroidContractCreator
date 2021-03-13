package org.sse.contracts.core;

import org.junit.Test;
import org.sse.contracts.Constants;
import org.sse.contracts.FontUtils;
import org.sse.contracts.core.classes.Class_тэст;
import org.sse.contracts.core.exceptions.ExpressionException;

import java.util.Calendar;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class ExpressionTest
{
    @Test
    public void testSimpleExpression() throws Exception
    {
        String expressionText = "Текст выражения, разные символы: (/%)";
        String expression = "##" + expressionText + "#";

        Expression exp = new Expression(expression);

        assertNotNull(exp.getParameters());
        assertEquals(exp.getParameters().getParameters().size(), 0);
        assertEquals(exp.getRequest(), expressionText);
        assertEquals(exp.getExpression(), expression);
    }

    @Test
    public void testExpressionWithOnlyParameters() throws Exception
    {
        String expression = "##.m='5' #";

        Expression exp = new Expression(expression);

        assertNotNull(exp.getParameters());
        assertEquals(exp.getParameters().getParameters().size(), 1);
        assertEquals(exp.getRequest(), "");
        assertEquals(exp.getExpression(), expression);
    }

    @Test
    public void testExpressionWithSpecialSpaceParameters() throws Exception
    {

        {
            /* Может встретится такое выражение. Только в этом случае нужно &nbsp; заменить на пробел  */
            String expression = "##.c='тэст'&nbsp;#";
            Expression exp = new Expression(expression);

            assertNotNull(exp.getParameters());
            assertEquals(exp.getParameters().getParameters().size(), 1);
            assertEquals(exp.getRequest(), Class_тэст.REQUEST);
            assertEquals(exp.getExpression(), expression);
        }

        {
            String expression = "##.c='тэст' &nbsp;#";
            Expression exp = new Expression(expression);

            assertNotNull(exp.getParameters());
            assertEquals(exp.getParameters().getParameters().size(), 1);
            assertEquals(exp.getRequest(), "");
            assertEquals(exp.getExpression(), expression);
        }
    }

    @Test
    public void testExpressionWithOneParameter() throws Exception
    {
        String parameter1 = "p";
        String value1 = "123";
        String expressionText = "Текст выражения";
        String expression = "##." + parameter1 + "='" + value1 + "' " + expressionText + "#";

        Expression exp = new Expression(expression);

        assertEquals(exp.getRequest(), expressionText);
        assertEquals(exp.getExpression(), expression);
        assertNotNull(exp.getParameters());
        assertEquals(exp.getParameters().getParameters().get(parameter1), value1);
    }

    @Test
    public void testExpressionWithSeveralParameters() throws Exception
    {
        String params[] = {"t", "m", "s", "h"};
        String vals[] = {"12", "1024", "Строка с пробелами", "Выбери меня"};

        String expressionText = "Текст&nbsp;выражения&nbsp;";
        String actualText = "Текст выражения";
        String expressionString = String.format("##.%s='%s'.%s='%s'.%s='%s'.%s='%s' %s#", params[0], vals[0], params[1], vals[1], params[2], vals[2], params[3], vals[3], expressionText);

        Expression exp = new Expression(expressionString);

        assertEquals(exp.getRequest(), actualText);
        assertEquals(exp.getExpression(), expressionString);
        assertNotNull(exp.getParameters());
        assertEquals(exp.getParameters().getParameters().get(params[0]), vals[0]);
        assertEquals(exp.getParameters().getParameters().get(params[1]), vals[1]);
        assertEquals(exp.getParameters().getParameters().get(params[2]), vals[2]);
    }

    @Test
    public void testExpresionWithPercentParameters() throws Exception
    {
        String parameter1 = "t";
        String value1 = "100%";
        String parameter2 = "m";
        String value2 = "50%";
        String expressionText = "Текст&nbsp; выражения&nbsp;";
        String actualText = "Текст  выражения";
        String expression = "##." + parameter1 + "='" + value1 + "'." + parameter2 + "='" + value2 + "' " + expressionText + "#";

        Expression exp = new Expression(expression);

        assertEquals(exp.getRequest(), actualText);
        assertEquals(exp.getExpression(), expression);
        assertNotNull(exp.getParameters());
        assertEquals(exp.getParameters().getParameters().get(parameter1), value1);
        assertEquals(exp.getParameters().getParameters().get(parameter2), value2);
    }

    @Test
    public void testReplaceSpecialSymbolAfterParameter() throws Exception
    {
        String expression = "##.m='1'&nbsp;#";
        String actual = "##.m='1' #";

        assertEquals(Expression.replaceSpecialSpaceSymbolAfterParameter(expression), actual);

        expression = "##.s='&nbsp;'.m='5'&nbsp;#";
        actual = "##.s='&nbsp;'.m='5' #";

        assertEquals(Expression.replaceSpecialSpaceSymbolAfterParameter(expression), actual);

        expression = "##.t='10'.s='&nbsp;' Выражение 1#";
        actual = "##.t='10'.s='&nbsp;' Выражение 1#";
        assertEquals(Expression.replaceSpecialSpaceSymbolAfterParameter(expression), actual);

        expression = "##.t='10'.s='&nbsp;'&nbsp;#";
        actual = "##.t='10'.s='&nbsp;' #";
        assertEquals(Expression.replaceSpecialSpaceSymbolAfterParameter(expression), actual);

        expression = "##.t='10'.s='&nbsp;' &nbsp;#";
        actual = "##.t='10'.s='&nbsp;' &nbsp;#";
        assertEquals(Expression.replaceSpecialSpaceSymbolAfterParameter(expression), actual);
    }

    @Test
    public void testGetRequestString() throws Exception
    {
        Expression exp1 = new Expression("##.c='тэст' #");
        assertEquals(exp1.getRequest(), Class_тэст.REQUEST);

        Expression exp2 = new Expression("##.c='тэст' Другой запрос#");
        assertEquals(exp2.getRequest(), "Другой запрос");
    }

    @Test
    public void testGetEmptyResponse() throws Exception
    {
        String expression = "##.c='тэст' #";
        Expression exp = new Expression(expression);

        assertNotEquals(exp.makeResponse().length(), 0);
        assertEquals(exp.makeResponse(), exp.makeTemplate());
    }

    @Test
    public void testReplaceSpaceSymbol() throws Exception
    {

        String expressionString = "##.c='ФИО' " +
                "Должность&nbsp;и " +
                "ФИО продавца#";

        String actualString = "Должность и " +
                "ФИО продавца";

        Expression exp = new Expression(expressionString);
        assertEquals(exp.getRequest(), actualString);
    }

    @Test
    public void testExpresionTextWithUnknownClass() throws Exception
    {
        String expressionString = "##.c='неизвестный_класс' #";
        String actualString = "неизвестный класс";

        Expression exp = new Expression(expressionString);
        assertEquals(exp.getRequest(), actualString);
    }

    @Test
    public void testParametersPriority() throws Exception
    {
        int len = Class_тэст.TEMPLATE_LEN * 2;
        int actualLen = FontUtils.getSymbolWidthCorrection(len, Constants.TEMPLATE_SYMBOL);
        String expressionString = String.format("##.t='%s'.c='тэст' #", len);

        Expression exp = new Expression(expressionString);
        assertEquals(exp.getTemplateLength(), actualLen);
    }

    @Test
    public void testEqualsExpressionWithSpecialSymbols() throws Exception
    {
        String expressionString1 = "##Строка с пробелом#";
        String expressionString2 = "##Строка с" + Constants.SPACE_SYMBOL + "пробелом#";

        Expression exp1 = new Expression(expressionString1);
        Expression exp2 = new Expression(expressionString2);

        assertTrue(exp1.equals(exp2));
        assertEquals(exp1.hashCode(), exp2.hashCode());
    }

    @Test
    public void testParameter_t() throws Exception
    {
        String len1 = "10";
        int actualLen1 = FontUtils.getSymbolWidthCorrection(10, Constants.TEMPLATE_SYMBOL);

        String len2 = "50%";
        int actualLen2 = FontUtils.getSymbolCountInOneLine(Constants.TEMPLATE_SYMBOL) / 2;

        String expression1String = String.format("##.t='%s' Пусто#", len1);
        String expression2String = String.format("##.t='%s' Пусто#", len2);

        Expression exp1 = new Expression(expression1String);
        Expression exp2 = new Expression(expression2String);

        assertEquals(exp1.getTemplateLength(), actualLen1);
        assertEquals(exp2.getTemplateLength(), actualLen2);
    }

    @Test
    public void testParameter_m() throws Exception
    {
        String len1 = "10";
        int actualLen1 = FontUtils.getSymbolWidthCorrection(10, Constants.TEMPLATE_SYMBOL);

        String len2 = "50%";
        int actualLen2 = FontUtils.getSymbolCountInOneLine(Constants.TEMPLATE_SYMBOL) / 2;

        String expression1String = String.format("##.m='%s' Пусто#", len1);
        String expression2String = String.format("##.m='%s' Пусто#", len2);

        Expression exp1 = new Expression(expression1String);
        Expression exp2 = new Expression(expression2String);

        assertEquals(exp1.getNormalTextMinimumLength(), actualLen1);
        assertEquals(exp2.getNormalTextMinimumLength(), actualLen2);
    }

    @Test
    public void testExpressionLengthWithoutResponce() throws Exception
    {
        String expressionText = "123";
        String expression = "##" + expressionText + "#";
        int actualLen = FontUtils.getSymbolWidthCorrection(expression.replaceAll(Constants.TEMPLATE_SYMBOL, " ").length(), Constants.TEMPLATE_SYMBOL);

        Expression exp = new Expression(expression);

        assertEquals(exp.makeResponse().length() / Constants.TEMPLATE_SYMBOL.length(), actualLen);
        assertEquals(exp.makeTemplate().length() / Constants.TEMPLATE_SYMBOL.length(), actualLen);
    }

    @Test
    public void testParameter_h() throws Exception
    {
        String sel[] = {"Вариант 1", "Вариант 2", "Вариант 3"};
        final String sep = Parameters.CHOIСES_SEPARATOR;

        String expression0String = String.format("## Empty#");
        String expression1String = String.format("##.h='%s' #", sel[0]);
        String expression2String = String.format("##.h='%s%s%s%s%s' #", sel[0], sep, sel[1], sep, sel[2]);

        Expression exp0 = new Expression(expression0String);
        Expression exp1 = new Expression(expression1String);
        Expression exp2 = new Expression(expression2String);


        assertNull(exp0.getChoices());
        assertEquals(exp1.getChoices()[0], sel[0]);

        String[] choices = exp2.getChoices();
        assertEquals(choices[0], sel[0]);
        assertEquals(choices[1], sel[1]);
        assertEquals(choices[2], sel[2]);
    }

    @Test
    public void testEqualsAndHashInExpressionWithParameter_c() throws Exception
    {
        String expression0String = "##.c='цена' Цена#"; //c = en
        String expression1String = "##.с='цена' Цена#"; //c = ru

        Expression exp0 = new Expression(expression0String);
        Expression exp1 = new Expression(expression1String);

        assertEquals(exp0, exp1);
        assertEquals(exp0.hashCode(), exp1.hashCode());
        assertEquals(exp0.getExpression(), exp1.getExpression());
    }

    @Test
    public void testMultilines() throws Exception
    {
        Expression exp = new Expression("## Empty#");

        exp.setResponse("Line1\nLine2\r\nLine3");
        assertEquals(exp.makeResponse(), "Line1" + Constants.HTML_NEW_LINE_TAG + "Line2" + Constants.HTML_NEW_LINE_TAG + "Line3");
    }

    @Test
    public void testParameter_g() throws Exception
    {
        String expression1String = "##Пример для параметра .g#";
        String expression2String = "##.g='' Пример для параметра .g#";
        String expression3String = "##.g='group" + Constants.SPACE_SYMBOL + "1' Пример для параметра .g#";

        Expression exp1_g_null = new Expression(expression1String);
        Expression exp2_g_null = new Expression(expression2String);

        Expression exp1_g1 = new Expression(expression1String, "group 1");
        Expression exp1_g2 = new Expression(expression1String, "group 2");

        Expression exp2_g1 = new Expression(expression2String, "group 1");
        Expression exp2_g2 = new Expression(expression2String, "group 2");

        Expression exp3_g1 = new Expression(expression3String, "group 1");
        Expression exp3_g2 = new Expression(expression3String, "group 2");


        assertEquals(exp1_g_null, exp2_g_null);
        assertEquals(exp1_g_null.hashCode(), exp2_g_null.hashCode());
        assertEquals(exp1_g_null.getExpression(), expression1String);

        assertNotEquals(exp1_g1, exp1_g2);
        assertNotEquals(exp1_g1.hashCode(), exp1_g2.hashCode());

        assertEquals(exp2_g1, exp2_g2);
        assertEquals(exp2_g1.hashCode(), exp2_g2.hashCode());
        assertEquals(exp2_g1.getExpression(), expression2String);

        assertEquals(exp3_g1, exp3_g2);
        assertEquals(exp3_g1.hashCode(), exp3_g2.hashCode());
        assertEquals(exp3_g1.getGroup(), "group 1");
        assertEquals(exp3_g2.getGroup(), "group 1");
        assertEquals(exp3_g1.getExpression(), expression3String);
   }

    @Test
    public void testParameter_g_position() throws ExpressionException
    {
        String expression0String = "##Выражение 1#";
        String expression1String = "##.g='' Выражение 1#";

        String expression2String = "##.t='5' Выражение 1#";
        String expression3String = "##.t='5'.g='' Выражение 1#";

        String expression4String = "##.t='5' Выражение 1#";
        String expression5String = "##.g=''.t='5' Выражение 1#";

        Expression exp0 = new Expression(expression0String);
        Expression exp1 = new Expression(expression1String);
        Expression exp2 = new Expression(expression2String);
        Expression exp3 = new Expression(expression3String);
        Expression exp4 = new Expression(expression4String);
        Expression exp5 = new Expression(expression5String);

        assertEquals(exp0, exp1);
        assertEquals(exp0.hashCode(), exp1.hashCode());
        assertNotEquals(exp0.getExpression(), exp1.getExpression());

        assertEquals(exp2, exp3);
        assertEquals(exp2.hashCode(), exp3.hashCode());
        assertNotEquals(exp2.getExpression(), exp3.getExpression());

        assertEquals(exp4, exp5);
        assertEquals(exp4.hashCode(), exp5.hashCode());
        assertNotEquals(exp4.getExpression(), exp5.getExpression());
    }
}