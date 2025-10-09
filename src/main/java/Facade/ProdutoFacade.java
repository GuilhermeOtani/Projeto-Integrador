package Facade;

import Entidade.Produto;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
public class ProdutoFacade extends AbstractFacade<Produto> {

    @PersistenceContext(unitName = "SistemaDeEstoque")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ProdutoFacade() {
        super(Produto.class);
    }

    // O método "salvar(Produto produto)" foi REMOVIDO daqui.
    // A classe agora vai usar o método "salvar" que ela herda do AbstractFacade,
    // que já faz o que é preciso (cria ou edita).

    public Produto buscarPorId(Long id) {
        return em.find(Produto.class, id);
    }

    public Long totalProdutosEstoque() {
        return em.createQuery("SELECT COALESCE(SUM(p.estoque),0) FROM Produto p", Long.class)
                .getSingleResult();
    }
}