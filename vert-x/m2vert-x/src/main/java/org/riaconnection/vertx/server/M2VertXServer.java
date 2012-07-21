package org.riaconnection.vertx.server;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.http.HttpServer;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.http.ServerWebSocket;
import org.vertx.java.deploy.Verticle;

public class M2VertXServer extends Verticle {

	private static final int BUFF_SIZE = 32 * 1024;

	@Override
	public void start() throws Exception {

		HttpServer server = vertx.createHttpServer();

		// Register HTTP container
		server.requestHandler(new Handler<HttpServerRequest>() {

			@Override
			public void handle(HttpServerRequest req) {
				String file = req.path.equals("/") ? "index.html" : req.path;
				req.response.sendFile("webroot/" + file);
			}

		});

		// Register WebSocket call
		server.setReceiveBufferSize(BUFF_SIZE).setSendBufferSize(BUFF_SIZE)
				.setAcceptBacklog(32000)
				.websocketHandler(new Handler<ServerWebSocket>() {

					@Override
					public void handle(final ServerWebSocket ws) {
						if (ws.path.equals("/myapp")) {
							ws.dataHandler(new Handler<Buffer>() {
								@Override
								public void handle(Buffer data) {
									// Echo it back
									ws.writeTextFrame(data.toString());
								}
							});
						} else {
							ws.reject();
						}
					}

				});

		server.listen(8080, "localhost");
	}
}