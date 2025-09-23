package Controle;

import Converter.CidadeConverter;
import Entidade.Cidade;
import Entidade.Cliente;
import Facade.CidadeFacade;
import Facade.ClienteFacade;
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
public class ClienteControle implements Serializable {

    private Cliente cliente = new Cliente();
    private Cliente clienteSelecionado;
    @EJB
    private ClienteFacade clientefacade;
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

    public Cliente getClienteSelecionado() {
        return clienteSelecionado;
    }

    public void setClienteSelecionado(Cliente clienteSelecionado) {
        this.clienteSelecionado = clienteSelecionado;
    }

    public void salvar() {
        clientefacade.salvar(cliente);
        cliente = new Cliente();
    }

    public void remover() {
        //verifica se tem um cliente selecionado
        if (clienteSelecionado != null) {
            System.out.println("Cliente a ser removido: " + clienteSelecionado.getNome()); // verifique se o cliente está correto
            clientefacade.remover(clienteSelecionado); //chama método remover
            //limpar a seleção após remoção
            clienteSelecionado = null;
        }
    }

    public void novo() {
        cliente = new Cliente();
    }

    public String editar(Cliente cli) {
        this.cliente = cli;
        return "index";
    }

    public void consultar(Cliente cli) {
        this.cliente = cli;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public List<Cliente> getListCliente() {
        return clientefacade.listaTodos();
    }

    public Converter getClienteConverter() {
        return new Converter() {
            @Override
            public Object getAsObject(FacesContext fc, UIComponent component, String value) {
                System.out.println("ClienteConverter getAsObject chamado com value: " + value);
                if (value == null || value.isEmpty()) {
                    return null;
                }
                Cliente cliente = clientefacade.buscarPorId(Long.valueOf(value));
                System.out.println("Cliente encontrado: " + (cliente != null ? cliente.getNome() : "null"));
                return cliente;
            }

            @Override
            public String getAsString(FacesContext fc, UIComponent component, Object object) {
                if (object == null) {
                    return "";
                }
                if (object instanceof Cliente) {
                    Cliente c = (Cliente) object;
                    return c.getId() != null ? c.getId().toString() : "";
                }
                return "";
            }
        };
    }

}
