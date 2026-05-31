package com.example.irp.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired(required = false)
    private JavaMailSender mailSender;

    // 🎛️ EMAIL TOGGLE SWITCH:
    // false = Local Development Mode (₹0 balance, no net used, prints clean logs on console instantly)
    // true  = Live Production Mode (Sends real HTML emails - Interview/Viva ke liye)
    private final boolean isEmailEnabled = true;

    // --- Helper Method to log simulation text in a beautiful box layout ---
    private void printEmailSimulation(String toEmail, String subject, String content) {
        System.out.println("\n📧 [EMAIL SIMULATION MODE ACTIVE - SMTP INTERCEPTION SUCCESS]");
        System.out.println("To: " + toEmail);
        System.out.println("Subject: " + subject);
        System.out.println("HTML Content Structure:\n--------------------------------------------------");
        // Clean out HTML tags for clear terminal view
        System.out.println(content.replaceAll("<[^>]*>", " ").replaceAll("\\s+", " ").trim());
        System.out.println("--------------------------------------------------\n");
    }

    // --- 1. Resource Request Status Update Email ---
    public void sendStatusEmail(String toEmail, String userName, String resourceName, String status) {
        String subject = "Resource Request Status Update: " + status;
        String htmlContent = "<div style='font-family: sans-serif;'>"
                + "<h3>Hello " + userName + ",</h3>"
                + "<p>Your allocation request status for <b>" + resourceName + "</b> has been updated to: "
                + "<b style='color: #4f46e5;'>" + status + "</b>.</p>"
                + "</div>";

        if (!isEmailEnabled) {
            printEmailSimulation(toEmail, subject, htmlContent);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
            System.out.println("🚀 Status email sent successfully to: " + toEmail);
        } catch (MessagingException e) {
            System.err.println("Failed to send status email: " + e.getMessage());
        }
    }

    // --- 2. User Receipt Confirmation Email ---
    public void sendConfirmationEmail(String toEmail, String userName, String resourceName, int id) {
        String subject = "Action Required: Please Confirm Receipt of " + resourceName;
        String baseUrl = "http://localhost:8080/user/verify/";
        String htmlContent = "<div style='font-family: sans-serif;'>"
                + "<h3>Hello " + userName + ",</h3>"
                + "<p>Admin ne aapka <b>" + resourceName + "</b> ke liye request approve kar diya hai. "
                + "Kripya confirm karein ki aapko resource mil gaya hai ya nahi:</p>"
                + "<a href='" + baseUrl + "yes/" + id + "' style='padding:10px 20px; background:#22c55e; color:white; text-decoration:none; border-radius:5px;'>Yes, I received it</a> &nbsp;"
                + "<a href='" + baseUrl + "no/" + id + "' style='padding:10px 20px; background:#ef4444; color:white; text-decoration:none; border-radius:5px;'>No, not received</a>"
                + "</div>";

        if (!isEmailEnabled) {
            printEmailSimulation(toEmail, subject, htmlContent);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
            System.out.println("🚀 Confirmation email sent successfully to: " + toEmail);
        } catch (MessagingException e) {
            System.err.println("Failed to send confirmation email: " + e.getMessage());
        }
    }

    // --- 3. Notify Admin About Incoming Return Request With Fine Amount ---
    public void sendAdminReturnNotification(String adminEmail, String userName, String resourceName, int quantity, int id, double fineAmount) {
        String subject = "Pending Return Verification: Request ID #" + id;
        String htmlContent = "<div style='font-family: sans-serif; border-left: 4px solid #f59e0b; padding-left: 15px;'>"
                + "<h2>Resource Return Notice</h2>"
                + "<p>User <b>" + userName + "</b> has submitted a request to return an issued resource.</p>"
                + "<p><b>Allocation Details:</b></p>"
                + "<ul>"
                + "  <li><b>Resource Name:</b> " + resourceName + "</li>"
                + "  <li><b>Quantity:</b> " + quantity + "</li>"
                + "  <li><b>Allocation ID:</b> " + id + "</li>"
                + "  <li><b>Accumulated Fine:</b> <span style='color: #ef4444; font-weight: bold;'>₹" + fineAmount + "</span></li>"
                + "</ul>"
                + "<p>Please inspect the hardware asset and process the decision on your "
                + "<a href='http://localhost:8080/admin/admin-request'>Admin Action Panel</a>.</p>"
                + "</div>";

        if (!isEmailEnabled) {
            printEmailSimulation(adminEmail, subject, htmlContent);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(adminEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
            System.out.println("🚀 Return notification email sent to Admin: " + adminEmail);
        } catch (MessagingException e) {
            System.err.println("Failed to send return notification email to admin: " + e.getMessage());
        }
    }

    // --- 4. Initial Overdue Immediate HTML Warning Notice ---
    public void sendOverdueAlertEmail(String toEmail, String userName, String resourceName, String expiryDate, int id) {
        String subject = "🚨 URGENT: Resource Return Period Overdue Notice (ID #" + id + ")";
        String htmlContent = "<div style='font-family: sans-serif; border: 2px solid #ef4444; padding: 20px; border-radius: 10px; background-color: #fef2f2;'>"
                + "<h2 style='color: #b91c1c; margin-top: 0;'>⚠️ Return Deadline Expired!</h2>"
                + "<p>Dear <b>" + userName + "</b>,</p>"
                + "<p>This is an automated system alert to notify you that your allocation period for <b>" + resourceName + "</b> has expired on <span style='color: #b91c1c; font-weight: bold;'> " + expiryDate + "</span>.</p>"
                + "<p>You have not returned the resource yet. Your request status has been flagged as <b style='color: #ef4444;'>OVERDUE</b> in the portal and your penalty session has started.</p>"
                + "<hr style='border: 0; border-top: 1px solid #fee2e2; margin: 15px 0;'>"
                + "<p style='font-weight: 500;'>What to do next?</p>"
                + "<p>Please return the item immediately to clear your dashboard and freeze further fines.</p>"
                + "<p style='margin-bottom: 0; font-size: 13px; color: #64748b;'>Regards,<br>Institute Resource Portal Team</p>"
                + "</div>";

        if (!isEmailEnabled) {
            printEmailSimulation(toEmail, subject, htmlContent);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
            System.out.println("🚀 Overdue HTML immediate warning sent successfully to: " + toEmail);
        } catch (MessagingException e) {
            System.err.println("Failed to send overdue alert email: " + e.getMessage());
        }
    }

    // --- 5. Periodic Automated Dynamic Fine Interval Email Alert ---
    public void sendFineIncrementAlert(String toEmail, String studentName, String resourceName, double updatedFine) {
        String subject = "⏳ ALERT: Late Fine Updated for " + resourceName;
        String htmlContent = "<div style='font-family: sans-serif; border: 2px solid #f59e0b; padding: 20px; border-radius: 10px; background-color: #fffbeb;'>"
                + "<h3 style='color: #b45309; margin-top: 0;'>⚠️ Periodic Fine Update Reminder</h3>"
                + "<p>Dear <b>" + studentName + "</b>,</p>"
                + "<p>This is a periodic reminder that you have not returned the allocated resource: <b>" + resourceName + "</b>.</p>"
                + "<div style='background: #fef3c7; padding: 12px; border-radius: 6px; border: 1px solid #fde68a; display: inline-block; font-size: 16px; font-weight: bold; color: #b45309; margin: 10px 0;'>"
                + "💰 Current Accumulated Fine: ₹" + updatedFine
                + "</div>"
                + "<p>Please clear your dashboard dues and return the physical asset to the coordinator desk as soon as possible to avoid account blocking.</p>"
                + "<hr style='border: 0; border-top: 1px solid #fde68a; margin: 15px 0;'>"
                + "<p style='margin-bottom: 0; font-size: 13px; color: #71717a;'>Regards,<br>IRP Automated Billing Cell</p>"
                + "</div>";

        if (!isEmailEnabled) {
            printEmailSimulation(toEmail, subject, htmlContent);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
            System.out.println("🚀 Dynamic calculated fine alert email sent successfully to: " + toEmail);
        } catch (MessagingException e) {
            System.err.println("Failed to send calculated fine alert email: " + e.getMessage());
        }
    }

    // --- 6. Student Status Update With Fine Amount ---
    public void sendStatusEmailWithFine(String toEmail, String studentName, String resourceName, String status, double fineAmount) {
        String subject = "📄 Update on your Resource Allocation Status";
        String htmlContent = "<div style='font-family: sans-serif; border-left: 4px solid #10b981; padding-left: 15px;'>"
                + "<h3>Dear " + studentName + ",</h3>"
                + "<p>Your request status for '<b>" + resourceName + "</b>' has been updated to: <b style='color: #10b981;'>" + status + "</b>.</p>"
                + "<p><b>Outstanding Fine Logged:</b> <span style='color: #ef4444; font-weight: bold;'>₹" + fineAmount + "</span></p>"
                + "<p>If there is any remaining fine balance, please visit the central desk immediately to clear it.</p>"
                + "<p style='font-size: 13px; color: #64748b;'>Regards,<br>IRP Management Team</p>"
                + "</div>";

        if (!isEmailEnabled) {
            printEmailSimulation(toEmail, subject, htmlContent);
            return;
        }

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
            System.out.println("🚀 Status email with fine sent successfully to: " + toEmail);
        } catch (MessagingException e) {
            System.err.println("Failed to send status email with fine: " + e.getMessage());
        }
    }
}