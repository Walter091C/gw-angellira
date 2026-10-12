package br.com.gwsistemas.angellira.client;

import br.com.gwsistemas.angellira.config.AngelLiraConfig;
import br.com.gwsistemas.angellira.generated.*;
import com.sun.net.httpserver.HttpServer;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.ws.soap.SOAPFaultException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import static org.junit.jupiter.api.Assertions.*;

class AngelLiraClientTest {
    private static final String NS = "http://www.angellira.com.br/";
    private HttpServer server;
    private AngelLiraClient client;
    private final BlockingQueue<String> requests = new LinkedBlockingQueue<>();
    private final BlockingQueue<String> actions = new LinkedBlockingQueue<>();
    private volatile String operation;
    private volatile String result;
    private volatile boolean fault;
    private volatile boolean delay;

    @BeforeEach void start() throws Exception {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.createContext("/soap", exchange -> {
            try (InputStream in = exchange.getRequestBody()) {
                ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int n;
                while ((n = in.read(buffer)) != -1) { bytes.write(buffer, 0, n); }
                requests.add(new String(bytes.toByteArray(), StandardCharsets.UTF_8));
                actions.add(exchange.getRequestHeaders().getFirst("SOAPAction"));
            }
            if (delay) {
                try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            }
            String body = fault
                    ? "<soap:Fault><faultcode>soap:Server</faultcode><faultstring>Falha simulada</faultstring></soap:Fault>"
                    : "<" + operation + "Response xmlns=\"" + NS + "\"><" + operation + "Result>"
                        + result + "</" + operation + "Result></" + operation + "Response>";
            byte[] response = ("<soap:Envelope xmlns:soap=\"http://schemas.xmlsoap.org/soap/envelope/\">"
                    + "<soap:Body>" + body + "</soap:Body></soap:Envelope>").getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/xml; charset=utf-8");
            exchange.sendResponseHeaders(fault ? 500 : 200, response.length);
            try (java.io.OutputStream out = exchange.getResponseBody()) { out.write(response); }
        });
        server.start();
        client = new AngelLiraClient(new AngelLiraConfig(
                "http://127.0.0.1:" + server.getAddress().getPort() + "/soap",
                "usuario-teste", "senha<&teste", true, false));
    }

    @AfterEach void stop() {
        if (client != null) { client.close(); }
        if (server != null) { server.stop(0); }
    }

    private Element request(String expectedOperation) throws Exception {
        String xml = requests.poll(5, TimeUnit.SECONDS);
        assertNotNull(xml, "O servidor deve ter recebido a chamada SOAP");
        Element root = br.com.gwsistemas.angellira.soap.ViagemXml.parse(xml);
        assertEquals(1, root.getElementsByTagNameNS(NS, "ValidationSoapHeader").getLength());
        assertEquals("usuario-teste", text(root, "userCod"));
        assertEquals("senha<&teste", text(root, "userPwd"));
        assertEquals("true", text(root, "homologacao"));
        assertEquals("false", text(root, "api_interna"));
        assertEquals("\"" + NS + expectedOperation + "\"", actions.poll(5, TimeUnit.SECONDS));
        NodeList nodes = root.getElementsByTagNameNS(NS, expectedOperation);
        assertEquals(1, nodes.getLength());
        return (Element) nodes.item(0);
    }

    private static String text(Element root, String name) {
        return root.getElementsByTagNameNS(NS, name).item(0).getTextContent();
    }

    @Test void cadastraClientesEmUmaRequisicaoEPreservaRetornosParciais() throws Exception {
        operation = "CadastrarCliente";
        result = "<RetornoCliente><status>1</status><motivo>OK</motivo><codigo>101</codigo><id>111</id><lat>0</lat><lon>0</lon></RetornoCliente>"
                + "<RetornoCliente><status>2</status><motivo>Cliente inválido</motivo><codigo>0</codigo><id>222</id><lat>0</lat><lon>0</lon></RetornoCliente>";
        Cliente first = new Cliente(); first.setId("111"); first.setCnpj("111"); first.setNome("Empresa & Filhos");
        Cliente second = new Cliente(); second.setId("222"); second.setCpf("222"); second.setNome("Pessoa");
        ArrayOfRetornoCliente response = client.cadastrarClientes(Arrays.asList(first, second));
        assertEquals(2, response.getRetornoCliente().size());
        assertEquals(101, response.getRetornoCliente().get(0).getCodigo());
        assertEquals("222", response.getRetornoCliente().get(1).getId());
        assertEquals("Cliente inválido", response.getRetornoCliente().get(1).getMotivo());
        assertEquals(2, response.getRetornoCliente().get(1).getStatus());
        Element req = request(operation);
        NodeList customers = req.getElementsByTagNameNS(NS, "Cliente");
        assertEquals(2, customers.getLength());
        assertEquals("Empresa & Filhos", text((Element) customers.item(0), "nome"));
        assertEquals(0, ((Element) customers.item(0)).getElementsByTagNameNS(NS, "cpf").getLength());
        assertEquals(0, ((Element) customers.item(1)).getElementsByTagNameNS(NS, "cnpj").getLength());
    }

    @Test void consultaRotasPorOrigemEDestino() throws Exception {
        operation = "GetRotas";
        result = "<RetornoRota><status>1</status><codigo>77</codigo><descricao>Rota teste</descricao></RetornoRota>";
        PesquisaRota points = new PesquisaRota(); points.setOrigem("2611606"); points.setDestino("3550308");
        assertEquals(77, client.consultarRotas(points).getRetornoRota().get(0).getCodigo());
        Element req = request(operation);
        assertEquals("2611606", text(req, "Origem"));
        assertEquals("3550308", text(req, "Destino"));
        assertEquals("0", text(req, "UltimaRotaRecebida"));
        assertEquals("false", text(req, "retornarPolilinha"));
    }

    @Test void consultaProcedimentos() throws Exception {
        operation = "GetProcedimentoEmbarque";
        result = "<RetornoItem><status>1</status><codigo>12</codigo><descricao>Procedimento teste</descricao></RetornoItem>";
        RetornoItem item = client.consultarProcedimentosEmbarque().getRetornoItem().get(0);
        assertEquals(12, item.getCodigo()); assertEquals("Procedimento teste", item.getDescricao());
        request(operation);
    }

    @Test void enviaXmlDeValidacaoComoElementoSemEscape() throws Exception {
        operation = "ValidarViagem";
        result = "<RetornoViagem><status>1</status><codigo>123</codigo><carga>000006-002-12345</carga></RetornoViagem>";
        // Fixture de transporte apenas: o formato interno deve ser confirmado na documentação.
        ArrayOfRetornoViagem response = client.validarViagem("<Fixture xmlns=\"urn:teste\"><Valor>A &amp; B</Valor></Fixture>");
        assertEquals(123, response.getRetornoViagem().get(0).getCodigo());
        Element req = request(operation);
        Element wrapper = (Element) req.getElementsByTagNameNS(NS, "_Viagens").item(0);
        assertEquals(1, wrapper.getElementsByTagNameNS("urn:teste", "Fixture").getLength());
        assertEquals("A & B", wrapper.getElementsByTagNameNS("urn:teste", "Valor").item(0).getTextContent());
    }

    @Test void enviaAgendamentoTipadoSemNamespaceDoSoap() throws Exception {
        operation = "ValidarViagem";
        result = "<RetornoViagem><status>1</status><codigo>123</codigo><carga>carga-teste</carga></RetornoViagem>";
        br.com.gwsistemas.angellira.model.Agendamento a = new br.com.gwsistemas.angellira.model.Agendamento();
        br.com.gwsistemas.angellira.model.Viagem v = new br.com.gwsistemas.angellira.model.Viagem();
        v.setCarga("carga-teste"); v.setOrigem("origem"); v.setDestino("destino"); v.setVeiculo("ABC9999");
        v.setPrevInicio(DatatypeFactory.newInstance().newXMLGregorianCalendar("2026-10-12T10:30:00-03:00"));
        a.getViagens().add(v);
        assertEquals(123, client.validarViagem(a).getRetornoViagem().get(0).getCodigo());
        Element req = request(operation);
        assertEquals(0, req.getElementsByTagNameNS(NS, "Agendamento").getLength());
        Element payload = (Element) req.getElementsByTagNameNS("", "Agendamento").item(0);
        assertNotNull(payload);
        assertEquals("1.2", payload.getElementsByTagNameNS("", "Versao").item(0).getTextContent());
        javax.xml.validation.SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI)
                .newSchema(getClass().getResource("/xsd/Agendamento.xsd"))
                .newValidator().validate(new javax.xml.transform.dom.DOMSource(payload));
    }

    @Test void enviaAgendamento() throws Exception {
        operation = "AgendarViagem";
        result = "<RetornoViagem><status>1</status><codigo>124</codigo><carga>carga-teste</carga></RetornoViagem>";
        assertEquals(124, client.agendarViagem("<Fixture xmlns=\"urn:teste\"/>").getRetornoViagem().get(0).getCodigo());
        assertEquals(1, request(operation).getElementsByTagNameNS("urn:teste", "Fixture").getLength());
    }

    @Test void cancelaViagensEmLote() throws Exception {
        operation = "CancelarViagem";
        result = "<RetornoViagem><status>1</status><codigo>123</codigo><carga>000006-002-12345</carga></RetornoViagem>";
        Cancelamento cancel = new Cancelamento(); cancel.setVCarga("000006-002-12345"); cancel.setVProcedimentoEmbarque(12);
        client.cancelarViagens(Arrays.asList(cancel, cancel));
        Element req = request(operation);
        assertEquals(2, req.getElementsByTagNameNS(NS, "Cancelamento").getLength());
        assertEquals("000006-002-12345", text(req, "V_Carga"));
        assertEquals("12", text(req, "V_ProcedimentoEmbarque"));
    }

    @Test void finalizaComDataEFusoDoChamador() throws Exception {
        operation = "FinalizarViagem";
        result = "<status>1</status><codigo>123</codigo><carga>000006-002-12345</carga>";
        IdentificadorViagem trip = new IdentificadorViagem(); trip.setVCarga("000006-002-12345"); trip.setVProcedimentoEmbarque(12);
        assertEquals(123, client.finalizarViagem(trip,
                DatatypeFactory.newInstance().newXMLGregorianCalendar("2026-10-12T10:30:00-03:00"), "Finalizada").getCodigo());
        Element req = request(operation);
        assertEquals("2026-10-12T10:30:00-03:00", text(req, "data"));
        assertEquals("000006-002-12345", text(req, "V_Carga"));
        assertEquals("Finalizada", text(req, "observacao"));
    }

    @Test void preservaSoapFaultSemRepetirOperacao() throws Exception {
        operation = "GetProcedimentoEmbarque"; fault = true;
        SOAPFaultException error = assertThrows(SOAPFaultException.class, () -> client.consultarProcedimentosEmbarque());
        assertEquals("Falha simulada", error.getFault().getFaultString());
        request(operation);
        assertTrue(requests.isEmpty());
    }

    @Test void rejeitaLotesVaziosAntesDeEnviar() {
        assertThrows(IllegalArgumentException.class, () -> client.cadastrarClientes(Collections.emptyList()));
        assertThrows(IllegalArgumentException.class, () -> client.cancelarViagens(Arrays.asList((Cancelamento) null)));
        assertTrue(requests.isEmpty());
    }

    @Test void interrompeRespostaQueExcedeTimeout() throws Exception {
        operation = "GetProcedimentoEmbarque"; result = ""; delay = true;
        client.close();
        client = new AngelLiraClient(new AngelLiraConfig(
                "http://127.0.0.1:" + server.getAddress().getPort() + "/soap",
                "usuario-teste", "senha<&teste", true, false, 2000, 100));
        javax.xml.ws.WebServiceException failure = assertThrows(javax.xml.ws.WebServiceException.class,
                () -> client.consultarProcedimentosEmbarque());
        Throwable cause = failure;
        while (cause.getCause() != null) { cause = cause.getCause(); }
        assertInstanceOf(java.net.SocketTimeoutException.class, cause);
        request(operation);
        assertTrue(requests.isEmpty());
    }

    @Test void configuraTimeoutsNoTransporte() {
        org.apache.cxf.transport.http.HTTPConduit conduit = (org.apache.cxf.transport.http.HTTPConduit)
                org.apache.cxf.frontend.ClientProxy.getClient(client.getPort()).getConduit();
        assertEquals(10000, conduit.getClient().getConnectionTimeout());
        assertEquals(60000, conduit.getClient().getReceiveTimeout());
        assertFalse(conduit.getClient().isAutoRedirect());
    }

    @Test void wsdlGeradoTemLocalizacaoDisponivelNoClasspath() {
        assertNotNull(WSImport.WSDL_LOCATION);
    }
}
