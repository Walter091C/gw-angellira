
package br.com.gwsistemas.angellira.client;

import br.com.gwsistemas.angellira.config.AngelLiraConfig;
import br.com.gwsistemas.angellira.generated.WSImport;
import br.com.gwsistemas.angellira.generated.WSImportSoap;

import java.net.URL;
import javax.xml.ws.BindingProvider;

public class AngelLiraClient {

    private final AngelLiraConfig config;
    private final WSImportSoap port;

    public AngelLiraClient(AngelLiraConfig config) {
        this.config = config;

        try {
            WSImport service = new WSImport();
            this.port = service.getWSImportSoap();

            BindingProvider bindingProvider = (BindingProvider) this.port;

            bindingProvider.getRequestContext().put(
                    BindingProvider.ENDPOINT_ADDRESS_PROPERTY,
                    config.getEndpoint()
            );

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Não foi possível inicializar o cliente SOAP da AngelLira.",
                    e
            );
        }
    }

    public WSImportSoap getPort() {
        return port;
    }
}