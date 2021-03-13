package org.sse.contracts.core;

import org.junit.Test;
import org.sse.contracts.Constants;
import org.sse.contracts.Utils;
import org.sse.contracts.core.assets.AbstractAssetLoader;
import org.sse.contracts.core.exceptions.NestedContractTypeException;

import static org.junit.Assert.assertEquals;

public class UtilsTest
{
    @Test
    public void textRemoveContractTypeClassFromHtml() throws Exception
    {
        String source = "123<span class=\"typeUU\">UU</span><span class=\"typeUU\"\n" +
                " style=\"text-decoration: underline;\">##.c='ФИО' #</span>456<span class=\"typeUF\"\n" +
                "UF</span>789<span class=\"typeUF\">,\n" +
                "действующего на основании </span>\n";

        String actualTypeUU = "123<span class=\"typeUU\">UU</span><span class=\"typeUU\"\n" +
                " style=\"text-decoration: underline;\">##.c='ФИО' #</span>456789\n";

        String actualTypeUF = "123456<span class=\"typeUF\"\n" +
                "UF</span>789<span class=\"typeUF\">,\n" +
                "действующего на основании </span>\n";

        String actualTypeEmpty = "123456789\n";

        assertEquals(Utils.removeContractTypeClassFromHtml(source, ContractType.ЮР_ФИЗ), actualTypeUU);
        assertEquals(Utils.removeContractTypeClassFromHtml(source, ContractType.ЮР_ЮР), actualTypeUF);
        assertEquals(Utils.removeContractTypeClassFromHtml(Utils.removeContractTypeClassFromHtml(source, ContractType.ЮР_ЮР), ContractType.ЮР_ФИЗ), actualTypeEmpty);
    }

    @Test
    public void textRemoveContractTypeClassFromHtmlExcept() throws Exception
    {
        String source = "123<div><br /><div class=\"typeFF\"><div><span><div>DIV</div></span>DIV</div><span> SPAN </span></div>456</div>7<span class=\"typeFF\">8<span>9</span>10</span>";

        String actualTypeUU = "123<div><br />456</div>7";
        String actualTypeFF = source;

        assertEquals(Utils.removeAllContractTypeClassesFromHtmlExcept(source, ContractType.ЮР_ЮР), actualTypeUU);
        assertEquals(Utils.removeAllContractTypeClassesFromHtmlExcept(source, ContractType.ФИЗ_ФИЗ), actualTypeFF);
    }

    @Test
    public void textNestedContractTypeClassFromHtml_Exception_SameTag() throws Exception
    {

        String source1 = "123<div><br /><div class=\"typeFF\"><div><div class=\"typeUnknown\"><div><br />DIV</div></div>DIV</div><span> SPAN </span></div>456</div>7<span class=\"typeFF\">8<span>9</span>10</span>";
        String source2 = "123<div><br /><div class=\"typeFF\"><div><div class=\"typeUU\"><div><br />DIV</div></div>DIV</div><span> SPAN </span></div>456</div>7<span class=\"typeFF\">8<span>9</span>10</span>";

        int index = 0;
        try
        {
            Utils.removeContractTypeClassFromHtml(source1, ContractType.ФИЗ_ФИЗ);
            index++;
            Utils.removeContractTypeClassFromHtml(source2, ContractType.ФИЗ_ФИЗ);
            index++;
        } catch (NestedContractTypeException ignore)
        {

        }
        assertEquals(index, 1);
    }

    @Test
    public void textNestedContractTypeClassFromHtml_Exception_NotSameTag() throws Exception
    {

        String source1 = "123<div class=\"typeFF\"><br /><span></span></div>";
        String source2 = "123<div class=\"typeFF\"><br /><span class=\"typeFF\"></span></div>";

        int index = 0;
        try
        {
            Utils.removeContractTypeClassFromHtml(source1, ContractType.ФИЗ_ФИЗ);
            index++;
            Utils.removeContractTypeClassFromHtml(source2, ContractType.ФИЗ_ФИЗ);
            index++;
        } catch (NestedContractTypeException ignore)
        {

        }
        assertEquals(index, 1);
    }

    @Test
    public void textRemoveContractTypeClassFromHtmlExcept_SingleCloseTag() throws Exception
    {
        String source = "123<br class=\"typeUU\"/>BR<span class=\"typeUU\">UU</span>456<br class=\"typeUU\" />789";

        String actualTypeUU = "123<br class=\"typeUU\"/>BR<span class=\"typeUU\">UU</span>456<br class=\"typeUU\" />789";

        String actualTypeEmpty = "123BR456789";

        assertEquals(Utils.removeAllContractTypeClassesFromHtmlExcept(source, ContractType.ЮР_ЮР), actualTypeUU);
        assertEquals(Utils.removeAllContractTypeClassesFromHtmlExcept(source, ContractType.ФИЗ_ФИЗ), actualTypeEmpty);
    }


    @Test
    public void testPrepareWebViewHtml() throws Exception
    {
        String source1 = "__________что то еще____1______";
        String actual1 = "____ ____ что то еще____1____ _";
        assertEquals(Utils.prepareWebViewHtml(source1, "_", 5), actual1);

        String source2 = "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;что то еще&nbsp;&nbsp;&nbsp;&nbsp;1&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;";
        String actual2 = "&nbsp;&nbsp;&nbsp;&nbsp; &nbsp;&nbsp;&nbsp;&nbsp; что то еще&nbsp;&nbsp;&nbsp;&nbsp;1&nbsp;&nbsp;&nbsp;&nbsp; &nbsp;";
        assertEquals(Utils.prepareWebViewHtml(source2, "&nbsp;", 5), actual2);
    }

    @Test
    public void testReplaceSpanUnderline() throws Exception
    {
        String source = "12 <span class=\"typeUU\"\n style=\"text-decoration: underline;\">34\n</span> 56 <span class=\"typeUU\"> 7 8 </span><span style=\"text-decoration: underline;\">90</span>";
        String actual = "12 <u>34\n</u> 56 <span class=\"typeUU\"> 7 8 </span><u>90</u>";

        assertEquals(Utils.replaceStyleUnderlineSpanToTagU(source), actual);
    }

    @Test
    public void testRemoveSpanTags() throws Exception
    {
        String source = "12 <span class=\"typeUU\"\n style=\"text-decoration: underline;\">34\n</span> 56 <span style=\"text-decoration: underline;\"> 7 8 </span><span style=\"text-decoration: underline;\">90</span>";
        String actual = "12 34\n 56  7 8 90";

        assertEquals(Utils.removeSpanTags(source), actual);
    }

    @Test
    public void testPrepareExpressionRequest() throws Exception
    {
        assertEquals(Utils.prepareExpressionRequest("exp"), "exp");
        assertEquals(Utils.prepareExpressionRequest("exp."), "exp");
        assertEquals(Utils.prepareExpressionRequest("exp .."), "exp");
        assertEquals(Utils.prepareExpressionRequest("exp ..."), "exp");
    }


    @Test
    public void testExpExpressonsRegExp()
    {
        String source = "123\r\n##$Файл 1,Параметр 1, Параметр 2,  Параметр3 #456\r\n##$Файл 2," + Constants.SPACE_SYMBOL + "Вложенный параметр#\r\n##$Файл 4#\r\n##$Отсутствующий файл#";

        assertEquals(Utils.prepareExtExpressions(source, new AbstractAssetLoader()
        {
            @Override
            public String load(String fileName)
            {
                if (fileName.contains("Файл 1"))
                    return "ФАЙЛ 1: {P1}-{P2}-{P3}";
                if (fileName.contains("Файл 2"))
                    return "##$Файл 3, {P1}#";
                if (fileName.contains("Файл 3"))
                    return "ФАЙЛ 3: {P1}";
                if (fileName.contains("Файл 4"))
                    return "ФАЙЛ 4";

                return null;
            }
        }), "123\r\nФАЙЛ 1: Параметр 1-Параметр 2-Параметр3 456\r\nФАЙЛ 3: Вложенный параметр\r\nФАЙЛ 4\r\n");
    }

    @Test
    public void testRemoveFirstWhiteSpaces()
    {
        assertEquals(Utils.removeFirstWhiteSpaces("  123  456 "), "123  456 ");
    }

    @Test
    public void testExtractBody()
    {
        String body = "\n<span class=\"typeUU\" style=\"text-decoration: underline;\">##@{P1}#\n" +
                "##.c='ФИО' Полное наименование организации или ФИО ИП#</span><span class=\"typeUU\">, именуемое в дальнейшем \"{P1}\", в лице </span><span class=\"typeUU\" style=\"text-decoration: underline;\">##.c='ФИО'\n";

        String source = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\">\n" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\"\n" +
                " lang=\"en\">\n" +
                "<head>\n" +
                "  <meta content=\"text/html; charset=UTF-8\"\n" +
                " http-equiv=\"content-type\" />\n" +
                "  <link rel=\"stylesheet\" href=\"../style.css\"\n" +
                " type=\"text/css\" />\n" +
                "  <title>Шапка_ФИЗ-ФИЗ</title>\n" +
                "</head>\n" +
                "<body>" + body +
                "</body>\n" +
                "</html>\n";

        assertEquals(Utils.extractBody(source), body);
        assertEquals(Utils.extractBody(body), "");
    }

    @Test
    public void testChangeCssFontSize()
    {
        final String source = "h1 {\n" +
                "  font-size: 100%;\n" +
                "  text-align: center;\n" +
                "  font-weight: bold;\n" +
                "  margin-bottom: 16pt\n" +
                "}\n" +
                "body {\n" +
                "  width: 595pt;\n" +
                "  font-size: 12pt;\n" +
                "  line-height: 1.5;\n" +
                "  margin-left: 64pt;\n" +
                "  margin-right: 32pt;\n" +
                "  margin-top: 64pt;\n" +
                "  margin-bottom: 64pt;\n" +
                "}\n" +
                "h2 {\n" +
                "  font-size: 100%;\n" +
                "  text-align: center;\n" +
                "  font-weight: bold;\n" +
                "  margin-bottom: 16pt\n" +
                "}";

        final String actual = "h1 {\n" +
                "  font-size: 100%;\n" +
                "  text-align: center;\n" +
                "  font-weight: bold;\n" +
                "  margin-bottom: 16pt\n" +
                "}\n" +
                "body {\n" +
                "  width: 595pt;\n" +
                "  font-size: 14pt;\n" +
                "  line-height: 1.5;\n" +
                "  margin-left: 64pt;\n" +
                "  margin-right: 32pt;\n" +
                "  margin-top: 64pt;\n" +
                "  margin-bottom: 64pt;\n" +
                "}\n" +
                "h2 {\n" +
                "  font-size: 100%;\n" +
                "  text-align: center;\n" +
                "  font-weight: bold;\n" +
                "  margin-bottom: 16pt\n" +
                "}";

        assertEquals(Utils.changeBodyCssFontSize(source, "14pt"), actual);
    }

    @Test
    public void testChangeCssLeftMargin()
    {
        final String source = "h1 {\n" +
                "  font-size: 100%;\n" +
                "  text-align: center;\n" +
                "  font-weight: bold;\n" +
                "  margin-bottom: 16pt\n" +
                "}\n" +
                "body {\n" +
                "  width: 595pt;\n" +
                "  font-size: 12pt;\n" +
                "  line-height: 1.5;\n" +
                "  margin-left: 64pt;\n" +
                "  margin-right: 32pt;\n" +
                "  margin-top: 64pt;\n" +
                "  margin-bottom: 64pt;\n" +
                "}\n" +
                "h2 {\n" +
                "  font-size: 100%;\n" +
                "  text-align: center;\n" +
                "  font-weight: bold;\n" +
                "  margin-bottom: 16pt\n" +
                "}";

        final String actual = "h1 {\n" +
                "  font-size: 100%;\n" +
                "  text-align: center;\n" +
                "  font-weight: bold;\n" +
                "  margin-bottom: 16pt\n" +
                "}\n" +
                "body {\n" +
                "  width: 595pt;\n" +
                "  font-size: 12pt;\n" +
                "  line-height: 1.5;\n" +
                "  margin-left: 16pt;\n" +  // 0.5 см = 16pt
                "  margin-right: 64pt;\n" + // 2 см = 64pt
                "  margin-top: 32pt;\n" +   // 1 см = 32 pt
                "  margin-bottom: 16pt;\n" + // 0.5 см = 16pt
                "}\n" +
                "h2 {\n" +
                "  font-size: 100%;\n" +
                "  text-align: center;\n" +
                "  font-weight: bold;\n" +
                "  margin-bottom: 16pt\n" +
                "}";

        String result = Utils.changeBodyCssMargin(source, Constants.CSS_MARGIN_LEFT, 16);
        result = Utils.changeBodyCssMargin(result, Constants.CSS_MARGIN_RIGHT, 64);
        result = Utils.changeBodyCssMargin(result, Constants.CSS_MARGIN_TOP, 32);
        result = Utils.changeBodyCssMargin(result, Constants.CSS_MARGIN_BOTTOM, 16);

        assertEquals(result, actual);
    }
}
