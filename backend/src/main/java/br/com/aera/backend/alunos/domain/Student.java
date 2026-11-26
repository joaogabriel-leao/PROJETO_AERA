package br.com.aera.backend.alunos.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Cadastro de aluno com modelagem provisoria baseada nas telas e contratos.
 *
 * <p>Mapeia a tabela {@code students}. O codigo e os dados cadastrais devem
 * ser informados pela camada de servico antes da persistencia.
 * Os campos do Google Forms ainda precisam ser confirmados.</p>
 */
@Entity
@Table(name = "students")
public class Student {

    /** Identificador UUID gerado pelo JPA, serializado como texto no JSON. */
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(nullable = false, updatable = false)
    private UUID id;

    /** Codigo unico do aluno. Sua regra de geracao sera definida no service. */
    @Column(nullable = false, unique = true, length = 40)
    private String code;

    @Column(nullable = false, length = 255)
    private String fullName;

    @Column(nullable = false)
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StudentStatus status;

    @Column(length = 50)
    private String sex;

    @Column(length = 40)
    private String cpf;

    @Column(length = 40)
    private String rg;

    @Column(length = 255)
    private String address;

    @Column(length = 255)
    private String neighborhood;

    @Column(length = 255)
    private String city;

    @Column(length = 20)
    private String zipCode;

    @Column(length = 30)
    private String phone;

    @Column(length = 30)
    private String whatsapp;

    @Column(length = 255)
    private String email;

    @Column(length = 255)
    private String guardianName;

    @Column(length = 40)
    private String guardianCpf;

    @Column(length = 30)
    private String guardianPhone;

    @Column(length = 30)
    private String guardianWhatsapp;

    @Column(length = 100)
    private String relationship;

    @Column(length = 100)
    private String modality;

    @Column(length = 50)
    private String level;

    @Column(length = 100)
    private String classGroup;

    @Column
    private LocalDate startDate;

    @Column(length = 2000)
    private String notes;

    @Column(length = 2000)
    private String importantInformation;

    /** Data de cadastro gerada na insercao e preservada nas atualizacoes. */
    @Column(nullable = false, updatable = false)
    private OffsetDateTime createdAt;

    /** Construtor vazio exigido pelo JPA/Hibernate. */
    public Student() {
    }

    @PrePersist
    private void initializeCreatedAt() {
        createdAt = OffsetDateTime.now();
    }

    // ---- Getters e Setters ----

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public StudentStatus getStatus() {
        return status;
    }

    public void setStatus(StudentStatus status) {
        this.status = status;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getRg() {
        return rg;
    }

    public void setRg(String rg) {
        this.rg = rg;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getNeighborhood() {
        return neighborhood;
    }

    public void setNeighborhood(String neighborhood) {
        this.neighborhood = neighborhood;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWhatsapp() {
        return whatsapp;
    }

    public void setWhatsapp(String whatsapp) {
        this.whatsapp = whatsapp;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getGuardianName() {
        return guardianName;
    }

    public void setGuardianName(String guardianName) {
        this.guardianName = guardianName;
    }

    public String getGuardianCpf() {
        return guardianCpf;
    }

    public void setGuardianCpf(String guardianCpf) {
        this.guardianCpf = guardianCpf;
    }

    public String getGuardianPhone() {
        return guardianPhone;
    }

    public void setGuardianPhone(String guardianPhone) {
        this.guardianPhone = guardianPhone;
    }

    public String getGuardianWhatsapp() {
        return guardianWhatsapp;
    }

    public void setGuardianWhatsapp(String guardianWhatsapp) {
        this.guardianWhatsapp = guardianWhatsapp;
    }

    public String getRelationship() {
        return relationship;
    }

    public void setRelationship(String relationship) {
        this.relationship = relationship;
    }

    public String getModality() {
        return modality;
    }

    public void setModality(String modality) {
        this.modality = modality;
    }

    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public String getClassGroup() {
        return classGroup;
    }

    public void setClassGroup(String classGroup) {
        this.classGroup = classGroup;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getImportantInformation() {
        return importantInformation;
    }

    public void setImportantInformation(String importantInformation) {
        this.importantInformation = importantInformation;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(OffsetDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
