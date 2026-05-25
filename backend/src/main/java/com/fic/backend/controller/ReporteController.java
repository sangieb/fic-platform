package com.fic.backend.controller;

import com.fic.backend.model.FicData;
import com.fic.backend.model.Reporte;
import com.fic.backend.repository.FicDataRepository;
import com.fic.backend.repository.ReporteRepository;
import com.fic.backend.service.AuditService;
import com.fic.backend.service.JasperReporteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
@Tag(name = "Reportes", description = "Exportar reportes")
@CrossOrigin(origins = "*")
public class ReporteController {

    private final FicDataRepository ficRepo;
    private final ReporteRepository reporteRepo;
    private final AuditService auditService;
    private final MessageSource messageSource;
    private final JasperReporteService jasperService;

    @GetMapping("/pdf")
    @Operation(summary = "Reporte PDF imprimible")
    public ResponseEntity<String> descargarPDF() {
        Locale locale = LocaleContextHolder.getLocale();
        List<FicData> fondos = ficRepo.findAll();

        String titulo   = messageSource.getMessage("reporte.pdf.titulo", null, locale);
        String generado = messageSource.getMessage("reporte.generado", null, locale);
        String fecha    = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        String colFondo   = messageSource.getMessage("tabla.columna.fondo", null, locale);
        String colGestora = messageSource.getMessage("tabla.columna.gestora", null, locale);
        String colDiaria  = messageSource.getMessage("tabla.columna.diaria", null, locale);
        String colMensual = messageSource.getMessage("tabla.columna.mensual", null, locale);
        String colAnual   = messageSource.getMessage("tabla.columna.anual", null, locale);
        String colPeriodo = messageSource.getMessage("tabla.columna.periodo", null, locale);

        StringBuilder filas = new StringBuilder();
        for (FicData f : fondos) {
            filas.append("<tr>")
                .append("<td>").append(safe(f.getNombreFondo())).append("</td>")
                .append("<td>").append(safe(f.getSociedadGestora())).append("</td>")
                .append("<td style='color:").append(colorRent(f.getRentabilidadDiaria())).append(";font-weight:600'>").append(formatRent(f.getRentabilidadDiaria())).append("</td>")
                .append("<td style='color:").append(colorRent(f.getRentabilidadMensual())).append(";font-weight:600'>").append(formatRent(f.getRentabilidadMensual())).append("</td>")
                .append("<td style='color:").append(colorRent(f.getRentabilidad())).append(";font-weight:600'>").append(formatRent(f.getRentabilidad())).append("</td>")
                .append("<td>").append(safe(f.getPeriodo(), 10)).append("</td>")
                .append("</tr>");
        }

        String html = """
            <!DOCTYPE html><html><head><meta charset="UTF-8"><title>%s</title>
            <style>
              *{margin:0;padding:0;box-sizing:border-box}
              body{font-family:Arial,sans-serif;font-size:11px;color:#1a1a2e;padding:20px}
              .header{text-align:center;margin-bottom:20px;border-bottom:2px solid #1a1a2e;padding-bottom:10px}
              .header h1{font-size:16px}.header p{font-size:10px;color:#666;margin-top:4px}
              table{width:100%%;border-collapse:collapse;margin-top:10px}
              thead tr{background:#1a1a2e;color:white}
              thead th{padding:7px 8px;text-align:left;font-size:10px;text-transform:uppercase}
              tbody tr:nth-child(even){background:#f0f4ff}
              tbody td{padding:5px 8px;border-bottom:1px solid #eee}
              .footer{margin-top:15px;font-size:9px;color:#999;text-align:right}
              @media print{body{padding:0}@page{margin:1.5cm;size:A4 landscape}}
            </style></head><body>
            <div class="header"><h1>%s</h1><p>%s %s · %d registros</p></div>
            <table><thead><tr>
              <th>%s</th><th>%s</th><th>%s</th><th>%s</th><th>%s</th><th>%s</th>
            </tr></thead><tbody>%s</tbody></table>
            <div class="footer">FIC Colombia · datos.gov.co</div>
            <script>window.onload=function(){window.print()}</script>
            </body></html>
            """.formatted(titulo, titulo, generado, fecha, fondos.size(),
                colFondo, colGestora, colDiaria, colMensual, colAnual, colPeriodo,
                filas.toString());

        Reporte r = new Reporte();
        r.setTitulo(titulo); r.setFormato("PDF");
        r.setGeneratedAt(LocalDateTime.now());
        reporteRepo.save(r);
        auditService.registrar("Reporte", "PDF", "CREATE", "sistema", "Generacion reporte PDF");

        return ResponseEntity.ok().contentType(MediaType.TEXT_HTML).body(html);
    }

    @GetMapping("/excel")
    @Operation(summary = "Reporte Excel CSV")
    public ResponseEntity<byte[]> descargarExcel() {
        Locale locale = LocaleContextHolder.getLocale();
        List<FicData> fondos = ficRepo.findAll();

        String titulo   = messageSource.getMessage("reporte.excel.titulo", null, locale);
        String generado = messageSource.getMessage("reporte.generado", null, locale);
        String fecha    = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        String colFondo   = messageSource.getMessage("tabla.columna.fondo", null, locale);
        String colGestora = messageSource.getMessage("tabla.columna.gestora", null, locale);
        String colTipo    = messageSource.getMessage("tabla.columna.tipo", null, locale);
        String colDiaria  = messageSource.getMessage("tabla.columna.diaria", null, locale);
        String colMensual = messageSource.getMessage("tabla.columna.mensual", null, locale);
        String colAnual   = messageSource.getMessage("tabla.columna.anual", null, locale);
        String colInv     = messageSource.getMessage("tabla.columna.inversionistas", null, locale);
        String colPeriodo = messageSource.getMessage("tabla.columna.periodo", null, locale);

        StringBuilder csv = new StringBuilder('\uFEFF');
        csv.append(titulo).append("\n");
        csv.append(generado).append(" ").append(fecha).append("\n\n");
        csv.append(csvCelda(colFondo)).append(",")
           .append(csvCelda(colGestora)).append(",")
           .append(csvCelda(colTipo)).append(",")
           .append(csvCelda(colDiaria)).append(",")
           .append(csvCelda(colMensual)).append(",")
           .append(csvCelda(colAnual)).append(",")
           .append(csvCelda(colInv)).append(",")
           .append(csvCelda(colPeriodo)).append("\n");

        for (FicData f : fondos) {
            csv.append(csvCelda(f.getNombreFondo())).append(",")
               .append(csvCelda(f.getSociedadGestora())).append(",")
               .append(csvCelda(f.getTipoFondo())).append(",")
               .append(f.getRentabilidadDiaria() != null ? f.getRentabilidadDiaria() : "").append(",")
               .append(f.getRentabilidadMensual() != null ? f.getRentabilidadMensual() : "").append(",")
               .append(f.getRentabilidad() != null ? f.getRentabilidad() : "").append(",")
               .append(f.getNumInversionistas() != null ? f.getNumInversionistas() : "").append(",")
               .append(csvCelda(f.getPeriodo() != null
                   ? f.getPeriodo().substring(0, Math.min(10, f.getPeriodo().length())) : ""))
               .append("\n");
        }

        byte[] bytes = csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8);

        Reporte r = new Reporte();
        r.setTitulo(titulo); r.setFormato("EXCEL");
        r.setGeneratedAt(LocalDateTime.now());
        reporteRepo.save(r);
        auditService.registrar("Reporte", "EXCEL", "CREATE", "sistema", "Generacion reporte Excel");

        return ResponseEntity.ok()
            .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=reporte-fic.csv")
            .contentType(MediaType.parseMediaType("text/csv; charset=UTF-8"))
            .body(bytes);
    }

    /**
     * Genera y descarga un reporte PDF profesional usando JasperReports.
     *
     * @return PDF binario con los datos de los fondos de inversión
     */
    @GetMapping("/jasper")
    @Operation(summary = "Reporte PDF profesional con JasperReports")
    public ResponseEntity<byte[]> descargarJasper() {
        try {
            byte[] pdf = jasperService.generarPDF();
            return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=reporte-fic-jasper.pdf")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    private String safe(String val) {
        return val != null ? val : "-";
    }

    private String safe(String val, int maxLen) {
        if (val == null) return "-";
        return val.length() > maxLen ? val.substring(0, maxLen) : val;
    }

    private String formatRent(Double val) {
        return val != null ? String.format("%.2f%%", val) : "-";
    }

    private String colorRent(Double val) {
        if (val == null) return "#666";
        return val >= 0 ? "#065f46" : "#991b1b";
    }

    private String csvCelda(String val) {
        if (val == null) return "";
        return "\"" + val.replace("\"", "\"\"") + "\"";
    }
}