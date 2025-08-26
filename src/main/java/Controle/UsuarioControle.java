package Controle;

import Entidade.Usuario;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.Serializable;

@ManagedBean
@ViewScoped
public class UsuarioControle implements Serializable {

    private Usuario usuario = new Usuario();

    public void salvar() {
        FacesContext.getCurrentInstance().addMessage(null,
            new FacesMessage("Usuário salvo com sucesso!"));
        usuario = new Usuario(); //limpa o formulário
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }
}
