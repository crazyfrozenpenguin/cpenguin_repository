package org.riaconnection.vertx.server;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.deploy.Verticle;

public class MongoTest extends Verticle {

	@Override
	public void start() throws Exception {

		container.deployModule("vertx.mongo-persistor-v1.0", null, 1,
				new Handler<String>() {

					@Override
					public void handle(String info) {

						String[] teams = { "Benfica", "Sporting", "Porto" };

						String pa = "vertx.mongopersistor";

						EventBus eb = vertx.eventBus();

						JsonObject team = new JsonObject();
						JsonObject entry = new JsonObject();
						entry.putString("action", "save");
						entry.putString("collection", "teams");

						for (String name : teams) {
							team.putString("name", name);
							entry.putObject("document", team);
							eb.send(pa, entry);
						}
					}
				});

		// Create HTTP server
		HttpServer server = vertx.createHttpServer();

		// Register HTTP container
		server.requestHandler(new Handler<HttpServerRequest>() {

			@Override
			public void handle(HttpServerRequest req) {
				String file = req.path.equals("/") ? "index.html" : req.path;
				req.response.sendFile("webroot/" + file);
			}

		});

		server.listen(8080, "localhost");
	}
}
