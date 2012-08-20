package crazypenguin.servlets;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.atmosphere.cpr.ApplicationConfig;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;
import org.atmosphere.cpr.HeaderConfig;
import org.atmosphere.handler.AbstractReflectorAtmosphereHandler;
import org.atmosphere.websocket.WebSocketEventListenerAdapter;

public class PubSubAtmosphereHandler extends AbstractReflectorAtmosphereHandler {

	@Override
	public void onRequest(AtmosphereResource event) throws IOException {
	
	  HttpServletRequest req = event.getRequest();
	  HttpServletResponse res = event.getResponse();
	  String method = req.getMethod();
	
	  // Suspend the response.
	  if ("GET".equalsIgnoreCase(method)) {
		  
		  // Log all events on the console, including WebSocket events.
          event.addEventListener(new WebSocketEventListenerAdapter());
          
	      res.setContentType("text/html;charset=ISO-8859-1");
	      
	      Broadcaster b = lookupBroadcaster(req.getPathInfo());
	      event.setBroadcaster(b);
	
	      String atmoTransport = req.getHeader(HeaderConfig.X_ATMOSPHERE_TRANSPORT);
	      
	      if (atmoTransport != null && !atmoTransport.isEmpty() && 
	    		  atmoTransport.equalsIgnoreCase(HeaderConfig.LONG_POLLING_TRANSPORT)) {
	    	  
	          req.setAttribute(ApplicationConfig.RESUME_ON_BROADCAST, Boolean.TRUE);
	          event.suspend(-1, false);
	          
	      } else {
	          event.suspend(-1);
	      }
	  } else if ("POST".equalsIgnoreCase(method)) {
	      Broadcaster b = lookupBroadcaster(req.getPathInfo());
	
	      String message = req.getReader().readLine();
	
	      if (message != null && message.indexOf("message") != -1) {
	          b.broadcast(message.substring("message=".length()));
	      }
	  }
	}
	
	@Override
	public void destroy() {
		// empty
	}
	
	Broadcaster lookupBroadcaster(String pathInfo) {
		String[] decodedPath = pathInfo.split("/");
		Broadcaster b = BroadcasterFactory.getDefault().lookup(decodedPath[decodedPath.length - 1], true);
		return b;
	}

}
