package Controle;

import Entidade.ItemVenda;
import Entidade.Produto;
import Entidade.Venda;
import Facade.ProdutoFacade;
import Facade.VendaFacade;
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
public class VendaListaControle implements Serializable {

    private List<Venda> listaVendas;
    private Venda vendaSelecionada = new Venda(); // inicializa para o dialog

    @EJB
    private VendaFacade vendaFacade;
    
    @EJB    
    private ProdutoFacade produtoFacade;

    @PostConstruct
    public void init() {
        listaVendas = vendaFacade.findAll(); // já traz todos os itens e produtos
    }

    public void carregarVendas() {
        listaVendas = vendaFacade.listaTodos();
    }

    public void selecionarVenda(Venda venda) {
        this.vendaSelecionada = venda;
    }

    public void excluirVenda(Venda venda) {
        try {
            //reverte o estoque antes da exclusao
            for (ItemVenda item : venda.getItensVendas()) {
                Produto produto = item.getProduto();
                if (produto != null) {
                    produto.setEstoque(produto.getEstoque()+ item.getQuantidade());
                    produtoFacade.edit(produto);
                }
            }

            //agora sim remove a venda
            vendaFacade.remover(venda);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Venda excluída e estoque revertido!"));

            carregarVendas();
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Não foi possível excluir a venda."));
            e.printStackTrace();
        }
    }

    // Getters e Setters
    public List<Venda> getListaVendas() {
        return listaVendas;
    }

    public void setListaVendas(List<Venda> listaVendas) {
        this.listaVendas = listaVendas;
    }

    public Venda getVendaSelecionada() {
        return vendaSelecionada;
    }

    public void setVendaSelecionada(Venda vendaSelecionada) {
        this.vendaSelecionada = vendaSelecionada;
    }
}
