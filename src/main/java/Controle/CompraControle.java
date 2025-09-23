package Controle;

import Entidade.Fornecedor;
import Entidade.ItemCompra;
import Entidade.Produto;
import Entidade.Compra;
import Facade.FornecedorFacade;
import Facade.ProdutoFacade;
import Facade.CompraFacade;
import java.io.IOException;
import java.io.Serializable;
import java.math.BigDecimal;
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

    private Compra compra = new Compra();
    private ItemCompra itemCompra = new ItemCompra();
    private Produto produtoSelecionado;
    private Fornecedor fornecedorSelecionado;

    @EJB
    private ProdutoFacade produtoFacade;

    @EJB
    private FornecedorFacade fornecedorFacade;

    @EJB
    private CompraFacade compraFacade;

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

        // limpa seleção
        itemCompra = new ItemCompra();
        produtoSelecionado = null;
    }

    public void removerItem(ItemCompra iv) {
        compra.getItemCompras().remove(iv);
    }

    public void finalizarCompra() throws IOException {

        compra.setValorTotal(compra.calcularValorTotal());
        compraFacade.salvar(compra);

        // Atualizar estoque
        for (ItemCompra iv : compra.getItemCompras()) {
            Produto p = iv.getProduto();
            p.setEstoque(p.getEstoque() + iv.getQuantidade());
            produtoFacade.salvar(p);
        }

        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Compra finalizada!"));

        context.getExternalContext().getFlash().setKeepMessages(true);

        context.getExternalContext().redirect(
                context.getExternalContext().getRequestContextPath() + "/Compra/compra.xhtml?faces-redirect=true");
    }

    public BigDecimal getValorTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (ItemCompra iv : compra.getItemCompras()) {
            total = total.add(iv.getSubTotal());
        }
        return total;
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

    //maneira que achei para a tela nao travar quando der f5, mudar para viewscoped tambem ajudou
    @PostConstruct
    public void init() {
        compra = new Compra();
        itemCompra = new ItemCompra();
        produtoSelecionado = null;
        fornecedorSelecionado = null;
    }

}
