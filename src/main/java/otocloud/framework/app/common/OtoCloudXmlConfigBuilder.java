package otocloud.framework.app.common;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;

import com.hazelcast.config.InvalidConfigurationException;
import com.hazelcast.config.XmlConfigBuilder;
import com.hazelcast.nio.IOUtil;


/**
 * A XML {@link ConfigBuilder} implementation.
 */
public class OtoCloudXmlConfigBuilder extends XmlConfigBuilder {

    public OtoCloudXmlConfigBuilder(String xmlFileName) throws FileNotFoundException {
    	super(xmlFileName);
    }

    /**
     * Constructs a XmlConfigBuilder that reads from the given InputStream.
     *
     * @param inputStream the InputStream containing the XML configuration.
     * @throws IllegalArgumentException if inputStream is null.
     */
    public OtoCloudXmlConfigBuilder(InputStream inputStream) {
        super(inputStream);
    }

    /**
     * Constructs a XMLConfigBuilder that reads from the given URL.
     *
     * @param url the given url that the XMLConfigBuilder reads from
     * @throws IOException
     */
    public OtoCloudXmlConfigBuilder(URL url) throws IOException {
        super(url);
    }

    /**
     * Constructs a XmlConfigBuilder that tries to find a usable XML configuration file.
     */
    public OtoCloudXmlConfigBuilder(){
        super();
    }

    @Override
    protected Document parse(InputStream is) throws Exception {
        DocumentBuilderFactory dbf = new org.apache.crimson.jaxp.DocumentBuilderFactoryImpl(); //DocumentBuilderFactory.newInstance();
        //dbf.setNamespaceAware(true);
        //dbf.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        final DocumentBuilder builder = dbf.newDocumentBuilder();
        Document doc;
        try {
            doc = builder.parse(is);
        } catch (final Exception e) {
        	e.printStackTrace();
            throw new InvalidConfigurationException(e.getMessage(), e);
        } finally {
            IOUtil.closeResource(is);
        }        
        
        return doc;
    }
    
    @Override
    protected void schemaValidation(Document doc) throws Exception {
    }


}
