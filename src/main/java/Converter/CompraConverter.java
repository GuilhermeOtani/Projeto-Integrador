package Converter;

import Entidade.Compra;
import Facade.CompraFacade;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

public class CompraConverter implements Converter{
    
    private CompraFacade compraFacade;

    public CompraConverter(CompraFacade compraFacade) {
        this.compraFacade = compraFacade;
    }
    
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        return compraFacade.buscar(Long.parseLong(value.toString()));
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object obj) {
        return ((Compra)obj).getId().toString();
    }
    
}
