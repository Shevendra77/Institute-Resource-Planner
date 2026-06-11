package com.example.irp.controller;

import com.example.irp.entity.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import java.util.List;

@Controller
public class ResourceController {

    @Autowired
    com.example.irp.repository.ResourceRepository resourceRepository;

    @Autowired
    com.example.irp.repository.AllocationRepository allocationRepository;

    // 1. Admin/View Resources Page Mapped here
    @GetMapping("/resources")
    public String resourcePage(Model model) {
        List<Resource> resourceList = resourceRepository.findAll();

        // Loop chalakar runtime par live available quantity safely calculate karein
        for (Resource res : resourceList) {
            // Allocation table se live locked/booked quantity uthayein (ISSUED, OVERDUE, etc.)
            int bookedQty = allocationRepository.getBookedQuantityByResourceId(res.getResource_id());

            // Available Quantity calculate karein (Total Stock - Booked Stock)
            int available = res.getQuantity() - bookedQty;

            // Safety Check: Stock kabhi minus me na dikhe aur total quantity se zyada na ho
            if (available < 0) {
                res.setAvailableQuantity(0);
            } else if (available > res.getQuantity()) {
                res.setAvailableQuantity(res.getQuantity());
            } else {
                res.setAvailableQuantity(available);
            }
        }

        model.addAttribute("resources", resourceList);
        return "resources";
    }

    // 🔴 DUPICATE MAPPING REMOVED: /user/dashboard method ko yahan se poori tarah hata diya hai.

    @PostMapping("/addResource")
    public String addResource(com.example.irp.entity.Resource resource) {
        resourceRepository.save(resource);
        return "redirect:/resources";
    }

    @PostMapping("/requestResource")
    public String requestResource(com.example.irp.entity.Allocation allocation) {
        // Default status pending set karein
        allocation.setStatus("Pending");
        allocationRepository.save(allocation);

        return "redirect:/user/dashboard";
    }
}