package ru.vtb.tester3000.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "test_messages_from_25")
public class TestMessageFrom25Entity {

    @Id
    @Column(name = "test_code", nullable = false, length = 255)
    private String testCode;

    @Column(name = "test_description", nullable = false, columnDefinition = "text")
    private String testDescription;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "auth", nullable = false, columnDefinition = "jsonb")
    private String auth;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "clr", columnDefinition = "jsonb")
    private String clr;

    public String getTestCode() {
        return testCode;
    }

    public void setTestCode(String testCode) {
        this.testCode = testCode;
    }

    public String getTestDescription() {
        return testDescription;
    }

    public void setTestDescription(String testDescription) {
        this.testDescription = testDescription;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public String getClr() {
        return clr;
    }

    public void setClr(String clr) {
        this.clr = clr;
    }
}
