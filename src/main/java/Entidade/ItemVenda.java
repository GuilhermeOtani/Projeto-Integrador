package Entidade;


import Facade.ProdutoFacade;
import Facade.VendaFacade;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;

@ManagedBean
@SessionScoped
public class ItemVenda implements Serializable {

    private List<Venda> itens = new ArrayList<>();
    private Cliente clienteSelecionado;

    @EJB
    private VendaFacade vendaFacade;

    @EJB
    private ProdutoFacade produtoFacade;

    public void adicionar(Produto produto, int quantidade) {
    // Verifica se há estoque disponível
    if (produto.getEstoque() < quantidade) {
        FacesContext.getCurrentInstance().addMessage(null, 
            new FacesMessage(FacesMessage.SEVERITY_WARN,
                             "Estoque insuficiente",
                             "Produto: " + produto.getNome() + " possui apenas " + produto.getEstoque() + " unidades."));
        return;
    }

    for (Venda item : itens) {
        if (item.getProduto().equals(produto)) {
            int novaQuantidade = item.getQuantidade() + quantidade;

            if (produto.getEstoque() < novaQuantidade) {
                FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN,
                                     "Estoque insuficiente",
                                     "Produto: " + produto.getNome() + " possui apenas " + produto.getEstoque() + " unidades."));
                return;
            }

            item.setQuantidade(novaQuantidade);
            return;
        }
    }

    Venda novo = new Venda();
    novo.setProduto(produto);
    novo.setCliente(clienteSelecionado);
    novo.setQuantidade(quantidade);
    itens.add(novo);
}

    public void finalizarCompra() {
        for (Venda item : itens) {
            Produto produto = item.getProduto();
            int novoEstoque = produto.getEstoque() - item.getQuantidade();
            if (novoEstoque < 0) {
                throw new RuntimeException("Estoque insuficiente para: " + produto.getNome());
            }
            produto.setEstoque(novoEstoque);
            produtoFacade.salvar(produto); // atualiza estoque
            vendaFacade.salvar(item);   // salva item no banco 
        }
        itens.clear(); // limpa carrinho
    }

    public List<Venda> getItens() { return itens; }

    public Cliente getClienteSelecionado() { return clienteSelecionado; }
    public void setClienteSelecionado(Cliente clienteSelecionado) {
        this.clienteSelecionado = clienteSelecionado;
    }
    

    
}
