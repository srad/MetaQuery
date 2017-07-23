package com.github.srad.textimager.model.graphql;

import com.github.srad.textimager.model.type.Document;

import java.util.List;

public class RedisGraphQLDocumentQuery extends DocumentGraphQLQuery {

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
