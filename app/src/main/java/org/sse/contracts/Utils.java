package org.sse.contracts;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.Settings;
import trikita.log.Log;
import android.widget.Toast;

import com.crashlytics.android.Crashlytics;

import org.sse.contracts.core.conf.InternalConfigurations;
import org.sse.contracts.core.ContractType;
import org.sse.contracts.core.HtmlString;
import org.sse.contracts.core.assets.AbstractAssetLoader;
import org.sse.contracts.core.exceptions.NestedContractTypeException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils
{

    public static InputStream getRawResource(Context context, String name)
    {
        Resources resources = context.getResources();
        int resId = resources.getIdentifier(name, "raw", context.getPackageName());
        return resources.openRawResource(resId);
    }

    public static InputStream getAssetResource(Context context, String name) throws IOException
    {
        return context.getAssets().open(name);
    }

    public static String convertStreamToString(InputStream is)
    {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }

    public static String convertAndDecryptStreamToString(InputStream is)
    {
        return decryptAssetData(is);
    }

    public static InputStream convertStringToInputStream(String str)
    {
        return new ByteArrayInputStream(str.getBytes());
    }

    public static boolean writeToFile(InputStream is, File newFile)
    {
        try
        {
            FileOutputStream os = new FileOutputStream(newFile);

            while (is.available() > 0)
            {
                byte[] b = new byte[is.available()];
                is.read(b);
                os.write(b);
            }

            return true;
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        return false;
    }

    public static File getDocumentsFolder() throws Exception
    {
        File folder;
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.JELLY_BEAN_MR2)
            folder = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
        else
            folder = new File(Environment.getExternalStorageDirectory() + "/Documents");

        if (!folder.exists() && !folder.mkdir())
        {
            throw new Exception("Error get documents folder! Path: " + folder.getAbsolutePath());
        }

        return folder;
    }

    public static void showToast(Context context, String text)
    {
        Toast.makeText(context, text, Toast.LENGTH_LONG).show();
    }

    public static void exceptionReport(final Throwable ex)
    {
        if (isRunningOnDebugMode())
        {
        } else
        {
        }
        ex.printStackTrace();
    }

    public static String timeStampToString(long time)
    {
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return df.format(new Date(time));
    }

    public static String injectCssIntoHtml(String html, String css)
    {
        css = "<style type=\"text/css\">"
                + css
                + "</style>";


        return html.replaceFirst("</head>", "\r\n" + css + "\r\n</head>");
    }

    public static String loadDefaultCssStyle(Context context) throws IOException
    {
        InputStream inCssFile = context.getAssets().open(Constants.STYLE_CSS_PATH);
        return Utils.decryptAssetData(inCssFile);
    }

    public static String modifyCss(String css, ContractType contractType, String fontSize, float leftMargin, float rightMargin, float topMargin, float bottomMargin)
    {
        css = css.replaceAll("\n", "\r\n");

        for (ContractType type : ContractType.values())
        {
            String regExp = makeRegExpCssClassName(type.getStyleClassName());
            if (type == contractType)
                css = css.replaceAll(regExp, "$1 $3");
            else
                css = css.replaceAll(regExp, "$1\n" + Constants.CSS_DISPLAY_NONE + "\n$3");
        }

        css = changeBodyCssFontSize(css, fontSize);
        css = changeBodyCssMargin(css, Constants.CSS_MARGIN_LEFT, leftMargin);
        css = changeBodyCssMargin(css, Constants.CSS_MARGIN_RIGHT, rightMargin);
        css = changeBodyCssMargin(css, Constants.CSS_MARGIN_TOP, topMargin);
        css = changeBodyCssMargin(css, Constants.CSS_MARGIN_BOTTOM, bottomMargin);

        return css;
    }

    public static String changeBodyCssFontSize(String css, String fontSize)
    {
        if (fontSize != null)
            return css.replaceFirst(makeRegExpCssFieldValue("font-size"), "$1" +  fontSize + "$3");

        return css;
    }

    public static String changeBodyCssMargin(String css, String fieldName, float marginValue)
    {
        if (marginValue > 0)
        {
            String marginPt = convertNumberToPt(marginValue);
            return css.replaceFirst(makeRegExpCssFieldValue(fieldName), "$1" + marginPt + "$3");
        }

        return css;
    }

    private static String convertNumberToPt(float margin)
    {
        return ((int)(margin)) + "pt";
    }

    public static String removeContractTypeClassFromHtml(String source, ContractType type) throws NestedContractTypeException
    {
        Pattern p = Pattern.compile(makeRegExpStringForHtmlContractType(type));
        Matcher m = p.matcher(source);

        if (m.find())
        {
            String tagName = m.group(Constants.GROUP_CONTRACT_TYPE_TAG).toUpperCase();
            int startIndexForRemove = m.start();

            int tagIndex = 1;
            int i = startIndexForRemove + tagName.length();

            String upperSource = source.toUpperCase();

            if (!tagIsAutoClosed(source, i))
                while (tagIndex > 0)
                {
                    i = upperSource.indexOf(tagName, i + 1);
                    if (i < 0)
                    {
                        throw new NestedContractTypeException("Не найден закрывающий тег '" + tagName + "'\nв строке: " + source);
                    }

                    if (source.charAt(i - 1) == '/')
                    {
                        tagIndex--;
                    } else if (source.charAt(i - 1) == '<')
                    {
                        if (!tagIsAutoClosed(source, i))
                        {
                            tagIndex++;
                        }
                    } else
                    {

                    }
                }

            int stopIndexForRemove = source.indexOf('>', i) + 1;

            int startNestedIndex = source.indexOf('>', startIndexForRemove);
            String removed = source.substring(startNestedIndex, stopIndexForRemove);
            if (containContractType(removed))
                throw new NestedContractTypeException("Вложенный класс типа документа\nв строке: '" + source + "'\nподстрока: '" + removed + "'");

            source = source.substring(0, startIndexForRemove) + source.substring(stopIndexForRemove, source.length());
            return removeContractTypeClassFromHtml(source, type);
        }

        return source;
    }

    private static boolean tagIsAutoClosed(String source, int i)
    {
        return source.charAt(source.indexOf(">", i) - 1) == '/';
    }

    private static boolean containContractType(String str)
    {
        for (ContractType type : ContractType.values())
        {
            if (str.contains("\"" + type.getStyleClassName() + "\"")) return true;
        }
        return false;
    }

    public static String removeAllContractTypeClassesFromHtmlExcept(String source, ContractType type) throws NestedContractTypeException
    {
        for (ContractType typeForRemove : ContractType.values())
        {
            if (typeForRemove == type) continue;
            source = Utils.removeContractTypeClassFromHtml(source, typeForRemove);
        }
        return source;
    }

    public static String prepareWebViewHtml(String text, String sym)
    {
        return prepareWebViewHtml(text, sym, FontUtils.getSymbolCountInOneLine(sym));
    }

    public static String prepareWebViewHtml(String text, String sym, int len)
    {
        return text.replaceAll((makeRegExpUnderscoreLongSequence(sym, len)), "$" + Constants.GROUP_UNDERSCORE + " ");
    }

    public static String getAllPurchases()
    {
        Set set = InternalConfigurations.getInstance().getAvailablePurchases();
        if (set == null && set.isEmpty()) return "Empty";

        String[] p = (String[]) set.toArray(new String[0]);
        return Arrays.toString(p);
    }

    public static String removeFileExtension(String fileName)
    {
        return fileName.replaceFirst("\\..*$", "");
    }

    public static String removeStartSpaces(String source)
    {
        return source.replaceFirst("^(\\s)*", "");
    }

    public static String removeEndSpaces(String source)
    {
        return source.replaceFirst("(\\s)*$", "");
    }

    public static String replace_cRu_to_cEn(String source)
    {
        return source.replaceAll("\\.с='", ".c='"); // Replace ru 'с' to en 'c'
    }

    public static boolean equals(String str1, String str2)
    {
        if (str1 != null) return str1.equals(str2);
        if (str2 != null) return str2.equals(str1);
        return true;
    }

    public static String replaceStyleUnderlineSpanToTagU(String str)
    {
        return str.replaceAll(Constants.REG_EXP_SPAN_UNDERLINE, "<u>$" + Constants.GROUP_SPAN_UNDERLINE_TEXT + "</u>");
    }

    public static String removeSpanTags(String str)
    {
        return str.replaceAll(Constants.REG_EXP_SPAN, "$" + Constants.GROUP_SPAN_TEXT);
    }

    public static String prepareExpressionRequest(String str)
    {
        return str.replaceFirst(Constants.REG_EXP_EXPRESSION_REQUEST_END_OF_LINE, "");
    }

    public static long convertPurchasedSkuToUsingTime_mls(String sku)
    {
       return 0;
    }

    public static long getSaveCurrentTimeMillis()
    {
        if (InternalConfigurations.getInstance().getLastCurrentTime() - System.currentTimeMillis() > Constants.HOUR_IN_MS * 23)
        {
        }

        InternalConfigurations.getInstance().setLastCurrentTime();
        return System.currentTimeMillis();
    }

    public static String decryptAssetData(InputStream is)
    {
        try
        {
            return new String(Constants.ASSETS_CIPHER.doFinal(toByteArray(is)));
        } catch (Exception e)
        {
            Utils.exceptionReport(e);
        }
        return "";
    }

    private static byte[] toByteArray(InputStream is) throws IOException
    {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();

        int nRead;
        byte[] data = new byte[16384];

        while ((nRead = is.read(data, 0, data.length)) != -1)
        {
            buffer.write(data, 0, nRead);
        }

        buffer.flush();

        return buffer.toByteArray();
    }

    public static String removeExtension(String str)
    {
        if (str.contains(".")) str = str.substring(0, str.indexOf("."));
        return str;
    }

    public static String prepareExtExpressions(String source, AbstractAssetLoader loader)
    {
        Matcher m = Constants.PATTERN_EXT_EXPRESSIONS.matcher(source);
        if (m.find())
        {
            {
                String[] vals = m.group(Constants.GROUP_EXT_EXPRESSIONS).split(",");
                String fileName = removeFirstWhiteSpaces(new HtmlString(vals[0]).getTextOnly());

                if (fileName.isEmpty())
                    return source;

                String extContent = loader.load(fileName + Constants.DOCUMENT_EXT);
                if (extContent == null) extContent = "";

                for (int i = 1; i < vals.length; i++)
                {
                    String param = removeFirstWhiteSpaces(new HtmlString(vals[i]).getTextOnly());
                    extContent = extContent.replaceAll(Pattern.quote("{P" + i + "}"), param);
                }

                source = m.replaceFirst(Matcher.quoteReplacement(extContent));
                m = null;
            }

            return prepareExtExpressions(source, loader);
        }

        return source;
    }

    public static String removeFirstWhiteSpaces(String val)
    {
        return val.replaceFirst(Constants.REG_EXP_FIRST_WHITESPACES, "");
    }

    public static String extractBody(String source)
    {
        Matcher m = Constants.PATTERN_EXT_BODY.matcher(source);
        if (m.find())
            return m.group(Constants.GROUP_EXT_BODY);

        Log.e("Тэг <body> не обнаружен !");

        return "";
    }

    public static String smartRemoveGlobalParameter(String source)
    {
        source = source.replaceFirst(Constants.REG_EXP_GLOBAL_PARAMETER, "");

        if (source.charAt(2) == ' ')
            source = source.substring(0, 2) + source.substring(3);

        return source;
    }

    public static final String makeRegExpUnderscoreLongSequence(String sym, int len)
    {
        return "(?s)((" + sym + "){" + (len - 1) + "})(" + sym + ")";
    }

    public static final String makeRegExpStringForHtmlContractType(ContractType type)
    {
        return "(?s)\\<(?=[^<]*?\\\"" + type.getStyleClassName() + "\\\")(.+?) .+?\\\"" + type.getStyleClassName() + "\\\"";
    }

    public static String makeRegExpCssClassName(String styleClassName)
    {
        return "(?s)(\\." + styleClassName + "\\s+\\{)(.+?)(\\})";
    }

    public static String makeRegExpCssFieldValue(String fieldName)
    {
        return "(?s)(body\\s*\\{.*?" + fieldName + ":\\s+)(\\d+.*?)(;.*?\\})";
    }

    public static boolean isRunningOnDebugMode()
    {
        return BuildConfig.DEBUG;
    }

    private static Context WORKING_CONTEXT = null;

    public static synchronized void setWorkingContext(Context context)
    {
        WORKING_CONTEXT = context;
    }

    public static synchronized Context getWorkingContext()
    {
        return WORKING_CONTEXT;
    }

    public static String makePreviewColorString(String source, int expressionHashAsId)
    {
        return "<font color=\"#ff0000\" id=\"" + expressionHashAsId + "\">" + source + "</font>";
    }

    public static boolean isDeveloper()
    {
        return Constants.DEVELOPERS_ID.contains(getUserId());
    }

    public static String getApplicationVersion(Context context)
    {
        String versionName = "unknown";
        try
        {
            versionName = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName.replaceAll("\\.", "_");
        } catch (PackageManager.NameNotFoundException e)
        {
            exceptionReport(e);
        }
        return versionName;
    }

    public static String getUserId()
    {
        return Settings.Secure.getString(getWorkingContext().getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static void disableDeathOnFileUriExposure()
    {
        // Разрешает делиться файлом а не контентом в API > 24
        // Источник: https://stackoverflow.com/questions/38200282/android-os-fileuriexposedexception-file-storage-emulated-0-test-txt-exposed

        if (Build.VERSION.SDK_INT >= 24)
        {
            try
            {
                Method m = StrictMode.class.getMethod("disableDeathOnFileUriExposure");
                m.invoke(null);
            } catch (Exception ex)
            {
                exceptionReport(ex);
            }
        }
    }
}
