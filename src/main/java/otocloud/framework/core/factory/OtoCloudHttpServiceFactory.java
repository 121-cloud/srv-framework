package otocloud.framework.core.factory;

import io.vertx.core.AsyncResult;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.file.AsyncFile;
import io.vertx.core.file.OpenOptions;
import io.vertx.core.http.HttpClient;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpClientRequest;
import io.vertx.core.json.JsonObject;
import io.vertx.core.spi.VerticleFactory;
import io.vertx.ext.httpservicefactory.PGPHelper;
import io.vertx.ext.httpservicefactory.ValidationPolicy;
import io.vertx.maven.MavenCoords;

import org.bouncycastle.openpgp.PGPPublicKey;
import org.bouncycastle.openpgp.PGPSignature;








import otocloud.common.OtoCloudDirectoryHelper;
import otocloud.framework.core.OtoCloudServiceImpl;

import java.io.File;
import java.io.FileInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiFunction;
import java.util.jar.Attributes;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

/**
 * TODO: DOCUMENT ME!
 * @date 2015年9月27日
 * @author lijing@yonyou.com
 */
public class OtoCloudHttpServiceFactory extends OtoCloudServiceFactory {
  public static final String REPO_ADDRESS_BASE = "address_base";
  
  public static final String CACHE_DIR_PROPERTY = "vertx.httpServiceFactory.cacheDir";
  public static final String CACHE_DIR = OtoCloudDirectoryHelper.getLibDirectory();
  public static final String HTTP_CLIENT_OPTIONS_PROPERTY = "vertx.httpServiceFactory.httpClientOptions";
  public static final String HTTPS_CLIENT_OPTIONS_PROPERTY = "vertx.httpServiceFactory.httpsClientOptions";
  public static final String AUTH_USERNAME_PROPERTY = "vertx.httpServiceFactory.authUsername";
  public static final String AUTH_PASSWORD_PROPERTY = "vertx.httpServiceFactory.authPassword";
  public static final String PROXY_HOST_PROPERTY = "vertx.httpServiceFactory.proxyHost";
  public static final String PROXY_PORT_PROPERTY = "vertx.httpServiceFactory.proxyPort";
  public static final String KEYSERVER_URI_TEMPLATE = "vertx.httpServiceFactory.keyserverURITemplate";
  public static final String VALIDATION_POLICY = "vertx.httpServiceFactory.validationPolicy";

  //private static final String FILE_SEP = System.getProperty("file.separator");
  //private static final String FILE_CACHE_DIR = ".vertx" + FILE_SEP + "vertx-http-service-factory";

  //private Vertx vertx;
  
  private String repoAddressBase;
  private File cacheDir;
  private String username;
  private String password;
  private String proxyHost;
  private int proxyPort;
  private String keyserverURITemplate;
  private ValidationPolicy validationPolicy;
  private HttpClientOptions options;
  
  //private List<String> dependencies;

  @Override
  public void init(Vertx vertx) {
	  super.init(vertx);
	  
	repoAddressBase = System.getProperty(REPO_ADDRESS_BASE, "");
	
    cacheDir = new File(System.getProperty(CACHE_DIR_PROPERTY, CACHE_DIR));
    options = configOptions();
    validationPolicy = ValidationPolicy.valueOf(System.getProperty(VALIDATION_POLICY, ValidationPolicy.VERIFY.toString()).toUpperCase());
    username = System.getProperty(AUTH_USERNAME_PROPERTY);
    password = System.getProperty(AUTH_PASSWORD_PROPERTY);
    proxyHost = System.getProperty(PROXY_HOST_PROPERTY);
    proxyPort = Integer.parseInt(System.getProperty(PROXY_PORT_PROPERTY, "-1"));
    keyserverURITemplate = System.getProperty(KEYSERVER_URI_TEMPLATE, "http://pool.sks-keyservers.net:11371/pks/lookup?op=get&options=mr&search=0x%016X");
    //this.vertx = vertx;
  }

  protected HttpClientOptions createHttpClientOptions(String scheme) {
	    if ("https".equals(scheme)) {
	      String optionsJson = System.getProperty(HTTPS_CLIENT_OPTIONS_PROPERTY);
	      HttpClientOptions options;
	      if (optionsJson != null) {
	        options = new HttpClientOptions(new JsonObject(optionsJson));
	      } else {
	        options = createHttpClientOptions("http").setTrustAll(true);
	      }
	      options.setSsl(true);
	      return options;
	    } else {
	      String optionsJson = System.getProperty(HTTP_CLIENT_OPTIONS_PROPERTY);
	      return optionsJson != null ? new HttpClientOptions(new JsonObject(optionsJson)) : new HttpClientOptions();
	    }
  }


  protected HttpClientOptions configOptions() {
    return createHttpClientOptions(internalPrefix());
  }

  @Override
  public String prefix() {
    return "otocloud_http";
  }
  
  public String internalPrefix() {
	  String preFixStr = prefix();
	  if(preFixStr.equals("otocloud_http"))
	    return "http";
	  return "https";
  }

  @Override
  //otocloud:otocloud-acct-rel:1.0.0-SNAPSHOT
  public void resolve(String identifier, DeploymentOptions deploymentOptions, ClassLoader classLoader, Future<String> resolution) {
    int pos = identifier.lastIndexOf("::");
    String serviceName;
    String fileName;
    String stringURL;
    if (pos != -1) {
    	String coordsString = VerticleFactory.removePrefix(identifier.substring(0, pos));
    	MavenCoords coords = new MavenCoords(coordsString);
    	fileName = coords.serviceName() + "-" + coords.version() + ".jar";
    	serviceName = identifier.substring(pos + 2);
    } else {
    	String coordsString = VerticleFactory.removePrefix(identifier);
    	MavenCoords coords = new MavenCoords(coordsString);
    	fileName = coords.serviceName() + "-" + coords.version() + ".jar";
    	serviceName = null;
    }
	stringURL = repoAddressBase + fileName;
    
/*	JsonObject serviceCfg  = deploymentOptions.getConfig();
	JsonArray dependencies = serviceCfg.getJsonObject("deployment").getJsonArray("classpath", null);*/
	

    URI url;
    URI signatureURL;
    //String deploymentKey;
    String signatureKey;
    try {
      url = new URI(stringURL);
      signatureURL = new URI(url.getScheme(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath() + ".asc", url.getQuery(), url.getFragment());
      //deploymentKey = URLEncoder.encode(url.toString(), "UTF-8");
      signatureKey = URLEncoder.encode(signatureURL.toString(), "UTF-8");
    } catch (Exception e) {
      resolution.fail(e);
      return;
    }
    File deploymentFile = new File(cacheDir, fileName);
    File signatureFile = new File(cacheDir, signatureKey);

    //
    HttpClient client = vertx.createHttpClient(options);
    doRequest(client, deploymentFile, url, signatureFile, signatureURL, ar -> {
      if (ar.succeeded()) {
    	  final List<String> dependencies = new ArrayList<String>();
    	  try{
	          JarFile jarFile = new JarFile(ar.result().deployment);
	          Manifest manifest = jarFile.getManifest();
	          if (manifest != null) {
	            String classPaths = (String)manifest.getMainAttributes().get(new Attributes.Name("Class-Path"));
	            
	            if(classPaths != null && !classPaths.isEmpty()){
	            	String[] classPathArray = classPaths.split(" ");
	            	for(String classPath: classPathArray){
	            		dependencies.add(classPath);
	            	}
	            }
	          }
	          jarFile.close();
    	  }catch(Exception e){
    		  e.printStackTrace();
    	  }    	  
    	  
        if (ar.result().signature != null) {
          PGPSignature signature;
          URI publicKeyURI;
          File publicKeyFile;
          try {
            signature = PGPHelper.getSignature(Files.readAllBytes(ar.result().signature.toPath()));
            String uri = String.format(keyserverURITemplate, signature.getKeyID());
            publicKeyURI = new URI(uri);
            publicKeyFile = new File(cacheDir, URLEncoder.encode(publicKeyURI.toString(), "UTF-8"));
          } catch (Exception e) {
            client.close();
            resolution.fail(e);
            return;
          }
          HttpClient keyserverClient;
          if (!publicKeyURI.getScheme().equals(internalPrefix())) {
            //client.close();
            keyserverClient = vertx.createHttpClient(createHttpClientOptions(publicKeyURI.getScheme()));
          } else {
            keyserverClient = client;
          }

          BiFunction<String, Buffer, Buffer> unmarshallerFactory = (mediaType, buf) -> {
            switch (mediaType) {
              case "application/json":
                JsonObject json = new JsonObject(buf.toString());
                return Buffer.buffer(json.getJsonArray("keys").getJsonObject(0).getString("bundle"));
              case "application/pgp-keys":
              default:
                return buf;
            }
          };

          doRequest(keyserverClient, publicKeyFile, publicKeyURI, null, null, false, unmarshallerFactory, new HashSet<>(), ar2 -> {
            if (ar2.succeeded()) {
              try {
                long keyID = signature.getKeyID();
                File file = ar2.result();
                Path path = file.toPath();
                PGPPublicKey publicKey = PGPHelper.getPublicKey(Files.readAllBytes(path), keyID);
                if (publicKey != null) {
                  FileInputStream f = new FileInputStream(ar.result().deployment);
                  boolean verified = PGPHelper.verifySignature(f, new FileInputStream(ar.result().signature), publicKey);
                  if (verified) {
                	  if(dependencies != null && dependencies.size() > 0){
                		  AtomicInteger downloadCompletedCount = new AtomicInteger(0);
                		  int size = dependencies.size();
                		  for (String dependency : dependencies) {
                			    Future<Boolean> downloadFuture = Future.future();  
                			    downLoadDependencies(client, dependency, downloadFuture);
                			    downloadFuture.setHandler(depRet -> {
	
                		    		if(depRet.failed()){
    	                        		if (downloadCompletedCount.incrementAndGet() >= size) {  
    	                        			if(keyserverClient != client)
    	                        				keyserverClient.close();    	                        			
    	                        			client.close();    	                        			
    	                        			resolution.fail(depRet.cause());
    	                                    return;    	                        			
    	                                }            		

                		    		}else{		    			
	   	                        		if (downloadCompletedCount.incrementAndGet() >= size) {	    	                        			
    	                        			if(keyserverClient != client)
    	                        				keyserverClient.close();    	   
    	                        			client.close();
	   	                        			deploy(dependencies, deploymentFile, identifier, serviceName, deploymentOptions, classLoader, resolution);
    	                                    return;	    	                        			
	    	                             }
                		    		}
                				});	               			    

	            			}
                	  }else{ 
              			if(keyserverClient != client)
            				keyserverClient.close();    	   
                		client.close();
	                    deploy(dependencies, deploymentFile, identifier, serviceName, deploymentOptions, classLoader, resolution);
	                    return;
                	  }
                  }
                }
    			if(keyserverClient != client)
    				keyserverClient.close();    	   
    			client.close();
                resolution.fail(new Exception("Signature verification failed"));
              } catch (Exception e) {
      			if(keyserverClient != client)
    				keyserverClient.close();    	   
            	  client.close();
            	  resolution.fail(e);
              } finally {
                //keyserverClient.close();
              }
            } else {
    			if(keyserverClient != client)
    				keyserverClient.close();    	   
            	client.close();
            	resolution.fail(ar2.cause());
            }
          });
        } else {
          //client.close();
      	  if(dependencies != null && dependencies.size() > 0){
    		  AtomicInteger downloadCompletedCount = new AtomicInteger(0);
    		  int size = dependencies.size();
    		  for (String dependency : dependencies) {
    			    Future<Boolean> downloadFuture = Future.future();  
    			    downLoadDependencies(client, dependency, downloadFuture);
    			    downloadFuture.setHandler(depRet -> {

    		    		if(depRet.failed()){
                    		if (downloadCompletedCount.incrementAndGet() >= size) {    
                    			client.close();
                    			resolution.fail(depRet.cause());
                                return;    	                        			
                            }            		

    		    		}else{		    			
                       		if (downloadCompletedCount.incrementAndGet() >= size) {	    	                        			
                       			client.close();
                       			deploy(dependencies, deploymentFile, identifier, serviceName, deploymentOptions, classLoader, resolution);
                                return;	    	                        			
                             }
    		    		}
    				});	               			    

    			}
      	  }else {
      		client.close();
      		deploy(dependencies, deploymentFile, identifier, serviceName, deploymentOptions, classLoader, resolution);          
      	  }
        }
      } else {
    	  //client.close();
          resolution.fail(ar.cause());
      }
    });
  }
  
  
  private void downLoadDependencies(HttpClient client, String dependencyFile, Future<Boolean> resolution) {
	  
	  	String stringURL = repoAddressBase + dependencyFile;

	    URI url;
	    URI signatureURL;
	    //String deploymentKey;
	    String signatureKey;
	    try {
	      url = new URI(stringURL);
	      signatureURL = new URI(url.getScheme(), url.getUserInfo(), url.getHost(), url.getPort(), url.getPath() + ".asc", url.getQuery(), url.getFragment());
	      //deploymentKey = URLEncoder.encode(url.toString(), "UTF-8");
	      signatureKey = URLEncoder.encode(signatureURL.toString(), "UTF-8");
	    } catch (Exception e) {
	      resolution.fail(e);
	      return;
	    }
	    File deploymentFile = new File(cacheDir, dependencyFile);
	    File signatureFile = new File(cacheDir, signatureKey);

	    //
	    //HttpClient client = vertx.createHttpClient(options);
	    doRequest(client, deploymentFile, url, signatureFile, signatureURL, ar -> {
	      if (ar.succeeded()) {  	  
	        if (ar.result().signature != null) {
	          PGPSignature signature;
	          URI publicKeyURI;
	          File publicKeyFile;
	          try {
	            signature = PGPHelper.getSignature(Files.readAllBytes(ar.result().signature.toPath()));
	            String uri = String.format(keyserverURITemplate, signature.getKeyID());
	            publicKeyURI = new URI(uri);
	            publicKeyFile = new File(cacheDir, URLEncoder.encode(publicKeyURI.toString(), "UTF-8"));
	          } catch (Exception e) {
	            //client.close();
	            resolution.fail(e);
	            return;
	          }
	          HttpClient keyserverClient;
	          if (!publicKeyURI.getScheme().equals(internalPrefix())) {
	            //client.close();
	            keyserverClient = vertx.createHttpClient(createHttpClientOptions(publicKeyURI.getScheme()));
	          } else {
	            keyserverClient = client;
	          }

	          BiFunction<String, Buffer, Buffer> unmarshallerFactory = (mediaType, buf) -> {
	            switch (mediaType) {
	              case "application/json":
	                JsonObject json = new JsonObject(buf.toString());
	                return Buffer.buffer(json.getJsonArray("keys").getJsonObject(0).getString("bundle"));
	              case "application/pgp-keys":
	              default:
	                return buf;
	            }
	          };

	          doRequest(keyserverClient, publicKeyFile, publicKeyURI, null, null, false, unmarshallerFactory, new HashSet<>(), ar2 -> {
	            if (ar2.succeeded()) {
	              try {
	                long keyID = signature.getKeyID();
	                File file = ar2.result();
	                Path path = file.toPath();
	                PGPPublicKey publicKey = PGPHelper.getPublicKey(Files.readAllBytes(path), keyID);
	                if (publicKey != null) {
	                  FileInputStream f = new FileInputStream(ar.result().deployment);
	                  boolean verified = PGPHelper.verifySignature(f, new FileInputStream(ar.result().signature), publicKey);
	                  if (verified) {       	  
	                	  
	                    //deploy(deploymentFile, identifier, serviceName, deploymentOptions, classLoader, resolution);
	                	  resolution.complete(true);
	                	  return;
	                  }else {
	                	  //keyserverClient.close();
	                  }
	                }
	                resolution.fail(new Exception("Signature verification failed"));
	              } catch (Exception e) {
	            	  //keyserverClient.close();
	                  resolution.fail(e);
	              } finally {
	            	  if(keyserverClient != client)
	            		  keyserverClient.close();
	              }
	            } else {
	            	  if(keyserverClient != client)
	            		  keyserverClient.close();
	              
	            	  resolution.fail(ar2.cause());
	            }
	          });
	        } else {
	          //client.close();
	          //deploy(deploymentFile, identifier, serviceName, deploymentOptions, classLoader, resolution);
	        	resolution.complete(true);
	        }
	      } else {
	        resolution.fail(ar.cause());
	      }
	    });
	  }
  

  /**
   * The {@code unmarshallerFactory} argument is a function that returns an {@code Function<Buffer, Buffer>} unmarshaller
   * function for a given media type value. The returned function unmarshaller function will be called with the buffers
   * to unmarshall and finally with a null buffer to signal the end of the unmarshalled data. It can return a buffer
   * or a null value.
   *
   * @param client the http client
   * @param file the file where to save the content
   * @param url the resource url
   * @param username the optional username used for basic auth
   * @param password the optional password used for basic auth
   * @param doAuth whether to perform authentication or not
   * @param unmarshaller the unmarshaller
   * @param history the previous urls for detecting redirection loops
   * @param handler the result handler
   */
  private void doRequest(
      HttpClient client,
      File file,
      URI url,
      String username,
      String password,
      boolean doAuth,
      BiFunction<String, Buffer, Buffer> unmarshaller,
      Set<URI> history,
      Handler<AsyncResult<File>> handler) {
    if (file.exists() && file.isFile()) {
      handler.handle(Future.succeededFuture(file));
      return;
    }
    String requestURI = url.getPath();
    if (url.getQuery() != null) {
      requestURI += "?" + url.getQuery();
    }
    int port = url.getPort();
    if (port == -1) {
      if ("http".equals(url.getScheme())) {
        port = 80;
      } else {
        port = 443;
      }
    }
    HttpClientRequest req;
    if (proxyHost == null) {
      req = client.get(port, url.getHost(), requestURI);
    } else {
      req = client.get(proxyPort, proxyHost, url.toString());
      req.putHeader("host", url.getHost());
    }
    if (doAuth && username != null && password != null) {
      req.putHeader("Authorization", "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes()));
    }
    req.putHeader("user-agent", "Vert.x Http Service Factory");
    req.exceptionHandler(err -> {
      handler.handle(Future.failedFuture(err));
    });
    req.handler(resp -> {
      int status = resp.statusCode();
      switch (resp.statusCode()) {
        case 200: {
          String contentType = resp.getHeader("Content-Type");
          int index = contentType.indexOf(";");
          String mediaType = index > -1 ? contentType.substring(0, index) : contentType;
          AtomicBoolean done = new AtomicBoolean();
          resp.exceptionHandler(err -> {
            if (done.compareAndSet(false, true)) {
              handler.handle(Future.failedFuture(err));
            }
          });
          resp.bodyHandler(body -> {
            if (!done.compareAndSet(false, true)) {
              return;
            }
            File parentFile = file.getParentFile();
            if (!parentFile.exists()) {
              parentFile.mkdirs(); // Handle that
            }
            Buffer data;
            try {
              data = unmarshaller.apply(mediaType, body);
            } catch (Exception e) {
              handler.handle(Future.failedFuture(e));
              return;
            }
            vertx.fileSystem().open(file.getPath(), new OpenOptions().setCreate(true), ar2 -> {
              if (ar2.succeeded()) {
                AsyncFile result = ar2.result();
                result.write(data);
                result.close(v2 -> {
                  if (v2.succeeded()) {
                    handler.handle(Future.succeededFuture(file));
                  } else {
                    handler.handle(Future.failedFuture(v2.cause()));
                  }
                });
              } else {
                handler.handle(Future.failedFuture(ar2.cause()));
              }
            });
          });
          break;
        }
        case 301:
        case 302:
        case 303:
        case 308: {
          // Redirect
          String location = resp.headers().get("location");
          if (location == null) {
            handler.handle(Future.failedFuture("HTTP redirect with no location header"));
          } else {
            URI redirectURI;
            try {
              redirectURI = new URI(location);
            } catch (URISyntaxException e) {
              handler.handle(Future.failedFuture("Invalid redirect URI: " + location));
              return;
            }
            if (history.contains(redirectURI)) {
              handler.handle(Future.failedFuture(new Exception("Server redirected to a previous uri " + redirectURI)));
              return;
            }
            Set<URI> nextHistory = new HashSet<>(history);
            nextHistory.add(url);
            doRequest(client, file, redirectURI, username, password, doAuth, unmarshaller, nextHistory, handler);
          }
          break;
        }
        case 401: {
          if (internalPrefix().equals("https") && resp.getHeader("WWW-Authenticate") != null && username != null && password != null) {
            doRequest(client, file, url, username, password, true, unmarshaller, history, handler);
            return;
          }
          handler.handle(Future.failedFuture(new Exception("Unauthorized")));
          break;
        }
        default: {
          handler.handle(Future.failedFuture(new Exception("Cannot get file" + url.getPath() + " status:" + status)));
          break;
        }
      }
    });
    req.end();
  }

  private static class Result {

    final File deployment;
    final File signature;

    public Result(File deployment, File signature) {
      this.deployment = deployment;
      this.signature = signature;
    }
  }

  protected void doRequest(HttpClient client, File file, URI url, File signatureFile,
                           URI signatureURL, Handler<AsyncResult<Result>> handler) {
    doRequest(client, file, url, username, password, false, (mediatype,buf) -> buf, new HashSet<>(), ar1 -> {
      if (ar1.succeeded()) {
        // Now get the signature if any
        if (validationPolicy != ValidationPolicy.NONE) {
          doRequest(client, signatureFile, signatureURL, username, password, false, (mediatype,buf) -> buf, new HashSet<>(), ar3 -> {
            if (ar3.succeeded()) {
              handler.handle(Future.succeededFuture(new Result(ar1.result(), ar3.result())));
            } else {
              if (validationPolicy == ValidationPolicy.MANDATORY) {
                handler.handle(Future.failedFuture(ar3.cause()));
              } else {
                handler.handle(Future.succeededFuture(new Result(ar1.result(), null)));
              }
            }
          });
        } else {
          handler.handle(Future.succeededFuture(new Result(file, null)));
        }
      } else {
        handler.handle(Future.failedFuture(ar1.cause()));
      }
    });
  }

  private void deploy(List<String> dependencies, File file, String identifier, String serviceName, DeploymentOptions deploymentOptions, 
		  ClassLoader classLoader, Future<String> resolution) {
    try {
      String serviceIdentifer = null;
      if (serviceName == null) {
        JarFile jarFile = new JarFile(file);
        Manifest manifest = jarFile.getManifest();
        if (manifest != null) {
          serviceIdentifer = (String) manifest.getMainAttributes().get(new Attributes.Name("Main-Verticle"));
        }
        jarFile.close();
      } else {
        //serviceIdentifer = "otocloud_srv:" + serviceName;
        serviceIdentifer = serviceName;
      }
      if (serviceIdentifer == null) {
        throw new IllegalArgumentException("Invalid service identifier, missing service name: " + identifier);
      }
      
      List<String> urls = new ArrayList<String>();
      urls.add(file.getAbsolutePath());
      if(dependencies != null && dependencies.size() > 0){
    	  urls.addAll(OtoCloudServiceImpl.buildClassPathToList(dependencies)); 
      }
      
      deploymentOptions.setExtraClasspath(urls);
      //deploymentOptions.setIsolationGroup("__vertx_maven_" + file.getName());
      deploymentOptions.setIsolationGroup(file.getName());
      //URLClassLoader urlc = new URLClassLoader(new URL[]{file.toURI().toURL()}, classLoader);
      
      internalDeploy(serviceIdentifer, deploymentOptions, null, resolution);
      
    } catch (Exception e) {
      resolution.fail(e);
    }
  }
  
  public void internalDeploy(String serviceName, DeploymentOptions deploymentOptions, ClassLoader classLoader, Future<String> resolution) {
	  super.loadServiceConfig(serviceName, deploymentOptions, classLoader, resolution);
  }
  
}

