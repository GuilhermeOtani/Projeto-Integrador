package Controle;

import Converter.FornecedorConverter;
import Entidade.Fornecedor;
import Entidade.Produto;
import Facade.FornecedorFacade;
import Facade.ProdutoFacade;
import java.io.Serializable;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

@ManagedBean
@SessionScoped
public class ProdutoControle implements Serializable {

    private Produto produto = new Produto();
    private Produto produtoSelecionado;
    @EJB
    private ProdutoFacade produtofacade;
    @EJB
    private FornecedorFacade fornecedorFacade;
    private FornecedorConverter fornecedorConverter;

    public List<Fornecedor> getListaFornecedor() {
        return fornecedorFacade.listaTodos();
    }

    public FornecedorConverter getFornecedorConverter() {
        if (fornecedorConverter == null) {
            fornecedorConverter = new FornecedorConverter(fornecedorFacade);
        }
        return fornecedorConverter;
    }

    public void setFornecedorConverter(FornecedorConverter fornecedorConverter) {
        this.fornecedorConverter = fornecedorConverter;
    }

    public void salvar() {
        produtofacade.salvar(produto);
        produto = new Produto();
    }

    public void remover() {
        if (produtoSelecionado != null) {
            System.out.println("Produto a ser removido: " + produtoSelecionado.getNome()); // Verifique se o produto está correto
            produtofacade.remover(produtoSelecionado);
            //limpar a seleção após remoção
            produtoSelecionado = null;
        } else {
            System.out.println("Nenhum produto selecionado para exclusão.");
        }
    }

    public void editar(Produto pro) {
        this.produto = pro;
    }

    public void novo() {
        produto = new Produto();
    }

    public Produto getProduto() {
        return produto;
    }

    public void setProduto(Produto produto) {
        this.produto = produto;
    }

    public Produto getProdutoSelecionado() {
        return produtoSelecionado;
    }

    public void setProdutoSelecionado(Produto produtoSelecionado) {
        this.produtoSelecionado = produtoSelecionado;
    }

    public List<Produto> getListaProduto() {
        return produtofacade.listaTodos();

    }

    public void carregarProdutoSelecionado() {
        if (produtoSelecionado != null && produtoSelecionado.getId() != null) {
            produtoSelecionado = produtofacade.buscarPorId(produtoSelecionado.getId());
            System.out.println("Produto selecionado: " + produtoSelecionado.getNome() + ", preço: " + produtoSelecionado.getValor());
        } else {
            System.out.println("Nenhum produto selecionado");
        }
    }

    public Converter getProdutoConverter() {
        return new Converter() {
            @Override
            public Object getAsObject(FacesContext fc, UIComponent component, String value) {
                if (value == null || value.isEmpty()) {
                    return null;
                }
                return produtofacade.buscarPorId(Long.valueOf(value));
            }

            @Override
            public String getAsString(FacesContext fc, UIComponent component, Object object) {
                if (object == null) {
                    return "";
                }
                if (object instanceof Produto) {
                    Produto p = (Produto) object;
                    return p.getId() != null ? p.getId().toString() : "";
                }
                return "";
            }
        };
    }

}
