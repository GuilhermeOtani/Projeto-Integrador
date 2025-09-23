package Facade;

import Entidade.Produto;
import Entidade.Compra;
import java.math.BigDecimal;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
public class CompraFacade extends AbstractFacade<Compra> {

    @PersistenceContext(unitName = "SistemaDeEstoque")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public CompraFacade() {
        super(Compra.class);
    }

    public void removerPorProduto(Produto produto) {
        em.createQuery("DELETE FROM Compra co WHERE co.produto = :produto")
                .setParameter("produto", produto)
                .executeUpdate();
    }

    public List<Compra> itensPorProduto(Produto produto) {
        return em.createQuery("SELECT c FROM Compra c WHERE c.produto = :produto", Compra.class)
                .setParameter("produto", produto)
                .getResultList();
    }

    public BigDecimal totalComprado() {
        return em.createQuery("SELECT COALESCE(SUM(c.valorTotal),0) FROM Compra c", BigDecimal.class)
                .getSingleResult();
    }

    public List<Compra> ultimasCompras(int quantidade) {
        return em.createQuery("SELECT c FROM Compra c ORDER BY c.data DESC", Compra.class)
                .setMaxResults(quantidade)
                .getResultList();
    }
}
