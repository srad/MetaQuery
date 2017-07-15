package main.java.org.srad.textimager.reader.type;

import javax.xml.namespace.QName;

final public class Paragraph extends ElementType {
    public Paragraph(final Sofa sofa, final String id, final String begin, final String end) {
        super(sofa, id, begin, end);
    }

    public static QName getElementInfo() {
        return new QName("http:///de/tudarmstadt/ukp/dkpro/core/api/segmentation/type.ecore", "Paragraph");
    }
}
