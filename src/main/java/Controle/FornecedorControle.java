package Controle;

import Converter.CidadeConverter;
import Converter.FornecedorConverter;
import Entidade.Cidade;
import Entidade.Fornecedor;
import Facade.CidadeFacade;
import Facade.FornecedorFacade;
import java.io.Serializable;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

@ManagedBean
@SessionScoped
public class FornecedorControle implements Serializable {

    private Fornecedor fornecedor = new Fornecedor();
    private Fornecedor fornecedorSelecionado;
    @EJB
    private FornecedorFacade fornecedorfacade;
    @EJB
    private CidadeFacade cidadeFacade;
    private CidadeConverter cidadeConverter;

    public List<Cidade> getListaCidade() {
        return cidadeFacade.listaTodos();
    }

    public CidadeConverter getCidadeConverter() {
        if (cidadeConverter == null) {
            cidadeConverter = new CidadeConverter(cidadeFacade);
        }
        return cidadeConverter;
    }

    public void setCidadeConverter(CidadeConverter cidadeConverter) {
        this.cidadeConverter = cidadeConverter;
    }

    
    public void salvar() {
       fornecedorfacade.salvar(fornecedor);
        fornecedor = new Fornecedor();
    }

    public void remover() {
        if (fornecedorSelecionado != null) {
            System.out.println("Fornecedor a ser removido: " + fornecedorSelecionado.getNome()); // Verifique se o fornecedor está correto
            fornecedorfacade.remover(fornecedorSelecionado);
            //limpar a seleção após remoção
            fornecedorSelecionado = null;
        } else {
            System.out.println("Nenhum fornecedor selecionado para exclusão."); //apenas para testar
        }
    }

    public void editar(Fornecedor forn) {
        this.fornecedor = forn;
    }

    public void novo() {
        fornecedor = new Fornecedor();
    }

    public Fornecedor getFornecedor() {
        return fornecedor;
    }

    public void setFornecedor(Fornecedor fornecedor) {
        this.fornecedor = fornecedor;
    }

    public Fornecedor getFornecedorSelecionado() {
        return fornecedorSelecionado;
    }

    public void setFornecedorSelecionado(Fornecedor fornecedorSelecionado) {
        this.fornecedorSelecionado = fornecedorSelecionado;
    }

    public List<Fornecedor> getListaFornecedor() {
        return fornecedorfacade.listaTodos();

    }
}