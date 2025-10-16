package Controle;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;

// 1. ANOTAÇÃO QUE REGISTRA O SERVLET E DEFINE SUA URL
@WebServlet("/relatorio/produtos")
public class RelatorioServlet extends HttpServlet {

    // 2. INJETAR O ENTITY MANAGER (igual ao bean)
    @PersistenceContext(unitName = "SistemaDeEstoque")
    private EntityManager entityManager;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            // 3. PEGAR O PARÂMETRO DA URL
            String filtro = request.getParameter("filtro");
            
            Session session = entityManager.unwrap(Session.class);
            final JasperPrint[] jasperPrintHolder = new JasperPrint[1];

            session.doWork(connection -> {
                try {
                    InputStream relatorioStream = getClass().getResourceAsStream("/Relatorios/relProduto.jasper");
                    if (relatorioStream == null) {
                        throw new RuntimeException("Relatório não encontrado: /Relatorios/relProduto.jasper");
                    }

                    Map<String, Object> parametros = new HashMap<>();
                    String parametroFiltro = (filtro == null || filtro.isEmpty()) ? "%" : "%" + filtro + "%";
                    parametros.put("filtro", parametroFiltro);

                    jasperPrintHolder[0] = JasperFillManager.fillReport(relatorioStream, parametros, connection);
                } catch (Exception e) {
                    throw new RuntimeException("Erro ao preencher o relatório", e);
                }
            });

            // 4. CONFIGURAR A RESPOSTA HTTP (aqui é direto, sem FacesContext)
            response.reset();
            response.setContentType("application/pdf");
            response.setHeader("Content-Disposition", "inline; filename=\"relatorio_produtos.pdf\"");

            // 5. ENVIAR O PDF
            JasperExportManager.exportReportToPdfStream(jasperPrintHolder[0], response.getOutputStream());

        } catch (Exception e) {
            // Se der erro, envia uma resposta de erro HTTP
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Erro ao gerar o relatório: " + e.getMessage());
            e.printStackTrace();
        }
    }
}