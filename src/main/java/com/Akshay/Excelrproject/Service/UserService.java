package com.Akshay.Excelrproject.Service;


import com.Akshay.Excelrproject.DTO.UserDTO;
import com.Akshay.Excelrproject.Exceptions.EmailVerificationException;
import com.Akshay.Excelrproject.Model.*;
import com.Akshay.Excelrproject.Repository.*;
import jakarta.mail.MessagingException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AddressRepository addressRepository;

    @Autowired
    private CertificationRepository certificationRepository;

    @Autowired
    private EducationalHistoryRepository educationalHistoryRepository;

    @Autowired
    private EmploymentHistoryRepository employmentHistoryRepository;

    @Autowired
    private SkillRepository skillRepository;

    @Autowired
    private JavaMailSender mailSender;

    @Transactional
    public void registerUser(UserDTO userDTO) {
        User user = new User();
        user.setFirstName(userDTO.getFirstName());
        user.setLastName(userDTO.getLastName());
        user.setEmail(userDTO.getEmail());
        user.setPhoneNumber(userDTO.getPhoneNumber());
        user.setPassword(userDTO.getPassword());  // No encryption, as requested
        user.setSsn(userDTO.getSsn());

        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);
        user.setEmailVerified(false);

        // Save user first
        userRepository.save(user);

        sendVerificationEmail(user.getEmail(), token);
    }

    public User signIn(String email, String password) {
        User user = userRepository.findByEmail(email);

        if (user != null && password.equals(user.getPassword())) {
            return user;
        }

        return null;
    }

    @Transactional
    public void verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token);

        if (user != null) {
            user.setEmailVerified(true);
            user.setVerificationToken(null);
            userRepository.save(user);
        }
    }

    @Transactional
    public void addUserDetails(Long userId, UserDTO userDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (userDTO.getCurrentAddress() != null) {
            Address currentAddress = userDTO.getCurrentAddress();
            addressRepository.save(currentAddress); // Save the address first
            user.setCurrentAddress(currentAddress);
        }

        if (userDTO.getPermanentAddress() != null) {
            Address permanentAddress = userDTO.getPermanentAddress();
            addressRepository.save(permanentAddress); // Save the address first
            user.setPermanentAddress(permanentAddress);
        }

        // Handle Certifications
        if (userDTO.getCertifications() != null) {
            userDTO.getCertifications().forEach(cert -> {
                if (cert.getId() == null) {
                    cert.setUser(user); // Set the user reference
                } else {
                    Certification existingCert = certificationRepository.findById(cert.getId())
                            .orElseThrow(() -> new RuntimeException("Certification not found"));
                    existingCert.setName(cert.getName());
                    existingCert.setInstitution(cert.getInstitution());
                    existingCert.setProvidedBy(cert.getProvidedBy());
                    existingCert.setDate(cert.getDate());
                }
            });
            certificationRepository.saveAll(userDTO.getCertifications());
            user.setCertifications(userDTO.getCertifications());
        }

        // Handle Educational History
        if (userDTO.getEducationalHistory() != null) {
            userDTO.getEducationalHistory().forEach(eh -> {
                if (eh.getAddress() != null) {
                    addressRepository.save(eh.getAddress()); // Save the address
                }
                if (eh.getId() == null) {
                    eh.setUser(user); // Set the user reference
                } else {
                    EducationalHistory existingEh = educationalHistoryRepository.findById(eh.getId())
                            .orElseThrow(() -> new RuntimeException("Educational History not found"));
                    existingEh.setSchoolName(eh.getSchoolName());
                    existingEh.setGpa(eh.getGpa());
                    existingEh.setCourseName(eh.getCourseName());
                    existingEh.setCourseCompletionDate(eh.getCourseCompletionDate());
                    existingEh.setAddress(eh.getAddress());
                }
            });
            educationalHistoryRepository.saveAll(userDTO.getEducationalHistory());
            user.setEducationalHistory(userDTO.getEducationalHistory());
        }

        // Handle Employment History
        if (userDTO.getEmploymentHistory() != null) {
            userDTO.getEmploymentHistory().forEach(emp -> {
                if (emp.getId() == null) {
                    emp.setUser(user); // Set the user reference
                } else {
                    EmploymentHistory existingEmp = employmentHistoryRepository.findById(emp.getId())
                            .orElseThrow(() -> new RuntimeException("Employment History not found"));
                    existingEmp.setCompanyName(emp.getCompanyName());
                    existingEmp.setCompanyAddress(emp.getCompanyAddress());
                    existingEmp.setCompanyPhone(emp.getCompanyPhone());
                    existingEmp.setPrincipalDuties(emp.getPrincipalDuties());
                    existingEmp.setStartDate(emp.getStartDate());  // Correct method
                    existingEmp.setEndDate(emp.getEndDate());      // Correct method
                    existingEmp.setSalary(emp.getSalary());
                    existingEmp.setReasonForLeaving(emp.getReasonForLeaving());
                }
            });
            employmentHistoryRepository.saveAll(userDTO.getEmploymentHistory());
            user.setEmploymentHistory(userDTO.getEmploymentHistory());
        }

        if (userDTO.getSkills() != null) {
            List<Skill> skills = userDTO.getSkills();
            for (Skill skill : skills) {
                if (skill.getId() == null) {
                    skill.setUser(user); // Set the user reference for new skills
                } else {
                    Skill existingSkill = skillRepository.findById(skill.getId())
                            .orElseThrow(() -> new RuntimeException("Skill not found"));
                    existingSkill.setSkillName(skill.getSkillName());
                    existingSkill.setProficiencyLevel(skill.getProficiencyLevel());
                    // Update other properties if needed
                }
            }
            skillRepository.saveAll(skills); // Save all skills
            user.setSkills(skills); // Update user's skills
        }

        userRepository.save(user);
    }

    public User getUserDetails(Long userId) {
        return userRepository.findById(userId).orElse(null);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Remove related entities
        user.getCertifications().forEach(certificationRepository::delete);
        user.getEducationalHistory().forEach(educationalHistoryRepository::delete);
        user.getEmploymentHistory().forEach(employmentHistoryRepository::delete);
        user.getSkills().forEach(skillRepository::delete);

        userRepository.delete(user);
    }

    private void sendVerificationEmail(String email, String token) {
        String subject = "Email Verification";
        String verificationUrl = "http://localhost:8080/api/verify?token=" + token;
        String message = "Please verify your email by clicking the link below:\n" + verificationUrl;

        try {
            MimeMessageHelper helper = new MimeMessageHelper(mailSender.createMimeMessage(), false);
            helper.setTo(email);
            helper.setSubject(subject);
            helper.setText(message);

            mailSender.send(helper.getMimeMessage());
        } catch (MessagingException e) {
            e.printStackTrace();
        }
    }
}