package com.example.irp.controller;

import com.example.irp.entity.Allocation;
import com.example.irp.entity.Resource;
import com.example.irp.repository.AllocationRepository;
import com.example.irp.repository.ResourceRepository;
import com.example.irp.repository.UserRepository;
import com.example.irp.service.EmailService;
import com.example.irp.service.PdfReportService;
import com.example.irp.service.SmsService; // 🟢 Import added
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private ResourceRepository resourceRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AllocationRepository allocationRepository;

    @Autowired
    private EmailService emailService;

    @Autowired
    private PdfReportService pdfReportService;

    @Autowired // 🟢 SmsService injected for Twilio SMS
    private SmsService smsService;

    // 1. Admin Analytics Dashboard
    @GetMapping("/dashboard")
    public String adminDashboard(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        model.addAttribute("username", username != null ? username : "Administrator");

        List<Resource> resources = resourceRepository.findAll();

        long availableResources = 0;
        long notAvailableResources = 0;

        for (Resource res : resources) {
            int bookedQty = allocationRepository.getBookedQuantityByResourceId(res.getResource_id());
            int calculatedAvailable = res.getQuantity() - bookedQty;
            res.setAvailableQuantity(calculatedAvailable < 0 ? 0 : calculatedAvailable);

            if ("Maintenance".equalsIgnoreCase(res.getStatus())) {
                notAvailableResources++;
            } else if (res.getAvailableQuantity() <= 0) {
                res.setStatus("Not Available");
                notAvailableResources++;
            } else {
                res.setStatus("Available");
                availableResources++;
            }
        }

        long totalResources = resourceRepository.count();
        long totalUsers = userRepository.count();

        model.addAttribute("resources", resources);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalResources", totalResources);
        model.addAttribute("availableResources", availableResources);
        model.addAttribute("notAvailableResources", notAvailableResources);

        return "admin-dashboard";
    }

    // Page open Form
    @GetMapping("/add-resources")
    public String addResourcePage() {
        return "add-resources";
    }

    // 2. Admin Resource Inventory Main Dashboard
    @GetMapping("/dashboardmain")
    public String dashboardmain(HttpSession session, Model model) {
        String username = (String) session.getAttribute("username");
        model.addAttribute("username", username != null ? username : "Administrator");

        List<Resource> resources = resourceRepository.findAll();

        long availableResources = 0;
        long notAvailableResources = 0;

        for (Resource res : resources) {
            int bookedQty = allocationRepository.getBookedQuantityByResourceId(res.getResource_id());
            int calculatedAvailable = res.getQuantity() - bookedQty;
            res.setAvailableQuantity(calculatedAvailable < 0 ? 0 : calculatedAvailable);

            if ("Maintenance".equalsIgnoreCase(res.getStatus())) {
                notAvailableResources++;
            } else if (res.getAvailableQuantity() <= 0) {
                res.setStatus("Not Available");
                notAvailableResources++;
            } else {
                res.setStatus("Available");
                availableResources++;
            }
        }

        long totalResources = resourceRepository.count();
        long totalUsers = userRepository.count();

        model.addAttribute("resources", resources);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("totalResources", totalResources);
        model.addAttribute("availableResources", availableResources);
        model.addAttribute("notAvailableResources", notAvailableResources);

        return "dashboardmain";
    }

    @GetMapping("/edit-resource")
    public String editResource(@RequestParam("resource_id") int resource_id, Model model) {
        Resource resource = resourceRepository.findById((long) resource_id).orElse(null);
        model.addAttribute("resource", resource);
        return "edit-resource";
    }

    @GetMapping("/delete-resource")
    public String deleteResource(@RequestParam("resource_id") int resource_id) {
        resourceRepository.deleteById((long) resource_id);
        return "redirect:/admin/dashboardmain";
    }

    @PostMapping("/add-resources")
    public String saveResource(@RequestParam String resource_name,
                               @RequestParam String type,
                               @RequestParam int quantity,
                               @RequestParam String status,
                               @RequestParam String location) {
        Resource resource = new Resource();
        resource.setResource_name(resource_name);
        resource.setType(type);
        resource.setQuantity(quantity);
        resource.setStatus(status);
        resource.setLocation(location);

        resourceRepository.save(resource);
        return "redirect:/admin/dashboardmain";
    }

    @PostMapping("/update-resource")
    public String updateResource(@RequestParam int resource_id,
                                 @RequestParam String resource_name,
                                 @RequestParam String type,
                                 @RequestParam int quantity,
                                 @RequestParam String status,
                                 @RequestParam String location) {
        Resource resource = resourceRepository.findById((long) resource_id).orElse(null);
        if (resource != null) {
            resource.setResource_name(resource_name);
            resource.setType(type);
            resource.setQuantity(quantity);
            resource.setStatus(status);
            resource.setLocation(location);
            resourceRepository.save(resource);
        }
        return "redirect:/admin/dashboardmain";
    }

    @GetMapping("/admin-request")
    public String showMyRequests(HttpSession session, Model model) {
        Object sessionUserId = session.getAttribute("userId");
        if (sessionUserId == null) {
            return "redirect:/";
        }

        String username = (String) session.getAttribute("username");
        model.addAttribute("username", username != null ? username : "Administrator");

        List<Allocation> requests = allocationRepository.findAll(Sort.by(Sort.Direction.DESC, "id"));
        model.addAttribute("requests", requests);

        return "admin-request";
    }

    @PostMapping("/request/approve")
    public String approveRequest(@RequestParam("id") int id) {
        Allocation allocation = allocationRepository.findById(id).orElse(null);

        if (allocation != null) {
            // 1. Status, Approval Time aur Inventory Update
            allocation.setStatus("APPROVED_PENDING_DELIVERY");
            allocation.setApprovalTime(LocalDateTime.now()); // ✅ Naya: Time track karne ke liye

            // ✅ Naya: Inventory kam karein (Reserve)
            Resource resource = allocation.getResource();
            if (resource != null) {
                resource.setQuantity(resource.getQuantity() - allocation.getQuantity());
                resourceRepository.save(resource);
            }

            allocationRepository.save(allocation);

            if (allocation.getUser() != null) {
                String studentRealEmail = allocation.getUser().getUserEmail();
                String studentName = allocation.getUser().getUserName();
                String resourceName = allocation.getResource() != null ? allocation.getResource().getResource_name() : "Resource";

                System.out.println("==================================================");
                System.out.println("LOG -> Approving Allocation ID #: " + id);
                System.out.println("LOG -> Inventory Reduced for Resource: " + resourceName);
                System.out.println("==================================================");

                // 1. Send Email Notification
                emailService.sendConfirmationEmail(
                        studentRealEmail.trim(),
                        studentName,
                        resourceName,
                        allocation.getId()
                );

                // 2. Send Live Mobile SMS via Twilio
                try {
                    String testMobileNumber = "+917723883326";
                    String smsBody = "Hello " + studentName + ", your request for " + resourceName + " (ID: " + id + ") has been APPROVED. Please collect it within 24 hours.";
                    smsService.sendSms(testMobileNumber, smsBody);
                } catch (Exception e) {
                    System.err.println("Twilio SMS failed: " + e.getMessage());
                }
            }
        }
        return "redirect:/admin/admin-request";
    }

    @PostMapping("/request/verify-return-yes")
    public String verifyReturnYes(@RequestParam("id") int id) {
        Allocation allocation = allocationRepository.findById(id).orElse(null);

        if (allocation != null && "RETURN_PENDING_ADMIN".equalsIgnoreCase(allocation.getStatus())) {
            allocation.setStatus("RETURNED");
            allocationRepository.save(allocation);

            String resourceName = allocation.getResource() != null ? allocation.getResource().getResource_name() : "Resource";
            String studentName = allocation.getUser() != null ? allocation.getUser().getUserName() : "Student";

            // 🟢 Safety Check Added: Prevents NullPointerException if database value is null
            double fineAmount = (allocation.getFineAmount() != null) ? allocation.getFineAmount() : 0.0;

            // ⏱️ Agar dynamic calculations hain toh use karein, nahi toh baseline fallback
            long lateMinutes = 30;

            if (allocation.getUser() != null) {
                emailService.sendStatusEmail(
                        allocation.getUser().getUserEmail(),
                        studentName,
                        resourceName,
                        "RETURNED"
                );
            }

            // Resource Request Approve Controller ke andar:
            try {
                String approvedSms = "IRP AUTOMATED ALERT\n" +
                        "▶ REQUEST STATUS: APPROVED\n\n" +
                        "Dear " + studentName + ",\n" +
                        "Your allocation request has been verified.\n\n" +
                        "• Request ID: #" + id + "\n" +
                        "• Asset Name: " + resourceName + "\n" +
                        "• Pickup Desk: Central Lab\n\n" +
                        "Please collect your asset within 24 hours.\n\n" +
                        "Regards,\n" +
                        "Operations Desk | IRP";

                smsService.sendSms("+917723883326", approvedSms);

            } catch (Exception e) {
                System.out.println("\n⚠️ [SMS SIMULATION MODE ACTIVE - REQUEST APPROVED]");
                System.out.println("To: +917723883326");
                System.out.println("Message Body:\n" +
                        "----------------------------------------\n" +
                        "IRP AUTOMATED ALERT\n" +
                        "▶ REQUEST STATUS: APPROVED\n\n" +
                        "Dear " + studentName + ",\n" +
                        "Your allocation request has been verified.\n\n" +
                        "• Request ID: #" + id + "\n" +
                        "• Asset Name: " + resourceName + "\n" +
                        "• Pickup Desk: Central Lab\n\n" +
                        "Please collect your asset within 24 hours.\n" +
                        "----------------------------------------");
            }
            String adminEmail = "admin@example.com";
            emailService.sendStatusEmail(
                    adminEmail,
                    "Admin Central Desk",
                    resourceName + " (Returned by " + studentName + ")",
                    "SUCCESSFULLY VERIFIED BY SYSTEM"
            );
        }
        return "redirect:/admin/admin-request";
    }

    @PostMapping("/request/verify-return-no")
    public String verifyReturnNo(@RequestParam("id") int id) {
        Allocation allocation = allocationRepository.findById(id).orElse(null);
        if (allocation != null && "RETURN_PENDING_ADMIN".equalsIgnoreCase(allocation.getStatus())) {

            LocalDateTime now = LocalDateTime.now();
            String resolvedStatus = "ISSUED";

            if (allocation.getEndTime() != null && now.isAfter(allocation.getEndTime())) {
                resolvedStatus = "OVERDUE";
            }

            allocation.setStatus(resolvedStatus);
            allocationRepository.save(allocation);

            String resourceName = allocation.getResource() != null ? allocation.getResource().getResource_name() : "Resource";
            String studentName = allocation.getUser() != null ? allocation.getUser().getUserName() : "Student";

            // 🟢 Safety Check Added: Prevents NullPointerException if database value is null
            double fineAmount = (allocation.getFineAmount() != null) ? allocation.getFineAmount() : 0.0;

            if (allocation.getUser() != null) {
                emailService.sendStatusEmail(
                        allocation.getUser().getUserEmail(),
                        studentName,
                        resourceName,
                        "RETURN REJECTED BY ADMIN (STATUS: STILL " + resolvedStatus + ")"
                );
            }
            // Resource Request Reject Controller ke andar:
            try {
                String rejectedSms = "IRP AUTOMATED ALERT\n" +
                        "▶ REQUEST STATUS: REJECTED\n\n" +
                        "Dear " + studentName + ",\n" +
                        "We regret to inform you that your resource request has been declined.\n\n" +
                        "• Request ID: #" + id + "\n" +
                        "• Asset Name: " + resourceName + "\n" +
                        "• Status: DENIED BY ADMIN\n\n" +
                        "Kindly visit the Central Admin Desk for further clarifications.\n\n" +
                        "Regards,\n" +
                        "Operations Desk | IRP";

                smsService.sendSms("+917723883326", rejectedSms);

            } catch (Exception e) {
                System.out.println("\n⚠️ [SMS SIMULATION MODE ACTIVE - REQUEST REJECTED]");
                System.out.println("To: +917723883326");
                System.out.println("Message Body:\n" +
                        "----------------------------------------\n" +
                        "IRP AUTOMATED ALERT\n" +
                        "▶ REQUEST STATUS: REJECTED\n\n" +
                        "Dear " + studentName + ",\n" +
                        "We regret to inform you that your resource request has been declined.\n\n" +
                        "• Request ID: #" + id + "\n" +
                        "• Asset Name: " + resourceName + "\n" +
                        "• Status: DENIED BY ADMIN\n\n" +
                        "Kindly visit the Central Admin Desk for further clarifications.\n" +
                        "----------------------------------------");
            }
            String adminEmail = "shevendrachandel@gmail.com";
            emailService.sendStatusEmail(
                    adminEmail,
                    "Admin Central Desk",
                    resourceName + " (Return Denied for " + studentName + ")",
                    "REJECTED & REVERTED BACK TO " + resolvedStatus + " STATUS"
            );
        }
        return "redirect:/admin/admin-request";
    }

    @PostMapping("/request/clear-fine")
    public String clearFine(@RequestParam("id") int id) {
        Allocation allocation = allocationRepository.findById(id).orElse(null);
        if (allocation != null) {
            allocation.setFineAmount(0.0);
            allocationRepository.save(allocation);
        }
        return "redirect:/admin/admin-request";
    }

    @PostMapping("/request/reject")
    public String rejectRequest(@RequestParam("id") int id) {
        Allocation allocation = allocationRepository.findById(id).orElse(null);

        if (allocation != null) {
            allocation.setStatus("REJECTED");
            allocationRepository.save(allocation);

            if (allocation.getUser() != null) {
                String studentRealEmail = allocation.getUser().getUserEmail();
                String studentName = allocation.getUser().getUserName();
                String resourceName = allocation.getResource() != null ? allocation.getResource().getResource_name() : "Resource";

                // 1. Send Email Notification
                emailService.sendStatusEmail(
                        studentRealEmail.trim(),
                        studentName,
                        resourceName,
                        "REJECTED"
                );

                // 2. 🟢 Send Live Mobile SMS via Twilio
                try {
                    String testMobileNumber = "+917723883326"; // Hardcoded verified trial number
                    String smsBody = "Hello " + studentName + ", your allocation request for " + resourceName + " has been REJECTED by Admin.";
                    smsService.sendSms(testMobileNumber, smsBody);
                } catch (Exception e) {
                    System.err.println("Twilio SMS failed, but app will continue: " + e.getMessage());
                }
            }
        }
        return "redirect:/admin/admin-request";
    }

    // ====================================================================
    // 🟢 FIXED PDF REPORT GENERATION ENDPOINT
    // ====================================================================
    @GetMapping("/report/download-pdf")
    @ResponseBody
    public void downloadPdfReport(HttpServletResponse response) {
        try {
            response.setContentType("application/pdf");

            String headerKey = "Content-Disposition";
            String headerValue = "attachment; filename=irp_allocation_report.pdf";
            response.setHeader(headerKey, headerValue);

            List<Allocation> allAllocations = allocationRepository.findAll();

            pdfReportService.generateAllocationReport(allAllocations, response);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}