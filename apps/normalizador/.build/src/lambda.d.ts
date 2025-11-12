import type { APIGatewayProxyEvent, APIGatewayProxyResult, Context } from 'aws-lambda';
/**
 * Handler para POST /normalizar
 * AWS Lambda invoca esta función cuando llega una request al endpoint
 */
export declare function normalizarHandler(event: APIGatewayProxyEvent, context: Context): Promise<APIGatewayProxyResult>;
/**
 * Handler para GET /categorias
 * Retorna las categorías válidas configuradas
 */
export declare function categoriasHandler(event: APIGatewayProxyEvent, context: Context): Promise<APIGatewayProxyResult>;
/**
 * Handler para GET /health
 */
export declare function healthHandler(event: APIGatewayProxyEvent, context: Context): Promise<APIGatewayProxyResult>;
//# sourceMappingURL=lambda.d.ts.map