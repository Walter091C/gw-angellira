
package br.com.gwsistemas.angellira.config;

public class AngelLiraConfig {

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
        this.endpoint = endpoint;
        this.userCode = userCode;
        this.password = password;
        this.homologacao = homologacao;
        this.apiInterna = apiInterna;
    }

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