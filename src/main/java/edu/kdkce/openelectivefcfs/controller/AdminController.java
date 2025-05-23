package edu.kdkce.openelectivefcfs.controller;

import edu.kdkce.openelectivefcfs.dto.*;
import edu.kdkce.openelectivefcfs.enums.DepartmentName;
import edu.kdkce.openelectivefcfs.service.AdminService;
import edu.kdkce.openelectivefcfs.service.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    private final AdminService adminService;
    private final ReportService reportService;
    private final Logger logger = LoggerFactory.getLogger(AdminController.class);

    public AdminController(AdminService adminService, ReportService reportService) {
        this.adminService = adminService;
        this.reportService = reportService;
    }

    //CRUD operations for elective
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/addElective")
    public ResponseEntity<?> addElective(@RequestBody CreatElectiveRequest electiveRequest) {
        try{
            adminService.addElective(electiveRequest);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        }catch (Exception e){
            logger.error(e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success",false,"message", e.getMessage()));
        }
    }
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/deleteElective/{id}")
    public ResponseEntity<?> deleteElective(@PathVariable String id) {
        try{
            adminService.deleteElective(id);
            return ResponseEntity.noContent().build();
        }catch (Exception e){
            logger.error(e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success",false,"message", e.getMessage()));
        }
    }
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/updateElective/{id}")
    public ResponseEntity<?> updateElective(@PathVariable String id, @RequestBody CreatElectiveRequest electiveRequest) {
        try{
            ElectiveResponse electiveResponse = adminService.updateElective(id, electiveRequest);
            return ResponseEntity.ok(electiveResponse);
        }catch (Exception e){
            logger.error(e.getMessage());
            return ResponseEntity.badRequest().body(Map.of("success",false,"message", e.getMessage()));
        }
    }

    @GetMapping("/getElectives")
    public ResponseEntity<?> getElectives() {
        try{
            List<ElectiveResponse> electives = adminService.getElectives();
            return ResponseEntity.ok(electives);
        }catch (Exception e){
            logger.error(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @GetMapping("/getElective/allowedDepartments/{id}")
    public ResponseEntity<?> getAllowedDepartments(@PathVariable String id) {
        try{
            Set<DepartmentName> allowedDepartments = adminService.allowedDepartments(id);
            return ResponseEntity.ok(allowedDepartments);
        }catch (Exception e){
            logger.error(e.getMessage());
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
            logger.error(e.getMessage());
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
            logger.error(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    //Reports
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/reports/allocation-complete")
    //Return xls file
    public ResponseEntity<?> getAllocationCompleteReport() {
        try{
            String preSignedUrl = reportService.uploadCompleteAllocationReportAndGetLink();
            return ResponseEntity.ok(preSignedUrl);
        }catch (Exception e){
            logger.error(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/reports/allocation-unallocated")
    public ResponseEntity<?> getAllocationUnallocatedReport() {
        try{
            String preSignedUrl = reportService.uploadUnallocatedStudentsReportAndGetLink();
            return ResponseEntity.ok(preSignedUrl);
        }catch (Exception e){
            logger.error(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/reports/department-outgoing")
    public ResponseEntity<?> getDepartmentOutgoingReport() {
        try{

            String preSignedUrl = reportService.uploadDepartmentOutgoingReportAndGetLink();
            return ResponseEntity.ok(preSignedUrl);
        }catch (Exception e){
            logger.error(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/reports/department-incoming")
    public ResponseEntity<?> getDepartmentIncomingReport() {
        try{
            String preSignedUrl = reportService.uploadDepartmentIncomingReportAndGetLink();
            return ResponseEntity.ok(preSignedUrl);
        }catch (Exception e){
            logger.error(e.getMessage());
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
            logger.error(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/settings")
    public ResponseEntity<?> getSettings() {
        try{
            return ResponseEntity.ok(adminService.getElectiveTimeSettings());
        }catch (Exception e){
            logger.error(e.getMessage());
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
            logger.error(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/archived-reports/{cycleId}/{reportType}/xls")
    public ResponseEntity<?> downloadArchivedReportAsXLS(
            @PathVariable String cycleId,
            @PathVariable String reportType) {
        try {
            String preSignedUrl = reportService.uploadArchivedReportAndGetLink(cycleId, reportType);
            return ResponseEntity.ok(preSignedUrl);

        } catch (Exception e) {
            logger.error(e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage().getBytes());
        }

    }



}
