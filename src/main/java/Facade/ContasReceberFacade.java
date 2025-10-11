package Facade;

import Entidade.ContasReceber;
import java.util.Date;
import java.util.List;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

@Stateless
public class ContasReceberFacade extends AbstractFacade<ContasReceber> {

    @PersistenceContext(unitName = "SistemaDeEstoque")
    private EntityManager em;

    @Override
    protected EntityManager getEntityManager() {
        return em;
    }

    public ContasReceberFacade() {
        super(ContasReceber.class);
    }

    public boolean existeParcelaAnteriorEmAberto(ContasReceber conta) {
        try {
            Long count = getEntityManager().createQuery(
                    "SELECT COUNT(c) FROM ContasReceber c "
                    + "WHERE c.venda = :venda "
                    + "AND c.parcela < :parcelaAtual "
                    + "AND c.dataRecebimento IS NULL", Long.class)
                    .setParameter("venda", conta.getVenda())
                    .setParameter("parcelaAtual", conta.getParcela())
                    .getSingleResult();

            return count > 0;
        } catch (Exception e) {
            // Logar o erro é uma boa prática aqui
            return true; // Mantém a segurança em caso de falha na consulta
        }
    }

    public List<ContasReceber> buscar(Boolean somenteEmAberto, Date dataVencimentoMaxima, String nomeCliente) {
        // MUDANÇA 1: Adicionado "LEFT JOIN FETCH c.venda" para otimizar o carregamento dos dados da venda.
        StringBuilder jpql = new StringBuilder("SELECT c FROM ContasReceber c LEFT JOIN FETCH c.cliente LEFT JOIN FETCH c.venda WHERE 1=1");

        // Adiciona a condição de status (Em Aberto) se o filtro for ativado
        if (Boolean.TRUE.equals(somenteEmAberto)) {
            jpql.append(" AND c.dataRecebimento IS NULL");
        }

        // Adiciona a condição de data de vencimento se uma data for fornecida
        if (dataVencimentoMaxima != null) {
            jpql.append(" AND c.dataVencimento <= :dataVencimento");
        }

        // Adiciona a condição de nome do cliente se o filtro for preenchido
        if (nomeCliente != null && !nomeCliente.trim().isEmpty()) {
            jpql.append(" AND c.cliente.nome LIKE :nomeCliente");
        }

        // MUDANÇA 2: A ordenação agora agrupa por venda e depois por parcela. ESTA É A MUDANÇA PRINCIPAL!
        jpql.append(" ORDER BY c.venda.id ASC, c.parcela ASC");

        TypedQuery<ContasReceber> query = em.createQuery(jpql.toString(), ContasReceber.class);

        // Define os parâmetros na query, caso eles existam
        if (dataVencimentoMaxima != null) {
            query.setParameter("dataVencimento", dataVencimentoMaxima);
        }

        if (nomeCliente != null && !nomeCliente.trim().isEmpty()) {
            query.setParameter("nomeCliente", "%" + nomeCliente + "%");
        }

        return query.getResultList();
    }
}
