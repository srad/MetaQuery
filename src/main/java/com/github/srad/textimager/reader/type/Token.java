package com.github.srad.textimager.reader.type;

import javax.xml.namespace.QName;
import java.util.HashMap;
import java.util.Map;

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
    public Map<String, String> toMap() {
        Map map = super.toMap();

        map.put("morph", morph);
        map.put("lemma", lemma);
        map.put("pos", pos);

        return map;
    }

    public static QName getElementInfo() {
        return new QName("http:///de/tudarmstadt/ukp/dkpro/core/api/segmentation/type.ecore", "Token");
    }
}
