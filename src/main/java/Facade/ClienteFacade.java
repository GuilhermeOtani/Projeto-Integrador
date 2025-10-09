package Facade;

import Entidade.Cliente;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

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

    // O método "salvar(Cliente cliente)" foi REMOVIDO daqui.
    // A classe agora vai usar o método "salvar" que ela herda do AbstractFacade,
    // que já faz o que é preciso (cria ou edita).

    public Cliente buscarPorId(Long id) {
        return em.find(Cliente.class, id);
    }

}