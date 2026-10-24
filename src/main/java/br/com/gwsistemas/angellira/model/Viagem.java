package br.com.gwsistemas.angellira.model;

import java.math.BigDecimal;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.ArrayList;
import java.util.List;

/** Dados da documentação de Agendamento 1.2, independentes de manifesto/romaneio. */
public class Viagem {
    private TipoViagem tipo;
    private String carga;
    private String origem;
    private String destino;
    private Integer rota;
    private String polilinha;
    private XMLGregorianCalendar prevInicio;
    private XMLGregorianCalendar prevFim;
    private XMLGregorianCalendar dataFat;
    private String enviarEmail = "N";
    private String enviarSMS = "N";
    private String veiculo;
    private String eqpto;
    private Integer frota;
    private String carreta1;
    private String carreta2;
    private String carreta3;
    private String motorista;
    private String senhaMot;
    private BigDecimal peso;
    private BigDecimal volume;
    private BigDecimal valor;
    private BigDecimal frete;
    private Integer procedimentoEmbarque;
    private String observacao;
    private final List<Entrega> entregas = new ArrayList<>();

    public TipoViagem getTipo() { return tipo; }
    public void setTipo(TipoViagem tipo) { this.tipo = tipo; }

    public String getCarga() { return carga; }
    public void setCarga(String carga) { this.carga = carga; }

    public String getOrigem() { return origem; }
    public void setOrigem(String origem) { this.origem = origem; }

    public String getDestino() { return destino; }
    public void setDestino(String destino) { this.destino = destino; }

    public Integer getRota() { return rota; }
    public void setRota(Integer rota) { this.rota = rota; }

    public String getPolilinha() { return polilinha; }
    public void setPolilinha(String polilinha) { this.polilinha = polilinha; }

    public XMLGregorianCalendar getPrevInicio() { return prevInicio; }
    public void setPrevInicio(XMLGregorianCalendar prevInicio) { this.prevInicio = prevInicio; }

    public XMLGregorianCalendar getPrevFim() { return prevFim; }
    public void setPrevFim(XMLGregorianCalendar prevFim) { this.prevFim = prevFim; }

    public XMLGregorianCalendar getDataFat() { return dataFat; }
    public void setDataFat(XMLGregorianCalendar dataFat) { this.dataFat = dataFat; }

    public String getEnviarEmail() { return enviarEmail; }
    public void setEnviarEmail(String enviarEmail) { this.enviarEmail = enviarEmail; }

    public String getEnviarSMS() { return enviarSMS; }
    public void setEnviarSMS(String enviarSMS) { this.enviarSMS = enviarSMS; }

    public String getVeiculo() { return veiculo; }
    public void setVeiculo(String veiculo) { this.veiculo = veiculo; }

    public String getEqpto() { return eqpto; }
    public void setEqpto(String eqpto) { this.eqpto = eqpto; }

    public Integer getFrota() { return frota; }
    public void setFrota(Integer frota) { this.frota = frota; }

    public String getCarreta1() { return carreta1; }
    public void setCarreta1(String carreta1) { this.carreta1 = carreta1; }

    public String getCarreta2() { return carreta2; }
    public void setCarreta2(String carreta2) { this.carreta2 = carreta2; }

    public String getCarreta3() { return carreta3; }
    public void setCarreta3(String carreta3) { this.carreta3 = carreta3; }

    public String getMotorista() { return motorista; }
    public void setMotorista(String motorista) { this.motorista = motorista; }

    public String getSenhaMot() { return senhaMot; }
    public void setSenhaMot(String senhaMot) { this.senhaMot = senhaMot; }

    public BigDecimal getPeso() { return peso; }
    public void setPeso(BigDecimal peso) { this.peso = peso; }

    public BigDecimal getVolume() { return volume; }
    public void setVolume(BigDecimal volume) { this.volume = volume; }

    public BigDecimal getValor() { return valor; }
    public void setValor(BigDecimal valor) { this.valor = valor; }

    public BigDecimal getFrete() { return frete; }
    public void setFrete(BigDecimal frete) { this.frete = frete; }

    public Integer getProcedimentoEmbarque() { return procedimentoEmbarque; }
    public void setProcedimentoEmbarque(Integer procedimentoEmbarque) { this.procedimentoEmbarque = procedimentoEmbarque; }

    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }

    /** Lista mutável: a ordem de inserção será preservada no XML. */
    public List<Entrega> getEntregas() { return entregas; }
}
