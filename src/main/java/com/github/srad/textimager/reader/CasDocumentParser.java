package com.github.srad.textimager.reader;

import com.github.srad.textimager.reader.type.*;

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
public class CasDocumentParser extends AbstractParser {

    private String documentId;

    private String documentTitle;

    final private Sofa sofa = new Sofa();

    final static public List<Class<? extends ElementType>> parsedElements = Arrays.asList(Lemma.class, Token.class, Paragraph.class, Sentence.class);

    public CasDocumentParser(File file) throws FileNotFoundException, XMLStreamException { super(file); }

    /**
     * Only the addedElements with the tag-name and attribute-name defined in {@link Element#acceptedElements} are read
     * and on the Attributes defined in {@link Element#acceptedAttributes} parsed.
     */
    @Override
    public void parse() throws Exception {
        try {
            XMLInputFactory inputFactory = XMLInputFactory.newInstance();
            InputStream in = new FileInputStream(getFile());
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
                        documentTitle = attr.get("documentTitle");
                    }

                    // Pass further to all addedElements for storage purposes
                    attr.put("documentId", documentId);

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
                }
                streamReader.next();
            }
            // Count characters
            String text = sofa.getText();
            for (int i = 0; i < text.length(); i += 1) {
                addElement(new Char(sofa, java.util.UUID.randomUUID().toString(), i, i));
            }
        } catch (Exception e) {
            // dont want to mess up the signature
            throw new Exception(String.format("Parse-error of file(%s): %s", getFile(), e.getMessage()));
        }
    }

    /**
     * A single character is not an actual element within the xmi, but it's treated for unity as such within the types system.
     *
     * @return
     */
    public static List<Class<? extends ElementType>> getParsedTypes() {
        List<Class<? extends ElementType>> types = new ArrayList<>(parsedElements);
        types.add(Char.class);
        return types;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getDocumentTitle() { return documentTitle; }

    public String getDocumentText() { return sofa.getText(); }

    @Override
    public Map<String, String> getDocumentMeta() {
        final HashMap<String, String> map = new HashMap<>();

        map.put("id", getDocumentId());
        map.put("title", getDocumentTitle());
        map.put("text", getDocumentText());

        return map;
    }
}
