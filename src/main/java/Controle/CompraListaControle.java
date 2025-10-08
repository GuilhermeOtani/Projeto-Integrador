package Controle;

import Entidade.Compra;
import Entidade.ItemCompra;
import Entidade.Produto;
import Facade.CompraFacade;
import Facade.ProdutoFacade;
import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.faces.context.FacesContext;

@Named
@ViewScoped
public class CompraListaControle implements Serializable {

    private List<Compra> listaCompras;
    private Compra compraSelecionada = new Compra(); // inicializa para o dialog

    @EJB
    private CompraFacade compraFacade;
    
   @EJB
   private ProdutoFacade produtoFacade;

    @PostConstruct
    public void init() {
        listaCompras = compraFacade.findAll(); // já traz todos os itens e produtos
    }

    public void carregarCompras() {
        listaCompras = compraFacade.listaTodos();
    }

    public void selecionarCompra(Compra compra) {
        this.compraSelecionada = compra;
    }

    public void excluirCompra(Compra compra) {
        try {
            //reverte estoque antes de excluir 
            for (ItemCompra item : compra.getItemCompras()) {
                Produto produto = item.getProduto();
                if (produto != null) {
                    produto.setEstoque(produto.getEstoque()- item.getQuantidade());
                    produtoFacade.edit(produto);
                }
            }

            //agora remove a compra
            compraFacade.remover(compra);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Compra excluída e estoque revertido!"));

            carregarCompras();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Compra foi possível excluir a venda."));
            e.printStackTrace();
        }
    }

    // Getters e Setters
    public List<Compra> getListaCompras() {
        return listaCompras;
    }

    public void setListaCompras(List<Compra> listaCompras) {
        this.listaCompras = listaCompras;
    }

    public Compra getCompraSelecionada() {
        return compraSelecionada;
    }

    public void setCompraSelecionada(Compra compraSelecionada) {
        this.compraSelecionada = compraSelecionada;
    }
}
