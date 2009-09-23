package org.cagrid.cql.cqlbuilder.gui;


import org.w3c.dom.Element;
import java.util.List;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.cagrid.cql.cqlbuilder.cqlquery.CQLQuery;
import org.cagrid.cql.cqlbuilder.metadata.UMLClass;
import org.cagrid.cql.cqlbuilder.servicecall.MetaDataProvider;
import org.cagrid.cql.cqlbuilder.servicecall.OneCallMetaDataProvider;
import org.cagrid.cql.cqlbuilder.servicecall.QueryDataProvider;


/**
 * This is the access class that provides access to Web service data for GUI classes
 * @author Monika
 */
public class ServiceDataAccess {

    public static Map<String, MetaDataProvider> serviceCache = new HashMap<String, MetaDataProvider>();
    private static MetaDataProvider lastUsedMetadataProv = null;

    public static String getTextDomainModel(String fUrl) {
        if (fUrl == null || "".equals(fUrl)) {
            return null;
        }
        MetaDataProvider mt = getMetadataProvider(fUrl);
        return formatToTwoColumns(mt.getDomainModelInfo());
    }

    public static String getTextGeneralInfo(String fUrl) {
        if (fUrl == null || "".equals(fUrl)) {
            return null;
        }
        MetaDataProvider mt = getMetadataProvider(fUrl);
        return formatToTwoColumns(mt.getServiceDescription());
    }

    public static String getTextHostingRes(String fUrl) {
        if (fUrl == null || "".equals(fUrl)) {
            return null;
        }
        MetaDataProvider mt = getMetadataProvider(fUrl);
        return formatToTwoColumns(mt.getResearchCenterInfo());
    }

    public static String getTextSemanticMetadata(String fUrl) {
        if (fUrl == null || "".equals(fUrl)) {
            return null;
        }
        MetaDataProvider mt = getMetadataProvider(fUrl);
        StringBuilder sb = new StringBuilder();
        LinkedList<LinkedHashMap<String, String>> ll = mt.getSemanticMetadataItems();
        for (LinkedHashMap<String, String> map : ll) {
            sb.append(formatToTwoColumns(map)).append("\n");
        }
        return sb.toString();
    }

    public static List<UMLClass> getUmlClasses(String fUrl) {
        if (fUrl == null || "".equals(fUrl)) {
            return null;
        }
        MetaDataProvider mt = getMetadataProvider(fUrl);
        return mt.getUMLClasses();
    }

    private static MetaDataProvider getMetadataProvider(String aUrl) {
        if (aUrl == null || "".equals(aUrl)) {
            return null;
        }
        if (lastUsedMetadataProv != null) {
            if (aUrl.equals(lastUsedMetadataProv.getServiceURL())) {
                return lastUsedMetadataProv;
            }
        }
        MetaDataProvider searchProv = serviceCache.get(aUrl);
        if (searchProv != null) {
            lastUsedMetadataProv = searchProv;
            return searchProv;
        }
        //else
        MetaDataProvider newProv = new OneCallMetaDataProvider(aUrl);
        serviceCache.put(aUrl, newProv);
        lastUsedMetadataProv = newProv;
        return newProv;
    }
    private static final int LABEL_LENGTH = 20;
    private static final int DESCR_LENGTH = 30;

    private static String formatToTwoColumns(LinkedHashMap<String, String> aStringMap) {
        StringBuilder strBuilder = new StringBuilder();
        Set<Entry<String, String>> entrySet = aStringMap.entrySet();
        Iterator<Entry<String, String>> it = entrySet.iterator();
        while (it.hasNext()) {
            Entry<String, String> entry = it.next();
            strBuilder.append(String.format("%" + LABEL_LENGTH + "s: ", entry.getKey()));
            String value = entry.getValue();
            if (value.length() <= DESCR_LENGTH) {
                strBuilder.append(String.format("%-" + DESCR_LENGTH + "s", value) + "\n");
            } else {
                strBuilder.append(wrap(value, LABEL_LENGTH + 2, DESCR_LENGTH) + "\n");
            }
        }
        return strBuilder.toString();
    }

    private static String wrap(String value, int i, int DESCR_LENGTH) {
        return value;
    }

    static void executeCQLQuery(CQLQuery cqlQuery, String fUrl) throws Exception {
        QueryDataProvider.executeCQLQuery(cqlQuery.get_mainNode(), fUrl);
    }

    static void executeCQLQuery(Element cqlQueryElement, String fUrl) throws Exception {
        QueryDataProvider.executeCQLQuery(cqlQueryElement, fUrl);
    }
}
/*private static String wrap(String aString, int aLeftIndent, int aTextWitdh) {
StringBuilder wrapped = new StringBuilder();

String[] splitted = aString.split("\\s");
int lineLength = 0;
StringBuilder strBuild = new StringBuilder();
if (splitted.length > 1) {
strBuild.append(splitted[0]).append(" ");
lineLength = lineLength + splitted[0].length();
}

for (int i = 1; i < splitted.length - 1; i++) {
String word = splitted[i];
if ((lineLength + 1) > aTextWitdh) {
wrapped.append(String.format("%-" + aTextWitdh + "s", strBuild.toString() + "\n"));
wrapped.append(String.format("%" + aLeftIndent + "s", ""));
strBuild = new StringBuilder();
lineLength = 0;
} else {
strBuild.append(word).append(" ");
lineLength = lineLength + word.length();
}
}

String word = splitted[splitted.length - 1];
if ((lineLength + 1) > aTextWitdh) {
wrapped.append(String.format("%-" + aTextWitdh + "s", strBuild.toString() + "\n"));
wrapped.append(String.format("%" + aLeftIndent + "s", ""));
wrapped.append(String.format("%-" + aTextWitdh + "s", word));
} else {
strBuild.append(word);
wrapped.append(String.format("%-" + aTextWitdh + "s", strBuild.toString()));

}
return wrapped.toString();
}*/

