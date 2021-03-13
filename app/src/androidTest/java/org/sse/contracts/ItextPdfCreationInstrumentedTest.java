package org.sse.contracts;

import android.content.Context;
import android.os.Environment;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.sse.contracts.core.ContractType;
import org.sse.contracts.core.HtmlParser;
import org.sse.contracts.core.PdfCreator;
import org.sse.contracts.core.assets.LocalExtAssetLoader;

import java.io.File;
import java.io.InputStream;

import static org.junit.Assert.assertTrue;


@RunWith(AndroidJUnit4.class)
public class ItextPdfCreationInstrumentedTest
{
    public static File getFolder(Context context, String folderName)
    {
        final String packageName = context.getPackageName();
        File path = new File(Environment.getExternalStorageDirectory(), packageName);
        if (!path.exists())
            path.mkdir();

        if (folderName != null && !folderName.isEmpty())
        {
            path = new File(path, folderName);
            if (!path.exists())
                path.mkdir();
        }

        return path;
    }

    @Test
    public void createNormalTestPdf() throws Exception
    {
        Context testContext = InstrumentationRegistry.getContext();
        Utils.setWorkingContext(InstrumentationRegistry.getTargetContext());

        InputStream inCssFile = testContext.getAssets().open(Constants.STYLE_CSS_PATH);
        String css = Utils.convertStreamToString(inCssFile);
        InputStream in = Utils.getAssetResource(testContext, Constants.CONTRACTS_PATH + "/test.html");
        String source = Utils.convertStreamToString(in);
        source = Utils.prepareExtExpressions(source, new LocalExtAssetLoader(testContext, Constants.EXT_PATH, false));
        HtmlParser parser = new HtmlParser(source, null);
        String normal = parser.buildNormal(false);

        File folder = getFolder(testContext, "");
        PdfCreator.Result result = PdfCreator.createPDF(PdfCreator.DocumentType.Normal, ContractType.ФИЗ_ФИЗ, normal, css, folder, "test", testContext);
        assertTrue(result.isSuccessful());
    }

    @Test
    public void createTemplateTestPdf() throws Exception
    {
        Utils.setWorkingContext(InstrumentationRegistry.getTargetContext());

        InputStream inCssFile = InstrumentationRegistry.getContext().getAssets().open(Constants.STYLE_CSS_PATH);
        String css = Utils.convertStreamToString(inCssFile);
        InputStream in = Utils.getAssetResource(InstrumentationRegistry.getContext(), Constants.CONTRACTS_PATH + "/test.html");
        String source = Utils.convertStreamToString(in);
        HtmlParser parser = new HtmlParser(source, null);
        String template = parser.buildTemplate(false);

        File folder = getFolder(InstrumentationRegistry.getContext(), Constants.FOLDER_TEMPLATE);

        PdfCreator.Result result = PdfCreator.createPDF(PdfCreator.DocumentType.Template, ContractType.ФИЗ_ФИЗ, template, css, folder, "test", InstrumentationRegistry.getContext());
        assertTrue(result.isSuccessful());
    }

    @Test
    public void createAllPdfContracts() throws Exception
    {

        Utils.setWorkingContext(InstrumentationRegistry.getTargetContext());
        Context targetContext = InstrumentationRegistry.getTargetContext();

        String[] contractFiles = targetContext.getAssets().list(Constants.CONTRACTS_PATH);
        InputStream inCssFile = targetContext.getAssets().open(Constants.STYLE_CSS_PATH);
        String css = Utils.convertAndDecryptStreamToString(inCssFile);
        String errors = "";

        for (String fileName : contractFiles)
        {
            String contract = Constants.CONTRACTS_PATH + "/" + fileName;
            InputStream in = targetContext.getAssets().open(contract);
            String source = Utils.convertAndDecryptStreamToString(in);

            File folder = getFolder(targetContext, "");

            HtmlParser parserTypes = new HtmlParser(source, null);
            HtmlParser parser;
            for (ContractType type : parserTypes.getCompatibleContractTypes())
            {
                parser = new HtmlParser(source, type);
                if (!PdfCreator.createPDF(PdfCreator.DocumentType.Normal, type, parser.buildNormal(false), css, folder, fileName, targetContext).isSuccessful())
                    errors = errors + "Ошибка создания документа: " + fileName + ", тип: " + type.getDescription() + "\r\n";
            }
        }

        assertTrue(errors, errors.isEmpty());
    }

    @Test
    public void createAllPdfTemplatesContract() throws Exception
    {

        Utils.setWorkingContext(InstrumentationRegistry.getTargetContext());
        Context targetContext = InstrumentationRegistry.getTargetContext();

        String[] contractFiles = targetContext.getAssets().list(Constants.CONTRACTS_PATH);
        InputStream inCssFile = targetContext.getAssets().open(Constants.STYLE_CSS_PATH);
        String css = Utils.convertAndDecryptStreamToString(inCssFile);
        String errors = "";

        for (String fileName : contractFiles)
        {
            String contract = Constants.CONTRACTS_PATH + "/" + fileName;
            InputStream in = targetContext.getAssets().open(contract);
            String source = Utils.convertAndDecryptStreamToString(in);

            File folder = getFolder(targetContext, Constants.FOLDER_TEMPLATE);

            HtmlParser parserTypes = new HtmlParser(source, null);
            HtmlParser parser;
            for (ContractType type : parserTypes.getCompatibleContractTypes())
            {
                parser = new HtmlParser(source, type);
                if (!PdfCreator.createPDF(PdfCreator.DocumentType.Template, type, parser.buildTemplate(false), css, folder, fileName, targetContext).isSuccessful())
                    errors = errors + "Ошибка создания шаблона документа: " + fileName + ", тип: " + type.getDescription() + "\r\n";
            }
        }
        assertTrue(errors, errors.isEmpty());
    }

}