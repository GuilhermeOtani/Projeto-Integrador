package Converter;

import Entidade.Produto;
import Facade.ProdutoFacade;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

public class ProdutoConverter implements Converter{
    
    private ProdutoFacade produtoFacade;

    public ProdutoConverter(ProdutoFacade produtoFacade) {
        this.produtoFacade = produtoFacade;
    }
    
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        return produtoFacade.buscar(Long.parseLong(value.toString()));
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object obj) {
        return ((Produto)obj).getId().toString();
    }
    
}
