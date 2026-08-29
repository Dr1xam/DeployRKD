package com.example.deeployrkd.service;

import com.example.deeployrkd.dto.ManagerSummaryDto;
import com.example.deeployrkd.dto.RegionSummaryDto;
import com.example.deeployrkd.dto.ReportSummaryDto;
import com.example.deeployrkd.dto.SaleResponse;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.category.StandardBarPainter;
import org.jfree.data.category.DefaultCategoryDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class PdfGeneratorService {

    private static final Logger log = LoggerFactory.getLogger(PdfGeneratorService.class);

    private static final DecimalFormat CURRENCY_FORMAT;
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final DateTimeFormatter DATETIME_FORMAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    static {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(new Locale("uk", "UA"));
        symbols.setGroupingSeparator(' ');
        symbols.setDecimalSeparator(',');
        CURRENCY_FORMAT = new DecimalFormat("#,##0.00 ₴", symbols);
    }

    // Colors
    private static final Color COLOR_PRIMARY = new Color(30, 58, 138); // Navy Blue
    private static final Color COLOR_SECONDARY = new Color(59, 130, 246); // Blue
    private static final Color COLOR_HEADER_BG = new Color(30, 41, 59); // Slate 800
    private static final Color COLOR_TABLE_ALT = new Color(248, 250, 252); // Slate 50
    private static final Color COLOR_TOTAL_BG = new Color(224, 242, 254); // Light Sky
    private static final Color COLOR_TEXT_MUTED = new Color(100, 116, 139);

    private BaseFont baseFont;
    private BaseFont baseFontBold;
    private java.awt.Font awtFontRegular;
    private java.awt.Font awtFontBold;

    public PdfGeneratorService() {
        initFonts();
    }

    private void initFonts() {
        try {
            ClassPathResource fontResource = new ClassPathResource("fonts/DejaVuSans.ttf");
            ClassPathResource fontBoldResource = new ClassPathResource("fonts/DejaVuSans-Bold.ttf");

            byte[] fontBytes;
            byte[] fontBoldBytes;

            try (InputStream is = fontResource.getInputStream()) {
                fontBytes = is.readAllBytes();
            }
            try (InputStream is = fontBoldResource.getInputStream()) {
                fontBoldBytes = is.readAllBytes();
            }

            this.baseFont = BaseFont.createFont("DejaVuSans.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, fontBytes, null);
            this.baseFontBold = BaseFont.createFont("DejaVuSans-Bold.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED, true, fontBoldBytes, null);

            try (InputStream is = new ClassPathResource("fonts/DejaVuSans.ttf").getInputStream()) {
                this.awtFontRegular = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, is);
            }
            try (InputStream is = new ClassPathResource("fonts/DejaVuSans-Bold.ttf").getInputStream()) {
                this.awtFontBold = java.awt.Font.createFont(java.awt.Font.TRUETYPE_FONT, is);
            }

            log.info("Шрифти DejaVu Sans з підтримкою кирилиці успішно завантажено.");
        } catch (Exception e) {
            log.error("Помилка завантаження шрифтів DejaVu Sans: {}. Використовую стандартні шрифти.", e.getMessage());
            try {
                this.baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
                this.baseFontBold = BaseFont.createFont(BaseFont.HELVETICA_BOLD, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
                this.awtFontRegular = new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 12);
                this.awtFontBold = new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12);
            } catch (Exception ex) {
                log.error("Критична помилка ініціалізації шрифтів", ex);
            }
        }
    }

    public byte[] generatePdfReport(ReportSummaryDto summary) {
        Document document = new Document(PageSize.A4, 36, 36, 40, 45);
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            PdfWriter writer = PdfWriter.getInstance(document, out);
            writer.setPageEvent(new PdfFooterEvent(baseFont));

            document.open();
            document.addTitle("Звіт про продажі");
            document.addSubject("Звіт про продажі за період: " + summary.getPeriodTitle());
            document.addAuthor("DeeployRKD");

            // 1. Header Banner
            addHeaderBanner(document, summary);

            // 2. KPI Summary Boxes
            addKpiSection(document, summary);

            // 3. Summary Tables (Regions & Managers side-by-side or stacked)
            addSummaryBreakdownTables(document, summary);

            // 4. Chart (Sales by Region)
            if (!summary.getRegionSummaries().isEmpty()) {
                addRegionChart(document, summary);
            }

            // 5. Detailed Sales Table
            addDetailedSalesTable(document, summary);

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            log.error("Помилка під час генерації PDF: ", e);
            throw new RuntimeException("Не вдалося згенерувати PDF-звіт: " + e.getMessage(), e);
        }
    }

    private void addHeaderBanner(Document document, ReportSummaryDto summary) throws DocumentException {
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{65f, 35f});
        headerTable.setSpacingAfter(15f);

        // Left Column: Company & Report Title
        PdfPCell leftCell = new PdfPCell();
        leftCell.setBorder(Rectangle.NO_BORDER);

        Paragraph companyName = new Paragraph("DEEPLOY RKD ENTERPRISE", new Font(baseFontBold, 10, Font.BOLD, COLOR_SECONDARY));
        companyName.setSpacingAfter(3f);
        leftCell.addElement(companyName);

        Paragraph reportTitle = new Paragraph("Звіт про результати продажів", new Font(baseFontBold, 18, Font.BOLD, COLOR_PRIMARY));
        reportTitle.setSpacingAfter(4f);
        leftCell.addElement(reportTitle);

        Paragraph periodPara = new Paragraph(summary.getPeriodTitle(), new Font(baseFont, 11, Font.NORMAL, COLOR_TEXT_MUTED));
        leftCell.addElement(periodPara);

        // Right Column: Date & Metadata
        PdfPCell rightCell = new PdfPCell();
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

        Paragraph dateTitle = new Paragraph("Дата формування:", new Font(baseFontBold, 9, Font.BOLD, COLOR_TEXT_MUTED));
        dateTitle.setAlignment(Element.ALIGN_RIGHT);
        rightCell.addElement(dateTitle);

        Paragraph dateValue = new Paragraph(LocalDateTime.now().format(DATETIME_FORMAT), new Font(baseFont, 10, Font.NORMAL, Color.BLACK));
        dateValue.setAlignment(Element.ALIGN_RIGHT);
        rightCell.addElement(dateValue);

        Paragraph statusBadge = new Paragraph("ОФІЦІЙНИЙ ЗВІТ", new Font(baseFontBold, 8, Font.BOLD, COLOR_PRIMARY));
        statusBadge.setAlignment(Element.ALIGN_RIGHT);
        statusBadge.setSpacingBefore(4f);
        rightCell.addElement(statusBadge);

        headerTable.addCell(leftCell);
        headerTable.addCell(rightCell);
        document.add(headerTable);
    }

    private void addKpiSection(Document document, ReportSummaryDto summary) throws DocumentException {
        PdfPTable kpiTable = new PdfPTable(3);
        kpiTable.setWidthPercentage(100);
        kpiTable.setWidths(new float[]{33.3f, 33.3f, 33.4f});
        kpiTable.setSpacingAfter(15f);

        kpiTable.addCell(createKpiCard("ЗАГАЛЬНИЙ ВИТОРГ", CURRENCY_FORMAT.format(summary.getTotalAmount()), COLOR_PRIMARY));
        kpiTable.addCell(createKpiCard("КІЛЬКІСТЬ УГОД", String.valueOf(summary.getTotalSalesCount()), new Color(13, 148, 136)));
        kpiTable.addCell(createKpiCard("СЕРЕДНІЙ ЧЕК", CURRENCY_FORMAT.format(summary.getAverageCheck()), new Color(124, 58, 237)));

        document.add(kpiTable);
    }

    private PdfPCell createKpiCard(String label, String value, Color accentColor) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(new Color(248, 250, 252));
        cell.setBorderColor(new Color(226, 232, 240));
        cell.setBorderWidth(1f);
        cell.setPadding(10f);

        Paragraph labelPara = new Paragraph(label, new Font(baseFontBold, 8, Font.BOLD, COLOR_TEXT_MUTED));
        labelPara.setSpacingAfter(4f);
        cell.addElement(labelPara);

        Paragraph valuePara = new Paragraph(value, new Font(baseFontBold, 14, Font.BOLD, accentColor));
        cell.addElement(valuePara);

        return cell;
    }

    private void addSummaryBreakdownTables(Document document, ReportSummaryDto summary) throws DocumentException {
        PdfPTable container = new PdfPTable(2);
        container.setWidthPercentage(100);
        container.setWidths(new float[]{49f, 51f});
        container.setSpacingAfter(15f);

        // Left: Regions Table
        PdfPCell leftContainerCell = new PdfPCell();
        leftContainerCell.setBorder(Rectangle.NO_BORDER);
        leftContainerCell.setPaddingRight(5f);

        Paragraph regionHeading = new Paragraph("Розбивка по регіонах", new Font(baseFontBold, 11, Font.BOLD, COLOR_PRIMARY));
        regionHeading.setSpacingAfter(6f);
        leftContainerCell.addElement(regionHeading);

        PdfPTable regionTable = new PdfPTable(4);
        regionTable.setWidthPercentage(100);
        regionTable.setWidths(new float[]{34f, 33f, 15f, 18f});

        addTableHeader(regionTable, new String[]{"Регіон", "Сума", "К-сть", "Частка"});

        boolean alt = false;
        for (RegionSummaryDto r : summary.getRegionSummaries()) {
            Color rowBg = alt ? COLOR_TABLE_ALT : Color.WHITE;
            regionTable.addCell(createCell(r.getRegion(), baseFont, 8, Element.ALIGN_LEFT, rowBg));
            regionTable.addCell(createCell(CURRENCY_FORMAT.format(r.getTotalAmount()), baseFontBold, 8, Element.ALIGN_RIGHT, rowBg));
            regionTable.addCell(createCell(String.valueOf(r.getCount()), baseFont, 8, Element.ALIGN_CENTER, rowBg));
            regionTable.addCell(createCell(String.format(Locale.US, "%.1f%%", r.getPercentage()), baseFont, 8, Element.ALIGN_RIGHT, rowBg));
            alt = !alt;
        }

        leftContainerCell.addElement(regionTable);

        // Right: Managers Table (Top Sellers)
        PdfPCell rightContainerCell = new PdfPCell();
        rightContainerCell.setBorder(Rectangle.NO_BORDER);
        rightContainerCell.setPaddingLeft(5f);

        Paragraph managerHeading = new Paragraph("Топ-продавці (по менеджерах)", new Font(baseFontBold, 11, Font.BOLD, COLOR_PRIMARY));
        managerHeading.setSpacingAfter(6f);
        rightContainerCell.addElement(managerHeading);

        PdfPTable managerTable = new PdfPTable(4);
        managerTable.setWidthPercentage(100);
        managerTable.setWidths(new float[]{38f, 32f, 14f, 16f});

        addTableHeader(managerTable, new String[]{"Менеджер", "Сума", "К-сть", "Частка"});

        alt = false;
        for (ManagerSummaryDto m : summary.getManagerSummaries()) {
            Color rowBg = alt ? COLOR_TABLE_ALT : Color.WHITE;
            managerTable.addCell(createCell(m.getManager(), baseFont, 8, Element.ALIGN_LEFT, rowBg));
            managerTable.addCell(createCell(CURRENCY_FORMAT.format(m.getTotalAmount()), baseFontBold, 8, Element.ALIGN_RIGHT, rowBg));
            managerTable.addCell(createCell(String.valueOf(m.getCount()), baseFont, 8, Element.ALIGN_CENTER, rowBg));
            managerTable.addCell(createCell(String.format(Locale.US, "%.1f%%", m.getPercentage()), baseFont, 8, Element.ALIGN_RIGHT, rowBg));
            alt = !alt;
        }

        rightContainerCell.addElement(managerTable);

        container.addCell(leftContainerCell);
        container.addCell(rightContainerCell);
        document.add(container);
    }

    private void addRegionChart(Document document, ReportSummaryDto summary) {
        try {
            DefaultCategoryDataset dataset = new DefaultCategoryDataset();
            for (RegionSummaryDto r : summary.getRegionSummaries()) {
                dataset.addValue(r.getTotalAmount().doubleValue(), "Продажі (₴)", r.getRegion());
            }

            JFreeChart barChart = ChartFactory.createBarChart(
                    "Розподіл продажів за регіонами (₴)",
                    "Регіон",
                    "Сума (₴)",
                    dataset,
                    PlotOrientation.VERTICAL,
                    false, true, false
            );

            // Chart styling with Cyrillic font
            barChart.setBackgroundPaint(Color.WHITE);
            barChart.getTitle().setFont(awtFontBold != null ? awtFontBold.deriveFont(12f) : new java.awt.Font("SansSerif", java.awt.Font.BOLD, 12));
            barChart.getTitle().setPaint(COLOR_PRIMARY);

            CategoryPlot plot = barChart.getCategoryPlot();
            plot.setBackgroundPaint(new Color(248, 250, 252));
            plot.setDomainGridlinePaint(new Color(226, 232, 240));
            plot.setRangeGridlinePaint(new Color(203, 213, 225));

            CategoryAxis domainAxis = plot.getDomainAxis();
            domainAxis.setTickLabelFont(awtFontRegular != null ? awtFontRegular.deriveFont(9f) : new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 9));
            domainAxis.setLabelFont(awtFontBold != null ? awtFontBold.deriveFont(10f) : new java.awt.Font("SansSerif", java.awt.Font.BOLD, 10));

            NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
            rangeAxis.setTickLabelFont(awtFontRegular != null ? awtFontRegular.deriveFont(9f) : new java.awt.Font("SansSerif", java.awt.Font.PLAIN, 9));
            rangeAxis.setLabelFont(awtFontBold != null ? awtFontBold.deriveFont(10f) : new java.awt.Font("SansSerif", java.awt.Font.BOLD, 10));

            BarRenderer renderer = (BarRenderer) plot.getRenderer();
            renderer.setBarPainter(new StandardBarPainter());
            renderer.setSeriesPaint(0, COLOR_SECONDARY);

            BufferedImage chartImage = barChart.createBufferedImage(520, 180);
            ByteArrayOutputStream chartBaos = new ByteArrayOutputStream();
            ImageIO.write(chartImage, "png", chartBaos);

            Image pdfChartImage = Image.getInstance(chartBaos.toByteArray());
            pdfChartImage.setAlignment(Element.ALIGN_CENTER);
            pdfChartImage.setSpacingAfter(15f);

            document.add(pdfChartImage);
        } catch (Exception e) {
            log.warn("Не вдалося додати графік у PDF: {}", e.getMessage());
        }
    }

    private void addDetailedSalesTable(Document document, ReportSummaryDto summary) throws DocumentException {
        Paragraph tableTitle = new Paragraph("Деталізований реєстр продажів", new Font(baseFontBold, 12, Font.BOLD, COLOR_PRIMARY));
        tableTitle.setSpacingAfter(8f);
        document.add(tableTitle);

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[]{6f, 14f, 24f, 26f, 15f, 15f});
        table.setHeaderRows(1);
        table.setSpacingAfter(15f);

        addTableHeader(table, new String[]{"№", "Дата", "Менеджер", "Товар / Послуга", "Регіон", "Сума (₴)"});

        int index = 1;
        boolean alt = false;
        for (SaleResponse s : summary.getSales()) {
            Color rowBg = alt ? COLOR_TABLE_ALT : Color.WHITE;
            table.addCell(createCell(String.valueOf(index++), baseFont, 8, Element.ALIGN_CENTER, rowBg));
            table.addCell(createCell(s.getDate().format(DATE_FORMAT), baseFont, 8, Element.ALIGN_CENTER, rowBg));
            table.addCell(createCell(s.getManager(), baseFont, 8, Element.ALIGN_LEFT, rowBg));
            table.addCell(createCell(s.getProduct(), baseFont, 8, Element.ALIGN_LEFT, rowBg));
            table.addCell(createCell(s.getRegion(), baseFont, 8, Element.ALIGN_LEFT, rowBg));
            table.addCell(createCell(CURRENCY_FORMAT.format(s.getAmount()), baseFontBold, 8, Element.ALIGN_RIGHT, rowBg));
            alt = !alt;
        }

        // Total Row
        PdfPCell totalLabelCell = new PdfPCell(new Phrase("РАЗОМ:", new Font(baseFontBold, 9, Font.BOLD, COLOR_PRIMARY)));
        totalLabelCell.setColspan(5);
        totalLabelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalLabelCell.setBackgroundColor(COLOR_TOTAL_BG);
        totalLabelCell.setPadding(6f);
        totalLabelCell.setBorderColor(new Color(186, 230, 253));
        table.addCell(totalLabelCell);

        PdfPCell totalValCell = new PdfPCell(new Phrase(CURRENCY_FORMAT.format(summary.getTotalAmount()), new Font(baseFontBold, 9, Font.BOLD, COLOR_PRIMARY)));
        totalValCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalValCell.setBackgroundColor(COLOR_TOTAL_BG);
        totalValCell.setPadding(6f);
        totalValCell.setBorderColor(new Color(186, 230, 253));
        table.addCell(totalValCell);

        document.add(table);
    }

    private void addTableHeader(PdfPTable table, String[] headers) {
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, new Font(baseFontBold, 8, Font.BOLD, Color.WHITE)));
            cell.setBackgroundColor(COLOR_HEADER_BG);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(5f);
            cell.setBorderColor(new Color(51, 65, 85));
            table.addCell(cell);
        }
    }

    private PdfPCell createCell(String text, BaseFont font, float size, int align, Color bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", new Font(font, size, Font.NORMAL, Color.BLACK)));
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBackgroundColor(bg);
        cell.setPadding(4.5f);
        cell.setBorderColor(new Color(226, 232, 240));
        return cell;
    }

    // Page event for footer and page numbers
    private static class PdfFooterEvent extends PdfPageEventHelper {
        private final BaseFont font;

        public PdfFooterEvent(BaseFont font) {
            this.font = font;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfPCell cell = new PdfPCell(new Phrase(
                    "Сторінка " + writer.getPageNumber() + " | Автоматично сформовано системою DeeployRKD",
                    new Font(font, 7, Font.ITALIC, COLOR_TEXT_MUTED)
            ));
            cell.setBorder(Rectangle.TOP);
            cell.setBorderColor(new Color(226, 232, 240));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPaddingTop(5f);

            PdfPTable footer = new PdfPTable(1);
            footer.setTotalWidth(document.right() - document.left());
            footer.addCell(cell);
            footer.writeSelectedRows(0, -1, document.left(), document.bottom() - 5, writer.getDirectContent());
        }
    }
}
