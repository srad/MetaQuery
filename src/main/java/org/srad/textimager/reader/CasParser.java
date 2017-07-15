package main.java.org.srad.textimager.reader;

import main.java.org.srad.textimager.reader.type.*;

import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.*;

/**
 * StreamReader has only at the end the actual text (sofaString)
 */
public class CasParser extends AbstractParser {
    final private File file;

    private String documentId;

    final private Sofa sofa = new Sofa();

    final private List<Class<? extends ElementType>> parsedElements = Arrays.asList(Lemma.class, Token.class, Paragraph.class, Sentence.class);

    public CasParser(File file) throws FileNotFoundException, XMLStreamException { this.file = file; parse(); }

    /**
     * Only the addedElements with the tag-name and attribute-name defined in {@link Element#acceptedElements} are read
     * and on the Attributes defined in {@link Element#acceptedAttributes} parsed.
     */
    public void parse() throws FileNotFoundException, XMLStreamException {
        XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        InputStream in = new FileInputStream(file);
        XMLStreamReader streamReader = inputFactory.createXMLStreamReader(in);

        while (streamReader.hasNext()) {
            boolean isAllowedStartElement = streamReader.isStartElement() && Element.acceptedElements.contains(streamReader.getLocalName());

            if (isAllowedStartElement) {
                QName name = streamReader.getName();
                String elementName = name.getLocalPart();
                int attrCount = streamReader.getAttributeCount();
                HashMap<String, String> attr = new HashMap<>();

                // Filter accepted attributes
                for (int i = 0; i < attrCount; i += 1) {
                    QName fullAttrName = streamReader.getAttributeName(i);
                    String attributeName = fullAttrName.getLocalPart();

                    // xmi:id
                    if (fullAttrName.getNamespaceURI() == Element.XMI && attributeName == "id" && fullAttrName.getPrefix() == "xmi") {
                        attr.put("id", streamReader.getAttributeValue(i));
                    } else if (Element.acceptedAttributes.contains(attributeName)) {
                        attr.put(attributeName, streamReader.getAttributeValue(i));
                    }
                }

                if (elementName.equals(Element.DocumentMetaData)) {
                    documentId = attr.get("documentId");
                }

                // Pass further to all addedElements for storage purposes
                attr.put("documentId", documentId);

                try {
                    if (Sofa.getElementInfo().equals(name)) {
                        sofa.set(attr.get("id"), attr.get("sofaString"));
                    } else {
                        // Instantiate accepted element types
                        for (Class<? extends ElementType> elementType : parsedElements) {
                            QName n = (QName) elementType.getMethod("getElementInfo").invoke(null);
                            if (name.equals(n)) {
                                addElement(elementType.getDeclaredConstructor(Sofa.class, String.class, String.class, String.class)
                                        .newInstance(sofa, attr.get("id"), attr.get("begin"), attr.get("end")));
                            }
                        }
                    }
                } catch (Exception e) {
                    System.err.printf("ERROR-ELEMENT-INIT: %s", e.getMessage());
                }
            }
            streamReader.next();
        }
    }

    public String getDocumentId() {
        return documentId;
    }
}
