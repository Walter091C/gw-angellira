package br.com.gwsistemas.angellira.client;

import br.com.gwsistemas.angellira.config.AngelLiraConfig;
import br.com.gwsistemas.angellira.generated.*;
import br.com.gwsistemas.angellira.soap.AuthenticationHandler;
import br.com.gwsistemas.angellira.soap.AgendamentoXml;
import br.com.gwsistemas.angellira.model.Agendamento;
import br.com.gwsistemas.angellira.soap.ViagemXml;
import java.net.URL;
import java.io.Closeable;
import java.io.IOException;
import javax.xml.ws.WebServiceException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.function.IntFunction;
import br.com.gwsistemas.angellira.model.AngelLiraStatus;
import java.util.Objects;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.handler.Handler;
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
            // Propriedades do JAX-WS RI incluído no Java 8.
            provider.getRequestContext().put("com.sun.xml.internal.ws.connect.timeout",
                    timeout(config.getConnectionTimeoutMillis()));
            provider.getRequestContext().put("com.sun.xml.internal.ws.request.timeout",
                    timeout(config.getReceiveTimeoutMillis()));
            // Streaming impede repetição automática de POST e redirecionamento
            // com reenvio do corpo pelo HttpURLConnection.
            provider.getRequestContext().put(
                    "com.sun.xml.internal.ws.transport.http.client.streaming.chunk.size", 8192);
            this.port = createdPort;
        } catch (RuntimeException e) {
            if (createdPort != null) {
                try { fecharPort(createdPort); }
                catch (RuntimeException fechamento) { e.addSuppressed(fechamento); }
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

    /** Lista todas as rotas acessíveis às credenciais no ambiente configurado, sem polilinha. */
    public List<RetornoRota> consultarTodasRotas() {
        return consultarTodasRotas(ultimaRota -> consultarPaginaRotas(ultimaRota), 1000);
    }

    /** Página sem filtros de pontos ou códigos. Não grava cadastros. */
    public ArrayOfRetornoRota consultarPaginaRotas(int ultimaRotaRecebida) {
        if (ultimaRotaRecebida < 0) {
            throw new IllegalArgumentException("Última rota recebida não pode ser negativa");
        }
        return port.getRotas(ultimaRotaRecebida, null, null, false);
    }

    // Separado do transporte para verificar paginação sem chamadas reais.
    static List<RetornoRota> consultarTodasRotas(IntFunction<ArrayOfRetornoRota> consulta,
                                                 int limitePaginas) {
        Objects.requireNonNull(consulta, "Consulta obrigatória");
        if (limitePaginas <= 0) { throw new IllegalArgumentException("Limite de páginas inválido"); }
        Map<Integer, RetornoRota> rotasPorCodigo = new LinkedHashMap<Integer, RetornoRota>();
        int ultimaRotaRecebida = 0;
        for (int pagina = 0; pagina < limitePaginas; pagina++) {
            ArrayOfRetornoRota resposta = consulta.apply(ultimaRotaRecebida);
            if (resposta == null) {
                throw new WebServiceException("Consulta de rotas sem resposta; listagem incompleta");
            }
            if (resposta.getRetornoRota().isEmpty()) {
                return new ArrayList<RetornoRota>(rotasPorCodigo.values());
            }
            int maiorCodigoRecebido = ultimaRotaRecebida;
            for (RetornoRota rota : resposta.getRetornoRota()) {
                if (rota == null || !AngelLiraStatus.isSucesso(rota) || rota.getCodigo() <= 0) {
                    String motivo = rota == null ? "Retorno ausente"
                            : "Status " + rota.getStatus() + ": " + rota.getMotivo();
                    throw new WebServiceException("Consulta de rotas inválida: " + motivo);
                }
                // Aceita repetição da fronteira da página, sem duplicar o resultado.
                if (rota.getCodigo() < ultimaRotaRecebida) {
                    throw new WebServiceException("Consulta retornou código anterior ao cursor; listagem incompleta");
                }
                rotasPorCodigo.put(rota.getCodigo(), rota);
                maiorCodigoRecebido = Math.max(maiorCodigoRecebido, rota.getCodigo());
            }
            if (maiorCodigoRecebido <= ultimaRotaRecebida) {
                throw new WebServiceException("Paginação de rotas não avançou; listagem incompleta");
            }
            ultimaRotaRecebida = maiorCodigoRecebido;
        }
        throw new WebServiceException("Limite de páginas de rotas atingido; listagem incompleta");
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

    private static int timeout(long milissegundos) {
        if (milissegundos <= 0 || milissegundos > Integer.MAX_VALUE) {
            throw new IllegalArgumentException("Timeout deve estar entre 1 e 2147483647 milissegundos");
        }
        return (int) milissegundos;
    }

    private static void fecharPort(WSImportSoap port) {
        if (port instanceof Closeable) {
            try { ((Closeable) port).close(); }
            catch (IOException e) { throw new WebServiceException("Falha ao fechar cliente SOAP", e); }
        }
    }

    @Override public void close() { fecharPort(port); }
}
