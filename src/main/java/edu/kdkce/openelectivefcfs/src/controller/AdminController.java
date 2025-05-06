package edu.kdkce.openelectivefcfs.src.controller;

import edu.kdkce.openelectivefcfs.src.dto.*;
import edu.kdkce.openelectivefcfs.src.model.AllocationCycle;
import edu.kdkce.openelectivefcfs.src.repository.AllocationCycleRepository;
import edu.kdkce.openelectivefcfs.src.service.AdminService;
import edu.kdkce.openelectivefcfs.src.service.ReportService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService adminService;
    private final ReportService reportService;
    private final AllocationCycleRepository allocationCycleRepository;

    public AdminController(AdminService adminService, ReportService reportService, AllocationCycleRepository allocationCycleRepository) {
        this.adminService = adminService;
        this.reportService = reportService;
        this.allocationCycleRepository = allocationCycleRepository;
    }

    //CRUD operations for elective
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/addElective")
    public ResponseEntity<?> addElective(@RequestBody CreatElectiveRequest electiveRequest) {
        try{
            adminService.addElective(electiveRequest);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        }catch (Exception e){
            return ResponseEntity.badRequest().body(Map.of("success",false,"message", e.getMessage()));
        }
    }
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/deleteElective/{id}")
    public ResponseEntity<?> deleteElective(@PathVariable Integer id) {
        try{
            adminService.deleteElective(id);
            return ResponseEntity.noContent().build();
        }catch (Exception e){
            return ResponseEntity.badRequest().body(Map.of("success",false,"message", e.getMessage()));
        }
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/updateElective/{id}")
    public ResponseEntity<?> updateElective(@PathVariable Integer id, @RequestBody CreatElectiveRequest electiveRequest) {
        try{
            ElectiveResponse electiveResponse = adminService.updateElective(id, electiveRequest);
            return ResponseEntity.ok(electiveResponse);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(Map.of("success",false,"message", e.getMessage()));
        }
    }

    @GetMapping("/getElectives")
    public ResponseEntity<?> getElectives() {
        try{
            List<ElectiveResponse> electives = adminService.getElectives();
            return ResponseEntity.ok(electives);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/departments/stats")
    public ResponseEntity<?> getDepartmentStats() {
        try{
            List<AdminPanelElectiveStatResponse> electiveStats = adminService.getDepartmentStats();
            return ResponseEntity.ok(electiveStats);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/students")
    public ResponseEntity<?> getStudents() {
        try{
            List<UserProfileResponse> students = adminService.getStudents();
            return ResponseEntity.ok(students);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    //System Management
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/reset-allocations")
    public ResponseEntity<?> resetAllocations(@RequestBody String cycleName) {
        try{
            adminService.resetAllocations(cycleName);
            return ResponseEntity.ok(Map.of("success", true , "message", "Allocations reset successfully"));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    private ResponseEntity<byte[]> generateXlsResponse(byte[] data, String filename) {
        HttpHeaders headers = new HttpHeaders();

        // Add Content-Disposition header with quotes around the filename for safety
        headers.add("Content-Disposition", "attachment; filename=\"" + filename + "\"");
        headers.add("Content-Type", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

        return new ResponseEntity<>(data, headers, HttpStatus.OK);
    }

    //Reports
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/reports/allocation-complete")
    //Return xls file
    public ResponseEntity<?> getAllocationCompleteReport() {
        try{
            byte[] data = reportService.generateCompleteAllocationReport();
            return generateXlsResponse(data, "Complete_Allocation_Report.xlsx");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/reports/allocation-unallocated")
    public ResponseEntity<?> getAllocationUnallocatedReport() {
        try{
            byte[] data = reportService.generateUnallocatedStudentsReport();
            return generateXlsResponse(data, "Unallocated_Report.xlsx");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/reports/department-outgoing")
    public ResponseEntity<?> getDepartmentOutgoingReport() {
        try{
            byte[] data = reportService.generateDepartmentOutgoingReport();
            return generateXlsResponse(data, "Department_Wise_Outgoing_Report.xlsx");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/reports/department-incoming")
    public ResponseEntity<?> getDepartmentIncomingReport() {
        try{
            byte[] data = reportService.generateDepartmentIncomingReport();
            return generateXlsResponse(data, "Department_Wise_Incoming_Report.xlsx");
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/settings")
    public ResponseEntity<?> updateElectiveTimeSettings(@RequestBody ElectiveTimeUpdateRequest electiveTimeUpdateRequest) {
        try{
            adminService.updateElectiveTimeSettings(electiveTimeUpdateRequest);
            return ResponseEntity.ok(Map.of("success",true,"message", "Elective time settings updated successfully"));
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/settings")
    public ResponseEntity<?> getSettings() {
        try{
            return ResponseEntity.ok(adminService.getElectiveTimeSettings());
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //Archived Reports
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/archived-reports")
    public ResponseEntity<?> getArchivedReports() {
        try{
            List<ArchivedReportNameResponse> archivedReports = reportService.getArchivedReports();


            return ResponseEntity.ok(archivedReports);
        }catch (Exception e){
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/archived-reports/{cycleId}/{reportType}/xls")
    public ResponseEntity<byte[]> downloadArchivedReportAsXLS(
            @PathVariable Long cycleId,
            @PathVariable String reportType) {
        try {
            byte[] reportData;

            if ("outgoing".equalsIgnoreCase(reportType)) {
                reportData = reportService.getOutgoingReportsForCycle(cycleId);
            } else if ("incoming".equalsIgnoreCase(reportType)) {
                reportData = reportService.getIncomingReportsForCycle(cycleId);
            } else {
                return ResponseEntity.badRequest().body("Invalid report type".getBytes());
            }

            // Generate filename based on cycle name and report type
            AllocationCycle cycle = allocationCycleRepository.findById(cycleId)
                    .orElseThrow(() -> new RuntimeException("Cycle not found"));
            String cycleName = cycle.getCycleName().replaceAll("[{}\"]", "").replace("cycleName:", "").trim();
            // Generate filename
            String filename = cycleName + " Department-Wise " + reportType + " Report.xlsx";

            // Call the generateXlsResponse method with the data and filename
            return generateXlsResponse(reportData, filename);

        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage().getBytes());
        }

    }



}
