package Facade;

import Entidade.ContasPagar;
import java.util.Date;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

@Stateless
public class ContasPagarFacade extends AbstractFacade<ContasPagar> {

    @PersistenceContext(unitName = "SistemaDeEstoque")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ContasPagarFacade() {
        super(ContasPagar.class);
    }

    public boolean existeParcelaAnteriorEmAberto(ContasPagar conta) {
        try {
            Long count = getEntityManager().createQuery(
                    "SELECT COUNT(c) FROM ContasPagar c "
                    + "WHERE c.compra = :compra "
                    + "AND c.parcela < :parcelaAtual "
                    + "AND c.dataPagamento IS NULL", Long.class)
                    .setParameter("compra", conta.getCompra())
                    .setParameter("parcelaAtual", conta.getParcela())
                    .getSingleResult();

            return count > 0;
        } catch (Exception e) {
            // Logar o erro é uma boa prática aqui
            return true; // Mantém a segurança em caso de falha na consulta
        }
    }

    public List<ContasPagar> buscar(Boolean somenteEmAberto, Date dataVencimentoMaxima, String nomeFornecedor) {
        // MUDANÇA 1: Adicionado "LEFT JOIN FETCH c.compra" para otimizar o carregamento dos dados da compra.
        StringBuilder jpql = new StringBuilder("SELECT c FROM ContasPagar c LEFT JOIN FETCH c.fornecedor LEFT JOIN FETCH c.compra WHERE 1=1");

        // Adiciona a condição de status (Em Aberto) se o filtro for ativado
        if (Boolean.TRUE.equals(somenteEmAberto)) {
            jpql.append(" AND c.dataPagamento IS NULL");
        }

        // Adiciona a condição de data de vencimento se uma data for fornecida
        if (dataVencimentoMaxima != null) {
            jpql.append(" AND c.dataVencimento <= :dataVencimento");
        }

        // Adiciona a condição de nome do fornecedor se o filtro for preenchido
        if (nomeFornecedor != null && !nomeFornecedor.trim().isEmpty()) {
            jpql.append(" AND c.fornecedor.nome LIKE :nomeFornecedor");
        }

        // MUDANÇA 2: A ordenação agora agrupa por compra e depois por parcela. ESTA É A MUDANÇA PRINCIPAL!
        jpql.append(" ORDER BY c.compra.id ASC, c.parcela ASC");

        TypedQuery<ContasPagar> query = em.createQuery(jpql.toString(), ContasPagar.class);

        // Define os parâmetros na query, caso eles existam
        if (dataVencimentoMaxima != null) {
            query.setParameter("dataVencimento", dataVencimentoMaxima);
        }

        if (nomeFornecedor != null && !nomeFornecedor.trim().isEmpty()) {
            query.setParameter("nomeFornecedor", "%" + nomeFornecedor + "%");
        }

        return query.getResultList();
    }
}
