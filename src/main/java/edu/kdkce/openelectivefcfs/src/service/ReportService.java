package edu.kdkce.openelectivefcfs.src.service;

import edu.kdkce.openelectivefcfs.src.dto.ArchivedReportNameResponse;
import edu.kdkce.openelectivefcfs.src.enums.DepartmentName;
import edu.kdkce.openelectivefcfs.src.model.AllocationCycle;
import edu.kdkce.openelectivefcfs.src.model.Elective;
import edu.kdkce.openelectivefcfs.src.model.PastAllocation;
import edu.kdkce.openelectivefcfs.src.model.Student;
import edu.kdkce.openelectivefcfs.src.repository.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Comparator;
import java.util.List;

@Service
public class ReportService {
    private final ElectiveRepository electiveRepository;
    private final PastAllocationRepository pastAllocationRepository;
    private final AllocationCycleRepository allocationCycleRepository;
    private final StudentRepository studentRepository;
    private final S3Service s3Service;

    @Autowired
    public ReportService(ElectiveRepository electiveRepository, PastAllocationRepository pastAllocationRepository, AllocationCycleRepository allocationCycleRepository, StudentRepository studentRepository, S3Service s3Service) {
        this.electiveRepository = electiveRepository;
        this.pastAllocationRepository = pastAllocationRepository;
        this.allocationCycleRepository = allocationCycleRepository;
        this.studentRepository = studentRepository;
        this.s3Service = s3Service;
    }

    private void createReportHeader(Sheet sheet, String title) {
        Workbook workbook = sheet.getWorkbook();
        CellStyle headerStyle = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 14);
        headerStyle.setFont(font);
        headerStyle.setAlignment(HorizontalAlignment.CENTER);

        Row row1 = sheet.createRow(0);
        Cell cell1 = row1.createCell(0);
        cell1.setCellValue("K.D.K. College Of Engineering, Nagpur");
        cell1.setCellStyle(headerStyle);

        Row row2 = sheet.createRow(1);
        Cell cell2 = row2.createCell(0);
        cell2.setCellValue(title);
        cell2.setCellStyle(headerStyle);

        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 5));
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 0, 5));
    }

    private CellStyle createTableCellStyle(Workbook workbook) {
        CellStyle style = workbook.createCellStyle();
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    public byte[] generateCompleteAllocationReport() {
        List<Student> students = studentRepository.findAll()
                .stream()
                .filter(Student::getIsEnabled) // Assuming `isEnabled()` is the correct method
                .filter(student -> student.getElectiveId() != null)
                .sorted(Comparator.comparing(student -> student.getEmail().toLowerCase()))
                .toList();

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Complete Allocation Report");
            createReportHeader(sheet, "Complete Allocation Report");

            CellStyle headerCellStyle = createTableCellStyle(workbook);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerCellStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(2);
            String[] columns = {"Name", "Email", "Contact No.", "Department", "Allocated Elective", "Elective Department"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }

            CellStyle cellStyle = createTableCellStyle(workbook);
            int rowNum = 3;
            for (Student student : students) {
                Row row = sheet.createRow(rowNum++);
                Cell cell0 = row.createCell(0);
                cell0.setCellValue(student.getName());
                cell0.setCellStyle(cellStyle);

                Cell cell1 = row.createCell(1);
                cell1.setCellValue(student.getEmail());
                cell1.setCellStyle(cellStyle);

                Cell cell2 = row.createCell(2);
                if (student.getContactNumber() != null) {
                    cell2.setCellValue(student.getContactNumber());
                }
                cell2.setCellStyle(cellStyle);

                Cell cell3 = row.createCell(3);
                cell3.setCellValue(student.getDepartment().name());
                cell3.setCellStyle(cellStyle);

                Elective elective = electiveRepository.findById(student.getElectiveId())
                        .orElseThrow(() -> new RuntimeException("Elective not found"));

                Cell cell4 = row.createCell(4);
                cell4.setCellValue(elective.getName());
                cell4.setCellStyle(cellStyle);

                Cell cell5 = row.createCell(5);
                cell5.setCellValue(elective.getDepartmentName().name());
                cell5.setCellStyle(cellStyle);
            }
            sheet.createFreezePane(0, 3);

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] generateUnallocatedStudentsReport() {
        List<Student> students = studentRepository.findAll()
                .stream()
                .filter(Student::getIsEnabled)
                .filter(student -> student.getElectiveId() == null)
                .sorted((u1, u2) -> u1.getEmail().compareToIgnoreCase(u2.getEmail()))
                .toList();

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            Sheet sheet = workbook.createSheet("Unallocated Students Report");
            createReportHeader(sheet, "Unallocated Students Report");

            CellStyle headerCellStyle = createTableCellStyle(workbook);
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerCellStyle.setFont(headerFont);

            Row headerRow = sheet.createRow(2);
            String[] columns = {"Name", "Email", "Contact No.", "Department"};
            for (int i = 0; i < columns.length; i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(columns[i]);
                cell.setCellStyle(headerCellStyle);
            }

            CellStyle cellStyle = createTableCellStyle(workbook);
            int rowNum = 3;
            for (Student student : students) {
                Row row = sheet.createRow(rowNum++);
                Cell cell0 = row.createCell(0);
                cell0.setCellValue(student.getName());
                cell0.setCellStyle(cellStyle);

                Cell cell1 = row.createCell(1);
                cell1.setCellValue(student.getEmail());
                cell1.setCellStyle(cellStyle);

                Cell cell2 = row.createCell(2);
                if (student.getContactNumber() != null) {
                    cell2.setCellValue(student.getContactNumber());
                }
                cell2.setCellStyle(cellStyle);

                Cell cell3 = row.createCell(3);
                cell3.setCellValue(student.getDepartment().name());
                cell3.setCellStyle(cellStyle);
            }
            sheet.createFreezePane(0, 3);

            for (int i = 0; i < columns.length; i++) {
                sheet.autoSizeColumn(i);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] generateDepartmentOutgoingReport() {
        List<Student> students = studentRepository.findAll()
                .stream()
                .filter(Student::getIsEnabled)
                .filter(student -> student.getElectiveId() != null)
                .sorted(Comparator.comparing(Student::getClassRollNumber))
                .toList();

        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            for (DepartmentName department : DepartmentName.values()) {
                Sheet sheet = workbook.createSheet(department.name() + " Outgoing Report");
                createReportHeader(sheet, department.name() + " Outgoing Report");

                CellStyle headerCellStyle = createTableCellStyle(workbook);
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerCellStyle.setFont(headerFont);

                Row headerRow = sheet.createRow(2);
                String[] columns = {"Roll number","KDK ID","Name", "Email", "Contact No.", "Allocated Elective", "Elective Department"};
                for (int i = 0; i < columns.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(columns[i]);
                    cell.setCellStyle(headerCellStyle);
                }

                CellStyle cellStyle = createTableCellStyle(workbook);
                int rowNum = 3;
                for (Student student : students) {
                    if (student.getDepartment() == department) {
                        Row row = sheet.createRow(rowNum++);

                        Cell cell0 = row.createCell(0);
                        cell0.setCellValue(student.getClassRollNumber());
                        cell0.setCellStyle(cellStyle);

                        Cell cell1 = row.createCell(1);
                        cell1.setCellValue(student.getRollNumber()); // roll number is the KDK ID
                        cell1.setCellStyle(cellStyle);

                        Cell cell2 = row.createCell(2);
                        cell2.setCellValue(student.getName());
                        cell2.setCellStyle(cellStyle);

                        Cell cell3 = row.createCell(3);
                        cell3.setCellValue(student.getEmail());
                        cell3.setCellStyle(cellStyle);

                        Cell cell4 = row.createCell(4);
                        if (student.getContactNumber() != null) {
                            cell4.setCellValue(student.getContactNumber());
                        }
                        cell4.setCellStyle(cellStyle);

                        Elective elective = electiveRepository.getElectivesById(student.getElectiveId());

                        Cell cell5 = row.createCell(5);
                        cell5.setCellValue(elective.getName());
                        cell5.setCellStyle(cellStyle);

                        Cell cell6 = row.createCell(6);
                        cell6.setCellValue(elective.getDepartmentName().name());
                        cell6.setCellStyle(cellStyle);
                    }
                }
                sheet.createFreezePane(0, 3);

                for (int i = 0; i < columns.length; i++) {
                    sheet.autoSizeColumn(i);
                }
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] generateDepartmentIncomingReport() {
        List<Student> students = studentRepository.findAll()
                .stream()
                .filter(Student::getIsEnabled)
                .filter(student -> student.getElectiveId() != null)
                .map(user -> (Student) user)
                .sorted((u1, u2) -> u1.getEmail().compareToIgnoreCase(u2.getEmail()))
                .toList();

        List<Elective> electives = electiveRepository.findAll();
        try (Workbook workbook = new XSSFWorkbook();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            for (Elective elective : electives) {
                Sheet sheet = workbook.createSheet(elective.getName() + " (" + elective.getDepartmentName() + ") Student Allocation Report");
                createReportHeader(sheet, elective.getName() + " (" + elective.getDepartmentName() + ") Student Allocation Report");

                CellStyle headerCellStyle = createTableCellStyle(workbook);
                Font headerFont = workbook.createFont();
                headerFont.setBold(true);
                headerCellStyle.setFont(headerFont);

                Row headerRow = sheet.createRow(2);
                String[] columns = {"Name", "Email", "Contact No.", "Department", "Allocated Elective", "Elective Department"};
                for (int i = 0; i < columns.length; i++) {
                    Cell cell = headerRow.createCell(i);
                    cell.setCellValue(columns[i]);
                    cell.setCellStyle(headerCellStyle);
                }

                CellStyle cellStyle = createTableCellStyle(workbook);
                int rowNum = 3;
                for (Student student : students) {
                    if (student.getElectiveId().equals(elective.getId())) {
                        Row row = sheet.createRow(rowNum++);
                        Cell cell0 = row.createCell(0);
                        cell0.setCellValue(student.getName());
                        cell0.setCellStyle(cellStyle);

                        Cell cell1 = row.createCell(1);
                        cell1.setCellValue(student.getEmail());
                        cell1.setCellStyle(cellStyle);

                        Cell cell2 = row.createCell(2);
                        if (student.getContactNumber() != null) {
                            cell2.setCellValue(student.getContactNumber());
                        }
                        cell2.setCellStyle(cellStyle);

                        Cell cell3 = row.createCell(3);
                        cell3.setCellValue(student.getDepartment().name());
                        cell3.setCellStyle(cellStyle);

                        Cell cell4 = row.createCell(4);
                        cell4.setCellValue(elective.getName());
                        cell4.setCellStyle(cellStyle);

                        Cell cell5 = row.createCell(5);
                        cell5.setCellValue(elective.getDepartmentName().name());
                        cell5.setCellStyle(cellStyle);
                    }
                }
                sheet.createFreezePane(0, 3);

                for (int i = 0; i < columns.length; i++) {
                    sheet.autoSizeColumn(i);
                }
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

   //For archived reports
   public byte[] getIncomingReportsForCycle(String cycleId) {
       // Get students form the particular cycle
       // Iterate over Departments and create a sheet for each department
       // Each sheet will have students of the other department
       AllocationCycle allocationCycle = allocationCycleRepository.findById(cycleId)
               .orElseThrow(() -> new RuntimeException("Allocation cycle not found"));

       List<PastAllocation> pastAllocationList = pastAllocationRepository.findByAllocationCycleId(allocationCycle.getId());
       try (Workbook workbook = new XSSFWorkbook();
            ByteArrayOutputStream out = new ByteArrayOutputStream()) {

           for (DepartmentName department : DepartmentName.values()) {
               Sheet sheet = workbook.createSheet(department.name() + " Incoming Report");
               createReportHeader(sheet, department.name() + " Incoming Report");

               CellStyle headerCellStyle = createTableCellStyle(workbook);
               Font headerFont = workbook.createFont();
               headerFont.setBold(true);
               headerCellStyle.setFont(headerFont);

               Row headerRow = sheet.createRow(2);
               String[] columns = {"Name", "Email", "Branch", "Contact No.", "Allocated Elective", "Elective Department"};
               for (int i = 0; i < columns.length; i++) {
                   Cell cell = headerRow.createCell(i);
                   cell.setCellValue(columns[i]);
                   cell.setCellStyle(headerCellStyle);
               }

               CellStyle cellStyle = createTableCellStyle(workbook);
               int rowNum = 3;
               for (PastAllocation pastAllocation : pastAllocationList) {
                   if (pastAllocation.getElectiveDepartment().equals(department)) {
                       Row row = sheet.createRow(rowNum++);
                       Cell cell0 = row.createCell(0);
                       cell0.setCellValue(pastAllocation.getStudentName());
                       cell0.setCellStyle(cellStyle);

                       Cell cell1 = row.createCell(1);
                       cell1.setCellValue(pastAllocation.getMailId());
                       cell1.setCellStyle(cellStyle);

                       Cell cell2 = row.createCell(2);
                       cell2.setCellValue(pastAllocation.getStudentDepartment().name());
                       cell2.setCellStyle(cellStyle);

                       Cell cell3 = row.createCell(3);
                       if (pastAllocation.getContactNumber() != null) {
                           cell3.setCellValue(pastAllocation.getContactNumber());
                       }
                       cell3.setCellStyle(cellStyle);

                       Cell cell4 = row.createCell(4);
                       cell4.setCellValue(pastAllocation.getElectiveName());
                       cell4.setCellStyle(cellStyle);

                       Cell cell5 = row.createCell(5);
                       cell5.setCellValue(pastAllocation.getElectiveDepartment().name());
                       cell5.setCellStyle(cellStyle);
                   }
               }
               sheet.createFreezePane(0, 3);

               for (int i = 0; i < columns.length; i++) {
                   sheet.autoSizeColumn(i);
               }
           }
           workbook.write(out);
           return out.toByteArray();

       } catch (IOException e) {
           throw new RuntimeException(e);
       }
   }

   public byte[] getOutgoingReportsForCycle(String cycleId) {
       // Get students form the particular cycle
       // Iterate over Departments and create a sheet for each department
       // Each sheet will have students of the other department
       AllocationCycle allocationCycle = allocationCycleRepository.findById(cycleId)
               .orElseThrow(() -> new RuntimeException("Allocation cycle not found"));



       List<PastAllocation> pastAllocationList = pastAllocationRepository.findByAllocationCycleId(allocationCycle.getId());
       try (Workbook workbook = new XSSFWorkbook();
            ByteArrayOutputStream out = new ByteArrayOutputStream()) {

           for (DepartmentName department : DepartmentName.values()) {
               Sheet sheet = workbook.createSheet(department.name() + " Outgoing Report");
               createReportHeader(sheet, department.name() + " Outgoing Report");

               CellStyle headerCellStyle = createTableCellStyle(workbook);
               Font headerFont = workbook.createFont();
               headerFont.setBold(true);
               headerCellStyle.setFont(headerFont);

               Row headerRow = sheet.createRow(2);
               String[] columns = {"Name", "Email", "Branch", "Contact No.", "Allocated Elective", "Elective Department"};
               for (int i = 0; i < columns.length; i++) {
                   Cell cell = headerRow.createCell(i);
                   cell.setCellValue(columns[i]);
                   cell.setCellStyle(headerCellStyle);
               }

               CellStyle cellStyle = createTableCellStyle(workbook);
               int rowNum = 3;
               for (PastAllocation pastAllocation : pastAllocationList) {
                   if (pastAllocation.getStudentDepartment().equals(department)) {
                       Row row = sheet.createRow(rowNum++);
                       Cell cell0 = row.createCell(0);
                       cell0.setCellValue(pastAllocation.getStudentName());
                       cell0.setCellStyle(cellStyle);

                       Cell cell1 = row.createCell(1);
                       cell1.setCellValue(pastAllocation.getMailId());
                       cell1.setCellStyle(cellStyle);

                       Cell cell2 = row.createCell(2);
                       cell2.setCellValue(pastAllocation.getStudentDepartment().name());
                       cell2.setCellStyle(cellStyle);

                       Cell cell3 = row.createCell(3);
                       if (pastAllocation.getContactNumber() != null) {
                           cell3.setCellValue(pastAllocation.getContactNumber());
                       }
                       cell3.setCellStyle(cellStyle);

                       Cell cell4 = row.createCell(4);
                       cell4.setCellValue(pastAllocation.getElectiveName());
                       cell4.setCellStyle(cellStyle);

                       Cell cell5 = row.createCell(5);
                       cell5.setCellValue(pastAllocation.getElectiveDepartment().name());
                       cell5.setCellStyle(cellStyle);
                   }
               }
               sheet.createFreezePane(0, 3);

               for (int i = 0; i < columns.length; i++) {
                   sheet.autoSizeColumn(i);
               }
           }

           workbook.write(out);
           return out.toByteArray();
       } catch (IOException e) {
           throw new RuntimeException(e);
       }
   }
    public List<ArchivedReportNameResponse> getArchivedReports() {
        // Get all allocation cycles
        List<AllocationCycle> allocationCycles = allocationCycleRepository.findAll();
        // Convert to ArchivedReportResponse with properly formatted cycle name
        return allocationCycles.stream()
                .map(allocationCycle -> new ArchivedReportNameResponse(
                        allocationCycle.getId(),
                        allocationCycle.getCycleName().replaceAll("[{}\"]", "").replace("cycleName:", "").trim() // Clean up the cycle name
                ))
                .toList();
    }

    public String uploadDepartmentOutgoingReportAndGetLink() {
        byte[] data = generateDepartmentOutgoingReport(); // already exists
        return uploadReportAndGetLink("reports/Department_Wise_Outgoing_Report.xlsx", data);
    }
    private String uploadReportAndGetLink(String s3Key, byte[] data) {
        try {
            Path tempFile = Files.createTempFile("report-", ".xlsx");
            Files.write(tempFile, data);
            s3Service.uploadFile(s3Key, tempFile);
            return s3Service.generatePresignedUrl(s3Key, Duration.ofMinutes(10));
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload report", e);
        }
    }

    public String uploadDepartmentIncomingReportAndGetLink() {
        byte[] data = generateDepartmentIncomingReport();
        return uploadReportAndGetLink("reports/Department_Wise_Incoming_Report.xlsx", data);
    }
    public String uploadCompleteAllocationReportAndGetLink() {
        byte[] data = generateCompleteAllocationReport();
        return uploadReportAndGetLink("reports/Complete_Allocation_Report.xlsx", data);
    }
    public String uploadUnallocatedStudentsReportAndGetLink() {
        byte[] data = generateUnallocatedStudentsReport();
        return uploadReportAndGetLink("reports/Unallocated_Students_Report.xlsx", data);
    }
    public String uploadArchivedReportAndGetLink(String cycleId, String reportType) {
        byte[] reportData;

        if ("outgoing".equalsIgnoreCase(reportType)) {
            reportData = getOutgoingReportsForCycle(cycleId);
        } else if ("incoming".equalsIgnoreCase(reportType)) {
            reportData = getIncomingReportsForCycle(cycleId);
        } else {
            throw new IllegalArgumentException("Invalid report type");
        }

        AllocationCycle cycle = allocationCycleRepository.findById(cycleId)
                .orElseThrow(() -> new RuntimeException("Cycle not found"));
        String cycleName = cycle.getCycleName().replaceAll("[{}\"]", "").replace("cycleName:", "").trim();
        String filename = cycleName + "_Department-Wise_" + reportType + "_Report.xlsx";
        String s3Key = "archived-reports/" + filename;

        return uploadReportAndGetLink(s3Key, reportData);
    }

}