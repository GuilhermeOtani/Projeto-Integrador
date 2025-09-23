package Facade;

import Entidade.Produto;
import Entidade.Venda;
import java.math.BigDecimal;
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

    public BigDecimal totalVendido() {
        return em.createQuery("SELECT COALESCE(SUM(v.valorTotal),0) FROM Venda v", BigDecimal.class)
                .getSingleResult();
    }

    public List<Venda> ultimasVendas(int quantidade) {
        // exemplo
        return em.createQuery("SELECT v FROM Venda v ORDER BY v.data DESC", Venda.class)
                .setMaxResults(quantidade)
                .getResultList();
    }

}
