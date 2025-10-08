package Controle;

import Entidade.Compra;
import Entidade.ItemCompra;
import Entidade.ItemVenda;
import Entidade.Venda;
import Facade.CompraFacade;
import Facade.VendaFacade;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.primefaces.model.charts.bar.BarChartModel;
import org.primefaces.model.charts.bar.BarChartDataSet;
import org.primefaces.model.charts.ChartData;

@Named
@ViewScoped
public class DashboardControle implements Serializable {

    @EJB
    private CompraFacade compraFacade;

    @EJB
    private VendaFacade vendaFacade;

    private BigDecimal totalVendas;
    private BigDecimal totalCompras;
    private int totalProdutosVendidos;
    private int totalProdutosComprados;
    private List<ItemVenda> itensVendas;
    private List<ItemCompra> itensCompras;
    private List<Venda> vendas;
    private List<Compra> compras;

    public List<Compra> getCompras() {
        return compras;
    }

    public void setCompras(List<Compra> compras) {
        this.compras = compras;
    }
    
    public List<Venda> getVendas() {
        return vendas;
    }

    public void setVendas(List<Venda> vendas) {
        this.vendas = vendas;
    }

    private BarChartModel barChartModel;

    @PostConstruct
    public void init() {
        this.vendas = vendaFacade.listarVendasComItens();
        this.compras = compraFacade.listarComprasComItens();
        calcularTotais();
        carregarItens();
        criarBarChart();
    }

    private void carregarItens() {
        itensVendas = new ArrayList<ItemVenda>();
        List<Venda> vendas = vendaFacade.listaTodos();
        for (Venda v : vendas) {
            for (ItemVenda iv : v.getItensVendas()) {
                itensVendas.add(iv);
            }
        }

        itensCompras = new ArrayList<ItemCompra>();
        List<Compra> compras = compraFacade.listaTodos();
        for (Compra c : compras) {
            for (ItemCompra ic : c.getItemCompras()) {
                itensCompras.add(ic);
            }
        }
    }

// Getters para o xhtml
    public List<ItemVenda> getItensVendas() {
        return itensVendas;
    }

    public List<ItemCompra> getItensCompras() {
        return itensCompras;
    }

    private void calcularTotais() {
        totalVendas = BigDecimal.ZERO;
        totalCompras = BigDecimal.ZERO;
        totalProdutosVendidos = 0;
        totalProdutosComprados = 0;

        List<Venda> vendas = vendaFacade.listaTodos();
        for (Venda v : vendas) {
            for (ItemVenda iv : v.getItensVendas()) {
                totalVendas = totalVendas.add(iv.getSubTotal());
                totalProdutosVendidos += iv.getQuantidade();
            }
        }

        List<Compra> compras = compraFacade.listaTodos();
        for (Compra c : compras) {
            for (ItemCompra ic : c.getItemCompras()) {
                totalCompras = totalCompras.add(ic.getSubTotal());
                totalProdutosComprados += ic.getQuantidade();
            }
        }
    }

    private void criarBarChart() {
        barChartModel = new BarChartModel();
        ChartData data = new ChartData();

        // Dataset de Vendas
        BarChartDataSet vendasDataset = new BarChartDataSet();
        vendasDataset.setLabel("Vendas");
        List<Number> vendasData = new ArrayList<Number>();
        vendasData.add(totalVendas.doubleValue());
        vendasDataset.setData(vendasData);
        vendasDataset.setBackgroundColor("rgba(26, 188, 156, 0.7)");
        vendasDataset.setBorderColor("rgba(26, 188, 156, 1)");
        vendasDataset.setBorderWidth(1);

        // Dataset de Compras
        BarChartDataSet comprasDataset = new BarChartDataSet();
        comprasDataset.setLabel("Compras");
        List<Number> comprasData = new ArrayList<Number>();
        comprasData.add(totalCompras.doubleValue());
        comprasDataset.setData(comprasData);
        comprasDataset.setBackgroundColor("rgba(231, 76, 60, 0.7)");
        comprasDataset.setBorderColor("rgba(231, 76, 60, 1)");
        comprasDataset.setBorderWidth(1);

        // Adiciona datasets ao ChartData
        data.addChartDataSet(vendasDataset);
        data.addChartDataSet(comprasDataset);

        // Labels do gráfico
        List<String> labels = new ArrayList<String>();
        labels.add("Totais");
        data.setLabels(labels);

        // Aplica os dados ao gráfico
        barChartModel.setData(data);
    }

    // Getters
    public BigDecimal getTotalVendas() {
        return totalVendas;
    }

    public BigDecimal getTotalCompras() {
        return totalCompras;
    }

    public int getTotalProdutosVendidos() {
        return totalProdutosVendidos;
    }

    public int getTotalProdutosComprados() {
        return totalProdutosComprados;
    }

    public BarChartModel getBarChartModel() {
        return barChartModel;
    }

}
