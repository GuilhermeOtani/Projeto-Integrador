package Converter;

import Entidade.Venda;
import Facade.VendaFacade;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

public class VendaConverter implements Converter{
    
    private VendaFacade vendaFacade;

    public VendaConverter(VendaFacade vendaFacade) {
        this.vendaFacade = vendaFacade;
    }
    
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        return vendaFacade.buscar(Long.parseLong(value.toString()));
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object obj) {
        return ((Venda)obj).getId().toString();
    }
    
}
