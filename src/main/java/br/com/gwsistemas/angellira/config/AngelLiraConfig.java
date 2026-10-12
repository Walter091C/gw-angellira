
package br.com.gwsistemas.angellira.config;

import java.net.URI;

public class AngelLiraConfig {

    public static final String DEFAULT_ENDPOINT = "https://api.angellira.com.br/ws-soap/WSImport.asmx";
    private final long connectionTimeoutMillis;
    private final long receiveTimeoutMillis;
    private final String endpoint;
    private final String userCode;
    private final String password;
    private final boolean homologacao;
    private final boolean apiInterna;

    public AngelLiraConfig(
            String endpoint,
            String userCode,
            String password,
            boolean homologacao,
            boolean apiInterna) {
        this(endpoint, userCode, password, homologacao, apiInterna, 10000, 60000);
    }

    public AngelLiraConfig(String endpoint, String userCode, String password,
                           boolean homologacao, boolean apiInterna,
                           long connectionTimeoutMillis, long receiveTimeoutMillis) {
        requireText(endpoint, "endpoint");
        requireText(userCode, "userCode");
        requireText(password, "password");
        URI uri = URI.create(endpoint);
        if ((!"https".equalsIgnoreCase(uri.getScheme()) && !"http".equalsIgnoreCase(uri.getScheme()))
                || uri.getHost() == null || uri.getUserInfo() != null || uri.getFragment() != null) {
            throw new IllegalArgumentException("endpoint deve ser uma URL HTTP(S) sem credenciais ou fragmento");
        }
        if (connectionTimeoutMillis <= 0 || receiveTimeoutMillis <= 0) {
            throw new IllegalArgumentException("Os timeouts devem ser positivos");
        }
        this.connectionTimeoutMillis = connectionTimeoutMillis;
        this.receiveTimeoutMillis = receiveTimeoutMillis;
        this.endpoint = endpoint;
        this.userCode = userCode;
        this.password = password;
        this.homologacao = homologacao;
        this.apiInterna = apiInterna;
    }

    private static void requireText(String value, String name) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException(name + " deve ser informado");
        }
    }

    public long getConnectionTimeoutMillis() { return connectionTimeoutMillis; }
    public long getReceiveTimeoutMillis() { return receiveTimeoutMillis; }

    public String getEndpoint() {
        return endpoint;
    }

    public String getUserCode() {
        return userCode;
    }

    public String getPassword() {
        return password;
    }

    public boolean isHomologacao() {
        return homologacao;
    }

    public boolean isApiInterna() {
        return apiInterna;
    }
}