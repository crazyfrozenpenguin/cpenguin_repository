package org.crazypenguin.traits.rest;

import javax.ws.rs.core.Response;

public interface IPropertiesProvider {

	public Response update(String type, String set, String propfile, String data);
	
    public Response delete(String set, String propfile);

    public Response create(String set, String propfile);
	
    public Response updateOrder(String set, String path);

	public Response list(String type, String path);
	
	public Response download(String type, String path);
}