package Converter;

import Entidade.Usuario;
import Facade.UsuarioFacade;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

public class UsuarioConverter implements Converter{
    
    private UsuarioFacade usuarioFacade;

    public UsuarioConverter(UsuarioFacade usuarioFacade) {
        this.usuarioFacade = usuarioFacade;
    }
    
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        return usuarioFacade.buscar(Long.parseLong(value.toString()));
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object obj) {
        return ((Usuario)obj).getId().toString();
    }
    
}
