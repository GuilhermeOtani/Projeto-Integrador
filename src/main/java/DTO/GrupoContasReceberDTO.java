package DTO;

import Entidade.Cliente;
import Entidade.ContasReceber;
import Entidade.Venda;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class GrupoContasReceberDTO implements Serializable {

    private Venda venda;
    private Cliente cliente;
    private BigDecimal valorTotalVenda;
    private int totalParcelas;
    private long parcelasAbertas;
    private List<ContasReceber> parcelas;

    public GrupoContasReceberDTO(Venda venda) {
        this.venda = venda;
        this.cliente = venda.getCliente();
        this.valorTotalVenda = venda.getValorTotal();
        this.parcelas = new ArrayList<>();
    }

    // ALTERADO: Método agora compatível com Java 7 e anteriores
    public void adicionarParcela(ContasReceber conta) {
        this.parcelas.add(conta);
        this.totalParcelas = this.parcelas.size();

        // --- Início da substituição (lógica compatível) ---
        long contadorDeAbertas = 0;
        for (ContasReceber p : this.parcelas) {
            // Verifica se a data de recebimento é nula (parcela em aberto)
            if (p.getDataRecebimento() == null) {
                contadorDeAbertas++;
            }
        }
        this.parcelasAbertas = contadorDeAbertas;
        // --- Fim da substituição ---
    }

    // Getters e Setters (continuam iguais)
    public Venda getVenda() {
        return venda;
    }

    public void setVenda(Venda venda) {
        this.venda = venda;
    }

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public BigDecimal getValorTotalVenda() {
        return valorTotalVenda;
    }

    public void setValorTotalVenda(BigDecimal valorTotalVenda) {
        this.valorTotalVenda = valorTotalVenda;
    }

    public int getTotalParcelas() {
        return totalParcelas;
    }

    public void setTotalParcelas(int totalParcelas) {
        this.totalParcelas = totalParcelas;
    }

    public long getParcelasAbertas() {
        return parcelasAbertas;
    }

    public void setParcelasAbertas(long parcelasAbertas) {
        this.parcelasAbertas = parcelasAbertas;
    }

    public List<ContasReceber> getParcelas() {
        return parcelas;
    }

    public void setParcelas(List<ContasReceber> parcelas) {
        this.parcelas = parcelas;
    }
}
