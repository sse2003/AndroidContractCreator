package org.sse.contracts.core;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Font;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.FontProvider;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.tool.xml.XMLWorkerHelper;

import org.sse.contracts.Constants;
import org.sse.contracts.Utils;
import org.sse.contracts.core.conf.UserConfigurations;
import org.sse.contracts.core.exceptions.ExpressionException;
import org.sse.contracts.core.exceptions.NestedContractTypeException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;

import trikita.log.Log;

import static org.sse.contracts.Constants.FONTS_PATH;

public class PdfCreator
{
    private static final Map<Context, CyrillicFontProvider> fontProviders = new LinkedHashMap();

    public enum DocumentType
    {
        Normal,
        Template
    }

    public static class Result
    {
        private boolean successful;
        private File file = null;

        public Result(boolean successful)
        {
            this.successful = successful;
        }

        public Result(File createdFile)
        {
            this.successful = true;
            this.file = createdFile;
        }

        public boolean isSuccessful()
        {
            return successful;
        }

        public File getCreatedFile()
        {
            return file;
        }

        public String toString()
        {
            if (file != null)
                return "Результат: " + successful + ", файл: " + file + ", размер: " + this.file.length();
            else
                return "Результат: Файл не создан";
        }
    }

    public static Result createPDF(DocumentType documentType, ContractType contractType, String source, File folder, String fileName, Context context) throws ExpressionException, IOException, NestedContractTypeException
    {
        String css = Utils.loadDefaultCssStyle(context);

        return createPDF(documentType, contractType, source, css, folder, fileName, context);
    }

    public static Result createPDF(DocumentType documentType, ContractType contractType, String source, String css, File folder, String fileName, Context context) throws ExpressionException, NestedContractTypeException
    {
        css = Utils.modifyCss(css, contractType, UserConfigurations.getFontSize(), UserConfigurations.getLeftMargin(), UserConfigurations.getRightMargin(), UserConfigurations.getTopMargin(), UserConfigurations.getBottomMargin());

        fileName = Utils.removeFileExtension(fileName);
        if (!fileName.contains(contractType.getDescription()))
            fileName += ", " + contractType.getDescription();

        HtmlParser parser = new HtmlParser(source, contractType);
        if (documentType == DocumentType.Normal)
        {
            source = parser.buildNormal(false);
            fileName += Constants.NORMAL_SUFFIX + ".pdf";
        } else
        {
            source = parser.buildTemplate(false);
            fileName += Constants.TEMPLATE_SUFFIX + ".pdf";
        }

        try
        {
            File file = new File(folder, fileName);
            return createPdf(source, css, file, context);
        } catch (Exception e)
        {
            Utils.exceptionReport(e);
            return new Result(false);
        }
    }

    private static Result createPdf(String source, String css, File file, Context fontContext) throws IOException, DocumentException
    {
        InputStream in = Utils.convertStringToInputStream(source);
        InputStream inCss = Utils.convertStringToInputStream(css);

        float leftMargin = UserConfigurations.getLeftMargin();
        float rightMargin = UserConfigurations.getRightMargin();
        float topMargin = UserConfigurations.getTopMargin();
        float bottomMargin = UserConfigurations.getBottomMargin();

        Document document = new Document(Constants.PDF_PAGE_SIZE, leftMargin, rightMargin, topMargin, bottomMargin);

        FileOutputStream os = new FileOutputStream(file);
        PdfWriter writer = PdfWriter.getInstance(document, os);
        document.open();

        XMLWorkerHelper.getInstance().parseXHtml(writer, document, in, inCss, Charset.defaultCharset(), getFontProvider(fontContext));

        document.close();
        os.close();

        return new Result(file);
    }

    @NonNull
    private static CyrillicFontProvider getFontProvider(Context fontContext) throws IOException
    {
        if (!fontProviders.containsKey(fontContext))
            fontProviders.put(fontContext, new CyrillicFontProvider(fontContext));

        return fontProviders.get(fontContext);
    }


    private static class CyrillicFontProvider implements FontProvider
    {

        private static final String DEFAULT_FONT_PATH = "/system/fonts/DroidSans.ttf";
        private static final String DEFAULT_FONT_ALIAS = "default_font";

        public CyrillicFontProvider(Context context) throws IOException
        {
            setupAllFonts(context, FONTS_PATH);
            FontFactory.register(DEFAULT_FONT_PATH);
        }

        private void setupAllFonts(Context context, String path) throws IOException
        {
            String[] files = context.getAssets().list(path);
            for (String file : files)
            {
                String fontPath = "/assets/" + path + "/" + file;
                Log.d("Регистрируем шрифт: " + fontPath);
                FontFactory.register(fontPath);
            }

            Log.d("Fonts: " + FontFactory.getRegisteredFonts().toString());
            Log.d("Families: " + FontFactory.getRegisteredFamilies().toString());
        }

        @Override
        public Font getFont(String fontName, String encoding, boolean embedded, float size, int style, BaseColor color)
        {
            return FontFactory.getFont(fontName, BaseFont.IDENTITY_H, embedded, size, style, color);
        }

        @Override
        public boolean isRegistered(String name)
        {
            Log.d("-> isRegistered, name: " + name);
            return true;
        }
    }
}
