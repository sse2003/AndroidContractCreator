package org.sse.contracts.core;

import org.junit.Test;
import org.sse.contracts.Constants;
import org.sse.contracts.Utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;

public class RegExpTest
{
    @Test
    public void testReplace1()
    {
        String regExp = "(?s)(a)(b)(c)";
        String source = "abc abc abc45";
        String actual = "a c a c a c45";
        assertEquals(source.replaceAll(regExp, "$1 $3"), actual);
    }

    @Test
    public void testReplace2()
    {
        String regExp = "(a)(b)(c)";
        String source = "abc abc abc45";
        String actual = "a c a c a c45";

        Pattern p = Pattern.compile(regExp);
        Matcher m = p.matcher(source);
        while (m.find()) source = m.replaceAll("$1 $3");

        assertEquals(source, actual);
    }

    @Test
    public void testExpressionRegExp() throws Exception
    {
        String source = " ##@Группа# ##Выражение# ";

        Matcher m = Constants.PATTERN_EXPRESSION.matcher(source);

        assertEquals(m.find(), true);
        assertEquals(m.start(1), 12);
    }

    @Test
    public void testGroupRegExp() throws Exception
    {
        String source = " ##Выражение# ##@Группа#";

        Matcher m = Constants.PATTERN_GROUP.matcher(source);

        assertEquals(m.find(), true);
        assertEquals(m.start(1), 14);
    }

    @Test
    public void testPurchaseSkutitleReplace() throws Exception
    {
        final String reg = Constants.PATTERN_PURCHASE_SKU_TITLE_REPLACE;
        assertEquals("Без ограничений (Генератор договоров)".replaceFirst(reg, ""), "Без ограничений");

    }

    @Test
    public void testPatternQuote() throws Exception
    {
        String source = "123##.c='456'# ##.c='789'#";
        String replace = ".d='";
        String actual = "123##.d='456'# ##.d='789'#";

        String s2 = source.replaceAll("\\.c='", replace);
        String s3 = source.replaceAll(Pattern.quote(".c='"), Matcher.quoteReplacement(replace));
        assertEquals(s2, actual);
        assertEquals(s3, actual);
    }

    @Test
    public void testRemoveStartSpaces() throws Exception
    {
        final String actual = "<br /> 1234567";

        assertEquals(Utils.removeStartSpaces(" " + actual), actual);
        assertEquals(Utils.removeStartSpaces("   " + actual), actual);
        assertEquals(Utils.removeStartSpaces("\t" + actual), actual);
    }

    @Test
    public void testRemoveEndSpaces() throws Exception
    {
        final String actual = "<br /> 1234567";

        assertEquals(Utils.removeEndSpaces(actual + " "), actual);
        assertEquals(Utils.removeEndSpaces(actual + "   "), actual);
        assertEquals(Utils.removeEndSpaces(actual + "\t"), actual);
    }

    @Test
    public void testOptimizeHtml()
    {
        final String source = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\">\n" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\"><head>\n" +
                "<meta content=\"text/html; charset=UTF-8\" http-equiv=\"content-type\" />\n" +
                "<link rel=\"stylesheet\" href=\"../style.css\" type=\"text/css\" /><title>Генеральная доверенность на автотранспортное средство</title><!-- Версия документа: 1 Автор: SSE GroupId: group_16 --></head>\n" +
                "<body>\n" +
                "<p class=\"header\">Доверенность на автотранспортное\n" +
                "средство&nbsp;</p>\n" +
                "<span style=\"text-decoration: underline;\"><span style=\"font-weight: bold;\"></span></span>\n" +
                "<table style=\"text-align: left; width: 100%;\" border=\"0\" cellpadding=\"2\" cellspacing=\"2\">\n" +
                "<tbody>" +
                "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"+
                "<br /><span style=\"font-family: &quot;Times New Roman&quot;,&quot;serif&quot;;\">Зарегистрировано\n" +
                "в реестре:&nbsp;</span><span style=\"text-decoration: underline;\">##.t='50' Номер реестра (указывается нотариусом)#<br /><br /></span><span style=\"font-size: 12pt; font-family: &quot;Times New Roman&quot;,&quot;serif&quot;;\"><span style=\"\"></span></span><span style=\"font-family: &quot;Times New Roman&quot;,&quot;serif&quot;;\">Взыскано по\n" +
                "тарифу:&nbsp;</span><span style=\"text-decoration: underline;\">##.c='цена' Базовая стоимость услуги нотариуса, руб# руб</span><span style=\"font-family: &quot;Times New Roman&quot;,&quot;serif&quot;;\">.</span><br /><br /><span style=\"text-decoration: underline;\">##.t='99%' Подпись и расшифровка подписи нотариуса#</span><br />\n"+
                "<o:p></o:p>" +
                "</tbody>\n" +
                "</table>\n" +
                "</div>\n" +
                "</body></html>";

        final String actual = "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\">\n" +
                "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\" lang=\"en\"><head>\n" +
                "<meta content=\"text/html; charset=UTF-8\" http-equiv=\"content-type\" />\n" +
                "<link rel=\"stylesheet\" href=\"../style.css\" type=\"text/css\" /><title>Генеральная доверенность на автотранспортное средство</title><!-- Версия документа: 1 Автор: SSE GroupId: group_16 --></head>\n" +
                "<body>\n" +
                "<p class=\"header\">Доверенность на автотранспортное " +
                "средство&nbsp;</p>\n" +
                "\n" +
                "<table style=\"text-align: left; width: 100%;\" border=\"0\" cellpadding=\"2\" cellspacing=\"2\">\n" +
                "<tbody>" +
                "&nbsp;"+
                "<br /><span style=\"font-family: &quot;Times New Roman&quot;,&quot;serif&quot;;\">Зарегистрировано " +
                "в реестре:&nbsp;</span><span style=\"text-decoration: underline;\">##.t='50' Номер реестра (указывается нотариусом)#<br /><br /></span><span style=\"font-family: &quot;Times New Roman&quot;,&quot;serif&quot;;\">Взыскано по " +
                "тарифу:&nbsp;</span><span style=\"text-decoration: underline;\">##.c='цена' Базовая стоимость услуги нотариуса, руб# руб</span><span style=\"font-family: &quot;Times New Roman&quot;,&quot;serif&quot;;\">.</span><br /><br /><span style=\"text-decoration: underline;\">##.t='99%' Подпись и расшифровка подписи нотариуса#</span><br />\n"+
                "</tbody>\n" +
                "</table>\n" +
                "</div>\n" +
                "</body></html>";

        // ---------------------------------------------------------------------
        // см: app build.gradle: optimizeFile()

        String result = source.replaceAll("([^>])\r\n", "$1 ");
        result = result.replaceAll("([^>])\n", "$1 ");

        result = result.replaceAll("&nbsp; ", "&nbsp;");
        result = result.replaceAll(" &nbsp;", "&nbsp;");

        int len = 0;
        while (result.length() != len)
        {
            len = result.length();
            result = result.replaceFirst("&nbsp;&nbsp;", "&nbsp;");
        }

        len = 0;

        while (result.length() != len)
        {
            len = result.length();
            // Удаляем пустые, бессмысленные теги
            result = result.replaceFirst("<[^!\\/].[^>\\/]*?><\\/.*?>", "");
        }
        // ---------------------------------------------------------------------


        assertEquals(result, actual);
    }
}
