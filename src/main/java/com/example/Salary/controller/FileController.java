package com.example.Salary.controller;

import com.example.Salary.model.Cadet;
import com.example.Salary.repository.CadetRepository;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.io.*;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

@Controller
public class FileController {
    @Autowired
    private CadetRepository repository;

    private static final int BUFFER_SIZE = 4096;
    private String filePathPDF = "PDFFile.pdf";
    private String filePathExcel = "ExcelFile.xlsx";

    private enum TypeFile {
        PDF,
        EXCEL
    }

    @GetMapping("/download/pdf")
    public void getPDFFile(HttpServletRequest request, HttpServletResponse response) {
        try {
            doDownload(request, response, filePathPDF, TypeFile.PDF);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void getExcelFile(HttpServletRequest request,
                             HttpServletResponse response) {
        try {
            doDownload(request, response, filePathExcel, TypeFile.EXCEL);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void doDownload(HttpServletRequest request,
                           HttpServletResponse response, String filePath, TypeFile typeFile) throws IOException {
        if (typeFile == TypeFile.PDF) {
            createPDFFile(filePath);
        } else if (typeFile == TypeFile.EXCEL) {
            createExcelFile(filePath);
        } else
            return;

        ServletContext context = request.getServletContext();
        String appPath = context.getRealPath("");
        String fullPath = filePath;
        File downloadFile = new File(fullPath);
        FileInputStream inputStream = new FileInputStream(downloadFile);
        String mimeType = context.getMimeType(fullPath);
        if (mimeType == null) {
            mimeType = "application/octet-stream";
        }
        System.out.println("MIME type: " + mimeType);
        response.setContentType(mimeType);
        response.setContentLength((int) downloadFile.length());
        String headerKey = "Content-Disposition";
        String headerValue = String.format("attachment; filename=\"%s\"",
                downloadFile.getName());
        response.setHeader(headerKey, headerValue);
        OutputStream outStream = response.getOutputStream();
        byte[] buffer = new byte[BUFFER_SIZE];
        int bytesRead = -1;
        while ((bytesRead = inputStream.read(buffer)) != -1) {
            outStream.write(buffer, 0, bytesRead);
        }
        inputStream.close();
        outStream.close();
    }

    private void createExcelFile(String filePath) throws IOException {
        XSSFWorkbook workbook = new XSSFWorkbook();


        XSSFSheet spreadsheet
                = workbook.createSheet(" Student Data ");
        XSSFRow row;
        Map<String, Object[]> cadetData
                = new TreeMap<String, Object[]>();
        cadetData.put(
                "1",
                new Object[]

                        {
                                "Name", "Surname", "City"
                        });
        int i = 1;
        for (
                Cadet cadet : repository.findAll()) {
            cadetData.put(String.valueOf(i + 1), new Object[]{cadet.getName(), cadet.getBonus(), cadet.getDate()});
            i++;
        }

        Set<String> keyid = cadetData.keySet();
        int rowid = 0;
        for (
                String key : keyid) {
            row = spreadsheet.createRow(rowid++);
            Object[] objectArr = cadetData.get(key);
            int cellid = 0;
            for (Object obj : objectArr) {
                Cell cell = row.createCell(cellid++);
                cell.setCellValue((String) obj);
            }
        }

        FileOutputStream out = new FileOutputStream(filePath);
        workbook.write(out);
        out.close();
    }

    private void createPDFFile(String filePath) throws IOException {
        Document doc = new Document();
        try {
            PdfWriter.getInstance(doc, new FileOutputStream(filePath));
            doc.open();
            String text = "Report\n" +
                    "from 07/28/23 to 08/08/23 cadets have a rest\n";
            Paragraph paragraph = new Paragraph(text);
            paragraph.setAlignment(Element.ALIGN_JUSTIFIED);
            paragraph.setFont(new Font(
                    Font.FontFamily.HELVETICA, 10, Font.NORMAL));
            doc.add(paragraph);
            PdfPTable table = new PdfPTable(3);
            PdfPCell cell1 = new PdfPCell(new Phrase("Name"));
            PdfPCell cell2 = new PdfPCell(new Phrase("Bonus"));
            PdfPCell cell3 = new PdfPCell(new Phrase("Date"));
            table.addCell(cell1);
            table.addCell(cell2);
            table.addCell(cell3);
            for (Cadet cadet : repository.findAll()) {
                PdfPCell cellName = new PdfPCell(new Phrase(cadet.getName()));
                PdfPCell cellSurname = new PdfPCell(new Phrase(cadet.getBonus()));
                PdfPCell cellCity = new PdfPCell(new Phrase(String.valueOf(cadet.getDate())));
                table.addCell(cellName);
                table.addCell(cellSurname);
                table.addCell(cellCity);
            }
            doc.add(table);
        } catch (FileNotFoundException | DocumentException e) {
            e.printStackTrace();
        } finally {
            doc.close();
        }


    }

}

