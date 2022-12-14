package id.co.askrindo.penjurnalan.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class JournalProduksiKlaim {
    private String Id;
    private String branchId;
    private String branchIdString;
    private Long documentTypeId;
    private String accountingDate;
    private String entryDate;
    private String dueDate;
    private String journalNumber;
    private String accountId;
    private String accountCode;
    private String accountFromTo;
    private Long transactionSourceId;
    private String accountCashBankId;
    private String mainAccountId;
    private Long departmentId;
    private String debitCredit;
    private String transactionCurrencyCode;
    private Long transactionAmount;
    private Long exchangeRate;
    private String accountingCurrencyCode;
    private Long accountingAmount;
    private Long financialMonth;
    private Long financialYear;
    private String postingStatus;
    private String postedDate;
    private String postedByName;
    private String journalNotes;
    private String paymentTypeId;
    private String paymentReferenceNumber;
    private String referenceNumber;
    private Long journalStatusId;
    private String journalNotation;
    private Long budgetItemId;
    private List<JournalProduksiKlaimDetailDTO> journalDetailDTOs;
    private String summaryDetailId;
    private String accountIdString;
    private String accountTypeIdString;
    private JournalProduksiKlaimExtended journalExtended;
    private String vaNumberInternal;
    private String dataId;
    private String vaNumbers;
}