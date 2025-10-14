package Controle;

import Entidade.Fornecedor;
import Entidade.ItemCompra;
import Entidade.Produto;
import Entidade.Compra;
import Entidade.ContasPagar;
import Facade.FornecedorFacade;
import Facade.ProdutoFacade;
import Facade.CompraFacade;
import Facade.ContasPagarFacade;
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
public class CompraControle implements Serializable {

    private Compra compra;
    private ItemCompra itemCompra;
    private Produto produtoSelecionado;
    private Fornecedor fornecedorSelecionado;
    private Date dataPrimeiroVencimento;

    @EJB
    private transient ProdutoFacade produtoFacade;

    @EJB
    private transient FornecedorFacade fornecedorFacade;

    @EJB
    private transient CompraFacade compraFacade;

    @EJB
    private transient ContasPagarFacade contasPagarFacade;

    //maneira que achei para a tela nao travar quando der f5, mudar para viewscoped tambem ajudou
    @PostConstruct
    public void init() {
        compra = new Compra();
        itemCompra = new ItemCompra();
        produtoSelecionado = null;
        fornecedorSelecionado = null;
        dataPrimeiroVencimento = new Date();

    }

    public List<Produto> getListaProdutos() {
        return produtoFacade.listaTodos();
    }

    public List<Fornecedor> getListaFornecedores() {
        return fornecedorFacade.listaTodos();
    }

    public void adicionarItem() {
        if (fornecedorSelecionado == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atenção", "Selecione um fornecedor!"));
            return;
        }
        compra.setFornecedor(fornecedorSelecionado); // Garante que a compra saiba quem é o fornecedor

        if (produtoSelecionado == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atenção", "Selecione um produto!"));
            return;
        }

        if (itemCompra.getQuantidade() <= 0) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atenção", "Quantidade inválida!"));
            return;
        }

        // define fornecedor relacionado na compra
        compra.setFornecedor(fornecedorSelecionado);

        boolean produtoExistente = false;
        for (ItemCompra iv : compra.getItemCompras()) {
            if (iv.getProduto().getId().equals(produtoSelecionado.getId())) {
                iv.setQuantidade(iv.getQuantidade() + itemCompra.getQuantidade());
                iv.setPreco(produtoSelecionado.getValor());
                produtoExistente = true;
                break;
            }
        }

        if (!produtoExistente) {
            ItemCompra novoItem = new ItemCompra();
            novoItem.setProduto(produtoSelecionado);
            novoItem.setQuantidade(itemCompra.getQuantidade());
            novoItem.setPreco(produtoSelecionado.getValor());
            novoItem.setCompra(compra);
            compra.getItemCompras().add(novoItem);
        }

        compra.setValorTotal(compra.calcularValorTotal());
        // limpa seleção
        itemCompra = new ItemCompra();
        produtoSelecionado = null;
    }

    public void removerItem(ItemCompra iv) {
        compra.getItemCompras().remove(iv);
        compra.setValorTotal(compra.calcularValorTotal());

    }

    public void finalizarCompra() throws IOException {
//METODO DE VALIDAÇÃO PARA NAO DEIXAR O USUARIO FINALIZAR A VENDA SEM SEELCIONAR UM CLIENTE E PRODUTO
        if (compra.getFornecedor() == null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atenção", "Selecione um fornecedor antes de finalizar a venda"));
            return;
        }

        if (compra.getItemCompras() == null || compra.getItemCompras().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atenção", "Adicione pelo menos um produto antes de finalizar a compra"));
            return;
        }

        if (compra.getFormaPagamento() == null || compra.getFormaPagamento().trim().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atenção", "Selecione uma forma de pagamento!"));
            return;
        }

        compra.setValorTotal(compra.calcularValorTotal());
        Compra compraSalva = compraFacade.salvarERetornar(compra);

        // Atualizar estoque
        for (ItemCompra iv : compra.getItemCompras()) {
            Produto p = iv.getProduto();
            p.setEstoque(p.getEstoque() + iv.getQuantidade());
            produtoFacade.salvar(p);
        }
        if ("A Prazo".equalsIgnoreCase(compraSalva.getFormaPagamento())) {
            // Passamos a 'vendaSalva' (que tem ID) como parâmetro
            gerarParcelas(compraSalva);
        }
        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Compra finalizada!"));

        context.getExternalContext().getFlash().setKeepMessages(true);

        context.getExternalContext().redirect(
                context.getExternalContext().getRequestContextPath() + "/Compra/compra.xhtml?faces-redirect=true");
    }

    private void gerarParcelas(Compra compraSalva) {
        BigDecimal valorTotal = compraSalva.getValorTotal();
        Integer numParcelas = compraSalva.getNumeroParcelas();

        if (numParcelas == null || numParcelas <= 0) {
            numParcelas = 1;
        }

        BigDecimal valorParcela = valorTotal.divide(new BigDecimal(numParcelas), 2, RoundingMode.HALF_UP);
        BigDecimal somaDasParcelas = BigDecimal.ZERO;

        Calendar cal = Calendar.getInstance();
        cal.setTime(dataPrimeiroVencimento);

        for (int i = 1; i <= numParcelas; i++) {
            ContasPagar cp = new ContasPagar();

            // Usando a 'vendaSalva', que já está sincronizada com o banco
            cp.setCompra(compraSalva);
            cp.setFornecedor(compraSalva.getFornecedor());
            cp.setDataLancamento(new Date());
            cp.setParcela(i);
            cp.setFormaPagamento(compraSalva.getFormaPagamento());
            cp.setObservacao("Ref. Compra Nº: " + compraSalva.getId());

            if (i > 1) {
                cal.add(Calendar.MONTH, 1);
            }
            cp.setDataVencimento(cal.getTime());

            if (i == numParcelas) {
                BigDecimal valorUltimaParcela = valorTotal.subtract(somaDasParcelas);
                cp.setValor(valorUltimaParcela);
            } else {
                cp.setValor(valorParcela);
                somaDasParcelas = somaDasParcelas.add(valorParcela);
            }

            contasPagarFacade.salvar(cp);
        }
    }

    public Compra getCompra() {
        return compra;
    }

    public void setCompra(Compra compra) {
        this.compra = compra;
    }

    public ItemCompra getItemCompra() {
        return itemCompra;
    }

    public void setItemCompra(ItemCompra itemCompra) {
        this.itemCompra = itemCompra;
    }

    public Produto getProdutoSelecionado() {
        return produtoSelecionado;
    }

    public void setProdutoSelecionado(Produto produtoSelecionado) {
        this.produtoSelecionado = produtoSelecionado;
    }

    public Fornecedor getFornecedorSelecionado() {
        return fornecedorSelecionado;
    }

    public void setFornecedorSelecionado(Fornecedor fornecedorSelecionado) {
        this.fornecedorSelecionado = fornecedorSelecionado;
    }

    public Date getDataPrimeiroVencimento() {
        return dataPrimeiroVencimento;
    }

    public void setDataPrimeiroVencimento(Date dataPrimeiroVencimento) {
        this.dataPrimeiroVencimento = dataPrimeiroVencimento;
    }

    public ProdutoFacade getProdutoFacade() {
        return produtoFacade;
    }

    public void setProdutoFacade(ProdutoFacade produtoFacade) {
        this.produtoFacade = produtoFacade;
    }

    public FornecedorFacade getFornecedorFacade() {
        return fornecedorFacade;
    }

    public void setFornecedorFacade(FornecedorFacade fornecedorFacade) {
        this.fornecedorFacade = fornecedorFacade;
    }

    public CompraFacade getCompraFacade() {
        return compraFacade;
    }

    public void setCompraFacade(CompraFacade compraFacade) {
        this.compraFacade = compraFacade;
    }

    public ContasPagarFacade getContasPagarFacade() {
        return contasPagarFacade;
    }

    public void setContasPagarFacade(ContasPagarFacade contasPagarFacade) {
        this.contasPagarFacade = contasPagarFacade;
    }
    

}
