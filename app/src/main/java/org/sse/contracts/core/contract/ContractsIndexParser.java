package org.sse.contracts.core.contract;

import org.sse.contracts.Utils;
import org.sse.contracts.Constants;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ContractsIndexParser
{
    private HashMap<String, Integer> map = new LinkedHashMap<>();

    public ContractsIndexParser(String contractsIndex)
    {
        if (contractsIndex == null || contractsIndex.isEmpty()) return;

        Pattern pattern = Pattern.compile(Constants.REG_EXP_INDEX);

        String[] lines = contractsIndex.split("[\r\n|\n]");
        for (String line : lines)
        {
            if (line.startsWith("#")) continue;
            Matcher m = pattern.matcher(line);
            if (m.find())
            {
                String name = Utils.removeExtension(m.group(Constants.GROUP_INDEX_NAME));
                Integer version = Integer.valueOf(m.group(Constants.GROUP_INDEX_VERSION));
                map.put(name, version);
            }
        }
    }

    public Integer getVersion(String name)
    {
        return map.get(Utils.removeExtension(name));
    }

    public int getSize()
    {
        return map.size();
    }

    public Set<String> getDocuments()
    {
        return map.keySet();
    }

}
