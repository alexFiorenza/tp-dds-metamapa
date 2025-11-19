package utn.dds.agregador.graphql;

import com.fasterxml.jackson.databind.ObjectMapper;
import graphql.ExecutionResult;
import graphql.GraphQL;
import graphql.schema.GraphQLSchema;
import graphql.schema.idl.RuntimeWiring;
import graphql.schema.idl.SchemaGenerator;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;
import graphql.schema.idl.errors.SchemaProblem;
import graphql.scalars.ExtendedScalars;
import io.javalin.http.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import utn.dds.agregador.service.ServiceAgregador;
import utn.dds.agregador.service.ServiceRegistry;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Map;

/**
 * Handler que procesa requests GraphQL en Javalin
 */
public class GraphQLHandler {

    private static final Logger logger = LoggerFactory.getLogger(GraphQLHandler.class);
    private final GraphQL graphQL;
    private final ObjectMapper objectMapper;

    public GraphQLHandler(ServiceAgregador serviceAgregador, ServiceRegistry serviceRegistry) {
        this.objectMapper = new ObjectMapper();
        this.graphQL = buildGraphQL(serviceAgregador, serviceRegistry);
        logger.info("GraphQL inicializado correctamente");
    }

    /**
     * Construye la instancia de GraphQL con el schema y los data fetchers
     */
    private GraphQL buildGraphQL(ServiceAgregador serviceAgregador, ServiceRegistry serviceRegistry) {
        try {
            // 1. Parsear el schema desde el archivo .graphqls
            TypeDefinitionRegistry typeRegistry = parseSchema();

            // 2. Crear los data fetchers
            GraphQLDataFetchers dataFetchers = new GraphQLDataFetchers(serviceAgregador, serviceRegistry);

            // 3. Configurar el runtime wiring (conectar queries/mutations con data fetchers)
            RuntimeWiring runtimeWiring = buildRuntimeWiring(dataFetchers);

            // 4. Generar el schema ejecutable
            SchemaGenerator schemaGenerator = new SchemaGenerator();
            GraphQLSchema graphQLSchema = schemaGenerator.makeExecutableSchema(typeRegistry, runtimeWiring);

            // 5. Crear la instancia de GraphQL
            return GraphQL.newGraphQL(graphQLSchema).build();

        } catch (Exception e) {
            logger.error("Error al construir GraphQL", e);
            throw new RuntimeException("Error al inicializar GraphQL: " + e.getMessage(), e);
        }
    }

    /**
     * Parsea el schema desde el archivo schema.graphqls
     */
    private TypeDefinitionRegistry parseSchema() throws IOException {
        SchemaParser schemaParser = new SchemaParser();
        InputStream schemaStream = getClass().getClassLoader().getResourceAsStream("schema.graphqls");

        if (schemaStream == null) {
            throw new IOException("No se encontró el archivo schema.graphqls en resources");
        }

        try (Reader reader = new InputStreamReader(schemaStream)) {
            return schemaParser.parse(reader);
        }
    }

    /**
     * Configura el RuntimeWiring que conecta el schema con los data fetchers
     */
    private RuntimeWiring buildRuntimeWiring(GraphQLDataFetchers dataFetchers) {
        return RuntimeWiring.newRuntimeWiring()
            // Scalars personalizados
            .scalar(ExtendedScalars.DateTime)
            .scalar(ExtendedScalars.Date)
            .scalar(ExtendedScalars.Json)

            // Queries
            .type("Query", typeWiring -> typeWiring
                .dataFetcher("hechos", dataFetchers.getHechosPaginados())
                .dataFetcher("fuentes", dataFetchers.getFuentes())
                .dataFetcher("fuentesPorHost", dataFetchers.getFuentesPorHost())
            )

            // Mutations
            .type("Mutation", typeWiring -> typeWiring
                .dataFetcher("ejecutarAgregacion", dataFetchers.ejecutarAgregacion())
                .dataFetcher("registrarFuente", dataFetchers.registrarFuente())
                .dataFetcher("actualizarFuente", dataFetchers.actualizarFuente())
                .dataFetcher("eliminarFuente", dataFetchers.eliminarFuente())
            )

            .build();
    }

    /**
     * Maneja las peticiones GraphQL desde Javalin
     */
    public void handle(Context ctx) {
        try {
            // Parsear el body del request
            Map<String, Object> body = objectMapper.readValue(ctx.body(), Map.class);

            String query = (String) body.get("query");
            String operationName = (String) body.get("operationName");
            Map<String, Object> variables = (Map<String, Object>) body.get("variables");

            // Ejecutar la query GraphQL
            ExecutionResult executionResult = graphQL.execute(builder -> builder
                .query(query)
                .operationName(operationName)
                .variables(variables != null ? variables : Map.of())
            );

            // Preparar la respuesta
            Map<String, Object> result = executionResult.toSpecification();

            // Devolver la respuesta
            ctx.json(result);

        } catch (Exception e) {
            logger.error("Error procesando request GraphQL", e);
            ctx.status(500).json(Map.of(
                "errors", Map.of(
                    "message", "Error interno del servidor: " + e.getMessage()
                )
            ));
        }
    }

    /**
     * Handler para GraphiQL UI (opcional)
     * Sirve una interfaz web para probar queries GraphQL
     */
    public void handleGraphiQL(Context ctx) {
        String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>GraphiQL - MetaMapa Agregador</title>
                <link href="https://unpkg.com/graphiql@3.0.0/graphiql.min.css" rel="stylesheet" />
                <style>
                    body {
                        height: 100vh;
                        margin: 0;
                        overflow: hidden;
                    }
                    #graphiql {
                        height: 100vh;
                    }
                </style>
            </head>
            <body>
                <div id="graphiql">Cargando...</div>
                <script
                    crossorigin
                    src="https://unpkg.com/react@18/umd/react.production.min.js"
                ></script>
                <script
                    crossorigin
                    src="https://unpkg.com/react-dom@18/umd/react-dom.production.min.js"
                ></script>
                <script
                    crossorigin
                    src="https://unpkg.com/graphiql@3.0.0/graphiql.min.js"
                ></script>
                <script>
                    const fetcher = GraphiQL.createFetcher({ url: '/graphql' });
                    const root = ReactDOM.createRoot(document.getElementById('graphiql'));
                    root.render(
                        React.createElement(GraphiQL, { fetcher: fetcher })
                    );
                </script>
            </body>
            </html>
            """;

        ctx.html(html);
    }
}
