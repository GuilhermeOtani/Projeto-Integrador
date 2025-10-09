package Facade;

import java.util.List;
import javax.persistence.EntityManager;

public abstract class AbstractFacade<T> {

    private Class<T> entityClass;

    protected abstract EntityManager getEntityManager();

    public AbstractFacade(Class<T> entityClass) {
        this.entityClass = entityClass;
    }

    // ==========================================================
    // MUDANÇA AQUI: Renomeamos o método que retorna o objeto
    // ==========================================================
    public T salvarERetornar(T entity) {
        return getEntityManager().merge(entity);
    }

    // ==========================================================
    // MUDANÇA AQUI: Adicionamos um método 'salvar' do tipo 'void'
    // para não quebrar seus controladores antigos.
    // O merge já lida se o objeto é novo ou existente.
    // ==========================================================
    public void salvar(T entity) {
        getEntityManager().merge(entity);
    }

    public void remover(T entity) {
        getEntityManager().remove(getEntityManager().merge(entity));
    }

    public T buscar(Object id) {
        return getEntityManager().find(entityClass, id);
    }

    public List<T> listaTodos() {
        return getEntityManager()
                .createQuery("SELECT t FROM "
                        + entityClass.getSimpleName() + " t ORDER BY t.id DESC", entityClass).getResultList();
    }

    public void create(T entity) {
        getEntityManager().persist(entity);
    }

    public void edit(T entity) {
        getEntityManager().merge(entity);
    }
}