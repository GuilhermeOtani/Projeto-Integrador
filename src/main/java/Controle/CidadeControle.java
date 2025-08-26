package Controle;

import Converter.EstadoConverter;
import Entidade.Cidade;
import Entidade.Estado;
import Facade.CidadeFacade;
import Facade.EstadoFacade;
import java.io.Serializable;
import java.util.List;
import javax.ejb.EJB;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

@ManagedBean
@SessionScoped
public class CidadeControle implements Serializable {

    private Cidade cidade = new Cidade();
    private Cidade cidadeSelecionada;
    @EJB
    private CidadeFacade cidadeFacade;
    @EJB
    private EstadoFacade estadoFacade;
    private EstadoConverter estadoConverter;

    public List<Estado> getListaEstados() {
        return estadoFacade.listaTodos();
    }

    public EstadoConverter getEstadoConverter() {
        if (estadoConverter == null) {
            estadoConverter = new EstadoConverter(estadoFacade);
        }
        return estadoConverter;
    }

    public void setEstadoConverter(EstadoConverter estadoConverter) {
        this.estadoConverter = estadoConverter;
    }

    public void salvar() {
        cidadeFacade.salvar(cidade);
        cidade = new Cidade();
    }

    public void remover() {
        if (cidadeSelecionada != null) {
            System.out.println("Cidade a ser removida: " + cidadeSelecionada.getNome()); // Verifique se o produto está correto
            cidadeFacade.remover(cidadeSelecionada);
            //limpar a seleção após remoção
            cidadeSelecionada = null;
        } else {
            System.out.println("Nenhuma cidade selecionada para exclusão."); //apenas para testar
        }
    }

    public void novo() {
        cidade = new Cidade();
    }

    public String editar(Cidade cid) {
        this.cidade = cid;
        return "index";
    }

    public Cidade getCidade() {
        return cidade;
    }

    public void setCidade(Cidade cidade) {
        this.cidade = cidade;
    }

    public List<Cidade> getListaCidades() {
        return cidadeFacade.listaTodos();
    }

    public Cidade getCidadeSelecionada() {
        return cidadeSelecionada;
    }

    public void setCidadeSelecionada(Cidade cidadeSelecionada) {
        this.cidadeSelecionada = cidadeSelecionada;
    }

    public CidadeFacade getCidadeFacade() {
        return cidadeFacade;
    }

    public void setCidadeFacade(CidadeFacade cidadeFacade) {
        this.cidadeFacade = cidadeFacade;
    }

    public EstadoFacade getEstadoFacade() {
        return estadoFacade;
    }

    public void setEstadoFacade(EstadoFacade estadoFacade) {
        this.estadoFacade = estadoFacade;
    }
    
}
