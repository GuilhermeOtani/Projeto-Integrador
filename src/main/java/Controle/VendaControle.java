package Controle;

import Entidade.Cliente;
import Entidade.ContasReceber;
import Entidade.ItemVenda;
import Entidade.Produto;
import Entidade.Venda;
import Facade.ClienteFacade;
import Facade.ContasReceberFacade;
import Facade.ProdutoFacade;
import Facade.VendaFacade;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Calendar;
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
public class VendaControle implements Serializable {

    private Venda venda;
    private ItemVenda itemVenda;
    private Produto produtoSelecionado;
    private Cliente clienteSelecionado;
    private Date dataPrimeiroVencimento;

    @EJB
    private transient ProdutoFacade produtoFacade; // Adicionado transient
    @EJB
    private transient ClienteFacade clienteFacade; // Adicionado transient
    @EJB
    private transient VendaFacade vendaFacade;     // Adicionado transient
    @EJB
    private transient ContasReceberFacade contasReceberFacade; // Adicionado transient

    @PostConstruct
    public void init() {
        venda = new Venda();
        itemVenda = new ItemVenda();
        produtoSelecionado = null;
        clienteSelecionado = null;
        dataPrimeiroVencimento = new Date();
    }

    // ... (Seus métodos getListaProdutos, getListaClientes, adicionarItem e removerItem continuam iguais) ...
    public List<Produto> getListaProdutos() {
        return produtoFacade.listaTodos();
    }

    public List<Cliente> getListaClientes() {
        return clienteFacade.listaTodos();
    }

    public void adicionarItem() {
        if (clienteSelecionado == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atenção", "Selecione um cliente!"));
            return;
        }
        venda.setCliente(clienteSelecionado);

        if (produtoSelecionado == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atenção", "Selecione um produto!"));
            return;
        }

        if (itemVenda.getQuantidade() <= 0) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atenção", "Quantidade inválida!"));
            return;
        }

        if (produtoSelecionado.getEstoque() < itemVenda.getQuantidade()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Estoque insuficiente!"));
            return;
        }

        boolean produtoExistente = false;
        for (ItemVenda iv : venda.getItensVendas()) {
            if (iv.getProduto().getId().equals(produtoSelecionado.getId())) {
                iv.setQuantidade(iv.getQuantidade() + itemVenda.getQuantidade());
                iv.setPreco(produtoSelecionado.getValor());
                produtoExistente = true;
                break;
            }
        }

        if (!produtoExistente) {
            ItemVenda novoItem = new ItemVenda();
            novoItem.setProduto(produtoSelecionado);
            novoItem.setQuantidade(itemVenda.getQuantidade());
            novoItem.setPreco(produtoSelecionado.getValor());
            novoItem.setVenda(venda);
            venda.getItensVendas().add(novoItem);
        }

        venda.setValorTotal(venda.calcularValorTotal());
        itemVenda = new ItemVenda();
        produtoSelecionado = null;
    }

    public void removerItem(ItemVenda iv) {
        venda.getItensVendas().remove(iv);
        venda.setValorTotal(venda.calcularValorTotal());
    }

    public void finalizarVenda() throws IOException {
        if (venda.getCliente() == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atenção", "Selecione um cliente antes de finalizar a venda"));
            return;
        }

        if (venda.getItensVendas() == null || venda.getItensVendas().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atenção", "Adicione pelo menos um produto antes de finalizar a venda"));
            return;
        }

        if (venda.getFormaPagamento() == null || venda.getFormaPagamento().trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atenção", "Selecione uma forma de pagamento!"));
            return;
        }

        venda.setValorTotal(venda.calcularValorTotal());
        Venda vendaSalva = vendaFacade.salvarERetornar(venda);

        // 2. Atualiza o estoque usando a instância correta
        for (ItemVenda iv : vendaSalva.getItensVendas()) {
            Produto p = iv.getProduto();
            p.setEstoque(p.getEstoque() - iv.getQuantidade());
            produtoFacade.salvar(p);
        }

        // 3. GERA AS CONTAS A RECEBER (SE NECESSÁRIO)
        if ("A Prazo".equalsIgnoreCase(vendaSalva.getFormaPagamento())) {
            // Passamos a 'vendaSalva' (que tem ID) como parâmetro
            gerarParcelas(vendaSalva);
        }

        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Venda finalizada!"));
        context.getExternalContext().getFlash().setKeepMessages(true);
        context.getExternalContext().redirect(
                context.getExternalContext().getRequestContextPath() + "/Venda/venda.xhtml?faces-redirect=true");
    }

    // ALTERADO: Método agora recebe a Venda como parâmetro
    private void gerarParcelas(Venda vendaSalva) {
        BigDecimal valorTotal = vendaSalva.getValorTotal();
        Integer numParcelas = vendaSalva.getNumeroParcelas();

        if (numParcelas == null || numParcelas <= 0) {
            numParcelas = 1;
        }

        BigDecimal valorParcela = valorTotal.divide(new BigDecimal(numParcelas), 2, RoundingMode.HALF_UP);
        BigDecimal somaDasParcelas = BigDecimal.ZERO;

        Calendar cal = Calendar.getInstance();
        cal.setTime(dataPrimeiroVencimento);

        for (int i = 1; i <= numParcelas; i++) {
            ContasReceber cr = new ContasReceber();

            // Usando a 'vendaSalva', que já está sincronizada com o banco
            cr.setVenda(vendaSalva);
            cr.setCliente(vendaSalva.getCliente());
            cr.setDataLancamento(new Date());
            cr.setParcela(i);
            cr.setFormaPagamento(vendaSalva.getFormaPagamento());
            cr.setObservacao("Ref. Venda Nº: " + vendaSalva.getId());

            if (i > 1) {
                cal.add(Calendar.MONTH, 1);
            }
            cr.setDataVencimento(cal.getTime());

            if (i == numParcelas) {
                BigDecimal valorUltimaParcela = valorTotal.subtract(somaDasParcelas);
                cr.setValor(valorUltimaParcela);
            } else {
                cr.setValor(valorParcela);
                somaDasParcelas = somaDasParcelas.add(valorParcela);
            }

            contasReceberFacade.salvar(cr);
        }
    }

    public Venda getVenda() {
        return venda;
    }

    public void setVenda(Venda venda) {
        this.venda = venda;
    }

    public ItemVenda getItemVenda() {
        return itemVenda;
    }

    public void setItemVenda(ItemVenda itemVenda) {
        this.itemVenda = itemVenda;
    }

    public Produto getProdutoSelecionado() {
        return produtoSelecionado;
    }

    public void setProdutoSelecionado(Produto produtoSelecionado) {
        this.produtoSelecionado = produtoSelecionado;
    }

    public Cliente getClienteSelecionado() {
        return clienteSelecionado;
    }

    public void setClienteSelecionado(Cliente clienteSelecionado) {
        this.clienteSelecionado = clienteSelecionado;
    }

    public Date getDataPrimeiroVencimento() {
        return dataPrimeiroVencimento;
    }

    public void setDataPrimeiroVencimento(Date dataPrimeiroVencimento) {
        this.dataPrimeiroVencimento = dataPrimeiroVencimento;
    }
}
