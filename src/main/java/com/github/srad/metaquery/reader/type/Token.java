package com.github.srad.metaquery.reader.type;

import javax.xml.namespace.QName;
import java.util.HashMap;

final public class Token extends ElementType {

    final public String morph;
    final public String lemma;
    final public String pos;

    public Token(final Sofa sofa, final HashMap<String, String> attr) {
        super(sofa, attr);
        this.morph = attr.get("morph");
        this.lemma = attr.get("lemma");
        this.pos = attr.get("pos");
    }

    @Override
    public HashMap<String, String> toMap(final String keyPrefix) {
        HashMap map = super.toMap(keyPrefix);

        map.put(keyPrefix + "morph", morph);
        map.put(keyPrefix +"lemma", lemma);
        map.put(keyPrefix +"pos", pos);

        return map;
    }

    public static QName getElementInfo() {
        return new QName("http:///de/tudarmstadt/ukp/dkpro/core/api/segmentation/type.ecore", "Token");
    }
}
