package Controle;

import Entidade.ContasReceber;
import Facade.ContasReceberFacade;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import javax.annotation.PostConstruct; // Importação necessária
import javax.ejb.EJB;
import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

@Named
@ViewScoped
public class ContasReceberControle implements Serializable {

    private ContasReceber contaSelecionada;
    private List<ContasReceber> listaContas;
    private Date dataFiltroVencimento;
    private boolean mostraTodasContas = true; // Para alternar entre todas ou abertas

    @EJB
    private ContasReceberFacade contasReceberFacade;

    @PostConstruct // CORREÇÃO: Garante que o método será chamado corretamente após a injeção
    public void init() {
        // Carrega a lista ao inicializar
        if (mostraTodasContas) {
            listaContas = contasReceberFacade.listaTodos();
        } else {
            // Se você criar o método na Facade: listaContas = contasReceberFacade.buscarEmAberto();
            // Por enquanto, mantemos a listagem completa se o método não existir
            listaContas = contasReceberFacade.listaTodos(); 
        }
    }

    // Método para recarregar a lista quando o filtro de status mudar na tela
    public void recarregarListaPorStatus() {
        init();
    }

    public void registrarRecebimento() {
        // Adicionando uma validação extra para garantir que a data não seja nula (boa prática)
        if (contaSelecionada == null || contaSelecionada.getId() == null) {
             FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atenção", "Selecione uma conta para registrar o recebimento!"));
             return;
        }

        if (contaSelecionada.getDataRecebimento() != null) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_WARN, "Atenção", "Conta Nº " + contaSelecionada.getId() + " já foi recebida em " + contaSelecionada.getDataRecebimento() + "!"));
            return;
        }

        // Define a data de recebimento como a data atual
        contaSelecionada.setDataRecebimento(new Date()); 
        
        try {
            contasReceberFacade.salvar(contaSelecionada);
            
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Sucesso", "Conta Nº " + contaSelecionada.getId() + " recebida com sucesso!"));
            
            // Atualiza a lista após o recebimento
            init(); 
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Erro", "Erro ao registrar recebimento: " + e.getMessage()));
        }
    }
    
    // --- Getters e Setters ---

    public ContasReceber getContaSelecionada() {
        return contaSelecionada;
    }

    public void setContaSelecionada(ContasReceber contaSelecionada) {
        this.contaSelecionada = contaSelecionada;
    }

    public List<ContasReceber> getListaContas() {
         // Não precisa do if(listaContas == null) aqui se usarmos @PostConstruct
        return listaContas;
    }

    public void setListaContas(List<ContasReceber> listaContas) {
        this.listaContas = listaContas;
    }

    public Date getDataFiltroVencimento() {
        return dataFiltroVencimento;
    }

    public void setDataFiltroVencimento(Date dataFiltroVencimento) {
        this.dataFiltroVencimento = dataFiltroVencimento;
    }
    
    public boolean isMostraTodasContas() {
        return mostraTodasContas;
    }

    public void setMostraTodasContas(boolean mostraTodasContas) {
        this.mostraTodasContas = mostraTodasContas;
    }
}