package com.example.zavodbanksystems.databasemodel;

import jakarta.persistence.*;
import java.util.Set;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

//TODO: ORM už by měl odpovídat, ještě to pak ale hoď do AI, aby zkontrolova, že to odpovídá ER diagramu
@Entity
@Table(name = "Client")
public class Client {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "idClient")
    private Integer idClient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "Address_idAddress", nullable = false)
    private Address address;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "social_security_ico", nullable = false, length = 100)
    private String socialSecurityIco;

    @Column(name = "password_hash", nullable = false, length = 64)
    private String passwordHash;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "profile_picture_url", length = 100)
    private String profilePictureUrl;

    @ManyToMany(mappedBy = "clients")
    private Set<Account> accounts;

    public Client() {}

    public Client(Address address, String name, String socialSecurityIco,
                  String plainPassword, String email, String phone, String profilePictureUrl) {
        this.address = address;
        this.name = name;
        this.socialSecurityIco = socialSecurityIco;
        this.passwordHash = hashPassword(plainPassword);
        this.email = email;
        this.phone = phone;
        this.profilePictureUrl = profilePictureUrl;
    }

    public static String hashPassword(String plainPassword) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(plainPassword.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }

    public Integer getIdClient() {
        return idClient;
    }

    public void setIdClient(Integer idClient) {
        this.idClient = idClient;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSocialSecurityIco() {
        return socialSecurityIco;
    }

    public void setSocialSecurityIco(String socialSecurityIco) {
        this.socialSecurityIco = socialSecurityIco;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getProfilePictureUrl() {
        return profilePictureUrl;
    }

    public void setProfilePictureUrl(String profilePictureUrl) {
        this.profilePictureUrl = profilePictureUrl;
    }

    public Set<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(Set<Account> accounts) {
        this.accounts = accounts;
    }
}