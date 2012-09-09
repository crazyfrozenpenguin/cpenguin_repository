package org.crazypenguin.traits.data.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import javax.annotation.PostConstruct;
import javax.ejb.Stateless;
import javax.validation.constraints.NotNull;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.crazypenguin.traits.file.ExtensionFilter;
import org.json.JSONWriter;


@Stateless
public class PropertiesService {

	private static final Logger log = Logger.getLogger(PropertiesService.class);
	
	private static String ROOT_PATH;
	
	@PostConstruct
	public void init() {
		ROOT_PATH = System.getProperty("ConfigDir");
	}
	
	public File getRootPath() throws FileNotFoundException {
		File rootPath = new File(ROOT_PATH);
		
		if (!rootPath.exists() || !rootPath.isDirectory() || !rootPath.canRead() || !rootPath.canWrite()) {
			throw new FileNotFoundException("Properties Root Path '" + ROOT_PATH + "' does not exist, it is not a directory or it is not have read/write permissions for the application.");
		}
		
		return rootPath;
	}
	
	public StringWriter listFiles(String path) {
		StringWriter writer = new StringWriter();
		
		File rootPath = new File(ROOT_PATH);
		if (!rootPath.exists() || !rootPath.isDirectory()) return writer;

		if (path == null || path.isEmpty()) {
			for (File setDir : rootPath.listFiles()) {
				if (setDir.exists() && setDir.isDirectory()) {
					writer.append(setDir.getName() + "\n");
				}
			}
		} else {
			String[] parts = path.replaceFirst("^/", "").split("/");
			String set = parts[0];

			File setDir = new File(rootPath, set);
			
			if (setDir.exists() && setDir.isDirectory()) {
				if (parts.length == 1) {
					
					try {
						File[] list = setDir.listFiles(new ExtensionFilter(".properties"));

						List<String> prefOrder = readOrderFile(setDir);
						if (prefOrder == null || prefOrder.isEmpty()) {
							for (File file : list) {
								writer.append(file.getName() + "\n");
							}
						} else {
							List<String> modPrefOrder = new ArrayList<String>(prefOrder);
							boolean needsUpdate = false;
							Map<String,File> actualFiles = new HashMap<String,File>();
							for (File file : list) {
								actualFiles.put(file.getName(), file);
							}
							for (String fname: prefOrder) {
								File f = actualFiles.remove(fname);
								if (f != null) {
									writer.append(f.getName() + "\n");
								} else {
									needsUpdate = true;
									modPrefOrder.remove(fname);
								}
							}
							if (!actualFiles.isEmpty()) {
								needsUpdate = true;
								for (String fname : actualFiles.keySet()) {
									modPrefOrder.add(fname);
									writer.append(fname + "\n");
								}
							}
							if (needsUpdate) {
								writeOrderFile(setDir, modPrefOrder);
							}
						}
					} catch (Exception e) {
						// empty
					}
				} else {
					String order = StringUtils.join(ArrayUtils.subarray(parts, 1, parts.length), "/");
					Properties props = fetchProperties(set, order);
					props.list(new PrintWriter(writer));
				}
			}
		}
		return writer;
	}
	
	public void listFilesJSon(String path, @NotNull JSONWriter jw) {
		
		File rootPath = new File(ROOT_PATH);
		if (!rootPath.exists() || !rootPath.isDirectory()) return;

		try {
			
			if (path == null || path.isEmpty()) {
				// Return set names
				jw.array();
				for (File setDir : rootPath.listFiles()) {
					if (setDir.exists() && setDir.isDirectory()) {
						jw.object();
						jw.key("set");
						jw.value(setDir.getName());
						jw.endObject();
					}
				}
				jw.endArray();
			} else {
				String[] parts = path.replaceFirst("^/", "").split("/");
				String set = parts[0];

				File setDir = new File(rootPath, set);
				
				if (setDir.exists() && setDir.isDirectory()) {
					try {
						jw.object();
						jw.key("set");
						jw.value(set);
						
						if (parts.length == 1) {
							jw.key("files");
							jw.array();
							
							File[] list = setDir.listFiles(new ExtensionFilter(".properties"));
							
							List<String> prefOrder = readOrderFile(setDir);
							if (prefOrder == null || prefOrder.isEmpty()) {
								for (File file : list) {
									try {
										jw.object();
										jw.key("filename");
										jw.value(file.getName());
										jw.endObject();
									} catch (Exception e) {
										// empty
									}
								}
							} else {
								List<String> modPrefOrder = new ArrayList<String>(prefOrder);
								boolean needsUpdate = false;
								Map<String,File> actualFiles = new HashMap<String,File>();
								for (File file : list) {
									actualFiles.put(file.getName(), file);
								}
								for (String fname: prefOrder) {
									File f = actualFiles.remove(fname);
									if (f != null) {
										try {
											jw.object();
											jw.key("filename");
											jw.value(f.getName());
											jw.endObject();
										} catch (Exception e) {
											// empty
										}
									} else {
										needsUpdate = true;
										modPrefOrder.remove(fname);
									}
								}
								if (!actualFiles.isEmpty()) {
									needsUpdate = true;
									for (String fname : actualFiles.keySet()) {
										modPrefOrder.add(fname);
										try {
											jw.object();
											jw.key("filename");
											jw.value(fname);
											jw.endObject();
										} catch (Exception e) {
											// empty
										}
									}
								}
								if (needsUpdate) {
									writeOrderFile(setDir, modPrefOrder);
								}
							}							
							
							jw.endArray();
						} else {
							String order = StringUtils.join(ArrayUtils.subarray(parts, 1, parts.length), "/");
							Properties props = fetchProperties(set, order);
							jw.key("properties");
							jw.array();
							for (Entry<Object, Object> entry: props.entrySet()) {
								jw.object();
								jw.key("key");
								jw.value(entry.getKey());
								jw.key("value");
								jw.value(entry.getValue());
								jw.endObject();
							}
							jw.endArray();
						}
						
						jw.endObject();
					} catch (Exception e) {
						log.warn(e.getMessage());
					}
				}
			}
		} catch (Exception e) {
			log.warn(e.getMessage());
		}
	}
	
	public Properties fetchProperties(@NotNull String set, @NotNull String order) {
		
		File rootPath = new File(ROOT_PATH);
		
		Properties projectProperties = new Properties();
		
		FileInputStream stream = null;
		if (rootPath.exists() && rootPath.isDirectory()) {
			File setDir = new File(rootPath, set);
			if (setDir.exists() && setDir.isDirectory()) {
				
				String[] orderItems = null;
				
				if (order != null && !order.isEmpty()) {
					orderItems = order.replaceFirst("^/", "").split("/");
				}
				
				if (orderItems != null && orderItems.length > 0) {
					File file = null; 
					for (String name : orderItems) {
						try {
							file = new File(setDir, name + ".properties");
							if (file.exists() && file.isFile()) {
								stream = new FileInputStream( file );
								projectProperties.load(stream);
							}
						} catch (Exception e) {
							log.warn(e.getMessage());
						}
					}
				} else {
					for (File file : setDir.listFiles(new ExtensionFilter(".properties"))) {
						try {
							stream = new FileInputStream( file );
							projectProperties.load(stream);
						} catch (Exception e) {
							log.warn(e.getMessage());
						}
					}
				}
			}
		}
		
		return projectProperties;
	}
	
	public List<String> readOrderFile(@NotNull File setDir) throws IOException {
		List<String> order = null;
		
		File orderFile = new File(setDir, ".order");
		if (orderFile.exists() || orderFile.isFile()) {
			return FileUtils.readLines(orderFile);
		}
		
		return order;
	}
	
	public void writeOrderFile(@NotNull File setDir, @NotNull List<String> orderList) throws IOException {
		File orderFile = new File(setDir, ".order");
		if (orderList.isEmpty()) {
			orderFile.delete();
		} else {
			FileUtils.writeLines(orderFile, orderList);
		}
	}
}
