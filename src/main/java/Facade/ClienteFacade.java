package Facade;

import Entidade.Cliente;
import Entidade.Produto;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

/**
 *
 * @author jaimedias
 */
@Stateless
public class ClienteFacade extends AbstractFacade<Cliente> {

    @PersistenceContext(unitName = "SistemaDeEstoque")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ClienteFacade() {
        super(Cliente.class);
    }

    public void salvar(Cliente cliente) {
        if (cliente.getId() == null) {
            create(cliente); // método herdado do AbstractFacade
        } else {
            edit(cliente); // método herdado, faz mersge no banco
        }
    }

    public Cliente buscarPorId(Long id) {
        return em.find(Cliente.class, id);
    }

}
