package Facade;

import Entidade.Produto;
import Entidade.Compra;
import Entidade.Venda;
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

    public List<Compra> findAll() {
        return em.createQuery(
                "SELECT DISTINCT c FROM Compra c LEFT JOIN FETCH c.itemCompras i LEFT JOIN FETCH i.produto", Compra.class)
                .getResultList();

    }

    public CompraFacade() {
        super(Compra.class);
    }

    public void removerPorProduto(Produto produto) {
        em.createQuery("DELETE FROM Compra co WHERE co.produto = :produto")
                .setParameter("produto", produto)
                .executeUpdate();
    }

    // Buscar vendas que tenham um item com determinado produto
    public List<Compra> itensPorProduto(Produto produto) {
        return em.createQuery(
                "SELECT DISTINCT c FROM Compra c "
                + "JOIN v.itemCompras i "
                + "WHERE i.produto = :produto", Compra.class)
                .setParameter("produto", produto)
                .getResultList();
    }

    public BigDecimal totalComprado() {
        return em.createQuery("SELECT COALESCE(SUM(c.valorTotal),0) FROM Compra c", BigDecimal.class)
                .getSingleResult();
    }

    public List<Compra> ultimasCompras(int quantidade) {
        return em.createQuery("SELECT c FROM Compra c ORDER BY c.dataCompra DESC", Compra.class)
                .setMaxResults(quantidade)
                .getResultList();
    }
    //metodo que eu achei pra resolver o erro de salvar uma venda com mais de 1 item venda ficar aparecendo
    //varias vezes na tabela

    public List<Compra> listarComprasComItens() {
        return em.createQuery("SELECT DISTINCT c FROM Compra c LEFT JOIN FETCH c.itemCompras", Compra.class)
                .getResultList();
    }
}
