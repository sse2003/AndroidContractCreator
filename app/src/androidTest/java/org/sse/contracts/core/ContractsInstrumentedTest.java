package org.sse.contracts.core;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.util.Log;

import org.junit.Before;
import org.junit.Test;
import org.sse.contracts.Constants;
import org.sse.contracts.Utils;
import org.sse.contracts.core.classes.ExpressionClassesFactory;
import org.sse.contracts.core.contract.AbstractContract;
import org.sse.contracts.core.contract.Contracts;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Created by Administrator on 08.08.2016.
 */

public class ContractsInstrumentedTest
{
    private static final String TAG = "ContractsInstrumentedTest";

    @Before
    public void setUp() throws Exception
    {
        Utils.setWorkingContext(InstrumentationRegistry.getTargetContext());
    }

    @Test
    public void testUnknownParametersAndClasses() throws Exception
    {
        /* Файлы с документами и шрифтами берем из контекста устройства (app\src\main\assets\)
         */
        String errors = "";

        Context targetContext = Utils.getWorkingContext();
        String[] contractFiles = targetContext.getAssets().list(Constants.CONTRACTS_PATH);

        for (String fileName : contractFiles)
        {
            String contract = Constants.CONTRACTS_PATH + "/" + fileName;
            InputStream in = targetContext.getAssets().open(contract);
            String source = Utils.convertAndDecryptStreamToString(in);
            HtmlParser parser = new HtmlParser(source, null);

            Set<Expression> expressions = parser.getExpressions();
            for (Expression exp : expressions)
            {
                for (String paramName : exp.getParameters().getParameters().keySet())
                {
                    if (!Parameters.Params.contains(paramName))
                        errors = errors + "В документе '" + fileName + "' найден неизвестный параметр: '" + paramName + "', строка выражения: '" + exp + "'\r\n";


                    if (paramName.equals(Parameters.Params.c.name()) || paramName.equals(Parameters.Params.с.name()))
                    {
//                        Log.i(TAG, paramName);
                        String cl = exp.getParameters().getParameters().get(paramName).toString();
                        if (ExpressionClassesFactory.create(cl) == null)
                            errors = errors + "В документе '" + fileName + "' найден неизвестный класс выражений: '" + cl + "', строка выражения: '" + exp + "'\r\n";
                    }

                    if (paramName.equals(Parameters.Params.t.name()) || paramName.equals(Parameters.Params.m.name()))
                    {
                        String par = exp.getParameters().getParameters().get(paramName).toString();
                        int l;
                        try
                        {
                            l = Expression.parse_t_m_parameters(par, "_");
                        } catch (Throwable ex)
                        {
                            errors = errors + "В документе '" + fileName + "' найден не числовой параметр: '" + par + "', строка выражения: '" + exp + "'\r\n";
                        }
                    }
                }
            }
        }

        assertTrue(errors, errors.isEmpty());
    }

    @Test
    public void testNonPrintedSymbolContains() throws Exception
    {
        /* Файлы с документами и шрифтами берем из контекста устройства (app\src\main\assets\)
         */
        String errors = "";

        Context targetContext = Utils.getWorkingContext();
        String[] contractFiles = targetContext.getAssets().list(Constants.CONTRACTS_PATH);

        for (String fileName : contractFiles)
        {
            String contract = Constants.CONTRACTS_PATH + "/" + fileName;
            InputStream in = targetContext.getAssets().open(contract);
            String source = Utils.convertAndDecryptStreamToString(in);
            HtmlParser parser = new HtmlParser(source, null);

            Set<Expression> expressions = parser.getExpressions();
            for (Expression exp : expressions)
            {
                if (containsNonPrinted(exp.getRequest()))
                    errors = errors + "В документе '" + fileName + "' найден не печатаемый символ, строка выражения: '" + exp + "'\r\n";

                if (containsNonPrinted(exp.getGroup()))
                    errors = errors + "В документе '" + fileName + "' найден не печатаемый символ, группа: '" + exp.getGroup() + "'\r\n";
            }
        }

        assertTrue(errors, errors.isEmpty());
    }

    private boolean containsNonPrinted(String str)
    {
        if (str == null) return false;
        if (str.contains(Constants.SPACE_SYMBOL)) return true;

        return false;
    }

    /**
     * Тестируем на наличие в документах вложенных тегов с несколькими классами типа документа. Таких не должно быть, т.к. не поддерживается
     * Файлы с документами и шрифтами берем из контекста устройства (app\src\main\assets\)
     */
    @Test
    public void testForNestedClasses() throws Exception
    {
        String errors = "";

        Context targetContext = Utils.getWorkingContext();
        String[] contractFiles = targetContext.getAssets().list(Constants.CONTRACTS_PATH);

        for (String fileName : contractFiles)
        {
            String contract = Constants.CONTRACTS_PATH + "/" + fileName;
            InputStream in = targetContext.getAssets().open(contract);
            String source = Utils.convertAndDecryptStreamToString(in);
            for (ContractType type : ContractType.values())
            {
                try
                {
                    Utils.removeContractTypeClassFromHtml(source, type);
                } catch (Exception ex)
                {
                    String err = "Ошибка в документе '" + fileName + "', type: " + type + " : '" + ex.getMessage() + "'";
                    errors = errors + err + "\r\n";
                    throw new Exception(errors, ex);
                }
            }
        }

        assertTrue(errors, errors.isEmpty());
    }

    /**
     * Проверяем, что все договора содержат GroupId и версию
     * Выводим список договоров и их группу
     */
    @Test
    public void testContainsGroipIdAndVersion() throws Exception
    {
        Map<String, List<AbstractContract>> groups = new HashMap<>();

        Set<AbstractContract> contracts = Contracts.getInstance().getContracts();

        for (AbstractContract contract : contracts)
        {
            assertNotNull("'" + contract.getName() + "' не содержит GroupId", contract.getGroupId());
            assertNotNull("'" + contract.getName() + "' не содержит версии документа", contract.getVersion());

            if (!groups.containsKey(contract.getGroupId()))
                groups.put(contract.getGroupId(), new ArrayList());

            List<AbstractContract> innerContracts = groups.get(contract.getGroupId());
            innerContracts.add(contract);
        }

        // Печатаем группы

        for (String gr : groups.keySet())
        {
            Log.d(TAG, "Группа " + gr + ":");
            for (AbstractContract cont : groups.get(gr))
            {
                Log.d(TAG, "    " + cont.getName());
            }
        }
    }

    @Test
    public void checkDocumentsForExpressionOccurences() throws Exception
    {
        String errors = "";

        Set<AbstractContract> contracts = Contracts.getInstance().getContracts();
        for (AbstractContract contract : contracts)
        {
            for (ContractType type : contract.getCompatibleContractTypes())
            {
                HtmlParser parser = new HtmlParser(contract.getContent(), type);
                List<String> expressions = findExpressions(parser.buildNormal(false));
                if (expressions != null && !expressions.isEmpty())
                    errors = errors + "В документе '" + contract.getName() + "', тип: '" + type.getDescription() + "' найдены не обработанные выражения: " + Arrays.toString(expressions.toArray()) + "\r\n";
            }
        }
        assertTrue(errors, errors.isEmpty());
    }

    private List<String> findExpressions(String source)
    {
        List<String> result = new ArrayList<>();
        Matcher m = Constants.PATTERN_EXPRESSION.matcher(source);

        while (m.find())
            result.add(m.group(Constants.GROUP_EXPRESSION_ALL));

        return result;
    }
}
