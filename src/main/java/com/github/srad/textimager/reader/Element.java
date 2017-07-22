package com.github.srad.textimager.reader;

import java.util.Arrays;
import java.util.HashSet;

final public class Element {
    final public static String XMI = "http://www.omg.org/XMI";

    final public static String Sofa = "Sofa";
    final public static String DocumentMetaData = "DocumentMetaData";

    final public static String Paragraph = "Paragraph";
    final public static String Lemma = "Lemma";
    final public static String Sentence = "Sentence";
    final public static String Token = "Token";

    final public static HashSet<String> acceptedElements = new HashSet<>(Arrays.asList(Element.Sofa, Element.DocumentMetaData, Element.Paragraph, Element.Lemma, Element.Sentence, Element.Token));

    final public static HashSet<String> acceptedAttributes = new HashSet<>(Arrays.asList("sofa", "begin", "end", "value", "documentTitle", "documentId", "sofaString"));
}
