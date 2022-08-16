package id.co.askrindo.penjurnalan.repository;

import id.co.askrindo.penjurnalan.entity.FinanceDataPosting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FinanceDataPostingRepository extends JpaRepository<FinanceDataPosting, String> {

}