package edu.kdkce.openelectivefcfs.src.service;

import edu.kdkce.openelectivefcfs.src.dto.ArchivedReportNameResponse;
import edu.kdkce.openelectivefcfs.src.enums.DepartmentName;
import edu.kdkce.openelectivefcfs.src.model.AllocationCycle;
import edu.kdkce.openelectivefcfs.src.model.Elective;
import edu.kdkce.openelectivefcfs.src.model.PastAllocation;
import edu.kdkce.openelectivefcfs.src.model.Student;
import edu.kdkce.openelectivefcfs.src.repository.AllocationCycleRepository;
import edu.kdkce.openelectivefcfs.src.repository.ElectiveRepository;
import edu.kdkce.openelectivefcfs.src.repository.PastAllocationRepository;
import edu.kdkce.openelectivefcfs.src.repository.UserRepository;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class ReportService {
    private final UserRepository userRepository;
    private final ElectiveRepository electiveRepository;
    private final PastAllocationRepository pastAllocationRepository;
    private final AllocationCycleRepository allocationCycleRepository;

    @Autowired
    public ReportService(UserRepository userRepository, ElectiveRepository electiveRepository, PastAllocationRepository pastAllocationRepository, AllocationCycleRepository allocationCycleRepository) {
        this.userRepository = userRepository;
        this.electiveRepository = electiveRepository;
        this.pastAllocationRepository = pastAllocationRepository;
        this.allocationCycleRepository = allocationCycleRepository;
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
        List<Student> students = userRepository.findAll()
                .stream()
                .filter(user -> user instanceof Student)
                .filter(user -> ((Student) user).getEnabled())
                .filter(user -> ((Student) user).getElective() != null)
                .map(user -> (Student) user)
                .sorted((u1, u2) -> u1.getEmail().compareToIgnoreCase(u2.getEmail()))
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

                Cell cell4 = row.createCell(4);
                cell4.setCellValue(student.getElective().getName());
                cell4.setCellStyle(cellStyle);

                Cell cell5 = row.createCell(5);
                cell5.setCellValue(student.getElective().getDepartmentName().name());
                cell5.setCellStyle(cellStyle);
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] generateUnallocatedStudentsReport() {
        List<Student> students = userRepository.findAll()
                .stream()
                .filter(user -> user instanceof Student)
                .filter(user -> ((Student) user).getEnabled())
                .filter(user -> ((Student) user).getElective() == null)
                .map(user -> (Student) user)
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

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] generateDepartmentOutgoingReport() {
        List<Student> students = userRepository.findAll()
                .stream()
                .filter(user -> user instanceof Student)
                .filter(user -> ((Student) user).getEnabled())
                .filter(user -> ((Student) user).getElective() != null)
                .map(user -> (Student) user)
                .sorted((u1, u2) -> u1.getEmail().compareToIgnoreCase(u2.getEmail()))
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
                String[] columns = {"Name", "Email", "Contact No.", "Allocated Elective", "Elective Department"};
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
                        cell3.setCellValue(student.getElective().getName());
                        cell3.setCellStyle(cellStyle);

                        Cell cell4 = row.createCell(4);
                        cell4.setCellValue(student.getElective().getDepartmentName().name());
                        cell4.setCellStyle(cellStyle);
                    }
                }
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public byte[] generateDepartmentIncomingReport() {
        List<Student> students = userRepository.findAll()
                .stream()
                .filter(user -> user instanceof Student)
                .filter(user -> ((Student) user).getEnabled())
                .filter(user -> ((Student) user).getElective() != null)
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
                    if (student.getElective().equals(elective)) {
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
                        cell4.setCellValue(student.getElective().getName());
                        cell4.setCellStyle(cellStyle);

                        Cell cell5 = row.createCell(5);
                        cell5.setCellValue(student.getElective().getDepartmentName().name());
                        cell5.setCellStyle(cellStyle);
                    }
                }
            }

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

   //For archived reports
   public byte[] getIncomingReportsForCycle(Long cycleId) {
       // Get students form the particular cycle
       // Iterate over Departments and create a sheet for each department
       // Each sheet will have students of the other department
       AllocationCycle allocationCycle = allocationCycleRepository.findById(cycleId)
               .orElseThrow(() -> new RuntimeException("Allocation cycle not found"));

       List<PastAllocation> pastAllocationList = pastAllocationRepository.findByAllocationCycle(allocationCycle);
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
           }
           workbook.write(out);
           return out.toByteArray();

       } catch (IOException e) {
           throw new RuntimeException(e);
       }
   }

   public byte[] getOutgoingReportsForCycle(Long cycleId) {
       // Get students form the particular cycle
       // Iterate over Departments and create a sheet for each department
       // Each sheet will have students of the other department
       AllocationCycle allocationCycle = allocationCycleRepository.findById(cycleId)
               .orElseThrow(() -> new RuntimeException("Allocation cycle not found"));



       List<PastAllocation> pastAllocationList = pastAllocationRepository.findByAllocationCycle(allocationCycle);
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
}