package com.github.srad.textimager.model.graphql;

import com.github.srad.textimager.model.query.AbstractQueryExecutor;
import com.github.srad.textimager.model.type.Document;
import com.github.srad.textimager.storage.redis.RedisStorage;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.language.Field;
import graphql.schema.*;

import java.util.List;
import java.util.Map;

import static graphql.Scalars.GraphQLID;
import static graphql.Scalars.GraphQLInt;
import static graphql.Scalars.GraphQLString;
import static graphql.schema.GraphQLFieldDefinition.newFieldDefinition;
import static graphql.schema.GraphQLObjectType.newObject;

abstract public class DocumentGraphQLQuery extends AbstractQueryExecutor {

    private GraphQL graphQL;

    protected static RedisStorage service = new RedisStorage();

    public DocumentGraphQLQuery() {
        GraphQLObjectType tokenType = GraphQLObjectType.newObject()
                .name("token")
                .field(newFieldDefinition()
                        .name("text")
                        .type(GraphQLString)
                        .staticValue("2"))
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
                        .type(tokenType))
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
                        .dataFetcher(env -> fetchDocument(env))
                        .build())
                .build();

        GraphQLSchema schema = GraphQLSchema.newSchema()
                .query(root)
                .build();

        graphQL = GraphQL.newGraphQL(schema).build();
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
        return graphQL.<ExecutionResult>execute(query);
    }
}
