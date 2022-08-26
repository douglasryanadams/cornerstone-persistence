package com.github.cornerstonews.persistence.jpa.entity;

import java.util.List;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "example")
public class Example {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(length = 16)
    private String someString;

    @Column
    private Integer someNumber;

    @OneToMany(mappedBy = "example", fetch = FetchType.LAZY)
    private List<MiddleTable> middleTables;

    public Example() {
        // For JPA
    }

    public Example(final String someString, final Integer someNumber) {
        this.someString = someString;
        this.someNumber = someNumber;
    }

    public Integer getId() {
        return id;
    }

    public void setId(final Integer id) {
        this.id = id;
    }

    public String getSomeString() {
        return someString;
    }

    public void setSomeString(final String someString) {
        this.someString = someString;
    }

    public Integer getSomeNumber() {
        return someNumber;
    }

    public void setSomeNumber(final Integer someNumber) {
        this.someNumber = someNumber;
    }

    public List<MiddleTable> getMiddleTables() {
        return middleTables;
    }

    public void setMiddleTables(final List<MiddleTable> middleTables) {
        this.middleTables = middleTables;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final Example example = (Example) o;
        return Objects.equals(id, example.id) && Objects.equals(someString, example.someString) && Objects.equals(someNumber, example.someNumber);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, someString, someNumber);
    }

    @Override
    public String toString() {
        return "Example{" +
                "id=" + id +
                ", someString='" + someString + '\'' +
                ", someNumber=" + someNumber +
                '}';
    }
}