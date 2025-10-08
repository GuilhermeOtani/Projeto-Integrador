package Entidade;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

@Entity
public class Venda implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Temporal(TemporalType.TIMESTAMP)
    private Date dataVenda;
    @ManyToOne(fetch = FetchType.EAGER)
    private Cliente cliente;
    @Column(name = "ValorTotal")
    private BigDecimal valorTotal;
    @OneToMany(cascade = CascadeType.ALL,
            mappedBy = "venda",fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    private List<ItemVenda> itensVendas;

    public Venda() {
        itensVendas = new ArrayList<>();
        dataVenda = new Date();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDataVenda() {
        return dataVenda;
    }

    public void setDataVenda(Date dataVenda) {
        this.dataVenda = dataVenda;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public BigDecimal calcularValorTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (ItemVenda it : itensVendas) {
            total = total.add(it.getSubTotal());
        }
        return total;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public List<ItemVenda> getItensVendas() {
        return itensVendas;
    }

    public void setItemVendas(List<ItemVenda> itemVendas) {
        this.itensVendas = itemVendas;
    }

}
