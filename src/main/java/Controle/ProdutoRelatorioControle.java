package Controle;

// SEU BEAN AGORA FICOU ASSIM:

import java.io.Serializable;
import javax.enterprise.context.RequestScoped;
import javax.inject.Named;

@Named("produtoRelatorioControle") // O nome precisa ser o mesmo!
@RequestScoped // Ou @ViewScoped
public class ProdutoRelatorioControle implements Serializable { // Lembre-se de implementar Serializable se usar @ViewScoped

    private String filtro;

    // Getters e Setters
    public String getFiltro() {
        return filtro;
    }

    public void setFiltro(String filtro) {
        this.filtro = filtro;
    }
}