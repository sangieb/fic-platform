package com.fic.backend.service;

import com.fic.backend.model.FicData;
import com.fic.backend.model.Reporte;
import com.fic.backend.repository.FicDataRepository;
import com.fic.backend.repository.ReporteRepository;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Servicio para la generación de reportes PDF profesionales usando JasperReports.
 * <p>
 * Utiliza plantillas {@code .jrxml} compiladas en tiempo de ejecución para
 * generar reportes con los datos de los fondos de inversión. Los reportes
 * son traducidos según el idioma activo del sistema.
 * </p>
 *
 * @author Angie
 * @version 1.0
 * @since 2026
 */
@Service
@RequiredArgsConstructor
public class JasperReporteService {

    /** Repositorio para obtener los fondos de inversión. */
    private final FicDataRepository ficRepo;

    /** Repositorio para registrar los reportes generados. */
    private final ReporteRepository reporteRepo;

    /** Servicio de auditoría para registrar la generación de reportes. */
    private final AuditService auditService;

    /** Fuente de mensajes para internacionalización de títulos. */
    private final MessageSource messageSource;

    /**
     * Genera un reporte PDF profesional con JasperReports.
     * <p>
     * Carga la plantilla {@code fic_reporte.jrxml} desde el classpath,
     * la compila, la llena con los datos de los fondos y la exporta
     * como arreglo de bytes PDF.
     * </p>
     *
     * @return Arreglo de bytes del PDF generado
     * @throws Exception si ocurre un error al compilar o llenar el reporte
     */
    public byte[] generarPDF() throws Exception {
        Locale locale = LocaleContextHolder.getLocale();
        List<FicData> fondos = ficRepo.findAll();

        String titulo = messageSource.getMessage(
            "reporte.pdf.titulo", null, locale);
        String generado = messageSource.getMessage(
            "reporte.generado", null, locale);
        String fecha = LocalDateTime.now()
            .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

        // Carga y compila la plantilla JRXML
        InputStream template = new ClassPathResource(
            "reportes/fic_reporte.jrxml").getInputStream();
        JasperReport jasperReport = JasperCompileManager
            .compileReport(template);

        // Parámetros del reporte
        Map<String, Object> params = new HashMap<>();
        params.put("TITULO", titulo);
        params.put("GENERADO", generado + " " + fecha);
        params.put("TOTAL", fondos.size());

        // Fuente de datos desde la lista de fondos
        JRBeanCollectionDataSource dataSource =
            new JRBeanCollectionDataSource(fondos);

        // Llena el reporte con datos
        JasperPrint jasperPrint = JasperFillManager
            .fillReport(jasperReport, params, dataSource);

        // Exporta a PDF
        byte[] pdf = JasperExportManager.exportReportToPdf(jasperPrint);

        // Auditoría y registro
        Reporte r = new Reporte();
        r.setTitulo(titulo);
        r.setFormato("PDF-JASPER");
        r.setGeneratedAt(LocalDateTime.now());
        reporteRepo.save(r);

        auditService.registrar("Reporte", "PDF-JASPER", "CREATE",
            "sistema", "Generacion reporte PDF con JasperReports");

        return pdf;
    }
}