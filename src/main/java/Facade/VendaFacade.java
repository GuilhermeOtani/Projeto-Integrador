package Facade;

import Entidade.Produto;
import Entidade.Venda;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
public class VendaFacade extends AbstractFacade<Venda> {

    @PersistenceContext(unitName = "SistemaDeEstoque")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public VendaFacade() {
        super(Venda.class);
    }

    public void removerPorProduto(Produto produto) {
        em.createQuery("DELETE FROM Venda ci WHERE ci.produto = :produto")
                .setParameter("produto", produto)
                .executeUpdate();
    }

    public List<Venda> itensPorProduto(Produto produto) {
        return em.createQuery("SELECT c FROM Venda c WHERE c.produto = :produto", Venda.class)
                .setParameter("produto", produto)
                .getResultList();
    }

}
