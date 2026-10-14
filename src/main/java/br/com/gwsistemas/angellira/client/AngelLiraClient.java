package br.com.gwsistemas.angellira.client;

import br.com.gwsistemas.angellira.config.AngelLiraConfig;
import br.com.gwsistemas.angellira.generated.*;
import br.com.gwsistemas.angellira.soap.AuthenticationHandler;
import br.com.gwsistemas.angellira.soap.AgendamentoXml;
import br.com.gwsistemas.angellira.model.Agendamento;
import br.com.gwsistemas.angellira.soap.ViagemXml;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
import org.apache.cxf.frontend.ClientProxy;
import org.apache.cxf.transport.http.HTTPConduit;
import org.apache.cxf.transports.http.configuration.HTTPClientPolicy;
import org.w3c.dom.Element;

/** Cliente SOAP 1.1. Crie uma instância por fluxo; não compartilhe entre threads. */
public class AngelLiraClient implements AutoCloseable {
    private final WSImportSoap port;

    public AngelLiraClient(AngelLiraConfig config) {
        Objects.requireNonNull(config, "config");
        URL wsdl = AngelLiraClient.class.getResource("/wsdl/WSImport.wsdl");
        if (wsdl == null) {
            throw new IllegalStateException("WSDL não encontrado no classpath: /wsdl/WSImport.wsdl");
        }
        WSImportSoap createdPort = null;
        try {
            createdPort = new WSImport(wsdl).getWSImportSoap();
            BindingProvider provider = (BindingProvider) createdPort;
            provider.getRequestContext().put(BindingProvider.ENDPOINT_ADDRESS_PROPERTY, config.getEndpoint());
            List<Handler> handlers = new ArrayList<>(provider.getBinding().getHandlerChain());
            handlers.add(new AuthenticationHandler(config));
            provider.getBinding().setHandlerChain(handlers);
            HTTPConduit conduit = (HTTPConduit) ClientProxy.getClient(createdPort).getConduit();
            HTTPClientPolicy policy = new HTTPClientPolicy();
            policy.setConnectionTimeout(config.getConnectionTimeoutMillis());
            policy.setReceiveTimeout(config.getReceiveTimeoutMillis());
            // Não redirecionar requisições autenticadas nem repetir operações de escrita.
            policy.setAutoRedirect(false);
            policy.setMaxRetransmits(0);
            conduit.setClient(policy);
            this.port = createdPort;
        } catch (RuntimeException e) {
            if (createdPort != null) {
                ClientProxy.getClient(createdPort).destroy();
            }
            throw new IllegalStateException("Não foi possível inicializar o cliente SOAP da AngelLira", e);
        }
    }

    /** Retornos preservam status, motivo, id e código por cliente, inclusive falhas parciais. */
    public ArrayOfRetornoCliente cadastrarClientes(List<Cliente> clientes) {
        requireBatch(clientes, "clientes");
        ArrayOfCliente request = new ArrayOfCliente();
        request.getCliente().addAll(clientes);
        return port.cadastrarCliente(request);
    }

    /** O contrato aceita uma pesquisa origem/destino por chamada, não uma lista de rotas. */
    public ArrayOfRetornoRota consultarRotas(PesquisaRota pontos) {
        Objects.requireNonNull(pontos, "pontos");
        return port.getRotas(0, pontos, null, false);
    }

    public ArrayOfRetornoItem consultarProcedimentosEmbarque() {
        return port.getProcedimentoEmbarque();
    }

    /** Recebe a raiz XML interna de _Viagens, sem Envelope ou wrapper SOAP. */
    public ArrayOfRetornoViagem validarViagem(Element viagens) {
        ValidarViagem.Viagens request = new ValidarViagem.Viagens();
        request.getContent().add(Objects.requireNonNull(viagens, "viagens").cloneNode(true));
        return port.validarViagem(request);
    }

    public ArrayOfRetornoViagem validarViagem(Agendamento agendamento) {
        return validarViagem(AgendamentoXml.toElement(agendamento));
    }

    public ArrayOfRetornoViagem validarViagem(String xml) {
        return validarViagem(ViagemXml.parse(xml));
    }

    public ArrayOfRetornoViagem agendarViagem(Element viagens) {
        AgendarViagem.Viagens request = new AgendarViagem.Viagens();
        request.getContent().add(Objects.requireNonNull(viagens, "viagens").cloneNode(true));
        return port.agendarViagem(request);
    }

    public ArrayOfRetornoViagem agendarViagem(Agendamento agendamento) {
        return agendarViagem(AgendamentoXml.toElement(agendamento));
    }

    public ArrayOfRetornoViagem agendarViagem(String xml) {
        return agendarViagem(ViagemXml.parse(xml));
    }

    public ArrayOfRetornoViagem cancelarViagens(List<Cancelamento> cancelamentos) {
        requireBatch(cancelamentos, "cancelamentos");
        ArrayOfCancelamento request = new ArrayOfCancelamento();
        request.getCancelamento().addAll(cancelamentos);
        return port.cancelarViagem(request);
    }

    /** Passe a data com fuso explícito; a lib não presume o fuso do Webtrans. */
    public RetornoViagem finalizarViagem(IdentificadorViagem viagem,
                                         XMLGregorianCalendar data, String observacao) {
        Objects.requireNonNull(viagem, "viagem");
        Objects.requireNonNull(data, "data");
        return port.finalizarViagem(viagem, data, observacao);
    }

    private static void requireBatch(List<?> items, String name) {
        if (items == null || items.isEmpty() || items.contains(null)) {
            throw new IllegalArgumentException(name + " deve conter ao menos um item e não pode conter null");
        }
    }

    /** Acesso ao stub para as demais operações do WSDL. */
    public WSImportSoap getPort() { return port; }

    @Override public void close() { ClientProxy.getClient(port).destroy(); }
}
