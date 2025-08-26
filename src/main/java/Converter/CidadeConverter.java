package Converter;

import Entidade.Cidade;
import Facade.CidadeFacade;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

public class CidadeConverter implements Converter{
    
    private CidadeFacade cidadeFacade;

    public CidadeConverter(CidadeFacade cidadeFacade) {
        this.cidadeFacade = cidadeFacade;
    }
    
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        return cidadeFacade.buscar(Long.parseLong(value.toString()));
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object obj) {
        return ((Cidade)obj).getId().toString();
    }
    
}
