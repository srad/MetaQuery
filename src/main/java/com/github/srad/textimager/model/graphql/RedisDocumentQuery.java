package com.github.srad.textimager.model.graphql;

import com.github.srad.textimager.model.type.Document;

import java.util.List;

public class RedisDocumentQuery extends DocumentSchema {

    @Override
    protected List<Document> getDocuments(String[] ids) {
        try {
            return service.getDocs(ids);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
