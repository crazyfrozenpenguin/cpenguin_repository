package crazypenguin.rest;

import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.cpr.BroadcasterFactory;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;

@Path("/control")
@Stateless
@LocalBean //Note: @javax.ejb.Local(WindowingResource.class) does not work on AS 7!
public class AtmosphereTestBean implements AtmosphereTest {
	
	private boolean m_visible = false;
	
	@POST
	@Path("/toggle")
	@Override
	public Response toggle(String xml) {
		m_visible = !m_visible;
		notifySubscribers(m_visible ? "ON" : "OFF");
		return Response.ok().build();
	}
	
	private void notifySubscribers(String state) {
		Broadcaster broadcaster = BroadcasterFactory.getDefault().lookup("control", true);
		if (broadcaster != null) broadcaster.broadcast(state);
	}
}