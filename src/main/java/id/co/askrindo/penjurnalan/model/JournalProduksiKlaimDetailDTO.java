package id.co.askrindo.penjurnalan.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class JournalProduksiKlaimDetailDTO {
    private String Id;
    private String journalId;
    private String branchId;
    private String branchIdString;
    private String accountTypeIdString;
    private String accountId;
    private String accountIdString;
    private Long journalDetailsTransactionSourceId;
    private String accountCashBankId;
    private String mainAccountId;
    private Long departmentId;
    private String journalDetailsTransactionCurrencyCode;
    private Double transactionAmountDebit;
    private Double transactionAmountCredit;
    private Double exchangeRate;
    private String accountingCurrencyCode;
    private Double accountingAmountDebit;
    private Double accountingAmountCredit;
    private String journalDetailNotes;
    private String settledTransactionId;
    private String taxJournalDetailId;
    private Long taxReclassStatus;
}
