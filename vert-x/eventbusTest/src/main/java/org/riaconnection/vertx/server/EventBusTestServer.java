package org.riaconnection.vertx.server;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.sockjs.SockJSServer;
import org.vertx.java.deploy.Verticle;

public class EventBusTestServer extends Verticle {
	Logger logger;

	@Override
	public void start() throws Exception {
		logger = container.getLogger();

		HttpServer server = vertx.createHttpServer();

		// Register HTTP handler
		server.requestHandler(new Handler<HttpServerRequest>() {

			@Override
			public void handle(HttpServerRequest req) {
				String file = req.path.equals("/") ? "index.html" : req.path;
				req.response.sendFile("webroot/" + file);
			}

		});

		// Set security permission to let everything go through
		JsonArray permitted = new JsonArray();
		permitted.add(new JsonObject());

		// Create SockJS and bridge it to the Event Bus
		SockJSServer sockJSServer = vertx.createSockJSServer(server);
		sockJSServer.bridge(new JsonObject().putString("prefix", "/eventbus"),
				permitted, permitted);

		EventBus eb = vertx.eventBus();

		// Register Handler 1
		eb.registerLocalHandler("app.conduit",
				new Handler<Message<JsonObject>>() {

					@Override
					public void handle(Message<JsonObject> message) {
						logger.info("Handler 1 (Local) received: "
								+ message.body.toString());
					}

				});

		// Register Handler 2
		eb.registerHandler("app.conduit", new Handler<Message<JsonObject>>() {

			@Override
			public void handle(Message<JsonObject> message) {
				logger.info("Handler 2 (Shared) received: "
						+ message.body.toString());
			}

		});

		// Start the server
		server.listen(8080);
	}
}