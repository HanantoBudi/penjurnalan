package id.co.askrindo.penjurnalan.repository;

import id.co.askrindo.penjurnalan.entity.FinanceDataPosting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FinanceDataPostingRepository extends JpaRepository<FinanceDataPosting, String> {

    Optional<FinanceDataPosting> findByTrxId(String trxId);

    List<FinanceDataPosting> findByStatus(Integer status);

    @Query(value="select*from brisurf.m_finance_data_posting where trx_id = :trxId and journal_name = :journalName order by created_date desc OFFSET 0 ROWS FETCH FIRST 1 ROWS ONLY", nativeQuery = true)
    Optional<FinanceDataPosting> findByTrxIdAndJournalName(String trxId, String journalName);

}