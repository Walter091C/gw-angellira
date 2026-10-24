package br.com.gwsistemas.angellira.soap;

import br.com.gwsistemas.angellira.config.AngelLiraConfig;
import java.util.Collections;
import java.util.Set;
import javax.xml.namespace.QName;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.ws.ProtocolException;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

/** Insere o cabeçalho exigido pelo WSDL em todas as operações. */
public final class AuthenticationHandler implements SOAPHandler<SOAPMessageContext> {
    public static final String NAMESPACE = "http://www.angellira.com.br/";
    private final AngelLiraConfig config;

    public AuthenticationHandler(AngelLiraConfig config) {
        this.config = java.util.Objects.requireNonNull(config, "config");
    }

    @Override
    public boolean handleMessage(SOAPMessageContext context) {
        if (!Boolean.TRUE.equals(context.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY))) {
            return true;
        }
        try {
            SOAPEnvelope envelope = context.getMessage().getSOAPPart().getEnvelope();
            SOAPHeader header = envelope.getHeader();
            if (header == null) {
                header = envelope.addHeader();
            }
            SOAPElement auth = header.addChildElement(new QName(NAMESPACE, "ValidationSoapHeader", "agl"));
            add(auth, "userCod", config.getUserCode());
            add(auth, "userPwd", config.getPassword());
            add(auth, "homologacao", Boolean.toString(config.isHomologacao()));
            add(auth, "api_interna", Boolean.toString(config.isApiInterna()));
            return true;
        } catch (SOAPException e) {
            throw new ProtocolException("Não foi possível montar o cabeçalho SOAP", e);
        }
    }

    private static void add(SOAPElement parent, String name, String value) throws SOAPException {
        parent.addChildElement(new QName(NAMESPACE, name, "agl")).addTextNode(value);
    }

    @Override public boolean handleFault(SOAPMessageContext context) { return true; }
    @Override public void close(MessageContext context) { }
    @Override public Set<QName> getHeaders() {
        return Collections.singleton(new QName(NAMESPACE, "ValidationSoapHeader"));
    }
}
