package br.com.gwsistemas.angellira.model;

import br.com.gwsistemas.angellira.generated.Retorno;

/**
 * Interpreta os status do contrato SOAP, sem alterar ou descartar o retorno.
 * Fonte: manual WSImport, seção Lista de possíveis status.
 * Não representa o status local de qualquer aplicação consumidora.
 */
public final class AngelLiraStatus {
    public static final int OK = 0;
    public static final int SUCESSO_COM_OBSERVACOES = 999;

    private AngelLiraStatus() { }

    public static boolean isSucesso(int status) {
        return status == OK || status == SUCESSO_COM_OBSERVACOES;
    }

    /** Ausência de resposta não é sucesso. */
    public static boolean isSucesso(Retorno retorno) {
        return retorno != null && isSucesso(retorno.getStatus());
    }

    public static boolean temObservacoes(int status) {
        return status == SUCESSO_COM_OBSERVACOES;
    }

    public static boolean temObservacoes(Retorno retorno) {
        return retorno != null && temObservacoes(retorno.getStatus());
    }
}
