package com.github.srad.metaquery.dbms.storage;

import com.github.srad.metaquery.model.Document;
import com.github.srad.metaquery.model.Token;
import com.github.srad.metaquery.reader.type.ElementType;

import java.util.ArrayList;
import java.util.Map;
import java.util.Set;

abstract public class AbstractStorage {
    /** Name of the used database engine, append all detain in one string to display on status monitor. */
    abstract public String engineName();

    abstract public Object[] unionScoredTypes(final String[] ids, final String type, final Long limit) throws Exception;

    abstract public Map<String, String> paginateTitles(final int offset, final Long limit) throws Exception;

    abstract public Set<String> unionSet(final String elemenType, String[] elements) throws Exception;

    abstract public Set<String> intersectSet(final String elemenType, String[] elements) throws Exception;

    abstract public ArrayList<Document> getDocs(String documentId) throws Exception;

    abstract public ArrayList<Document> getDocs(String[] documentIds) throws Exception;

    abstract public String getDocCount() throws Exception;

    abstract public Long setCardinality(String type, String text) throws Exception;

    abstract public ArrayList<Token> getElementTypes(String documentId, Class<? extends ElementType> type) throws Exception;

    abstract public Set<String> getElementIds(String documentId, Class<? extends ElementType> type) throws Exception;

    abstract public String getElement(String documentId, String elementId, String elementKey, Class<? extends ElementType> type) throws Exception;

    abstract public Map<String, String> getElements(String documentId, Class<? extends ElementType> type) throws Exception;

    abstract public void close();
}
