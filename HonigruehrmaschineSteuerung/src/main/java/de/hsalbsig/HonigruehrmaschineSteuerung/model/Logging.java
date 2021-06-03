package de.hsalbsig.HonigruehrmaschineSteuerung.model;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import java.time.LocalDateTime;


@Entity
public class Logging {

    @Id
    @GeneratedValue (strategy = GenerationType.AUTO)
    private long id;
    private String executedAt;
    private String command;

    public Logging() {
    }

    public Logging(String executedAt, String command) {
        this.executedAt = executedAt;
        this.command = command;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getExecutedAt() {
        return executedAt;
    }

    public void setExecutedAt(String executedAt) {
        this.executedAt = executedAt;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
