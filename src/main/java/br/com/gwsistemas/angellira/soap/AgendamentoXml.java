package br.com.gwsistemas.angellira.soap;

import br.com.gwsistemas.angellira.model.Agendamento;
import br.com.gwsistemas.angellira.model.Docto;
import br.com.gwsistemas.angellira.model.Entrega;
import br.com.gwsistemas.angellira.model.Viagem;
import java.math.BigDecimal;
import java.net.URL;
import java.util.Objects;
import javax.xml.XMLConstants;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/** Serializa os campos da demanda 7048 na ordem do Agendamento.xsd fornecido. */
public final class AgendamentoXml {
    private static final Schema SCHEMA = loadSchema();
    private AgendamentoXml() { }

    public static Element toElement(Agendamento agendamento) {
        Objects.requireNonNull(agendamento, "agendamento");
        if (agendamento.getViagens().isEmpty()) {
            throw new IllegalArgumentException("Informe ao menos uma viagem");
        }
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            Document doc = factory.newDocumentBuilder().newDocument();
            Element root = doc.createElementNS(null, "Agendamento");
            // O payload não pertence ao namespace do Envelope/wrapper SOAP.
            root.setAttributeNS(XMLConstants.XMLNS_ATTRIBUTE_NS_URI, "xmlns", "");
            doc.appendChild(root);
            add(root, "Versao", agendamento.getVersao());
            for (Viagem viagem : agendamento.getViagens()) {
                append(root, Objects.requireNonNull(viagem, "viagem"));
            }
            Validator validator = SCHEMA.newValidator();
            validator.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            validator.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            validator.validate(new DOMSource(doc));
            return root;
        } catch (IllegalArgumentException | NullPointerException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalArgumentException("Agendamento não atende ao XSD fornecido", e);
        }
    }

    private static void append(Element root, Viagem v) {
        requireText(v.getCarga(), "V_Carga");
        requireText(v.getOrigem(), "V_Origem");
        requireText(v.getDestino(), "V_Destino");
        Objects.requireNonNull(v.getPrevInicio(), "V_PrevInicio");
        if (v.getOrigem().equals(v.getDestino()) && v.getEntregas().isEmpty()) {
            throw new IllegalArgumentException("Entrega é obrigatória quando origem e destino são iguais");
        }
        if (v.getVeiculo() != null) { requireText(v.getVeiculo(), "V_Veiculo"); }
        if (v.getEqpto() != null) { requireText(v.getEqpto(), "V_Eqpto"); }
        limited(v.getObservacao(), 140, "V_Observacao");
        Element trip = child(root, "Viagem");
        add(trip, "V_Tipo", v.getTipo() == null ? null : v.getTipo().getCodigo());
        add(trip, "V_Carga", v.getCarga());
        add(trip, "V_Origem", v.getOrigem());
        add(trip, "V_Destino", v.getDestino());
        add(trip, "V_Rota", v.getRota());
        add(trip, "V_Polilinha", v.getPolilinha());
        add(trip, "V_PrevInicio", v.getPrevInicio());
        add(trip, "V_PrevFim", v.getPrevFim());
        add(trip, "V_DataFat", v.getDataFat());
        add(trip, "V_EnviarEmail", v.getEnviarEmail());
        add(trip, "V_EnviarSMS", v.getEnviarSMS());
        add(trip, "V_Peso", v.getPeso());
        add(trip, "V_Veiculo", v.getVeiculo());
        add(trip, "V_Eqpto", v.getEqpto());
        add(trip, "V_Frota", v.getFrota());
        add(trip, "V_Carreta1", v.getCarreta1());
        add(trip, "V_Carreta2", v.getCarreta2());
        add(trip, "V_Carreta3", v.getCarreta3());
        add(trip, "V_Motorista", v.getMotorista());
        add(trip, "V_SenhaMot", v.getSenhaMot());
        add(trip, "V_Volume", v.getVolume());
        add(trip, "V_Valor", v.getValor());
        add(trip, "V_Frete", v.getFrete());
        add(trip, "V_ProcedimentoEmbarque", v.getProcedimentoEmbarque());
        add(trip, "V_Observacao", v.getObservacao());
        for (Entrega e : v.getEntregas()) {
            Objects.requireNonNull(e, "entrega");
            requireText(e.getCliente(), "E_Cliente");
            limited(e.getObservacao(), 140, "E_Observacao");
            Element delivery = child(trip, "Entrega");
            add(delivery, "E_SeqEntrega", e.getSeqEntrega());
            add(delivery, "E_Cliente", e.getCliente());
            add(delivery, "E_PrevInicio", e.getPrevInicio());
            add(delivery, "E_PrevFim", e.getPrevFim());
            add(delivery, "E_Peso", e.getPeso());
            add(delivery, "E_Volume", e.getVolume());
            add(delivery, "E_Observacao", e.getObservacao());
            for (Docto d : e.getDocumentos()) {
                Objects.requireNonNull(d, "documento");
                Objects.requireNonNull(d.getNumero(), "D_Numero");
                Element document = child(delivery, "Docto");
                add(document, "D_Numero", d.getNumero());
                add(document, "D_Serie", d.getSerie());
                add(document, "D_Peso", d.getPeso());
                add(document, "D_Volume", d.getVolume());
                add(document, "D_Valor", d.getValor());
                add(document, "D_Frete", d.getFrete());
            }
        }
    }

    private static Element child(Element parent, String name) {
        Element element = parent.getOwnerDocument().createElementNS(null, name);
        parent.appendChild(element);
        return element;
    }

    private static void add(Element parent, String name, Object value) {
        if (value == null) { return; }
        String text = value instanceof BigDecimal ? ((BigDecimal) value).toPlainString()
                : value instanceof XMLGregorianCalendar ? ((XMLGregorianCalendar) value).toXMLFormat()
                : value.toString();
        child(parent, name).setTextContent(text);
    }

    private static void requireText(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " deve ser informado");
        }
    }

    private static void limited(String value, int max, String name) {
        if (value != null && value.length() > max) {
            throw new IllegalArgumentException(name + " excede " + max + " caracteres");
        }
    }

    private static Schema loadSchema() {
        URL url = AgendamentoXml.class.getResource("/xsd/Agendamento.xsd");
        if (url == null) { throw new IllegalStateException("Agendamento.xsd não encontrado no classpath"); }
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setProperty(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setProperty(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
            return factory.newSchema(url);
        } catch (Exception e) {
            throw new IllegalStateException("Não foi possível carregar Agendamento.xsd", e);
        }
    }
}
