package org.crazypenguin.traits.rest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.crazypenguin.traits.data.service.PropertiesService;
import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONWriter;


@Path("/properties")
@Stateless
@LocalBean
public class PropertiesProvider implements IPropertiesProvider {

	private static final Logger log = Logger.getLogger(PropertiesProvider.class);
	
	@Inject
	private PropertiesService propertiesService;
	
	@POST
	@Path("{type}/{set}/{file}")
//	@Consumes(MediaType.APPLICATION_JSON)
	@Override
	public Response update(@PathParam("type") String type, @PathParam("set") String set, @PathParam("file") String propfile, String data) {
		
		// TODO: Instead of accepting a String, just use JAX-RS powers to converti the received JSON data into a java list or object
		
		log.info(data);
		
		if (!type.equals("json") || set.isEmpty() || propfile.isEmpty()) return Response.notModified().build();
		
		try {
			File file = new File(propertiesService.getRootPath() + "/" + set, propfile + ".properties");
			if (!file.exists() || !file.isFile() || !file.canWrite()) return Response.notModified().build();
		
			JSONArray properties = new JSONArray(data);
			
			JSONObject entry;
			Properties props = new Properties();
			for (int index = 0; index < properties.length(); index++) {
				entry = properties.getJSONObject(index);
				props.put(entry.get("key"), entry.get("value"));
				if (index == 20) break; // force max props
			}
			
			props.store(new FileOutputStream(file), null);
			
		} catch (Exception e) {
			log.error(e.getMessage());
			return Response.notModified().build();
		}
		
		return Response.ok().build();
	}
	
	@POST
	@Path("delete/{set}{file:.*}")
	@Override
	public Response delete(@PathParam("set") String set, @PathParam("file") String propfile) {
		
		if (set.isEmpty()) return Response.notModified().build();
		
		try {
            File setDir = new File(propertiesService.getRootPath(), set);
            
            if (setDir.exists() && setDir.isDirectory() && setDir.canWrite()) {
                if (propfile.isEmpty()) {
                    FileUtils.deleteDirectory(setDir);
                } else {
                    File file = new File(setDir, propfile + ".properties");
        			if (file.exists() && file.isFile() && file.canWrite()) {
        				file.delete();
        				return Response.ok().build();
        			}
    			}
            }
		} catch (Exception e) {
			log.error(e.getMessage());
		}
		
		return Response.notModified().build();
	}
	
	@POST
    @Path("create/{set}{file:.*}")
    @Override
    public Response create(@PathParam("set") String set, @PathParam("file") String propfile) {
        
        if (set.isEmpty()) return Response.notModified().build();
        
        try {
            File setDir = new File(propertiesService.getRootPath(), set);
            
            // Force max of 5 sets or 5 properties files
            if (!setDir.exists() && propertiesService.getRootPath().list().length < 6) {
                setDir.mkdir();
                return Response.ok().build();
            } else if (!propfile.isEmpty() && propfile.endsWith(".properties") && setDir.exists() && setDir.list().length < 6) {
                String[] parts = propfile.replaceFirst("^/", "").split("/");
                if (parts.length == 1) {
                    File file = new File(setDir, propfile);
                    if (!file.exists()) {
                        file.createNewFile();
                        return Response.ok().build();
                    }
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        
        return Response.notModified().build();
    }
	
    @POST
    @Path("/order/{set}/{path:.*}")
    @Override
    public Response updateOrder(@PathParam("set") String set, @PathParam("path") String path) {

        try {
            File rootPath = propertiesService.getRootPath();
            
            if (rootPath.exists() && rootPath.isDirectory() && !set.isEmpty()) {

                if (!path.isEmpty()) {
                    String[] orderList = path.replaceFirst("^/", "").split("/");
                    for (int i = 0; i < orderList.length; i++) {
                        orderList[i] = orderList[i] + ".properties";
                    }
                    File setDir = new File(rootPath, set);
                    propertiesService.writeOrderFile(setDir, Arrays.asList(orderList));
                }
            }
        } catch (Exception e) {
            return Response.serverError().build();
        }
        
        return Response.ok().build();
    }
    
    @GET
	@Path("{type}{path:.*}")
	@Produces(MediaType.TEXT_PLAIN)
	@Override
	public Response list(@PathParam("type") String type, @PathParam("path") String path) {
		
		try {
			File rootPath = propertiesService.getRootPath();
			
			if (rootPath.exists() && rootPath.isDirectory()) {
				StringWriter writer = new StringWriter();
				
				try {
					JSONWriter jw = null;
					if (type.equals("json")) {
						jw = new JSONWriter(writer);				
						propertiesService.listFilesJSon(path, jw);
					} else if (type.equals("plain")) {
						writer = propertiesService.listFiles(path);
					}
				} catch (Exception e) {
					log.warn(e.getMessage());
				}
				
				if (writer.getBuffer().length() > 0) {
					return Response.ok(writer.getBuffer().toString()).build();
				}
			}
		} catch (Exception e) {
			Response.serverError().build();
		}
		
		return Response.noContent().build();
	}

	@GET
	@Path("/download/{set}{path:.*}")
	@Produces("application/properties")
	@Override
	public Response download(@PathParam("set") String set, @PathParam("path") String path) {
		
		try {
			File rootPath = propertiesService.getRootPath();
			
			if (rootPath.exists() && rootPath.isDirectory() && !set.isEmpty()) {
				StringWriter writer = new StringWriter();
				
				try {
				    if (path.isEmpty()) {
				        List<String> orderedFiles = propertiesService.readOrderFile(new File(rootPath, set));
				        path = "/" + StringUtils.join(orderedFiles, "/").replaceAll("\\.properties", "");
				    }
					writer = propertiesService.listFiles("/" + set + path);
				} catch (Exception e) {
					log.warn(e.getMessage());
				}
				
				if (writer.getBuffer().length() > 0) {
					String filename = set;
					if (!path.isEmpty()) {
						for (String name : path.replaceFirst("^/", "").split("/")) {
							filename += "-" + name;
						}
					}
					ResponseBuilder response = Response.ok(writer.getBuffer().toString());
					response.header("Content-Disposition", "attachment; filename=\"" + filename + ".properties\"");
					return response.build();
				}
			}
		} catch (Exception e) {
			return Response.serverError().build();
		}
		
		return Response.noContent().build();
	}
	
}
