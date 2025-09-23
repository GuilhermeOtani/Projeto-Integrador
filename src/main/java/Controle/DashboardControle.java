package Controle;

import Entidade.Compra;
import Entidade.ItemCompra;
import Entidade.ItemVenda;
import Entidade.Venda;
import Facade.CompraFacade;
import Facade.VendaFacade;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
import java.util.Arrays;
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

    private BarChartModel barChartModel;

    @PostConstruct
    public void init() {
        calcularTotais();
        criarBarChart();
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

        BarChartDataSet vendasDataset = new BarChartDataSet();
        vendasDataset.setLabel("Vendas");
        vendasDataset.setData(Arrays.<Number>asList(totalVendas.doubleValue()));

        BarChartDataSet comprasDataset = new BarChartDataSet();
        comprasDataset.setLabel("Compras");
        comprasDataset.setData(Arrays.<Number>asList(totalCompras.doubleValue()));

        data.addChartDataSet(vendasDataset);
        data.addChartDataSet(comprasDataset);
        data.setLabels(Arrays.asList("Totais"));

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
