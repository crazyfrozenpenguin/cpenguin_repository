package org.riaconnection.vertx.server;

import org.vertx.java.core.Handler;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.ServerWebSocket;
import org.vertx.java.core.streams.Pump;
import org.vertx.java.deploy.Verticle;

public class M2VertXServer extends Verticle {

	private static final int BUFF_SIZE = 32 * 1024;

	@Override
	public void start() throws Exception {

		HttpServer server = vertx.createHttpServer();

		// Register HTTP container
		server.requestHandler(new Handler<HttpServerRequest>() {

			public void handle(HttpServerRequest req) {
				String file = req.path.equals("/") ? "index.html" : req.path;
				req.response.sendFile("webroot/" + file);
			}

		});

		// Register WebSocket call
		server.setReceiveBufferSize(BUFF_SIZE).setSendBufferSize(BUFF_SIZE)
				.setAcceptBacklog(32000)
				.websocketHandler(new Handler<ServerWebSocket>() {

					public void handle(ServerWebSocket ws) {
						Pump.createPump(ws, ws, BUFF_SIZE).start();
					}

				});

		server.listen(8080, "localhost");
	}
}