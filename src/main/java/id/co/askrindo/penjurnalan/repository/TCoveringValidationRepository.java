package id.co.askrindo.penjurnalan.repository;

import id.co.askrindo.penjurnalan.entity.TCoveringValidation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TCoveringValidationRepository extends JpaRepository<TCoveringValidation, String> {

    Optional<TCoveringValidation> findById (Integer id);

}
