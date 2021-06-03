package de.hsalbsig.HonigruehrmaschineSteuerung.dao;

import de.hsalbsig.HonigruehrmaschineSteuerung.model.Logging;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoggingRepository extends JpaRepository<Logging,Long> {
}
