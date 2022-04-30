package com.github.cornerstonews.persistence.jpa.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Objects;

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