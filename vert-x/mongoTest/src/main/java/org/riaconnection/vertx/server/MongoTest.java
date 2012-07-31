package org.riaconnection.vertx.server;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.deploy.Verticle;

public class MongoTest extends Verticle {

	private Logger logger;

	private static String pa = "vertx.mongopersistor";
	private static String appNotify = "app.notify";
	private static String DB_LOADED = "db.loaded";
	private EventBus eb;

	@Override
	public void start() throws Exception {

		logger = container.getLogger();

		eb = vertx.eventBus();

		container.deployModule("vertx.mongo-persistor-v1.0", null, 1,
				new Handler<String>() {

					@Override
					public void handle(String info) {

						JsonObject delete = new JsonObject();
						delete.putString("action", "delete");
						delete.putString("collection", "teams");
						delete.putObject("matcher", new JsonObject("{}"));

						eb.send(pa, delete);

						String[] teams = { "Benfica", "Sporting", "Porto" };

						JsonObject team = new JsonObject();
						JsonObject entry = new JsonObject();
						entry.putString("action", "save");
						entry.putString("collection", "teams");

						for (String name : teams) {
							team.putString("name", name);
							entry.putObject("document", team);
							eb.send(pa, entry);
						}

						eb.send(appNotify, DB_LOADED);
					}
				});

		eb.registerHandler(appNotify, new Handler<Message<String>>() {

			@Override
			public void handle(Message<String> message) {

				logger.info("Notification message received: "
						+ message.body.toString());

				if (message.body.equals(DB_LOADED)) {
					JsonObject find = new JsonObject();
					find.putString("action", "find");
					find.putString("collection", "teams");
					find.putObject("matcher", new JsonObject());

					eb.send(pa, find, new Handler<Message<JsonObject>>() {

						@Override
						public void handle(Message<JsonObject> req) {

							JsonArray entries = req.body.getArray("results");
							for (Object obj : entries) {
								JsonObject entry = (JsonObject) obj;
								logger.info("name: " + entry.getString("name")
										+ ", id: " + entry.getString("_id"));
							}
						}
					});
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
