package ru.vtb.tester3000.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "regression_cases")
public class RegressionCaseEntity {

    @Id
    @Column(name = "test_code", nullable = false, length = 255)
    private String testCode;

    @Column(name = "test_description", nullable = false, columnDefinition = "text")
    private String testDescription;

    @Column(name = "regression_tag", length = 255)
    private String regressionTag;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "auth", columnDefinition = "jsonb")
    private String auth;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "clr", columnDefinition = "jsonb")
    private String clr;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "auth_add_1", columnDefinition = "jsonb")
    private String authAdd1;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "clr_add_1", columnDefinition = "jsonb")
    private String clrAdd1;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "auth_add_2", columnDefinition = "jsonb")
    private String authAdd2;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "clr_add_2", columnDefinition = "jsonb")
    private String clrAdd2;

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

    public String getRegressionTag() {
        return regressionTag;
    }

    public void setRegressionTag(String regressionTag) {
        this.regressionTag = regressionTag;
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

    public String getAuthAdd1() {
        return authAdd1;
    }

    public void setAuthAdd1(String authAdd1) {
        this.authAdd1 = authAdd1;
    }

    public String getClrAdd1() {
        return clrAdd1;
    }

    public void setClrAdd1(String clrAdd1) {
        this.clrAdd1 = clrAdd1;
    }

    public String getAuthAdd2() {
        return authAdd2;
    }

    public void setAuthAdd2(String authAdd2) {
        this.authAdd2 = authAdd2;
    }

    public String getClrAdd2() {
        return clrAdd2;
    }

    public void setClrAdd2(String clrAdd2) {
        this.clrAdd2 = clrAdd2;
    }
}
