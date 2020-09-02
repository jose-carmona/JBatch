package es.jose;

import java.io.Serializable;
import javax.inject.Named;


import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;

@Named
@ApplicationScoped
public class Foo implements Serializable {

    private String bar;

    public Foo() {}

    public Foo(String bar) {
        this.bar = bar;
    }

    @PostConstruct
    public void init() {
        bar = "Hello World!";
    }

    public String getBar() {
        return bar;
    }

}