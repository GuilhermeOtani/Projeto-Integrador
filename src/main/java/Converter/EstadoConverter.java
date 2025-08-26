package Converter;

import Entidade.Estado;
import Facade.EstadoFacade;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

public class EstadoConverter implements Converter{
    
    private EstadoFacade estadoFacade;

    public EstadoConverter(EstadoFacade estadoFacade) {
        this.estadoFacade = estadoFacade;
    }
    
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        return estadoFacade.buscar(Long.parseLong(value.toString()));
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object obj) {
        return ((Estado)obj).getId().toString();
    }
    
}
