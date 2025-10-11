package Controle;

import Entidade.ContasPagar;
import Entidade.Compra;
import Facade.ContasPagarFacade;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

@Named
@ViewScoped
public class ContasPagarControle implements Serializable {

    
    
    private ContasPagar contaSelecionada;
    private List<ContasPagar> listaContas;
    private List<Compra> listaComprasAgrupadas; 
   
    private Date dataFiltroVencimento;
    private boolean mostraTodasContas = false;
    private String nomeClienteFiltro;
    private BigDecimal totalEmAberto;
    private BigDecimal totalRecebido;
    private BigDecimal totalGeral;

    @EJB
    private transient ContasPagarFacade contasReceberFacade;

    @PostConstruct
    public void init() {
        buscarContas();
    }

    public void buscarContas() {
        boolean somenteEmAberto = !mostraTodasContas;
        listaContas = contasReceberFacade.buscar(somenteEmAberto, dataFiltroVencimento, nomeClienteFiltro);
        calcularTotais();
        agruparContasPorCompra(); // A chamada permanece aqui
    }

    // ======================================================================================
    // MÉTODO CORRIGIDO PARA SER COMPATÍVEL COM VERSÕES ANTIGAS DO JAVA
    // ======================================================================================
    private void agruparContasPorCompra() {
        if (listaContas == null || listaContas.isEmpty()) {
            listaComprasAgrupadas = new ArrayList<>();
            return;
        }

        Map<Long, Compra> vendasMap = new LinkedHashMap<>();
        
        for (ContasPagar conta : listaContas) {
            if (conta.getCompra() == null) {
                continue; // Pula contas que por algum motivo não têm venda
            }

            Long vendaId = conta.getCompra().getId();
            Compra vendaAgrupada;

            // 1. Verifica se já adicionamos esta Compra ao nosso mapa
            if (vendasMap.containsKey(vendaId)) {
                // Se sim, apenas a pegamos do mapa
                vendaAgrupada = vendasMap.get(vendaId);
            } else {
                // Se não, é a primeira parcela que vemos desta venda
                vendaAgrupada = conta.getCompra();
                // Preparamos a lista de parcelas para receber apenas as do filtro
                vendaAgrupada.setParcelas(new ArrayList<ContasPagar>()); // Use o seu método set, ex: setItensCompras
                // E adicionamos a nova Compra ao mapa
                vendasMap.put(vendaId, vendaAgrupada);
            }

            // 2. Adiciona a parcela atual à lista de parcelas da Compra correta
            vendaAgrupada.getParcelas().add(conta); // Use o seu método get, ex: getItensCompras
        }
        
        this.listaComprasAgrupadas = new ArrayList<>(vendasMap.values());
    }

    public void limparFiltros() {
        dataFiltroVencimento = null;
        mostraTodasContas = false;
        nomeClienteFiltro = null;
        buscarContas();
    }

    private void calcularTotais() {
        totalEmAberto = BigDecimal.ZERO;
        totalRecebido = BigDecimal.ZERO;

        if (listaContas != null) {
            for (ContasPagar conta : listaContas) {
                if (conta.getDataRecebimento() == null) {
                    totalEmAberto = totalEmAberto.add(conta.getValor());
                } else {
                    totalRecebido = totalRecebido.add(conta.getValor());
                }
            }
        }
        totalGeral = totalEmAberto.add(totalRecebido);
    }

    public void registrarRecebimento() {
        if (contaSelecionada == null || contaSelecionada.getId() == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atenção", "Selecione uma conta para registrar o recebimento!"));
            return;
        }

        if (contaSelecionada.getDataRecebimento() != null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atenção", "Esta conta já foi recebida!"));
            return;
        }

        if (contaSelecionada.getCompra() != null && contaSelecionada.getParcela() > 1) {
            boolean existeAnterior = contasReceberFacade.existeParcelaAnteriorEmAberto(contaSelecionada);
            if (existeAnterior) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Atenção", 
                        "Não é possível quitar esta parcela, pois existem parcelas anteriores em aberto para a mesma venda."));
                return;
            }
        }

        contaSelecionada.setDataRecebimento(new Date());

        try {
            contasReceberFacade.salvar(contaSelecionada);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Conta Nº " + contaSelecionada.getId() + " recebida com sucesso!"));

            buscarContas(); 
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Erro ao registrar recebimento: " + e.getMessage()));
        }
    }

    // ================== GETTERS E SETTERS ==================

    public List<Compra> getListaComprasAgrupadas() {
        return listaComprasAgrupadas;
    }
    
    public String getNomeClienteFiltro() {
        return nomeClienteFiltro;
    }

    public void setNomeClienteFiltro(String nomeClienteFiltro) {
        this.nomeClienteFiltro = nomeClienteFiltro;
    }

    public BigDecimal getTotalEmAberto() {
        return totalEmAberto;
    }

    public BigDecimal getTotalRecebido() {
        return totalRecebido;
    }

    public BigDecimal getTotalGeral() {
        return totalGeral;
    }

    public ContasPagar getContaSelecionada() {
        return contaSelecionada;
    }

    public void setContaSelecionada(ContasPagar contaSelecionada) {
        this.contaSelecionada = contaSelecionada;
    }

    public List<ContasPagar> getListaContas() {
        return listaContas;
    }

    public void setListaContas(List<ContasPagar> listaContas) {
        this.listaContas = listaContas;
    }

    public Date getDataFiltroVencimento() {
        return dataFiltroVencimento;
    }

    public void setDataFiltroVencimento(Date dataFiltroVencimento) {
        this.dataFiltroVencimento = dataFiltroVencimento;
    }

    public boolean isMostraTodasContas() {
        return mostraTodasContas;
    }

    public void setMostraTodasContas(boolean mostraTodasContas) {
        this.mostraTodasContas = mostraTodasContas;
    }
}