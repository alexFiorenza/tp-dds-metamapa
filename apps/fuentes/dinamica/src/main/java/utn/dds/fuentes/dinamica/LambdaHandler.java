package utn.dds.fuentes.dinamica;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import utn.dds.lambda.JavalinLambdaHandler;

public class LambdaHandler implements RequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {
    private static final JavalinLambdaHandler handler = new JavalinLambdaHandler(
        () -> {
            try {
                return Main.createApp();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        },
        "/dinamica"
    );

    @Override
    public APIGatewayV2HTTPResponse handleRequest(APIGatewayV2HTTPEvent event, Context context) {
        return handler.handleRequest(event, context);
    }
}
