package org.riaconnection.vertx.server;

import java.util.Iterator;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.logging.Logger;
import org.vertx.java.core.sockjs.SockJSServer;
import org.vertx.java.deploy.Verticle;

public class VToonsServer extends Verticle {

	private Logger log;

	@Override
	public void start() throws Exception {

		log = container.getLogger();

		JsonArray permitted = new JsonArray();
		permitted.add(new JsonObject());

		String configString = loadStaticData("permissions.json");

		JsonObject config = new JsonObject(configString);

		container.deployModule("vertx.mongo-persistor-v1.0", config, 1,
				new Handler<String>() {

					@Override
					public void handle(String info) {

						String pa = "vertx.mongopersistor";

						EventBus eb = vertx.eventBus();

						String albums = loadStaticData("albums.json");

						JsonObject albumDelete = new JsonObject();
						albumDelete.putString("action", "delete");
						albumDelete.putString("collection", "albums");
						albumDelete.putObject("matcher", new JsonObject("{}"));

						eb.send(pa, albumDelete);

						JsonObject usersDelete = new JsonObject();
						usersDelete.putString("action", "delete");
						usersDelete.putString("collection", "users");
						usersDelete.putObject("matcher", new JsonObject("{}"));

						eb.send(pa, usersDelete);

						JsonObject ordersDelete = new JsonObject();
						ordersDelete.putString("action", "delete");
						ordersDelete.putString("collection", "orders");
						ordersDelete.putObject("matcher", new JsonObject("{}"));

						eb.send(pa, ordersDelete);

						JsonArray albumsList = new JsonArray(albums);

						Iterator<Object> i = albumsList.iterator();

						while (i.hasNext()) {
							JsonObject album = (JsonObject) i.next();
							JsonObject entry = new JsonObject();
							entry.putString("action", "save");
							entry.putString("collection", "albums");
							entry.putObject("document", album);
							eb.send(pa, entry);
						}

						// And a user
						JsonObject userDetails = new JsonObject();
						userDetails.putString("firstname", "Tim");
						userDetails.putString("lastname", "Fox");
						userDetails.putString("email", "tim.fox@localhost");
						userDetails.putString("username", "tim");
						userDetails.putString("password", "password");

						JsonObject user = new JsonObject();
						user.putString("action", "save");
						user.putString("collection", "users");
						user.putObject("document", userDetails);

						eb.send(pa, user);
					}

				});

		container.deployModule("vertx.auth-mgr-v1.0");

		HttpServer server = vertx.createHttpServer();

		// Register HTTP container
		server.requestHandler(new Handler<HttpServerRequest>() {

			@Override
			public void handle(HttpServerRequest req) {
				String file = req.path.equals("/") ? "index.html" : req.path;
				req.response.sendFile("webroot/" + file);
			}

		});

		SockJSServer sockServer = vertx.createSockJSServer(server);
		sockServer.bridge(new JsonObject().putString("prefix", "/eventbus"),
				permitted, permitted);

		server.listen(8080, "localhost");
	}

	private String loadStaticData(String filename) {
		if (filename == null || filename.isEmpty()) {
			return null;
		}

		Buffer buffer = null;
		String data = null;

		try {
			buffer = vertx.fileSystem()
					.readFileSync("webroot/data/" + filename);
			data = buffer.getString(0, buffer.length());
		} catch (Exception e) {
			log.error(e.getMessage());
		}

		return data;
	}
}