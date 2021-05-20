package de.hsalbsig.HonigruehrmaschineSteuerung.model;

import org.springframework.security.core.GrantedAuthority;

import javax.persistence.Entity;
import javax.persistence.Id;
import java.io.Serializable;

@Entity
public class UAuthority implements GrantedAuthority , Serializable {

    @Id
    private String authority;

    public UAuthority() {
    }

    public UAuthority(String authority) {
        this.authority = authority;
    }

    @Override
    public String getAuthority() {
        return this.authority;
    }

    public void setAuthority(String authority) {
        this.authority = authority;
    }


}