package br.com.gwsistemas.angellira.soap;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ViagemXmlTest {
    @Test void rejeitaXmlInvalido() {
        assertThrows(IllegalArgumentException.class, () -> ViagemXml.parse("<Fixture>"));
        assertThrows(IllegalArgumentException.class, () -> ViagemXml.parse(" "));
    }
    @Test void rejeitaEntidadesExternas() {
        assertThrows(IllegalArgumentException.class, () -> ViagemXml.parse(
                "<!DOCTYPE Fixture [<!ENTITY xxe SYSTEM 'file:///etc/passwd'>]><Fixture>&xxe;</Fixture>"));
    }
    @Test void preservaNamespaceEAcentos() {
        org.w3c.dom.Element e = ViagemXml.parse("<Fixture xmlns='urn:teste'>São Paulo &amp; região</Fixture>");
        assertEquals("urn:teste", e.getNamespaceURI());
        assertEquals("São Paulo & região", e.getTextContent());
    }
}
