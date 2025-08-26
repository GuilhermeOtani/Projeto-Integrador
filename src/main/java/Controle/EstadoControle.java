package Controle;

import Entidade.Estado;
import Facade.EstadoFacade;
import java.io.Serializable;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

@ManagedBean
@SessionScoped
public class EstadoControle implements Serializable {

    private Estado estado = new Estado();
    private Estado estadoSelecionado;
    @EJB
    private EstadoFacade estadoFacade;

    public void salvar() {
        estadoFacade.salvar(estado);
        estado = new Estado();
    }
    public void remover() {
        if (estadoSelecionado != null) {
            System.out.println("Estado a ser removido: " + estadoSelecionado.getNome()); // Verifique se o produto está correto
            estadoFacade.remover(estadoSelecionado);
            //limpar a seleção após remoção
            estadoSelecionado = null;
        } else {
            System.out.println("Nenhum estado selecionado para exclusão."); //apenas para testar
        }
    }

    public void editar(Estado est) {
        this.estado = est;
    }

    public void novo() {
        estado = new Estado();
    }

    public Estado getEstado() {
        return estado;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public List<Estado> getListaEstado() {
        return estadoFacade.listaTodos();
    }

    public Estado getEstadoSelecionado() {
        return estadoSelecionado;
    }

    public void setEstadoSelecionado(Estado estadoSelecionado) {
        this.estadoSelecionado = estadoSelecionado;
    }

    public EstadoFacade getEstadoFacade() {
        return estadoFacade;
    }

    public void setEstadoFacade(EstadoFacade estadoFacade) {
        this.estadoFacade = estadoFacade;
    }
    
}
