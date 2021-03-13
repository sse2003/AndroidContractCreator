package org.sse.contracts.core;

import org.junit.Test;
import org.sse.contracts.Constants;
import org.sse.contracts.FontUtils;
import org.sse.contracts.Utils;
import org.sse.contracts.core.exceptions.ExpressionException;
import org.sse.contracts.core.exceptions.NestedContractTypeException;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;


public class HtmlParserTest
{
    @Test
    public void testSimpleExpression() throws Exception
    {
        String expressionText = "Текст выражения";
        String expression = "##" + expressionText + "#";
        String str = "Текст с пробелами, запятыми!" + expression + " что то еще.";

        HtmlParser parser = new HtmlParser(str, null);
        assertEquals(parser.getExpressions().size(), 1);

        Expression ex = parser.getExpressions().iterator().next();
        assertEquals(ex.getRequest(), expressionText);
    }

    @Test
    public void testSeveralExpressions() throws Exception
    {
        String expressionText = "Текст выражения";
        String expression = "##" + expressionText + "#";
        String str = "Текст с пробелами, запятыми!" + expression + " что то еще." + expression + " " + expression;

        HtmlParser parser = new HtmlParser(str, null);
        assertEquals(parser.getExpressions().size(), 1);

        Expression ex = parser.getExpressions().iterator().next();
        assertEquals(ex.getRequest(), expressionText);
    }


    @Test
    public void testNormalParser() throws Exception
    {
        String expression1 = "##Выражение1#";
        String expression2 = "##Выражение2#";
        String resultText1 = "Вставка 1";
        String resultText2 = "Вставка 2";

        {
            String source = "Текст с пробелами, запятыми!" + expression1 + " что то еще." + expression1;
            String actual = "Текст с пробелами, запятыми!" + resultText1 + " что то еще." + resultText1;

            HtmlParser parser = new HtmlParser(source, null);

            assertEquals(parser.getExpressions().size(), 1);
            ((Expression) (parser.getExpressions().toArray()[0])).setResponse(resultText1);
            String result = parser.buildNormal(false);
            assertEquals(result, actual);
        }

        {
            String source = "Текст с пробелами, запятыми!" + expression1 + " что то еще." + expression2;
            String actual = "Текст с пробелами, запятыми!" + resultText1 + " что то еще." + resultText2;

            HtmlParser parser = new HtmlParser(source, null);

            assertEquals(parser.getExpressions().size(), 2);
            ((Expression) (parser.getExpressions().toArray()[0])).setResponse(resultText1);
            ((Expression) (parser.getExpressions().toArray()[1])).setResponse(resultText2);

            String result = parser.buildNormal(false);
            assertEquals(result, actual);
        }
    }

    @Test
    public void testNormalParserWithSymbols() throws Exception
    {
        String expression1 = "##Выражение(1/2) %#";
        String resultText1 = "Вставка ! %";

        String source = "Текст с пробелами, запятыми!" + expression1 + " что то еще." + expression1;
        String actual = "Текст с пробелами, запятыми!" + resultText1 + " что то еще." + resultText1;

        HtmlParser parser = new HtmlParser(source, null);

        assertEquals(parser.getExpressions().size(), 1);
        ((Expression) (parser.getExpressions().toArray()[0])).setResponse(resultText1);
        String result = parser.buildNormal(false);
        assertEquals(result, actual);
    }

    @Test
    public void testMultilineNormalParser() throws Exception
    {
//        String expression1Text = "Выражение1";
//        String expression2Text = "Выражение2";
        int len1 = 10;
        String expression1 = "##.m='" + len1 + "'.s='_'\r\n Выражение1#";
        String expression2 = "##Выражение2\r\n#";
        String resultText1 = "Вставка 1";
        String resultText2 = "Вставка 2";

        int actualLen1 = FontUtils.getSymbolWidthCorrection(len1, "_");

        {
            String source = "Текст \r\n" + expression1 + "\r\n что то еще." + expression2;
            String fill1 = fill("_", Math.abs(actualLen1 - resultText1.length()));
            String actual = "Текст  " + resultText1 + fill1 + "  что то еще." + resultText2;

            HtmlParser parser = new HtmlParser(source, null);

            assertEquals(parser.getExpressions().size(), 2);
            ((Expression) (parser.getExpressions().toArray()[0])).setResponse(resultText1);
            ((Expression) (parser.getExpressions().toArray()[1])).setResponse(resultText2);

            String result = parser.buildNormal(false);
            assertEquals(result, actual);
        }
    }

    @Test
    public void testTemplateParser() throws Exception
    {
        String expression1Text = "Выражение1";
        String expression2Text = "Выражение2";
        String expression1 = "##" + expression1Text + "#";
        String expression2 = "##" + expression2Text + "#";
        String resultText1 = fill(Constants.TEMPLATE_SYMBOL, FontUtils.getSymbolWidthCorrection(expression1.length(), Constants.TEMPLATE_SYMBOL));
        String resultText2 = fill(Constants.TEMPLATE_SYMBOL, FontUtils.getSymbolWidthCorrection(expression2.length(), Constants.TEMPLATE_SYMBOL));

        {
            String source = "Текст с пробелами, запятыми!" + expression1 + " что то еще." + expression1;
            String actual = "Текст с пробелами, запятыми!" + resultText1 + " что то еще." + resultText1;

            HtmlParser parser = new HtmlParser(source, null);

            assertEquals(parser.getExpressions().size(), 1);
            ((Expression) (parser.getExpressions().toArray()[0])).setResponse("Любой текст");
            String result = parser.buildTemplate(false);
            assertEquals(result, actual);
        }

        {
            String source = "Текст с пробелами, запятыми!" + expression1 + " что то еще." + expression2;
            String actual = "Текст с пробелами, запятыми!" + resultText1 + " что то еще." + resultText2;

            HtmlParser parser = new HtmlParser(source, null);

            assertEquals(parser.getExpressions().size(), 2);
            ((Expression) (parser.getExpressions().toArray()[0])).setResponse(resultText1);
            ((Expression) (parser.getExpressions().toArray()[1])).setResponse(resultText2);

            String result = parser.buildTemplate(false);
            assertEquals(result, actual);
            assertEquals(result.length(), actual.length());
        }
    }

    @Test
    public void testTemplateParserWithParameters() throws Exception
    {
        int len1 = 21;
        int len2 = 10;
        int len3 = 12;
        String expression1 = "##.s='_' Выражение 1#.";      // exp len1 = 21
        String expression2 = "##.t='" + len2 + "%'.s='&nbsp;' Выражение 2#.";
        String expression3 = "##.s='&nbsp;' В1#.";          // &nbsp; = 1 символ, => длинна шаблона 12 символов.
        String expression4 = "##.c='ФИО'&nbsp;#.";          // 'с' английская
        String expression5 = "##.с='ФИО'&nbsp;#.";          // 'с' русская

        int actualLen1 = FontUtils.getSymbolWidthCorrection(len1, "_");
        int actualLen2 = FontUtils.linePercentSizeToCharCount(len2, "&nbsp;");
        int actualLen3 = FontUtils.getSymbolWidthCorrection(len3, "&nbsp;");


        String resultText1 = fill("_", actualLen1) + ".";
        String resultText2 = fill("&nbsp;", actualLen2) + ".";
        String resultText3 = fill("&nbsp;", actualLen3) + ".";

        {
            String source = "Текст " + expression1 + " что-то еще," + expression1;
            String actual = "Текст " + resultText1 + " что-то еще," + resultText1;

            HtmlParser parser = new HtmlParser(source, null);

            assertEquals(parser.getExpressions().size(), 1);
            ((Expression) (parser.getExpressions().toArray()[0])).setResponse("Любой текст");
            String result = parser.buildTemplate(false);
            assertEquals(result, actual);
        }

        {
            String source = "Текст " + expression1 + " что-то еще," + expression2;
            String actual = "Текст " + resultText1 + " что-то еще," + resultText2;

            HtmlParser parser = new HtmlParser(source, null);

            assertEquals(parser.getExpressions().size(), 2);
            String result = parser.buildTemplate(false);
            assertEquals(result, actual);
            assertEquals(result.length(), actual.length());
        }

        {
            String source = "Текст " + expression3 + ".";
            String actual = "Текст " + resultText3 + ".";

            HtmlParser parser = new HtmlParser(source, null);

            assertEquals(parser.getExpressions().size(), 1);
            String result = parser.buildTemplate(false);
            assertEquals(result, actual);
        }

        {
            String source = "Текст " + expression4 + ".";

            HtmlParser parser = new HtmlParser(source, null);

            assertEquals(parser.getExpressions().size(), 1);
            String result = parser.buildTemplate(false);
            assertNotEquals(result, source);
        }

        {
            String source = "Текст " + expression5 + ".";

            HtmlParser parser = new HtmlParser(source, null);

            assertEquals(parser.getExpressions().size(), 1);
            String result = parser.buildTemplate(false);
            assertNotEquals(result, source);
        }
    }

    private String fill(String s, int len)
    {
        String str = "";
        while (len-- > 0) str += s;
        return str;
    }

    @Test
    public void testNormalParserWithParameters() throws Exception
    {
        int len1 = 11;
        int len3 = 11;
        String expression1 = "##.s='_'.m='" + len1 + "' Выражение1#";
        String expression2 = "##.t='30'.s='_' Выражение1#"; // Игнорируется, т.к. параметр 't' используется при генерации пустого шаблона
        String expression3 = "##.s='&nbsp;'.m='" + len3 + "' Выражение1#";
        String resultText1 = "Вставка 1";
        String resultText2 = "Вставка 2";
        String resultText3 = "Вставка 3";

        int actualLen1 = FontUtils.getSymbolWidthCorrection(len1, "_");
        int actualLen3 = FontUtils.getSymbolWidthCorrection(len3, "&nbsp;");

        {
            String source = "Текст с пробелами, запятыми!" + expression1 + ", что то еще." + expression1 + ".";
            String fill1 = fill("_", Math.abs(actualLen1 - resultText1.length()));
            String actual = "Текст с пробелами, запятыми!" + resultText1 + fill1 + ", что то еще." + resultText1 + fill1 + ".";

            HtmlParser parser = new HtmlParser(source, null);

            assertEquals(parser.getExpressions().size(), 1);
            ((Expression) (parser.getExpressions().toArray()[0])).setResponse(resultText1);
            String result = parser.buildNormal(false);
            assertEquals(result, actual);
        }

        {
            String source = "Текст с пробелами, запятыми!" + expression1 + ", что то еще." + expression2 + ".";
            String fill1 = fill("_", Math.abs(actualLen1 - resultText1.length()));
            String actual = "Текст с пробелами, запятыми!" + resultText1 + fill1 + ", что то еще." + resultText2 + ".";

            HtmlParser parser = new HtmlParser(source, null);

            assertEquals(parser.getExpressions().size(), 2);
            ((Expression) (parser.getExpressions().toArray()[0])).setResponse(resultText1);
            ((Expression) (parser.getExpressions().toArray()[1])).setResponse(resultText2);

            String result = parser.buildNormal(false);
            assertEquals(result, actual);
        }

        {
            String source = "Текст с пробелами, запятыми!" + expression3 + ".";
            String fill3 = fill("&nbsp;", Math.abs(actualLen3 - resultText3.length()));
            String actual = "Текст с пробелами, запятыми!" + resultText3 + fill3 + ".";

            HtmlParser parser = new HtmlParser(source, null);

            assertEquals(parser.getExpressions().size(), 1);
            ((Expression) (parser.getExpressions().toArray()[0])).setResponse(resultText3);
            String result = parser.buildNormal(false);
            assertEquals(result, actual);
        }
    }

    @Test
    public void testGetContractTypes() throws Exception
    {
        String source = "Есть 4 стилевых класса для каждого из типа документа: <br />\n" +
                "ЮР-ЮР: typeUU <br />\n" +
                "ФИЗ-ФИЗ: typeFF<br />\n" +
                "ЮР-ФИЗ: typeUF<br />\n" +
                "ФИЗ-ЮР: typeFU<br />\n" +
                "Когда в документе нужно выделить блок, только для определенного типа\n" +
                "документа, его нужно обернуть в тег с нужным классом. И при генерации\n" +
                "документа с заданным типом, останется именно этот блок текста. Текст\n" +
                "без этих классов остается всегда.<br />\n" +
                "Например: <br />\n" +
                "Это слово останется только в документе ЮР-ЮР: <span class=\"typeUU\">Текст</span><br />\n" +
                "Это слово останется только в документе ФИЗ-ФИЗ: <span class=\"typeFF\">Текст<br />\n" +
                "</span>Это слово останется только в документе ЮР-ФИЗ:  <span\n" +
                " class=\"typeUF\">Текст</span>\n";

        HtmlParser parser = new HtmlParser(source, null);
        List<ContractType> types = parser.getCompatibleContractTypes();
        assertEquals(types.size(), 3);
        assertTrue(types.contains(ContractType.ЮР_ЮР));
        assertTrue(types.contains(ContractType.ФИЗ_ФИЗ));
        assertTrue(types.contains(ContractType.ЮР_ФИЗ));
        assertFalse(types.contains(ContractType.ФИЗ_ЮР));
    }

    @Test
    public void testGroups() throws Exception
    {
        String exp0 = "##Выражение 0#";
        String exp1 = "##Выражение 1#";
        String exp2 = "##Выражение 2#";
        String rep0 = "Замена 0";
        String rep1_1 = "Замена 1 для группы 1";
        String rep1_2 = "Замена 2 для группы 1";
        String rep2_1 = "Замена 1 для группы 2";
        String rep2_2 = "Замена 2 для группы 2";
        String group0 = "##@Группа 0#";
        String group1 = "##@Группа 1#";
        String group2 = "##@Группа 2#";

        String fmt = "Какой-то текст, скобочки(), запятые, и.т.д.!/ '%s' Группа 0: '%s' Группа 1: '%s' '%s' '%s' Группа 2: '%s' '%s' '%s' '%s' '%s'";
        String source = String.format(fmt, exp0, group0, group1, exp1, exp2, group2, exp1, exp2, exp1, exp2);
        String actual = String.format(fmt, rep0, "", "", rep1_1, rep1_2, "", rep2_1, rep2_2, rep2_1, rep2_2);

        HtmlParser parser = new HtmlParser(source, null);
        Set<Expression> expressions = parser.getExpressions();
        assertEquals(expressions.size(), 5);

        Iterator<Expression> it = expressions.iterator();
        it.next().setResponse(rep0);
        it.next().setResponse(rep1_1);
        it.next().setResponse(rep1_2);
        it.next().setResponse(rep2_1);
        it.next().setResponse(rep2_2);

        assertEquals(parser.buildNormal(false), actual);
    }

    @Test
    public void testPreview_OneExpressionInOneString() throws Exception
    {
        final String fmtStartBody = "Предыдущий текст<br />\n";
        final String fmtExp1 = "Марка: <span>%s</span>";
        final String fmtActualExp1 = "Марка: %s";
        final String fmtEndBody = "<br />\n";

        final String exp1 = "##.c='авто_марка'\n#";
        final String exp1Responce = "Какая-то марка автомобиля";

        String source = String.format(fmtStartBody + fmtExp1 + fmtEndBody, exp1);

        HtmlParser parser = new HtmlParser(source, null);
        Iterator<Expression> it = parser.getExpressions().iterator();

        Expression exp = it.next().setResponse(exp1Responce);
        assertEquals(parser.getPreview(exp), String.format(fmtActualExp1, Utils.makePreviewColorString(exp1Responce, exp.hashCode())));

        assertFalse(it.hasNext());
    }

    @Test
    public void testPreview_MultipleExpressionsInOneString() throws Exception
    {
        final String fmtStartBody = "Предыдущий текст<br />\n";
        final String fmtExp1 = "Марка: <span>%s</span>";
        final String fmtActualExp1 = "Марка: %s";
        final String fmtMiddle1Body = ", ";
        final String fmtExp2 = "модель: <span style=\"text-decoration: underline;\">%s</span>";
        final String fmtActualExp2 = "модель: <u>%s</u>";
        final String fmtEndBody = "<br />\n";

        final String exp1 = "##.c='авто_марка'\n#";
        final String exp2 = "##.c='авто_модель'\n#";
        final String exp1Responce = "Какая-то марка автомобиля";
        final String exp2Responce = "Noname";

        String source = String.format(fmtStartBody + fmtExp1 + fmtMiddle1Body + fmtExp2 + fmtEndBody, exp1, exp2);

        HtmlParser parser = new HtmlParser(source, null);
        Iterator<Expression> it = parser.getExpressions().iterator();
        it.next().setResponse(exp1Responce);
        it.next().setResponse(exp2Responce);
        assertFalse(it.hasNext());

        it = parser.getExpressions().iterator();

        Expression exp = it.next();
        assertEquals(parser.getPreview(exp), String.format(fmtActualExp1 + fmtMiddle1Body + fmtActualExp2, Utils.makePreviewColorString(exp1Responce, exp.hashCode()), exp2Responce));

        exp = it.next();
        assertEquals(parser.getPreview(exp), String.format(fmtActualExp1 + fmtMiddle1Body + fmtActualExp2, exp1Responce, Utils.makePreviewColorString(exp2Responce, exp.hashCode())));
    }

    @Test
    public void testPreview_MultipleExpressionsInMultipleStrings() throws Exception
    {
        final String fmtStartBody = "<br /> <br />Предыдущий текст с выражением, которое мы не хотим просматривать ##Смотреть не будем#<br />\n";
        final String fmtExp1 = "Марка: <span>%s</span>";
        final String fmtActualExp1 = "Марка: %s";
        final String fmtMiddle1Body = ", ";
        final String fmtExp2 = "модель: <span>%s</span>";
        final String fmtActualExp2 = "модель: %s";
        final String fmtEndBody = "<br />\n";

        final String exp1 = "##.c='авто_марка'\n#";
        final String exp2 = "##.c='авто_модель'\n#";
        final String exp1Responce = "Какая-то марка автомобиля";
        final String exp2Responce = "Noname";

        String source = String.format(fmtStartBody + fmtExp1 + fmtMiddle1Body + fmtExp2 + fmtEndBody, exp1, exp2);

        HtmlParser parser = new HtmlParser(source, null);
        Iterator<Expression> it = parser.getExpressions().iterator();
        it.next();
        it.next().setResponse(exp1Responce);
        it.next().setResponse(exp2Responce);
        assertFalse(it.hasNext());

        it = parser.getExpressions().iterator();
        it.next();
        Expression exp = it.next();
        assertEquals(parser.getPreview(exp), String.format(fmtActualExp1 + fmtMiddle1Body + fmtActualExp2, Utils.makePreviewColorString(exp1Responce, exp.hashCode()), exp2Responce));

        exp = it.next();
        assertEquals(parser.getPreview(exp), String.format(fmtActualExp1 + fmtMiddle1Body + fmtActualExp2, exp1Responce, Utils.makePreviewColorString(exp2Responce, exp.hashCode())));
    }

    @Test
    public void testPreview_MultipleExpressionsInMultipleStringsAndGroups() throws Exception
    {
        final String fmtStartBody = "<br /> <br />Предыдущий текст с выражением, которое мы не хотим просматривать ##Смотреть не будем#<br />\n";
        final String fmtExp1 = "Марка: <span style =\"text-decoration: underline;\">%s</span>";
        final String fmtActualExp1 = "Марка: <u>%s</u>";
        final String fmtExp2 = "модель: <span style=\"text-decoration: underline;\">%s</span>";
        final String fmtActualExp2 = "модель: <u>%s</u>";
        final String fmtMiddle1Body = "\n<h1>Группа1 ##@Группа 1#</h1>";
        final String fmtExp1Group1 = "Марка 1: <span style =\"text-decoration: underline;\">%s</span>";
        final String fmtActualExp1Group1 = "Марка 1: <u>%s</u>";
        final String fmtExp2Group1 = "модель 1: <span style=\"text-decoration: underline;\">%s</span>";
        final String fmtActualExp2Group1 = "модель 1: <u>%s</u>";
        final String fmtMiddle2Body = "\n<h1>Группа2 ##@Группа 2#</h1>";
        final String fmtExp1Group2 = "Марка 2: <span style =\"text-decoration: underline;\">%s</span>";
        final String fmtActualExp1Group2 = "Марка 2: <u>%s</u>";
        final String fmtExp2Group2 = "модель 2: <span style=\"text-decoration: underline;\">%s</span>";
        final String fmtActualExp2Group2 = "модель 2: <u>%s</u>";
        final String fmtMiddle3Body = "\n<h1>Группа1 ##@Группа 1#</h1>";
        final String fmtExp1Group3 = "Марка 3: <span>%s</span>";
        final String fmtActualExp1Group3 = "Марка 3: %s";
        final String fmtExp2Group3 = "модель 3: <span>%s</span>";
        final String fmtActualExp2Group3 = "модель 3: %s";
        final String fmtEndBody = "<br />\n";

        final String exp1 = "##.c='авто_марка'\n#";
        final String exp2 = "##.c='авто_модель'\n#";
        final String exp1Responce = "Марка автомобиля 0";
        final String exp2Responce = "Модель 0";
        final String exp1Group1Responce = "Марка автомобиля 1";
        final String exp2Group1Responce = "Модель 1";
        final String exp1Group2Responce = "Марка автомобиля 2";
        final String exp2Group2Responce = "Модель 2";

        String source = String.format(fmtStartBody + fmtExp1 + fmtExp2 + fmtMiddle1Body +
                        fmtExp1Group1 + fmtExp2Group1 + fmtMiddle2Body +
                        fmtExp1Group2 + fmtExp2Group2 + fmtMiddle3Body +
                        fmtExp1Group3 + fmtExp2Group3 + fmtEndBody,
                exp1, exp2, exp1, exp2, exp1, exp2, exp1, exp2);

        HtmlParser parser = new HtmlParser(source, null);
        Iterator<Expression> it = parser.getExpressions().iterator();
        it.next().setResponse("");
        it.next().setResponse(exp1Responce);
        it.next().setResponse(exp2Responce);
        it.next().setResponse(exp1Group1Responce);
        it.next().setResponse(exp2Group1Responce);
        it.next().setResponse(exp1Group2Responce);
        it.next().setResponse(exp2Group2Responce);
        assertFalse(it.hasNext());

        it = parser.getExpressions().iterator();

        it.next();
        Expression exp = it.next();
        assertEquals(parser.getPreview(exp), String.format(fmtActualExp1 + fmtActualExp2, Utils.makePreviewColorString(exp1Responce, exp.hashCode()), exp2Responce).replaceAll(Constants.PATTERN_GROUP.pattern(), ""));

        exp = it.next();
        assertEquals(parser.getPreview(exp), String.format(fmtActualExp1 + fmtActualExp2, exp1Responce, Utils.makePreviewColorString(exp2Responce, exp.hashCode()).replaceAll(Constants.PATTERN_GROUP.pattern(), "")));

        exp = it.next();
        assertEquals(parser.getPreview(exp), String.format(fmtActualExp1Group1 + fmtActualExp2Group1, Utils.makePreviewColorString(exp1Group1Responce, exp.hashCode()), exp2Group1Responce));

        exp = it.next();
        assertEquals(parser.getPreview(exp), String.format(fmtActualExp1Group1 + fmtActualExp2Group1, exp1Group1Responce, Utils.makePreviewColorString(exp2Group1Responce, exp.hashCode())));

        exp = it.next();
        assertEquals(parser.getPreview(exp), String.format(fmtActualExp1Group2 + fmtActualExp2Group2, Utils.makePreviewColorString(exp1Group2Responce, exp.hashCode()), exp2Group2Responce));

        exp = it.next();
        assertEquals(parser.getPreview(exp), String.format(fmtActualExp1Group2 + fmtActualExp2Group2, exp1Group2Responce, Utils.makePreviewColorString(exp2Group2Responce, exp.hashCode())));
    }

    @Test
    public void testPreview_RealText1() throws Exception
    {
        final String fmtStartBody = "</h1>\n" +
                "<h1>6. Обстоятельства непреодолимой силы (Форс-мажор)\n" +
                "##@Форс-мажор#<br />\n</h1>\n";

        final String fmtExp1 = "6.1. Стороны освобождаются от ответственности за неисполнение или\n" +
                "ненадлежащее исполнение обязательств по Договору, если надлежащее\n" +
                "исполнение оказалось невозможным вследствие непреодолимой силы, то есть\n" +
                "чрезвычайных и непредотвратимых при данных условиях обстоятельств, под\n" +
                "которыми понимаются: <span style=\"text-decoration: underline;\">%s</span> (запретные действия властей, гражданские\n" +
                "волнения, эпидемии, блокада, эмбарго, землетрясения, наводнения, пожары\n" +
                "или другие стихийные бедствия).";
        final String fmtActualExp1 = "6.1. Стороны освобождаются от ответственности за неисполнение или\n" +
                "ненадлежащее исполнение обязательств по Договору, если надлежащее\n" +
                "исполнение оказалось невозможным вследствие непреодолимой силы, то есть\n" +
                "чрезвычайных и непредотвратимых при данных условиях обстоятельств, под\n" +
                "которыми понимаются: <u>%s</u> (запретные действия властей, гражданские\n" +
                "волнения, эпидемии, блокада, эмбарго, землетрясения, наводнения, пожары\n" +
                "или другие стихийные бедствия).";
        final String fmtEndBody = "<br />\n";

        final String exp1 = "##.t='90%'\nФорс-мажор\nобстоятельства#";
        final String exp1Responce = "Обстоятельства";

        final String source = String.format(fmtStartBody + fmtExp1 + fmtEndBody, exp1);

        HtmlParser parser = new HtmlParser(source, null);
        Iterator<Expression> it = parser.getExpressions().iterator();

        Expression exp = it.next().setResponse(exp1Responce);
        assertEquals(parser.getPreview(exp), String.format(fmtActualExp1, Utils.makePreviewColorString(exp1Responce, exp.hashCode())).replaceAll("\n", " "));

        assertFalse(it.hasNext());
    }

    @Test
    public void testBuildNormalWithParameter_g() throws ExpressionException, NestedContractTypeException
    {
        String source = "##Выражение 1# ##Выражение 2# " +
                "##@Группа 1# " +
                "##Выражение 1# ##.g='' Выражение 2# " +
                "##@Группа 2# " +
                "##.g='Группа 1' Выражение 1# ##.g='' Выражение 2#";

        String actual = "1 2 " +
                " " +
                "3 2 " +
                " " +
                "3 2";


        HtmlParser parser = new HtmlParser(source, null);
        Iterator<Expression> it = parser.getExpressions().iterator();
        it.next().setResponse("1");
        it.next().setResponse("2");
        it.next().setResponse("3");
        assertFalse(it.hasNext());

        assertEquals(parser.buildNormal(false), actual);
    }

    @Test
    public void testPreview_Tables() throws ExpressionException, NestedContractTypeException
    {
        String source = "<br />\n" +
                "<table style=\"text-align: left; width: 100%;\" border=\"1\"\n" +
                " cellpadding=\"2\" cellspacing=\"2\">\n" +
                "  <tbody>\n" +
                "    <tr>\n" +
                "      <td width=\"3%\">№</td>\n" +
                "      <td width=\"14%\">Наименование&nbsp;узла</td>\n" +
                "      <td width=\"53%\">Техническое состояние</td>\n" +
                "      <td width=\"30%\">Заключение о дальнейшем использовании</td>\n" +
                "    </tr>\n" +
                "    <tr>\n" +
                "      <td>1</td>\n" +
                "      <td>Кузов<br />\n" +
                "      </td>\n" +
                "      <td style=\"text-decoration: underline;\">##.t='80'\n" +
                "Техническое состояние Кузова#</td>\n" +
                "      <td><span style=\"text-decoration: underline;\">##.c='заключение_об_использовании_узла'\n" +
                "Заключение о\n" +
                "дальнейшем использование автомобиля с учетом технического\n" +
                "состояния Кузова#</span></td>\n" +
                "    </tr>\n" +
                "  </tbody>\n" +
                "</table>\n" +
                "<br />\n";

        String exp1Responce = "Хорошее состояние кузова";
        String exp2Responce = "Можно использовать";

        String fmtActualExp1 = "%s"; // Без <u>, т.к. недоделано - выражение Constants.REG_EXP_SPAN_UNDERLINE обрабатывает только тэг <span>
        String fmtActualExp2 = "<u>%s</u>";

        HtmlParser parser = new HtmlParser(source, null);
        Iterator<Expression> it = parser.getExpressions().iterator();

        Expression exp = it.next().setResponse(exp1Responce);
        assertEquals(parser.getPreview(exp), String.format(fmtActualExp1, Utils.makePreviewColorString(exp1Responce, exp.hashCode())).replaceAll("\n", " "));

        exp = it.next().setResponse(exp2Responce);
        assertEquals(parser.getPreview(exp), String.format(fmtActualExp2, Utils.makePreviewColorString(exp2Responce, exp.hashCode())).replaceAll("\n", " "));

        assertFalse(it.hasNext());
    }
}