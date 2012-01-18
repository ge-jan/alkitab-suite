/* This work has been placed into the public domain. */
package kiyut.alkitab.api;

import java.lang.reflect.Field;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import org.crosswire.common.util.NetUtil;
import org.crosswire.common.util.ResourceUtil;
import org.crosswire.common.xml.Converter;
import org.crosswire.common.xml.SAXEventProvider;
import org.crosswire.common.xml.TransformingSAXEventProvider;

/**
 * Simple Swing-HTML stylesheet converter
 *
 *
 */
public class SwingHTMLConverter implements Converter {

    public static String DIRECTION = "direction";
    public static String BASE_URL = "baseURL";
    public static String FONT = "font";
    //public static String CSS = "css";
    /**
     * The xsl path , by default it is kiyut/alkitab/modules/jsword/xsl/simple.xsl
     */
    private static String xslPath = "kiyut/alkitab/modules/jsword/xsl/simple.xsl";

    @Override
    public SAXEventProvider convert(SAXEventProvider xmlsep) throws TransformerException {
        try {
            String path = xslPath;
            URL xslURL = ResourceUtil.getResource(path);

            TransformingSAXEventProvider tsep = new TransformingSAXEventProvider(NetUtil.toURI(xslURL), xmlsep);
            fixForJava7(tsep);

            return tsep;

        } catch (MissingResourceException ex) {
            throw new TransformerException(ex);
        }
    }

    /**
     * XXX Workaround for Java 7 and xalan use of extension function 'http://xml.apache.org/xalan/java:instance' is not
     * allowed when the secure processing feature is set to true. *
     *
     */
    protected void fixForJava7(TransformingSAXEventProvider tsep) {
        // use reflection to set the TransformerFactory private field _isNotSecureProcessing to true
        // thanks to Brian Fernandes (author of FireBible)
        try {
            Field field = tsep.getClass().getDeclaredField("transfact");
            field.setAccessible(true);
            Object transfact = field.get(tsep);
            Field _isNotSecureProcessing = transfact.getClass().getDeclaredField("_isNotSecureProcessing");
            _isNotSecureProcessing.setAccessible(true);
            _isNotSecureProcessing.set(transfact, Boolean.TRUE);
        } catch (Exception ex) {
            Logger logger = Logger.getLogger(this.getClass().getName());
            logger.log(Level.WARNING, ex.getMessage());
        }

        /*
         * XXX: workaround for Java 7 and xalan use of extension function 'http://xml.apache.org/xalan/java:instance' is
         * not allowed when the secure processing feature is set to true. 
         * By using reflection 
         * - Create new TransformerFactory 
         * - set isNotSecureProcessing to true (reflection) 
         * - Replace the TransformingSAXEventProvider.transfact with the above (reflection)
         */
        /*
         * try { TransformerFactory tf = TransformerFactory.newInstance(); Field _isNotSecureProcessing =
         * tf.getClass().getDeclaredField("_isNotSecureProcessing"); _isNotSecureProcessing.setAccessible(true);
         * _isNotSecureProcessing.set(tf, Boolean.TRUE);
         *
         * Field _transfact = tsep.getClass().getDeclaredField("transfact"); _transfact.setAccessible(true);
         * _transfact.set(tsep,tf);
         *
         * } catch (Exception ex) { Logger logger = Logger.getLogger(this.getClass().getName());
         * logger.log(Level.WARNING, ex.getMessage()); }
         *
         */
    }
}
