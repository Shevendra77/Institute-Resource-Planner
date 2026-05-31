package com.example.irp.controller;

import com.example.irp.entity.Allocation;
import com.example.irp.entity.Resource;
import com.example.irp.repository.AllocationRepository;
import com.example.irp.repository.ResourceRepository;
import com.example.irp.service.EmailService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/user")
public class UserController {

    @Autowired private ResourceRepository resourceRepository;
    @Autowired private AllocationRepository allocationRepository;
    @Autowired private EmailService emailService;

    // 1. Dashboard Mapping
    @RequestMapping(value = {"/dashboard", "/main-dashboard"}, method = {RequestMethod.GET, RequestMethod.POST})
    public String userDashboard(@RequestParam(value = "search", required = false) String search, HttpSession session, Model model) {
        if (session.getAttribute("userId") == null) return "redirect:/login";

        model.addAttribute("username", session.getAttribute("username"));
        List<Resource> resources = (search != null && !search.trim().isEmpty())
                ? resourceRepository.searchResources(search.trim())
                : resourceRepository.findAll();

        LocalDateTime now = LocalDateTime.now();
        for (Resource res : resources) {
            int bookedQty = allocationRepository.getBookedQuantityByResourceId(res.getResource_id());
            res.setAvailableQuantity(Math.max(0, res.getQuantity() - bookedQty));
            if (!"Maintenance".equalsIgnoreCase(res.getStatus())) {
                res.setStatus(allocationRepository.countActiveBookingsNow(res.getResource_id(), now) > 0 ? "Not Available" : "Available");
            }
        }
        model.addAttribute("resources", resources);
        return "user-dashboard";
    }

    // 2. My Requests Mapping
    @GetMapping("/my-requests")
    public String myRequests(HttpSession session, Model model) {
        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        List<Allocation> myRequests = allocationRepository.findByUserId(userId);
        model.addAttribute("requests", myRequests);
        model.addAttribute("username", session.getAttribute("username")); // Required for sidebar profile letter

        return "my-requests";
    }

    // User confirms receiving the resource
    @PostMapping("/confirm-receipt/yes")
    public String confirmReceiptYes(@RequestParam("id") int id, HttpSession session) {
        if (session.getAttribute("userId") == null) return "redirect:/login";

        Allocation allocation = allocationRepository.findById(id).orElse(null);
        if (allocation != null && "APPROVED_PENDING_DELIVERY".equalsIgnoreCase(allocation.getStatus())) {
            allocation.setStatus("ISSUED"); // Status turns to ISSUED smoothly
            allocationRepository.save(allocation);
        }
        return "redirect:/user/my-requests";
    }

    // User denies receiving the resource
    @PostMapping("/confirm-receipt/no")
    public String confirmReceiptNo(@RequestParam("id") int id, HttpSession session) {
        if (session.getAttribute("userId") == null) return "redirect:/login";

        Allocation allocation = allocationRepository.findById(id).orElse(null);
        if (allocation != null && "APPROVED_PENDING_DELIVERY".equalsIgnoreCase(allocation.getStatus())) {
            allocation.setStatus("REJECTED_BY_USER");
            allocationRepository.save(allocation);
        }
        return "redirect:/user/my-requests";
    }

    // 3. Request Resource Form Display
    @GetMapping("/request-resource/{id}")
    public String requestResourceForm(@PathVariable("id") int id, Model model, HttpSession session) {
        if (session.getAttribute("userId") == null) return "redirect:/login";
        model.addAttribute("resourceId", id);
        return "request-resource";
    }

    // 4. Handle Form Submission WITH STRICT LATE FINE CHECK
    @PostMapping("/request")
    public String handleResourceRequest(
            @RequestParam("resource_id") int resourceId,
            @RequestParam("quantity") int quantity,
            @RequestParam("start_time") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam("end_time") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam("reason") String reason,
            HttpSession session, Model model) {

        Integer userId = (Integer) session.getAttribute("userId");
        if (userId == null) return "redirect:/login";

        if (endTime.isBefore(startTime)) {
            model.addAttribute("errorMessage", "End time cannot be before Start time!");
            model.addAttribute("resourceId", resourceId);
            return "request-resource";
        }

        // BLOCKER LOGIC: Calculate active accumulated fine amount across all transactions
        List<Allocation> myRequests = allocationRepository.findByUserId(userId);
        double totalOutstandingFine = myRequests.stream()
                .filter(req -> req.getFineAmount() != null)
                .mapToDouble(Allocation::getFineAmount)
                .sum();

        if (totalOutstandingFine > 0) {
            model.addAttribute("errorMessage", "❌ Booking Blocked! Aapka ₹" + totalOutstandingFine + " ka outstanding fine bacha hai. Please pay at center first!");
            model.addAttribute("resourceId", resourceId);
            return "request-resource";
        }

        Allocation newAllocation = new Allocation();
        newAllocation.setUserId(userId);
        newAllocation.setResourceId(resourceId);
        newAllocation.setQuantity(quantity);
        newAllocation.setStartTime(startTime);
        newAllocation.setEndTime(endTime);
        newAllocation.setReason(reason);
        newAllocation.setStatus("Pending");

        allocationRepository.save(newAllocation);

        return "redirect:/user/my-requests";
    }

    // 5. Handle User Initiating Resource Return (🛠️ FIXED ARGUMENT MISMATCH ERROR)
    @PostMapping("/request/return")
    public String initiateReturn(@RequestParam("id") int id) {
        Allocation allocation = allocationRepository.findById(id).orElse(null);

        // Conditions update ho chuki hain: Ab ISSUED ke sath OVERDUE entries bhi allow hain
        if (allocation != null && ("ISSUED".equalsIgnoreCase(allocation.getStatus()) || "OVERDUE".equalsIgnoreCase(allocation.getStatus()))) {

            //  Fetch current live fine amount from DB before modifying status
            double currentFine = allocation.getFineAmount() != null ? allocation.getFineAmount() : 0.0;

            allocation.setStatus("RETURN_PENDING_ADMIN");
            allocationRepository.save(allocation);

            // Admin alert notification trigger
            String adminEmail = "admin@example.com";
            String requesterName = (allocation.getUser() != null) ? allocation.getUser().getUserName() : "A Student/Faculty member";
            String resourceName = (allocation.getResource() != null) ? allocation.getResource().getResource_name() : "Resource Asset";
            int qty = allocation.getQuantity();

            //  FIX: Passed 'currentFine' as 6th parameter to perfectly match updated EmailService method signature
            emailService.sendAdminReturnNotification(adminEmail, requesterName, resourceName, qty, allocation.getId(), currentFine);
        }
        return "redirect:/user/my-requests";
    }
}