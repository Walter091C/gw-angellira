package br.com.gwsistemas.angellira.soap;

import br.com.gwsistemas.angellira.model.*;
import java.math.BigDecimal;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.validation.SchemaFactory;
import javax.xml.XMLConstants;
import javax.xml.transform.dom.DOMSource;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;
import static org.junit.jupiter.api.Assertions.*;

class AgendamentoXmlTest {
    private Agendamento sample() throws Exception {
        Agendamento a = new Agendamento();
        Viagem v = new Viagem(); v.setTipo(TipoViagem.CIRCUITO);
        v.setCarga("000006-002-12345"); v.setOrigem("ORIGEM"); v.setDestino("DESTINO");
        v.setPrevInicio(DatatypeFactory.newInstance().newXMLGregorianCalendar("2026-10-12T10:30:00-03:00"));
        v.setVeiculo("ABC9999"); v.setRota(77); v.setMotorista("01234567800");
        v.setValor(new BigDecimal("75950.66")); v.setFrete(new BigDecimal("3500.00"));
        v.setVolume(new BigDecimal("500"));
        Entrega e = new Entrega(); e.setCliente("01234567800"); e.setSeqEntrega(1); e.setPrevInicio(v.getPrevInicio());
        Docto d = new Docto(); d.setNumero(123); d.setSerie("002"); d.setValor(new BigDecimal("75950.66"));
        e.getDocumentos().add(d); v.getEntregas().add(e); a.getViagens().add(v);
        return a;
    }

    @Test void geraXmlDaDemandaValidoNoXsd() throws Exception {
        Element root = AgendamentoXml.toElement(sample());
        assertEquals("Agendamento", root.getNodeName()); assertNull(root.getNamespaceURI());
        assertEquals("Versao", root.getFirstChild().getNodeName());
        assertEquals("1.2", root.getFirstChild().getTextContent());
        assertEquals("3", root.getElementsByTagName("V_Tipo").item(0).getTextContent());
        assertEquals("75950.66", root.getElementsByTagName("V_Valor").item(0).getTextContent());
        assertEquals("N", root.getElementsByTagName("V_EnviarEmail").item(0).getTextContent());
        assertEquals("N", root.getElementsByTagName("V_EnviarSMS").item(0).getTextContent());
        assertEquals("Entrega", root.getElementsByTagName("Docto").item(0).getParentNode().getNodeName());
        SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
                .newSchema(getClass().getResource("/xsd/Agendamento.xsd"))
                .newValidator().validate(new DOMSource(root));
    }
    @Test void preservaLotesOrdemDeEntregasEOpcionaisAusentes() throws Exception {
        Agendamento a = sample(); Viagem v = a.getViagens().get(0);
        Entrega next = new Entrega(); next.setCliente("segundo & cliente"); v.getEntregas().add(next);
        a.getViagens().add(v);
        Element root = AgendamentoXml.toElement(a);
        assertEquals(2, root.getElementsByTagName("Viagem").getLength());
        assertEquals("segundo & cliente", root.getElementsByTagName("E_Cliente").item(1).getTextContent());
        assertEquals(0, root.getElementsByTagName("V_PrevFim").getLength());
    }
    @Test void rejeitaRotaEPolilinhaJuntas() throws Exception {
        Agendamento a = sample(); a.getViagens().get(0).setPolilinha("encoded");
        assertThrows(IllegalArgumentException.class, () -> AgendamentoXml.toElement(a));
    }
    @Test void rejeitaVeiculoEEquipamentoJuntos() throws Exception {
        Agendamento a = sample(); a.getViagens().get(0).setEqpto("equipamento");
        assertThrows(IllegalArgumentException.class, () -> AgendamentoXml.toElement(a));
    }
    @Test void exigeUmIdentificadorDeVeiculo() throws Exception {
        Agendamento a = sample(); a.getViagens().get(0).setVeiculo(null);
        assertThrows(IllegalArgumentException.class, () -> AgendamentoXml.toElement(a));
    }
    @Test void aceitaFrotaEmLugarDePlaca() throws Exception {
        Agendamento a = sample(); Viagem v = a.getViagens().get(0); v.setVeiculo(null); v.setFrota(23);
        assertEquals("23", AgendamentoXml.toElement(a).getElementsByTagName("V_Frota").item(0).getTextContent());
    }
    @Test void rejeitaMotoristaESenhaJuntos() throws Exception {
        Agendamento a = sample(); a.getViagens().get(0).setSenhaMot("senha-teste");
        assertThrows(IllegalArgumentException.class, () -> AgendamentoXml.toElement(a));
    }
    @Test void rejeitaValorNegativoENumeroDeDocumentoZero() throws Exception {
        Agendamento a = sample(); Viagem v = a.getViagens().get(0); v.setValor(new BigDecimal("-0.01"));
        assertThrows(IllegalArgumentException.class, () -> AgendamentoXml.toElement(a));
        v.setValor(null); v.getEntregas().get(0).getDocumentos().get(0).setNumero(0);
        assertThrows(IllegalArgumentException.class, () -> AgendamentoXml.toElement(a));
    }
    @Test void exigeEntregaQuandoOrigemEDestinoIguais() throws Exception {
        Agendamento a = sample(); Viagem v = a.getViagens().get(0); v.setDestino(v.getOrigem()); v.getEntregas().clear();
        assertThrows(IllegalArgumentException.class, () -> AgendamentoXml.toElement(a));
    }
    @Test void exigeCargaEViagem() throws Exception {
        assertThrows(IllegalArgumentException.class, () -> AgendamentoXml.toElement(new Agendamento()));
        Agendamento a = sample(); a.getViagens().get(0).setCarga(" ");
        assertThrows(IllegalArgumentException.class, () -> AgendamentoXml.toElement(a));
    }
    @Test void exigeDataCompletaERejeitaFlagInvalida() throws Exception {
        Agendamento a = sample(); Viagem v = a.getViagens().get(0); v.setEnviarEmail("X");
        assertThrows(IllegalArgumentException.class, () -> AgendamentoXml.toElement(a));
        v.setEnviarEmail("N"); v.setPrevInicio(DatatypeFactory.newInstance().newXMLGregorianCalendar("2026-10-12"));
        assertThrows(IllegalArgumentException.class, () -> AgendamentoXml.toElement(a));
    }
}
