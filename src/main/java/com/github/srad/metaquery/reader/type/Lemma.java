package com.github.srad.metaquery.reader.type;

import javax.xml.namespace.QName;
import java.util.HashMap;

final public class Lemma extends ElementType {
    public Lemma(final Sofa sofa, final HashMap<String, String> attr) {
        super(sofa, attr);
    }

    public static QName getElementInfo() {
        return new QName("http:///de/tudarmstadt/ukp/dkpro/core/api/segmentation/type.ecore", "Lemma");
    }

    @Override
    public HashMap<String, String> toMap(String keyPrefix) {
        HashMap map = super.toMap(keyPrefix);
        map.put(keyPrefix + "value", attr.get("value"));

        return map;
    }
}
