package Converter;

import Entidade.Fornecedor;
import Facade.FornecedorFacade;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

public class FornecedorConverter implements Converter{
    
    private FornecedorFacade fornecedorFacade;

    public FornecedorConverter(FornecedorFacade fornecedorFacade) {
        this.fornecedorFacade = fornecedorFacade;
    }
    
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        return fornecedorFacade.buscar(Long.parseLong(value.toString()));
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object obj) {
        return ((Fornecedor)obj).getId().toString();
    }
    
}
