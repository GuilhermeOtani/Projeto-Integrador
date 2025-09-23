package Controle;

import Entidade.Cliente;
import Entidade.ItemVenda;
import Entidade.Produto;
import Entidade.Venda;
import Facade.ClienteFacade;
import Facade.ProdutoFacade;
import Facade.VendaFacade;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.view.ViewScoped;
import javax.faces.context.FacesContext;
import javax.inject.Named;

@Named
@ViewScoped
public class VendaControle implements Serializable {

    private Venda venda = new Venda();
    private ItemVenda itemVenda = new ItemVenda();
    private Produto produtoSelecionado;
    private Cliente clienteSelecionado;

    @EJB
    private ProdutoFacade produtoFacade;

    @EJB
    private ClienteFacade clienteFacade;

    @EJB
    private VendaFacade vendaFacade;

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
        venda.setCliente(clienteSelecionado); // Garante que a venda saiba quem é o cliente

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

        // define cliente relacionado na venda
        venda.setCliente(clienteSelecionado);

        // verifica estoque pra adicionar item
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

        // limpa seleção
        itemVenda = new ItemVenda();
        produtoSelecionado = null;
    }

    public void removerItem(ItemVenda iv) {
        venda.getItensVendas().remove(iv);
    }

    public void finalizarVenda() throws IOException {
        
        
        if (venda.getCliente() == null) {
            FacesContext.getCurrentInstance().addMessage(null,
           new FacesMessage(FacesMessage.SEVERITY_WARN, "Atenção", "Selecione um cliente antes de finalizar a venda")); 
            return;
        }
        
        if (venda.getItensVendas() == null ||  venda.getItensVendas().isEmpty()) {
            FacesContext.getCurrentInstance().addMessage(null,
           new FacesMessage(FacesMessage.SEVERITY_WARN, "Atenção", "Adicione pelo menos um produto antes de finalizar a venda")); 
            return;
        }
        
        
        //chamando o metodo da entidade, forma que resolvi pra parar ed dar null no valortotal no banco de dados
        venda.setValorTotal(venda.calcularValorTotal());
        vendaFacade.salvar(venda);

        // atualizar estoque
        for (ItemVenda iv : venda.getItensVendas()) {
            Produto p = iv.getProduto();
            p.setEstoque(p.getEstoque() - iv.getQuantidade());
            produtoFacade.salvar(p);
        }

        FacesContext context = FacesContext.getCurrentInstance();
        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Venda finalizada!"));

        context.getExternalContext().getFlash().setKeepMessages(true);

        context.getExternalContext().redirect(
                context.getExternalContext().getRequestContextPath() + "/Venda/venda.xhtml?faces-redirect=true");

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

    //maneira que achei para a tela nao travar quando der f5, mudar para viewscoped tambem ajudou
    @PostConstruct
    public void init() {
        venda = new Venda();
        itemVenda = new ItemVenda();
        produtoSelecionado = null;
        clienteSelecionado = null;
    }

}
