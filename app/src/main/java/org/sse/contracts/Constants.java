package org.sse.contracts;

import com.itextpdf.text.PageSize;
import com.itextpdf.text.Rectangle;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;


public class Constants
{
    public static final String CONTRACTS_PATH = "data/contracts";
    public static final String EXT_PATH =  "data/ext";
    public static final String FONTS_PATH = "fonts";
    public static final String STYLE_CSS_PATH = "data/style.css";
    public static final String DOCUMENT_EXT = ".html";

    public static final String MIME_PDF = "application/pdf";

    public static final Pattern PATTERN_EXPRESSION = Pattern.compile("(?s)(#{2}([^@][^#].+?)#)");
    public static final int GROUP_EXPRESSION_ALL = 1;
    public static final int GROUP_EXPRESSION_TEXT = 2;

    public static final Pattern PATTERN_PARAMETERS = Pattern.compile("(?s)^##(\\..+') (.*)#");
    public static final int GROUP_PARAMETERS_PARAMETERS = 1;
    public static final int GROUP_PARAMETERS_REQUEST_TEXT = 2;

    public static final String REG_EXP_SPECIAL_SPACE_SYMBOL_AFTER_PARAMETER = "(##\\..+')(&nbsp;)([^']*#)";
    public static final Pattern PATTERN_GROUP = Pattern.compile("(?s)(##@(.+?)#)");
    public static final int GROUP_GROUP_TEXT = 2;

    public static final Pattern PATTERN_GROUP_ID = Pattern.compile("(?s)GroupId:\\s*(.+?\\d+)");
    public static final int GROUP_GROUP_ID = 1;

    public static final Pattern PATTERN_DOCUMENT_VERSION = Pattern.compile("(?s)Версия документа:\\s*(\\d+)");
    public static final int GROUP_DOCUMENT_VERSION = 1;

    public static final Pattern PATTERN_EXT_EXPRESSIONS = Pattern.compile("(?s)(##\\$(.+?)#)");
    public static final int GROUP_EXT_EXPRESSIONS = 2;
    public static final String REG_EXP_FIRST_WHITESPACES = "^(\\s*)";

    public static final Pattern PATTERN_EXT_BODY = Pattern.compile("(?s)<body>(.*)<\\/body>");
    public static final int GROUP_EXT_BODY = 1;

    public static final String REG_EXP_SPAN_UNDERLINE = "(?s)<span(?=(?:[^>]*underline;.*?)>).*?>(.*?)<\\/span>";
    public static final int GROUP_SPAN_UNDERLINE_TEXT = 1;

    public static final String REG_EXP_SPAN = "(?s)<span.*?>(.*?)<\\/span>";
    public static final int GROUP_SPAN_TEXT = 1;

    public static final String REG_EXP_INDEX = "\\\"(.*)\\\"\\s(\\d+)";
    public static final int GROUP_INDEX_NAME = 1;
    public static final int GROUP_INDEX_VERSION = 2;

    public static final String REG_EXP_GLOBAL_PARAMETER = "\\.g='.*?'";

    public static final String TEST_CONTRACTS_PATH = "testDocuments";
    public static final String FILE_INDEX_NAME = "index";
    public static final int FIREBASE_REQUEST_SIZE = 3 * 1024 * 1024;


    public static final String REG_EXP_EXPRESSION_REQUEST_END_OF_LINE = "\\s*\\.*$";

    public static final int GROUP_UNDERSCORE = 1;

    public static final String PATTERN_PURCHASE_SKU_TITLE_REPLACE = "\\s*\\(.*\\)";

    public static final String PATTERN_NUMBERS = "^(\\d+)";
    public static final int GROUP_PATTERN_NUMBERS = 1;

    public static final String SPACE_SYMBOL = "&nbsp;";
    public static final String TEMPLATE_SYMBOL = "&nbsp;";
    public static final String CSS_DISPLAY_NONE = "display: none;";
    public static final String NORMAL_SUFFIX = "";
    public static final String TEMPLATE_SUFFIX = ", шаблон";
    public static final String FOLDER_TEMPLATE = "Шаблоны";

    public static final Rectangle PDF_PAGE_SIZE = PageSize.A4;
    public static final String PDF_DEFAULT_LEFT_MARGIN = "64"; // 2 см
    public static final String PDF_DEFAULT_RIGHT_MARGIN = "32"; // 1 см
    public static final String PDF_DEFAULT_TOP_MARGIN = "64"; // 2 см
    public static final String PDF_DEFAULT_BOTTOM_MARGIN = "64"; // 2 см
    public static final String PRELOAD_FILE_EXT = ".cnt";

    public static final String HTML_NEW_LINE_TAG = "<br />";

    public static final String GOOGLE_BASE64_PUBLIC_KEY = "MIII";


    // Generate your own 20 random bytes, and put them here.
    public static final byte[] SALT = new byte[]{
            1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1
    };
    public static final int GROUP_CONTRACT_TYPE_TAG = 1;
    public static final int DEFAULT_FREE_DOCUMENTS = 2;

    public static final long SECOND_IN_MS = 1000;
    public static final long MINUTE_IN_MS = SECOND_IN_MS * 60;
    public static final long HOUR_IN_MS = MINUTE_IN_MS * 60;
    public static final long DAY_IN_MS = HOUR_IN_MS * 24;
    public static final long WEEK_IN_MS = DAY_IN_MS * 7;
    public static final long MONTH_IN_MS = DAY_IN_MS * 30;
    public static final long QUARTER_IN_MS = DAY_IN_MS * 91;
    public static final long YEAR_IN_MS = DAY_IN_MS * 365;
    public static final String CHOICES_DESCRIPTION_SUFFIX = " ..";

    public static DateFormat DEFAULT_DATE_FORMAT = new SimpleDateFormat("dd-MM-yyyy HH:mm");
    public static final String DEFAULT_PDF_DATE_FORMAT = "\"dd\" MMMM yyyy г";

    public static final long UPDATE_TIME_PERIOD = DAY_IN_MS * 3;

    public static final String CSS_MARGIN_LEFT = "margin-left";
    public static final String CSS_MARGIN_RIGHT = "margin-right";
    public static final String CSS_MARGIN_TOP = "margin-top";
    public static final String CSS_MARGIN_BOTTOM = "margin-bottom";

    public static Cipher ASSETS_CIPHER;

    static
    {
        try
        {
            byte[] ASSETS_ENCRUPT_KEY = new String("1111111111111111").getBytes("UTF-8");
            final byte[] IV = new String("1111111111111111").getBytes("UTF-8");

            ASSETS_CIPHER = Cipher.getInstance("AES/CBC/PKCS5Padding");
            SecretKeySpec key = new SecretKeySpec(ASSETS_ENCRUPT_KEY, "AES");
            ASSETS_CIPHER.init(Cipher.DECRYPT_MODE, key, new IvParameterSpec(IV));
        } catch (Exception e)
        {
            Utils.exceptionReport(e);
        }
    }


    static final List<String> DEVELOPERS_ID = new ArrayList()
    {
        {
        }
    };
}
