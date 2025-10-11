package Controle;

import Entidade.ContasReceber;
import Facade.ContasReceberFacade;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
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

    // ALTERADO: Método com a nova verificação
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

        // =====================================================================
        // NOVO: Verificação de parcelas anteriores em aberto
        // =====================================================================
        // Só executa a verificação se a conta for proveniente de uma venda parcelada
        if (contaSelecionada.getVenda() != null && contaSelecionada.getParcela() > 1) {
            boolean existeAnterior = contasReceberFacade.existeParcelaAnteriorEmAberto(contaSelecionada);
            if (existeAnterior) {
                FacesContext.getCurrentInstance().addMessage(null,
                        new FacesMessage(FacesMessage.SEVERITY_WARN, "Atenção", 
                        "Não é possível quitar esta parcela, pois existem parcelas anteriores em aberto para a mesma venda."));
                return;
            }
        }
        // =====================================================================

        contaSelecionada.setDataRecebimento(new Date());

        try {
            contasReceberFacade.salvar(contaSelecionada);
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Conta Nº " + contaSelecionada.getId() + " recebida com sucesso!"));

            // Atualiza a lista e os totais
            buscarContas(); 
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Erro ao registrar recebimento: " + e.getMessage()));
        }
    }

    // Getters e Setters (sem alterações)

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