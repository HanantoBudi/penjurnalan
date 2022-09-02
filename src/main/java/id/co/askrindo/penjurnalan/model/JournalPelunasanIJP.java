package id.co.askrindo.penjurnalan.model;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class JournalPelunasanIJP {
    private String Id;
    private String branchId;
    private String branchIdString;
    private Long documentTypeId;
    private String accountingDate;
    private String entryDate;
    private String dueDate;
    private Long journalNumber;
    private Long accountId;
    private Long accountCode;
    private String accountFromTo;
    private Long transactionSourceId;
    private Long accountCashBankId;
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
    private Long paymentTypeId;
    private String paymentReferenceNumber;
    private String referenceNumber;
    private Long journalStatusId;
    private String journalNotation;
    private String budgetItemId;
    private List<JournalPelunasanIJPDetailDTO> journalDetailDTOS;
    private String summaryDetailId;
    private String accountIdString;
    private String accountTypeIdString;
    private String dataId;
}