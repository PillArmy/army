package io.army.bean;

import java.time.LocalDate;

public class MyBean extends BaseBean {


    private boolean noGetterSetterField;

    private int id;

    private boolean visible;

    private boolean aB;

    private String bD;

    private LocalDate a;

    private LocalDate B;


    public Integer publicInt;


    private Integer number;

    private String URL;

    private String Da;

    private Number absNumber;

    public int getId() {
        return id;
    }

    public MyBean setId(int id) {
        this.id = id;
        return this;
    }


    public boolean isVisible() {
        return visible;
    }

    public MyBean setVisible(boolean visible) {
        this.visible = visible;
        return this;
    }


    public boolean isaB() {
        return aB;
    }

    public MyBean setaB(boolean aB) {
        this.aB = aB;
        return this;
    }


    public String getbD() {
        return bD;
    }

    public MyBean setbD(String bD) {
        this.bD = bD;
        return this;
    }

    public Integer getPublicInt() {
        return publicInt;
    }

    public MyBean setPublicInt(Integer publicInt) {
        this.publicInt = publicInt;
        return this;
    }

    public LocalDate getA() {
        return a;
    }

    public MyBean setA(LocalDate a) {
        this.a = a;
        return this;
    }


    public LocalDate getB() {
        return B;
    }

    public MyBean setB(LocalDate b) {
        B = b;
        return this;
    }


    @Override
    public Integer getNumber() {
        return number;
    }

    public MyBean setNumber(Integer number) {
        this.number = number;
        return this;
    }


    public String getURL() {
        return URL;
    }

    public MyBean setURL(String URL) {
        this.URL = URL;
        return this;
    }

    public String getDa() {
        return Da;
    }

    public MyBean setDa(String da) {
        Da = da;
        return this;
    }


    public Number getAbsNumber() {
        return absNumber;
    }

    public MyBean setAbsNumber(Number absNumber) {
        this.absNumber = absNumber;
        return this;
    }
}
