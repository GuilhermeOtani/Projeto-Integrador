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
public class Compra implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Temporal(TemporalType.TIMESTAMP)
    private Date dataCompra;

    @ManyToOne(fetch = FetchType.EAGER)
    private Fornecedor fornecedor;

    @Column(name = "ValorTotal")
    private BigDecimal valorTotal;

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "compra", fetch = FetchType.EAGER)
    @Fetch(FetchMode.SUBSELECT)
    private List<ItemCompra> itemCompras;

    @Column(length = 50)
    private String formaPagamento;

    private Integer numeroParcelas;

    @OneToMany(mappedBy = "compra", fetch = FetchType.LAZY)
    private List<ContasPagar> parcelas;

    public List<ContasPagar> getParcelas() {
        return parcelas;
    }

    public void setParcelas(List<ContasPagar> parcelas) {
        this.parcelas = parcelas;
    }

    public Compra() {
        itemCompras = new ArrayList<>();
        dataCompra = new Date();
        valorTotal = BigDecimal.ZERO; // Boa prática inicializar
        numeroParcelas = 1; // Padrão
    }

    public BigDecimal calcularValorTotal() {
        BigDecimal total = BigDecimal.ZERO;
        for (ItemCompra it : itemCompras) {
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

    public Date getDataCompra() {
        return dataCompra;
    }

    public void setDataCompra(Date dataCompra) {
        this.dataCompra = dataCompra;
    }

    public Fornecedor getFornecedor() {
        return fornecedor;
    }

    public void setFornecedor(Fornecedor fornecedor) {
        this.fornecedor = fornecedor;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
    }

    public List<ItemCompra> getItemCompras() {
        return itemCompras;
    }

    public void setItemCompras(List<ItemCompra> itemCompras) {
        this.itemCompras = itemCompras;
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
