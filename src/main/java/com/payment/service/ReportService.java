package com.payment.service;

import com.payment.dto.ReportRequestDTO;
import com.payment.entity.Payment;
import com.payment.repository.PaymentRepo;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ReportService {

    private final PaymentRepo repo;

    ReportService(PaymentRepo repo){
        this.repo=repo;
    }

    public ByteArrayInputStream report(ReportRequestDTO req) throws IOException {
        LocalDateTime start = req.getStart().atStartOfDay();
        LocalDateTime end = req.getEnd().atTime(23, 59, 59);

        List<Payment> payments = repo.findByStatusAndPaymentTimeBetween(req.getStatus(), start, end);
        Workbook wrkBook = new XSSFWorkbook();
        Sheet sht = wrkBook.createSheet("Payments");
        Row header = sht.createRow(0);
        header.createCell(0).setCellValue("ID");
        header.createCell(1).setCellValue("Payment Type");
        header.createCell(2).setCellValue("Amount");
        header.createCell(3).setCellValue("Status");
        header.createCell(4).setCellValue("Created By");
        header.createCell(5).setCellValue("Payment Time");

        int rowNum = 1;

        for (Payment p : payments) {
            Row r = sht.createRow(rowNum++);
            r.createCell(0).setCellValue(p.getId());
            r.createCell(1).setCellValue(p.getPaymentType());
            r.createCell(2).setCellValue(p.getAmount());
            r.createCell(3).setCellValue(p.getStatus().toString());
            r.createCell(4).setCellValue(p.getCreatedBy());
            r.createCell(5).setCellValue(p.getPaymentTime().format(DateTimeFormatter.ISO_DATE));
        }

        String folder = "/payment-data/reports";

        File dir = new File(folder);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String filePath = folder + "/PaymentReport.xlsx";

        FileOutputStream fileOut = new FileOutputStream(filePath);
        wrkBook.write(fileOut);
        fileOut.close();

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        wrkBook.write(out);
        wrkBook.close();

        return new ByteArrayInputStream(out.toByteArray());
    }

}
