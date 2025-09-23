package Controle;

import Converter.CidadeConverter;
import Converter.FornecedorConverter;
import Entidade.Cidade;
import Entidade.Fornecedor;
import Entidade.Fornecedor;
import Facade.CidadeFacade;
import Facade.FornecedorFacade;
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
        System.out.println("fornecedorCUUUUUUUUUUUUUUUU" + fornecedor.getNome() + fornecedor.getCnpj());
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

    public Converter getFornecedorConverter() {
        return new Converter() {
            @Override
            public Object getAsObject(FacesContext fc, UIComponent component, String value) {
                System.out.println("FornecedorConverter getAsObject chamado com value: " + value);
                if (value == null || value.isEmpty()) {
                    return null;
                }
                Fornecedor fornecedor = fornecedorfacade.buscarPorId(Long.valueOf(value));
                System.out.println("Fornecedor encontrado: " + (fornecedor != null ? fornecedor.getNome() : "null"));
                return fornecedor;
            }

            @Override
            public String getAsString(FacesContext fc, UIComponent component, Object object) {
                if (object == null) {
                    return "";
                }
                if (object instanceof Fornecedor) {
                    Fornecedor f = (Fornecedor) object;
                    return f.getId() != null ? f.getId().toString() : "";
                }
                return "";
            }
        };
    }

}
