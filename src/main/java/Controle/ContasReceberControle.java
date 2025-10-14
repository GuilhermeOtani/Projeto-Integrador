package Controle;

import Entidade.ContasReceber;
import Entidade.Venda;
import Facade.ContasReceberFacade;
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
public class ContasReceberControle implements Serializable {

    private ContasReceber contaSelecionada;
    private List<ContasReceber> listaContas;
    private List<Venda> listaVendasAgrupadas;

    private Date dataFiltroVencimento;
    private boolean mostraTodasContas = false;
    private String nomeClienteFiltro;
    private BigDecimal totalEmAberto;
    private BigDecimal totalRecebido;
    private BigDecimal totalGeral;

    @EJB
    private transient ContasReceberFacade contasReceberFacade;

    @PostConstruct
    public void init() {
        buscarContas();
    }

    public void buscarContas() {
        boolean somenteEmAberto = !mostraTodasContas;
        listaContas = contasReceberFacade.buscar(somenteEmAberto, dataFiltroVencimento, nomeClienteFiltro);
        calcularTotais();
        agruparContasPorVenda(); // A chamada permanece aqui
    }

    // ======================================================================================
    // MÉTODO CORRIGIDO PARA SER COMPATÍVEL COM VERSÕES ANTIGAS DO JAVA
    // ======================================================================================
    private void agruparContasPorVenda() {
        if (listaContas == null || listaContas.isEmpty()) {
            listaVendasAgrupadas = new ArrayList<>();
            return;
        }

        Map<Long, Venda> vendasMap = new LinkedHashMap<>();

        for (ContasReceber conta : listaContas) {
            if (conta.getVenda() == null) {
                continue; // Pula contas que por algum motivo não têm venda
            }

            Long vendaId = conta.getVenda().getId();
            Venda vendaAgrupada;

            // 1. Verifica se já adicionamos esta Venda ao nosso mapa
            if (vendasMap.containsKey(vendaId)) {
                // Se sim, apenas a pegamos do mapa
                vendaAgrupada = vendasMap.get(vendaId);
            } else {
                // Se não, é a primeira parcela que vemos desta venda
                vendaAgrupada = conta.getVenda();
                // Preparamos a lista de parcelas para receber apenas as do filtro
                vendaAgrupada.setParcelas(new ArrayList<ContasReceber>()); // Use o seu método set, ex: setItensVendas
                // E adicionamos a nova Venda ao mapa
                vendasMap.put(vendaId, vendaAgrupada);
            }

            // 2. Adiciona a parcela atual à lista de parcelas da Venda correta
            vendaAgrupada.getParcelas().add(conta); // Use o seu método get, ex: getItensVendas
        }

        this.listaVendasAgrupadas = new ArrayList<>(vendasMap.values());
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
            for (ContasReceber conta : listaContas) {
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

        if (contaSelecionada.getVenda() != null && contaSelecionada.getParcela() > 1) {
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

    public String getStatus(ContasReceber conta) {
        if (conta == null) {
            return ""; // Retorna vazio se o objeto for nulo por algum motivo
        }
        if (conta.getDataRecebimento() != null) {
            return "RECEBIDA";
        }
        // Verifica se a data de vencimento não é nula e se a data atual é posterior a ela
        if (conta.getDataVencimento() != null && new Date().after(conta.getDataVencimento())) {
            return "VENCIDA";
        }
        return "ABERTA";
    }

    /**
     * Método público que retorna a classe CSS correspondente ao status da
     * conta.
     *
     * @param conta A instância de ContasReceber da linha da tabela.
     * @return A String com as classes CSS para o badge de status.
     */
    public String getStatusClass(ContasReceber conta) {
        if (conta == null) {
            return "data-badge";
        }
        // Reutiliza o método getStatus para não repetir a lógica
        switch (getStatus(conta)) {
            case "RECEBIDA":
                return "data-badge badge-success";
            case "VENCIDA":
                return "data-badge badge-danger";
            case "ABERTA":
            default:
                return "data-badge badge-warning"; // Usando a classe de alerta
        }
    }
    // ================== GETTERS E SETTERS ==================

    public List<Venda> getListaVendasAgrupadas() {
        return listaVendasAgrupadas;
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

    public ContasReceber getContaSelecionada() {
        return contaSelecionada;
    }

    public void setContaSelecionada(ContasReceber contaSelecionada) {
        this.contaSelecionada = contaSelecionada;
    }

    public List<ContasReceber> getListaContas() {
        return listaContas;
    }

    public void setListaContas(List<ContasReceber> listaContas) {
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
