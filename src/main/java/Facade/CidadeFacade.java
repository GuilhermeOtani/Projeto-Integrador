package Facade;

import Entidade.Cidade;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
public class CidadeFacade extends AbstractFacade<Cidade> {

    @PersistenceContext(unitName = "SistemaDeEstoque")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public CidadeFacade() {
        super(Cidade.class);
    }

}

