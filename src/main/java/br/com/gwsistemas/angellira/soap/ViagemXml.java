package br.com.gwsistemas.angellira.soap;

import java.io.StringReader;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/** Lê o XML interno de _Viagens sem resolver DTDs ou entidades externas. */
public final class ViagemXml {
    private ViagemXml() { }

    public static Element parse(String xml) {
        if (xml == null || xml.trim().isEmpty()) {
            throw new IllegalArgumentException("XML de viagens deve ser informado");
        }
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            builder.setErrorHandler(new DefaultHandler() {
                @Override public void error(SAXParseException e) throws SAXParseException { throw e; }
                @Override public void fatalError(SAXParseException e) throws SAXParseException { throw e; }
            });
            return builder.parse(new InputSource(new StringReader(xml))).getDocumentElement();
        } catch (Exception e) {
            throw new IllegalArgumentException("XML de viagens inválido; DTDs e entidades externas não são permitidos", e);
        }
    }
}
