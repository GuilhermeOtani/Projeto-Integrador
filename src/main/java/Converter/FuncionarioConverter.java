package Converter;

import Entidade.Funcionario;
import Facade.FuncionarioFacade;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

public class FuncionarioConverter implements Converter{
    
    private FuncionarioFacade funcionarioFacade;

    public FuncionarioConverter(FuncionarioFacade funcionarioFacade) {
        this.funcionarioFacade = funcionarioFacade;
    }
    
    @Override
    public Object getAsObject(FacesContext context, UIComponent component, String value) {
        return funcionarioFacade.buscar(Long.parseLong(value.toString()));
    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object obj) {
        return ((Funcionario)obj).getId().toString();
    }
    
}
