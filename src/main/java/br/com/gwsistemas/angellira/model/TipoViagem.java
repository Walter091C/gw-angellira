package br.com.gwsistemas.angellira.model;

public enum TipoViagem {
    COLETA(1), DISTRIBUICAO(2), CIRCUITO(3), TRANSFERENCIA(4);
    private final int codigo;
    TipoViagem(int codigo) { this.codigo = codigo; }
    public int getCodigo() { return codigo; }
}
