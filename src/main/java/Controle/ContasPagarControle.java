package Controle;

import Entidade.ContasPagar;
import Entidade.Compra;
import Entidade.ContasReceber;
import Facade.ContasPagarFacade;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
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
    private String nomeFornecedorFiltro;
    private BigDecimal totalEmAberto;
    private BigDecimal totalPagado;
    private BigDecimal totalGeral;

    @EJB
    private transient ContasPagarFacade contasPagarFacade;

    @PostConstruct
    public void init() {
        buscarContas();
    }

    public void buscarContas() {
        boolean somenteEmAberto = !mostraTodasContas;
        listaContas = contasPagarFacade.buscar(somenteEmAberto, dataFiltroVencimento, nomeFornecedorFiltro);
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

        Map<Long, Compra> comprasMap = new LinkedHashMap<>();

        for (ContasPagar conta : listaContas) {
            if (conta.getCompra() == null) {
                continue; // Pula contas que por algum motivo não têm compra
            }

            Long compraId = conta.getCompra().getId();
            Compra compraAgrupada;

            // 1. Verifica se já adicionamos esta Compra ao nosso mapa
            if (comprasMap.containsKey(compraId)) {
                // Se sim, apenas a pegamos do mapa
                compraAgrupada = comprasMap.get(compraId);
            } else {
                // Se não, é a primeira parcela que vemos desta compra
                compraAgrupada = conta.getCompra();
                // Preparamos a lista de parcelas para pagar apenas as do filtro
                compraAgrupada.setParcelas(new ArrayList<ContasPagar>()); // Use o seu método set, ex: setItemCompras
                // E adicionamos a nova Compra ao mapa
                comprasMap.put(compraId, compraAgrupada);
            }

            // 2. Adiciona a parcela atual à lista de parcelas da Compra correta
            compraAgrupada.getParcelas().add(conta); // Use o seu método get, ex: getItemCompras
        }

        this.listaComprasAgrupadas = new ArrayList<>(comprasMap.values());
    }

    public void limparFiltros() {
        dataFiltroVencimento = null;
        mostraTodasContas = false;
        nomeFornecedorFiltro = null;
        buscarContas();
    }

    private void calcularTotais() {
        totalEmAberto = BigDecimal.ZERO;
        totalPagado = BigDecimal.ZERO;

        if (listaContas != null) {
            for (ContasPagar conta : listaContas) {
                if (conta.getDataPagamento() == null) {
                    totalEmAberto = totalEmAberto.add(conta.getValor());
                } else {
                    totalPagado = totalPagado.add(conta.getValor());
                }
            }
        }
        totalGeral = totalEmAberto.add(totalPagado);
    }

    public void registrarPagamento() {
        if (contaSelecionada == null || contaSelecionada.getId() == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atenção", "Selecione uma conta para registrar o pagamento!"));
            return;
        }

        if (contaSelecionada.getDataPagamento() != null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atenção", "Esta conta já foi recebida!"));
            return;
        }

        if (contaSelecionada.getCompra() != null && contaSelecionada.getParcela() > 1) {
            boolean existeAnterior = contasPagarFacade.existeParcelaAnteriorEmAberto(contaSelecionada);
            if (existeAnterior) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Atenção",
                                "Não é possível quitar esta parcela, pois existem parcelas anteriores em aberto para a mesma compra."));
                return;
            }
        }

        contaSelecionada.setDataPagamento(new Date());

        try {
            contasPagarFacade.salvar(contaSelecionada);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Conta Nº " + contaSelecionada.getId() + " recebida com sucesso!"));

            buscarContas();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Erro ao registrar pagamento: " + e.getMessage()));
        }
    }

    public String getStatus(ContasPagar conta) {
        if (conta == null) {
            return "";
        }
        if (conta.getDataPagamento() != null) {
            return "PAGADA";
        }

        if (conta.getDataVencimento() != null) {
            // Converte a data de vencimento para LocalDate (sem horas)
            LocalDate dataVencimentoSemHoras = conta.getDataVencimento().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDate();

            // Pega a data de hoje (também sem horas)
            LocalDate hoje = LocalDate.now();

            // Compara: a conta só está vencida se a data de hoje for ESTRITAMENTE POSTERIOR à data de vencimento.
            if (hoje.isAfter(dataVencimentoSemHoras)) {
                return "VENCIDA";
            }
        }

        return "ABERTA";
    }

//arrumar q o dia de hoje ja conta como vencida e fazer na compra tambem
    public String getStatusClass(ContasPagar conta) {
        if (conta == null) {
            return "data-badge";
        }
        // Reutiliza o método getStatus para não repetir a lógica
        switch (getStatus(conta)) {
            case "PAGADA":
                return "data-badge badge-success";
            case "VENCIDA":
                return "data-badge badge-danger";
            case "ABERTA":
            default:
                return "data-badge badge-warning"; // Usando a classe de alerta
        }
    }
    // ================== GETTERS E SETTERS ==================

    public List<Compra> getListaComprasAgrupadas() {
        return listaComprasAgrupadas;
    }

    public String getNomeFornecedorFiltro() {
        return nomeFornecedorFiltro;
    }

    public void setNomeFornecedorFiltro(String nomeFornecedorFiltro) {
        this.nomeFornecedorFiltro = nomeFornecedorFiltro;
    }

    public BigDecimal getTotalEmAberto() {
        return totalEmAberto;
    }

    public BigDecimal getTotalPagado() {
        return totalPagado;
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
