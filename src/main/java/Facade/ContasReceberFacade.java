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
// Dentro da classe ContasReceberFacade.java

    public boolean existeParcelaAnteriorEmAberto(ContasReceber conta) {
        // Esta implementação pode variar dependendo se você usa JPA, Criteria API, etc.
        // O objetivo é criar uma consulta que procure por contas da mesma venda,
        // com um número de parcela menor e que ainda não tenham data de recebimento.
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
            // Tratar exceção, talvez logar o erro
            // Retornar 'true' por precaução pode evitar um pagamento indevido em caso de erro.
            return true;
        }
    }

    // MÉTODO DE BUSCA ATUALIZADO
    public List<ContasReceber> buscar(Boolean somenteEmAberto, Date dataVencimentoMaxima, String nomeCliente) {
        // A base da query agora usa LEFT JOIN FETCH para otimizar a busca do cliente
        StringBuilder jpql = new StringBuilder("SELECT c FROM ContasReceber c LEFT JOIN FETCH c.cliente WHERE 1=1");

        // Adiciona a condição de status (Em Aberto) se o filtro for ativado
        if (Boolean.TRUE.equals(somenteEmAberto)) {
            jpql.append(" AND c.dataRecebimento IS NULL");
        }

        // Adiciona a condição de data de vencimento se uma data for fornecida
        if (dataVencimentoMaxima != null) {
            jpql.append(" AND c.dataVencimento <= :dataVencimento");
        }

        // NOVO: Adiciona a condição de nome do cliente se o filtro for preenchido
        if (nomeCliente != null && !nomeCliente.trim().isEmpty()) {
            jpql.append(" AND c.cliente.nome LIKE :nomeCliente");
        }

        jpql.append(" ORDER BY c.dataVencimento ASC");

        TypedQuery<ContasReceber> query = em.createQuery(jpql.toString(), ContasReceber.class);

        // Define os parâmetros na query, caso eles existam
        if (dataVencimentoMaxima != null) {
            query.setParameter("dataVencimento", dataVencimentoMaxima);
        }

        // NOVO: Define o parâmetro para o nome do cliente
        if (nomeCliente != null && !nomeCliente.trim().isEmpty()) {
            query.setParameter("nomeCliente", "%" + nomeCliente + "%");
        }

        return query.getResultList();
    }
}
