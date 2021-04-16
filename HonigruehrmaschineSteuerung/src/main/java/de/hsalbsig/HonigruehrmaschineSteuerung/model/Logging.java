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
    private LocalDateTime startAt;
    private LocalDateTime endAt;

    public Logging() {
    }

    public Logging(long id, LocalDateTime startAt, LocalDateTime endAt) {
        this.id = id;
        this.startAt = startAt;
        this.endAt = endAt;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public LocalDateTime getStartAt() {
        return startAt;
    }

    public void setStartAt(LocalDateTime startAt) {
        this.startAt = startAt;
    }

    public LocalDateTime getEndAt() {
        return endAt;
    }

    public void setEndAt(LocalDateTime endAt) {
        this.endAt = endAt;
    }
}
