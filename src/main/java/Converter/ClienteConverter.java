package Converter;

import Entidade.Cliente;
import Facade.ClienteFacade;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

public class ClienteConverter implements Converter{
    
    private ClienteFacade clienteFacade;

    public ClienteConverter(ClienteFacade clienteFacade) {
        this.clienteFacade = clienteFacade;
    }
    
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        return clienteFacade.buscar(Long.parseLong(value.toString()));
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object obj) {
        return ((Cliente)obj).getId().toString();
    }
    
}
