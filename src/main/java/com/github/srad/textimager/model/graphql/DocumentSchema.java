package com.github.srad.textimager.model.graphql;

import com.github.srad.textimager.model.query.AbstractQueryExecutor;
import com.github.srad.textimager.model.type.Document;
import com.github.srad.textimager.reader.type.ElementType;
import com.github.srad.textimager.reader.type.Token;
import com.github.srad.textimager.storage.redis.RedisStorage;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.language.Field;
import graphql.schema.*;
import java.util.*;

import static graphql.Scalars.GraphQLID;
import static graphql.Scalars.GraphQLInt;
import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

abstract public class DocumentSchema extends AbstractQueryExecutor<ExecutionResult, HashMap<String, String>> {

    private GraphQL graph;

    protected RedisStorage service = new RedisStorage();

    public DocumentSchema() {
        GraphQLObjectType tokenType = GraphQLObjectType.newObject()
                .name("token")
                .field(newFieldDefinition()
                        .name("text")
                        .type(GraphQLString)
                        .dataFetcher(env -> fetch(() -> getElementField(env, "text", Token.class))))
                .field(newFieldDefinition()
                        .name("begin")
                        .type(GraphQLString)
                        .dataFetcher(env -> fetch(() -> getElementField(env, "begin", Token.class))))
                .field(newFieldDefinition()
                        .name("end")
                        .type(GraphQLString)
                        .dataFetcher(env -> fetch(() -> getElementField(env, "end", Token.class))))
                .build();

        GraphQLObjectType docType = newObject()
                .name("document")
                .field(newFieldDefinition()
                        .name("id")
                        .type(GraphQLID))
                .field(newFieldDefinition()
                        .type(GraphQLString)
                        .name("title"))
                .field(newFieldDefinition()
                        .type(GraphQLString)
                        .name("text"))
                .field(newFieldDefinition()
                        .name("token")
                        .type(new GraphQLList(tokenType))
                        .dataFetcher(env -> fetch(() -> {
                            try {
                                Document doc = (Document) env.getSource();
                                Set<String> ids = service.getElementIds(doc.getId(), com.github.srad.textimager.reader.type.Token.class);
                                String[][] entries = new String[ids.size()][2];
                                int i = 0;

                                for(String id: ids) {
                                    entries[i] = new String[]{doc.getId(), id};
                                    i += 1;
                                }

                                return entries;
                            } catch (Exception e) {
                                return e.getMessage();
                            }
                        })))
                .build();

        GraphQLObjectType root = GraphQLObjectType.newObject()
                .name("query")
                .field(GraphQLFieldDefinition.newFieldDefinition()
                        .name("document")
                        .type(new GraphQLList(docType))
                        .argument(GraphQLArgument.newArgument()
                                .name("id")
                                .type(new GraphQLList(new GraphQLNonNull(GraphQLID)))
                                .build())
                        .argument(GraphQLArgument.newArgument()
                                .name("limit")
                                .type(GraphQLInt)
                                .build())
                        .argument(GraphQLArgument.newArgument()
                                .name("offset")
                                .type(GraphQLInt)
                                .build())
                        .dataFetcher(env -> fetch(() -> fetchDocument(env)))
                        .build())
                .build();

        GraphQLSchema schema = GraphQLSchema.newSchema()
                .query(root)
                .build();

        graph = GraphQL.newGraphQL(schema).build();
    }

    private String getElementField(DataFetchingEnvironment env, String field, Class<? extends ElementType> type) {
        String[] docIdAndElementId = env.getSource();
        try {
            return service.getElement(docIdAndElementId[0], docIdAndElementId[1], field, type);
        } catch (Exception e2) {
            return e2.getMessage();
        }
    }

    private List<Document> fetchDocument(DataFetchingEnvironment env) {
        // optimized select title scrolling
        Map<String, List<Field>> selectedFields = env.getSelectionSet().get();

        boolean isOnlyTitleSelected = selectedFields.size() == 1 && selectedFields.containsKey("title");
/*
                            if (isOnlyTitleSelected) {
                                Map<String, Object> arguments = env.getArguments();
                                boolean areScrollArgsProvided = arguments.containsKey("limit") && arguments.containsKey("offset");
                                if (areScrollArgsProvided) {
                                    return service.paginateTitles());
                                } else {
                                    //throw new Exception("Arguments limit and offset missing for title scrolling");
                                }
                            }
*/
        try {
            List<String> idList = env.getArgument("id");
            String[] idArray = idList.stream().toArray(String[]::new);

            return getDocuments(idArray);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    abstract protected List<Document> getDocuments(String[] ids);

    @Override
    protected ExecutionResult executeImplementation(String query) {
        return graph.execute(query);
    }

    @Override
    protected HashMap<String, String> getResultSet(ExecutionResult result) {
        return result.getData();
    }
}
