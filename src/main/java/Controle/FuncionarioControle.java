
package Controle;

import Converter.CidadeConverter;
import Entidade.Cidade;
import Entidade.Funcionario;
import Facade.CidadeFacade;
import Facade.FuncionarioFacade;
import java.io.Serializable;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

@ManagedBean
@SessionScoped
public class FuncionarioControle implements Serializable {

    private Funcionario funcionario = new Funcionario();
    private Funcionario funcionarioSelecionado;
    @EJB
    private FuncionarioFacade funcionariofacade;
    @EJB
    private CidadeFacade cidadeFacade;
    private CidadeConverter cidadeConverter;
    
    public List<Cidade> getListaCidades() {
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

    public Funcionario getFuncionarioSelecionado() {
        return funcionarioSelecionado;
    }

    public void setFuncionarioSelecionado(Funcionario funcionarioSelecionado) {
        this.funcionarioSelecionado = funcionarioSelecionado;
    }

    public void salvar() {
        funcionariofacade.salvar(funcionario);
        funcionario = new Funcionario();
    }

    public void remover() {
        if (funcionarioSelecionado != null) {
            System.out.println("Funcionario a ser removido: " + funcionarioSelecionado.getNome()); // Verifique se o produto está correto
            funcionariofacade.remover(funcionarioSelecionado);
            //limpar a seleção após remoção
            funcionarioSelecionado = null;
        }
    }

    public void novo() {
        funcionario = new Funcionario();
    }

    public String editar(Funcionario func) {
        this.funcionario = func;
        return "index";
    }

    public void consultar(Funcionario func) {
        this.funcionario = func;
    }

    public Funcionario getFuncionario() {
        return funcionario;
    }

    public void setFuncionario(Funcionario funcionario) {
        this.funcionario = funcionario;
    }

    public List<Funcionario> getListFuncionario() {
        return funcionariofacade.listaTodos();
    }

    
}
