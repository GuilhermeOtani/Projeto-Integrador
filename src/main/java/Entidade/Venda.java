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

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "venda", fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    private List<ItemVenda> itensVendas;

    @Column(length = 50)
    private String formaPagamento;

    private Integer numeroParcelas;

    @OneToMany(mappedBy = "venda", fetch = FetchType.LAZY)
    private List<ContasReceber> parcelas;

    public List<ContasReceber> getParcelas() {
        return parcelas;
    }

    public void setParcelas(List<ContasReceber> parcelas) {
        this.parcelas = parcelas;
    }

    public Venda() {
        itensVendas = new ArrayList<>();
        dataVenda = new Date();
        valorTotal = BigDecimal.ZERO; // Boa prática inicializar
        numeroParcelas = 1; // Padrão
    }

    public BigDecimal calcularValorTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (ItemVenda it : itensVendas) {
            total = total.add(it.getSubTotal());
        }
        return total;
    }

    // --- Getters e Setters ---
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

    // NOVOS GETTERS E SETTERS
    public String getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(String formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public Integer getNumeroParcelas() {
        return numeroParcelas;
    }

    public void setNumeroParcelas(Integer numeroParcelas) {
        this.numeroParcelas = numeroParcelas;
    }
}
